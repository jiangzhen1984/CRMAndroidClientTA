
package com.auguraclient.view;

import com.auguraclient.R;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectItem;
import com.auguraclient.model.SuguraRestAPIImpl;
import com.auguraclient.util.GlobalHolder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ProjectItemView extends Activity {

    private static final int START_WAITING = 1;

    private static final int END_WAITING = 2;

    private static final int END_WAITING_WITH_ERROR = 3;

    private Context mContext;

    private ListView projectItemList;

    private Project project;

    private ListAdapter adapter;

    private ISuguraRestAPI api;

    private static final int LOAD_PROJECT_ITEM =  1;

    private LoaderHandler handler;

    private UIHandler uiHandler;

    private int currentProjectPosition;

    private TextView itemTitleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setfullScreen();
        this.setContentView(R.layout.project_item_list);
        projectItemList = (ListView)this.findViewById(R.id.projectItemList);
        currentProjectPosition = (Integer)this.getIntent().getExtras().get("project");
        project = GlobalHolder.getProject(currentProjectPosition);

        mContext = this;
        adapter = new ListAdapter(this);
        projectItemList.setAdapter(adapter);

        itemTitleTV = (TextView)this.findViewById(R.id.itemTitle);

        api = new SuguraRestAPIImpl();

        HandlerThread ht = new HandlerThread("it");
        ht.start();
        handler = new LoaderHandler(ht.getLooper());
        uiHandler = new UIHandler();
        Message.obtain(handler, LOAD_PROJECT_ITEM).sendToTarget();

        projectItemList.setOnItemClickListener( new OnItemClickListener() {

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
                            Toast.LENGTH_SHORT);
            }
        }
    }







   class LoaderHandler extends Handler {



    public LoaderHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case LOAD_PROJECT_ITEM:
              Message.obtain(uiHandler, START_WAITING).sendToTarget();
                List<ProjectItem> l;
                try {
                    l = api.queryProjectItemList(project.getId());
                    project.addProject(l);
                    Message.obtain(uiHandler, END_WAITING).sendToTarget();
                } catch (APIException e) {
                    e.printStackTrace();
                    Message.obtain(uiHandler, END_WAITING_WITH_ERROR).sendToTarget();
                }
                break;
        }
    }

   }

    class ListAdapter extends BaseAdapter {

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return project.getItemCount();
        }

        public Object getItem(int position) {
            return project.getItem(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ItemView appView = new ItemView(context);
                appView.updateView(project.getItem(position));
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

        public ItemView(Context context) {
            super(context);
            this.mContext = context;
            initilize(context);
        }

        public void initilize(Context c) {
            this.mContext = c;
            View view = LayoutInflater.from(this.mContext).inflate(R.xml.project_item, null);
            addView(view);
            itemNameTV = (TextView)this.findViewById(R.id.itemName);
            qcStatusTV = (TextView)this.findViewById(R.id.qcStatus);
            quantityTV = (TextView)this.findViewById(R.id.itemQuntity);
            itemPhotoIV = (ImageView)this.findViewById(R.id.projectItemPhoto);
        }

        public void updateView(ProjectItem pi) {
            itemNameTV.setText(pi.getName());
            qcStatusTV.setText("QC Status :"+ pi.getQcStatus());
            quantityTV.setText("Quantity :"+pi.getQuantity());
            itemPhotoIV.setImageDrawable(getDrawable(pi.getPhotoPath()));
        }

        private Drawable getDrawable(String url) {
            InputStream is = null;
            try {
                is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                return d;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
    }

}
