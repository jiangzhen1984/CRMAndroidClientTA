
package com.auguraclient.view;

import com.auguraclient.R;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.SuguraRestAPIImpl;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.SoundEngine;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ProjectListScreen extends Activity {

    private static final int QUERY_PROJECT = 1;

    private static final int START_WAITING = 1;

    private static final int END_WAITING = 2;

    private static final int END_WAITING_WITH_ERROR = 3;

    private LinearLayout addProjectLayout = null;

    private Context context;

    private ISuguraRestAPI api;

    private CmdHandler handler;

    private UIHandler uiHandler;

    private ListView projectList;

    private ListAdapter projectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setfullScreen();
         setContentView(R.layout.projectlist);



         projectList =    (ListView)findViewById(R.id.projectList);

         addProjectLayout =
        (LinearLayout)findViewById(R.id.tab_add_project_layout);
        addProjectLayout.setOnClickListener(addProjectListener);

        context = this;
        api = new SuguraRestAPIImpl();
        HandlerThread th = new HandlerThread("project");
        th.start();
        handler = new CmdHandler(th.getLooper());

        uiHandler = new UIHandler();

        projectAdapter = new  ListAdapter(context);
        projectList.setAdapter(projectAdapter);

    }


    OnClickListener addProjectListener = new OnClickListener() {
        public void onClick(View v) {
            SoundEngine.sharedEngine().playEffect(getApplicationContext(), R.raw.button);
            // custom dialog
            final Dialog dialog = new Dialog(context);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.add_project_dialog);
            dialog.setTitle(context.getText(R.string.add_project));
            LinearLayout dialogButton = (LinearLayout)dialog.findViewById(R.id.ok_button_lin);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    EditText projectText = (EditText)dialog.findViewById(R.id.project_numc);
                    if (projectText.getText() == null || projectText.getText().toString() == null
                            || projectText.getText().toString().isEmpty()) {
                        Toast.makeText(dialog.getContext(),
                                context.getText(R.string.not_null_query), Toast.LENGTH_LONG);
                    } else {
                        Message.obtain(handler, QUERY_PROJECT, projectText.getText().toString())
                                .sendToTarget();
                        Message.obtain(uiHandler, START_WAITING).sendToTarget();

                    }

                    dialog.dismiss();
                }
            });

            dialog.show();

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
                case QUERY_PROJECT:
                    dialog = ProgressDialog.show(context, "Loading", "Please wait...", true);
                    dialog.setIcon(R.drawable.logo_24_24);
                    dialog.show();
                    break;
                case END_WAITING:
                    if (dialog != null) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                    projectAdapter.notifyDataSetChanged();
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

    class CmdHandler extends Handler {

        public CmdHandler() {
            super();
        }

        public CmdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QUERY_PROJECT:

                    try {
                        ProjectList pl = api.queryProjectList((String)msg.obj);
                        if (pl == null) {
                            Message.obtain(uiHandler, END_WAITING_WITH_ERROR).sendToTarget();
                        } else {
                            GlobalHolder.setPl(pl);
                            Message.obtain(uiHandler, END_WAITING).sendToTarget();
                        }
                        // TODO populate project list;
                    } catch (APIException e) {
                        e.printStackTrace();
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
            return GlobalHolder.getPl()== null? 0: GlobalHolder.getPl().getResultCount();
        }

        public Object getItem(int position) {
            return GlobalHolder.getProject(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                ItemView appView = new ItemView(context);
                appView.updateView(GlobalHolder.getProject(position));
                convertView = appView;
               }
               return convertView;
              }

    }



      class ItemView extends LinearLayout {

         private Context mContext;

         private TextView tv;

         public ItemView(Context context) {
             super(context);
             this.mContext = context;
             initilize(context);
            }


         public void initilize(Context c){
             this.mContext = c;
             View view = LayoutInflater.from(this.mContext).inflate(R.xml.project_list_item, null);
             addView(view);
             tv = (TextView)this.findViewById(R.id.projectListItemName);
            }

         public void updateView(Project p) {
             tv.setText(p.getText());
         }
     }
}
