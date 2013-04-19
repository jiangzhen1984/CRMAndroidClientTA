
package com.auguraclient.view;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SuguraRestAPIImpl;
import com.auguraclient.util.GlobalHolder;

public class ProjectOrderListViewWithLIstVIew extends Activity {

    private static final int START_WAITING = 1;

    private static final int END_WAITING = 2;

    private static final int END_WAITING_WITH_ERROR = 3;

    private Context mContext;

    private ListView projectItemList;

    private Project project;

    private ListAdapter adapter;

    private ISuguraRestAPI api;

    private static final int LOAD_PROJECT_ITEM = 1;

    private LoaderHandler handler;

    private UIHandler uiHandler;

    private int currentProjectPosition;

    private TextView itemTitleTV;
    
    private LinearLayout returnButton;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.order_list);
        projectItemList = (ListView)this.findViewById(R.id.projectItemList);
        currentProjectPosition = (Integer)this.getIntent().getExtras().get("project");
        project = GlobalHolder.getProject(currentProjectPosition);

        mContext = this;
        adapter = new ListAdapter(this);
        projectItemList.setAdapter(adapter);

        itemTitleTV = (TextView)this.findViewById(R.id.itemTitle);
        returnButton = (LinearLayout) this.findViewById(R.id.order_list_return_button);
        returnButton.setOnClickListener(returnButtonListener);
        api = new SuguraRestAPIImpl();

        HandlerThread ht = new HandlerThread("it");
        ht.start();
        handler = new LoaderHandler(ht.getLooper());
        uiHandler = new UIHandler();
        Message.obtain(handler, LOAD_PROJECT_ITEM).sendToTarget();

        projectItemList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();
                i.setClass(mContext, OrderView.class);
                i.putExtra("project", currentProjectPosition);
                i.putExtra("itemPosition", position);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        itemTitleTV.setText(this.project.getName());
    }
    
    private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			finish();
		}
    	
    };
    
    

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
                    dialog = ProgressDialog.show(mContext, "Loading", "Please wait...", true);
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
            }
        }
    }

    class LoaderHandler extends Handler {

        public LoaderHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_PROJECT_ITEM:
                    Message.obtain(uiHandler, START_WAITING).sendToTarget();
                    List<ProjectOrder> l;
                    try {
                        if(!project.isLoadOrderFromDB()){
                            loadOrderFromDB();
                        }
                        if (project.getOrderCount() <= 0) {
                            l = api.queryProjectOrderList(project.getId());
                            project.addProjectOrder(l);
                        }
                        Message.obtain(uiHandler, END_WAITING).sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message.obtain(uiHandler, END_WAITING_WITH_ERROR).sendToTarget();
                    }
                    break;
            }
        }

    }

    private void loadOrderFromDB() {
       ContentResolver  cr =this.getContentResolver();
       Cursor c = null;
       c = cr.query(ContentDescriptor.ProjectOrderDesc.CONTENT_URI, ContentDescriptor.ProjectOrderDesc.Cols.ALL_COLS, ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID+"=?", new String[]{project.getId()}, null);

       while(c.moveToNext()) {
           ProjectOrder po = new ProjectOrder();
           po.setnID(c.getInt(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.ID)));
           po.setId(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.ORD_ID)));
           po.setName(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.NAME)));
           po.setDescription(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.DESCRIPTION)));
           po.setPhotoBigPath(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_BIG_PATH)));
           po.setPhotoPath(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_SMALL_PATH)));
           po.setPhotoName(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_NAME)));
           if(c.getColumnIndex(ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT)>=0) {
               po.setQcComment(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT)));
           }
           if(c.getColumnIndex(ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS)>=0) {
               po.setQcStatus(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS)));
           }
           if(c.getColumnIndex(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY)>=0) {
               po.setQuantity(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY)));
           }
           if(c.getColumnIndex(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED)>=0) {
               po.setQuantityChecked(c.getString(c.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED)));
           }
           project.addProjectOrder(po);
       }

       project.setLoadOrderFromDB(true);
       c.close();
   }

    class ListAdapter extends BaseAdapter {

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return project.getOrderCount();
        }

        public Object getItem(int position) {
            return project.getOrder(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ItemView appView = new ItemView(context);
                appView.updateView(project.getOrder(position));
                convertView = appView;
            }
            return convertView;
        }

    }

    class ItemView extends LinearLayout {

        private Context mContext;

        private TextView itemNameTV;

        private TextView qcStatusTV;

        private TextView quantityTV;

        private ImageView itemPhotoIV;

        private ImageView itemOperationIV;
        
        private String photoPath;

        public ItemView(Context context) {
            super(context);
            this.mContext = context;
            initilize(context);
        }

        public void initilize(Context c) {
            this.mContext = c;
            View view =null;// LayoutInflater.from(this.mContext).inflate(R.layout.project_item, null);
            addView(view);
            itemNameTV = (TextView)this.findViewById(R.id.itemName);
            qcStatusTV = (TextView)this.findViewById(R.id.qcStatus);
            quantityTV = (TextView)this.findViewById(R.id.itemQuntity);
            itemPhotoIV = (ImageView)this.findViewById(R.id.projectItemPhoto);            
            itemOperationIV = (ImageView)this.findViewById(R.id.imagesOp);
            itemOperationIV.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					System.out.println("===========");
				}
            	
            });
        }

        public void updateView(ProjectOrder pi) {
            itemNameTV.setText(pi.getName());
            qcStatusTV.setText("QC Status :" + pi.getQcStatus());
            quantityTV.setText("Quantity :" + pi.getQuantity());
            itemPhotoIV.setImageURI(Uri.fromFile(new File(GlobalHolder.GLOBAL_STORAGE_PATH
                    + pi.getPhotoPath())));
            itemPhotoIV.setOnClickListener(orderPhotoClickListener);
            photoPath = pi.getOriginPhotoPath();
        }
        
        private OnClickListener orderPhotoClickListener = new OnClickListener() {

    		public void onClick(View arg0) {
    			Intent intent = new Intent(Intent.ACTION_VIEW);
    			intent
    					.setDataAndType(Uri.fromFile(new File(
    							GlobalHolder.GLOBAL_STORAGE_PATH
    									+ photoPath)),
    							"image/*");
    			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(Intent.createChooser(intent, "Select Picture"));
    		}
        };

    }

}
