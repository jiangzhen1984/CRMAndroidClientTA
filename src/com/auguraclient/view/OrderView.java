
package com.auguraclient.view;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SuguraRestAPIImpl;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class OrderView extends Activity {

    private static final int START_WAITING = 1;

    private static final int END_WAITING = 2;

    private static final int END_WAITING_WITH_ERROR = 3;

    private static final int CONFIRM_DELETE = 4;

    private static final int LOAD_PROJECT_ITEM_ORDER = 1;

    private static final int DELETE_CHECKPOINT = 2;

    private Context mContext;

    //
    private ListView projectOrderCheckpointList;

    private Project project;

    private ListAdapter adapter;

    private ISuguraRestAPI api;

    private LoaderHandler handler;

    private UIHandler uiHandler;

    private ProjectOrder projectItem;

    private ImageView projectItemPhotoIV;

    private TextView itemOrderQuntityTV;

    private TextView itemOrderDescriptionTV;

    private TextView itemOrderCommentTV;

    private TextView itemOrderQTTV;

    private TextView orderItemTitleTV;

    private int currentSelectedCheckpointIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setfullScreen();
        this.setContentView(R.layout.project_order_list);
        projectOrderCheckpointList = (ListView)this.findViewById(R.id.projectOrderCheckpointList);

        projectItemPhotoIV = (ImageView)this.findViewById(R.id.projectItemPhoto);

        itemOrderQuntityTV = (TextView)this.findViewById(R.id.itemOrderQuntity);

        itemOrderDescriptionTV = (TextView)this.findViewById(R.id.itemOrderDescription);

        itemOrderCommentTV = (TextView)this.findViewById(R.id.itemOrderComment);

        itemOrderQTTV = (TextView)this.findViewById(R.id.itemOrderQT);

        orderItemTitleTV = (TextView)this.findViewById(R.id.orderItemTitle);

        Integer position = (Integer)this.getIntent().getExtras().get("project");
        Integer itemPosition = (Integer)this.getIntent().getExtras().get("itemPosition");

        project = GlobalHolder.getProject(position);

        projectItem = project.getOrder(itemPosition);

        mContext = this;

        adapter = new ListAdapter(this);
        projectOrderCheckpointList.setAdapter(adapter);

        api = new SuguraRestAPIImpl();

        HandlerThread ht = new HandlerThread("it");
        ht.start();
        handler = new LoaderHandler(ht.getLooper());
        uiHandler = new UIHandler();
        Message.obtain(handler, LOAD_PROJECT_ITEM_ORDER).sendToTarget();

        projectOrderCheckpointList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message.obtain(uiHandler, CONFIRM_DELETE, position).sendToTarget();
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        orderItemTitleTV.setText(projectItem.getName());
        itemOrderQuntityTV.setText(" Quantity: " + projectItem.getQuantity());
        itemOrderDescriptionTV.setText(projectItem.getDescription());
        itemOrderCommentTV.setText("QC Comments:\n" + projectItem.getQcComment() == null ? ""
                : projectItem.getQcComment());
        itemOrderQTTV.setText("Q777C Date:     " + "Quantity Checked:"
                + projectItem.getQuantityChecked() + "  QC Status:" + projectItem.getQcStatus());

        projectItemPhotoIV.setImageURI(Uri.fromFile(new File(GlobalHolder.GLOBAL_STORAGE_PATH
                + projectItem.getPhotoBigPath())));
        projectItemPhotoIV.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(
                        Uri.fromFile(new File(GlobalHolder.GLOBAL_STORAGE_PATH
                                + projectItem.getOriginPhotoPath())), "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(Intent.createChooser(intent, "Select Picture"));
            }

        });

    }

    public void setfullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private ProgressDialog dialog;

    class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_WAITING:
                    dialog = ProgressDialog.show(mContext, "", "Please wait...", true);
                    dialog.setIcon(R.drawable.logo_24_24);
                    dialog.show();
                    break;
                case END_WAITING:
                    if (dialog != null) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                    adapter.notifyDataSetChanged();

                    break;
                case END_WAITING_WITH_ERROR:
                    if (dialog != null) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                    // TODO show toast
                    Toast.makeText(dialog.getContext(), "errorr ---------------",
                            Toast.LENGTH_SHORT).show();
                    break;

                case CONFIRM_DELETE:
                    currentSelectedCheckpointIdx = (Integer)msg.obj;
                    showConfirmDialog();
            }
        }
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.alert_icon);
        builder.setMessage(this.getResources().getText(R.string.delete_checkpoint_confirm));
        builder.setTitle(this.getResources().getText(R.string.delete_checkpoint_confirm_title));

        builder.setPositiveButton(R.string.delete_checkpoint_confirm_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Message.obtain(handler, DELETE_CHECKPOINT).sendToTarget();
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(R.string.delete_checkpoint_confirm_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });

        final AlertDialog configDialog = builder.create();
        configDialog.show();
    }

    class LoaderHandler extends Handler {

        public LoaderHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_PROJECT_ITEM_ORDER:
                    Message.obtain(uiHandler, START_WAITING).sendToTarget();
                    List<ProjectCheckpoint> l;
                    try {
                        if (!projectItem.isLoadedCheckpointFromDB()) {
                            loadCheckpointFromDB();
                        }
                        if (projectItem.getCheckpointCount() <= 0) {
                            l = api.queryProjectOrderCheckpointList(projectItem.getId());
                            projectItem.addOrderCheckpoint(l);
                        }
                        Message.obtain(uiHandler, END_WAITING).sendToTarget();
                    } catch (APIException e) {
                        Log.e(Constants.TAG, e.getMessage(), e);
                        Message.obtain(uiHandler, END_WAITING_WITH_ERROR).sendToTarget();
                    }
                    break;
                case DELETE_CHECKPOINT:
                    Message.obtain(uiHandler, START_WAITING).sendToTarget();
                    try {
                        // TODO delete checkpoint from database and globalholder
                        // and api
                        doDeleteCheckpoint();
                        Message.obtain(uiHandler, END_WAITING).sendToTarget();
                    } catch (Exception e) {
                        Log.e(Constants.TAG, e.getMessage(), e);
                        Message.obtain(uiHandler, END_WAITING_WITH_ERROR).sendToTarget();
                    }
                    break;
            }
        }

    }

    private void doDeleteCheckpoint() throws APIException {
        ProjectCheckpoint pcp = projectItem
                .getOrderCheckpointrByIndex(currentSelectedCheckpointIdx);
        // TODO update
        api.deleteCheckpoint(pcp.getId());
        projectItem.removeCheckpoint(this.currentSelectedCheckpointIdx);
    }

    private void loadCheckpointFromDB() {
        ContentResolver cr = this.getContentResolver();
        Cursor c = null;
        c = cr.query(ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
                ContentDescriptor.ProjectCheckpointDesc.Cols.ALL_COLS,
                ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID + "=?", new String[] {
                    this.projectItem.getId()
                }, null);

        while (c.moveToNext()) {
            ProjectCheckpoint pcp = new ProjectCheckpoint();
            pcp.setnID(c.getInt(c
                    .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.ID)));
            pcp.setName(c.getString(c
                    .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME)));
            pcp.setDescription(c.getString(c
                    .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
            pcp.setPhotoPath(c.getString(c
                    .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH)));
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY) >= 0) {
                pcp.setCategory(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY)));
            }

            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT) >= 0) {
                pcp.setQcComments(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
            }
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT) >= 0) {
                pcp.setQcComments(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
            }
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS) >= 0) {
                pcp.setQcStatus(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS)));
            }
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION) >= 0) {
                pcp.setDescription(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
            }
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT) >= 0) {
                pcp.setNumberDefect(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT)));
            }
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE) >= 0) {
                pcp.setCheckType(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE)));
            }
            if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION) >= 0) {
                pcp.setQcAction(c.getString(c
                        .getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION)));
            }
            projectItem.addOrderCheckpoint(pcp);
        }

        projectItem.setLoadedCheckpointFromDB(true);
        c.close();
    }

    class ListAdapter extends BaseAdapter {

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return projectItem.getCheckpointCount();
        }

        public Object getItem(int position) {
            return projectItem.getOrderCheckpointrByIndex(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ItemView appView = new ItemView(context);
                appView.updateView(projectItem.getOrderCheckpointrByIndex(position));
                convertView = appView;
            }
            return convertView;
        }

    }

    class ItemView extends LinearLayout {

        private Context mContext;

        private TextView itemOrderCategoryCheckType;

        private TextView itemOrderDefectAlert;

        private TextView itemOrderQcComments;

        private ImageView projectItemOrderPhoto;

        private ImageView itemOperationIV;

        public ItemView(Context context) {
            super(context);
            this.mContext = context;
            initilize(context);
        }

        public void initilize(Context c) {
            this.mContext = c;
            View view = LayoutInflater.from(this.mContext).inflate(R.xml.project_item_order, null);
            addView(view);
            itemOrderCategoryCheckType = (TextView)this
                    .findViewById(R.id.itemOrderCategoryCheckType);
            itemOrderDefectAlert = (TextView)this.findViewById(R.id.itemOrderDefectAlert);
            itemOrderQcComments = (TextView)this.findViewById(R.id.itemOrderQcComments);
            projectItemOrderPhoto = (ImageView)this.findViewById(R.id.projectItemOrderPhoto);
            itemOperationIV = (ImageView)this.findViewById(R.id.projectItemOrderOperation);
        }

        public void updateView(ProjectCheckpoint pi) {
            if (pi == null) {
                Log.e(Constants.TAG, " can't update view for order view ProjectItemOrder is null");
                return;
            }
            itemOrderCategoryCheckType.setText(pi.getCategory() + "   >> " + pi.getCheckType()
                    + "   >>" + pi.getName());
            itemOrderDefectAlert.setText(pi.getNumberDefect() + "   " + pi.getQcAction());
            itemOrderQcComments.setText(pi.getQcComments() == null ? "" : pi.getQcComments());
            if (pi.getPhotoPath() != null && !pi.getPhotoPath().isEmpty()) {
                final Uri photo = Uri.fromFile(new File(GlobalHolder.GLOBAL_STORAGE_PATH
                        + pi.getPhotoPath()));
                projectItemOrderPhoto.setImageURI(photo);

            }
        }

    }

}
