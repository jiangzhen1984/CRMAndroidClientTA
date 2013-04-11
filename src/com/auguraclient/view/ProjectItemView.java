package com.auguraclient.view;

import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectItem;
import com.auguraclient.util.GlobalHolder;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ProjectItemView extends Activity {

    private Context mContext;

    private ListView projectItemList;

    private Project  project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setfullScreen();
        this.setContentView(R.layout.project_item_list);
        projectItemList = (ListView)this.findViewById(R.id.projectItemList);
        project = (Project)  this.getIntent().getExtras().get("project");
    }




    public void setfullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            View view = LayoutInflater.from(this.mContext).inflate(R.xml.project_item, null);
            addView(view);

           }

        public void updateView(Project p) {
            tv.setText(p.getText());
        }
    }


}
