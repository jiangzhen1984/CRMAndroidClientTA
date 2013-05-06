
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.AuguaModuleImpl;
import com.auguraclient.model.IAuguraModule;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.UpdateRecord;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;

public class ProjectOrderListView extends Activity {

    private static final int START_WAITING = 1;

    private static final int END_WAITING = 2;

    private static final int END_WAITING_WITH_ERROR = 3;
    
    private static final int END_WAITING_END_SESSION = 4;
    
    private static final int LOAD_PROJECT_ITEM = 1;
    
    private static final int DELETE_PROJECT = 2;

    private Context mContext;

    private ListView projectItemList;

    private Project project;

    private IAuguraModule moduleApi;

    private LoaderHandler handler;

    private UIHandler uiHandler;

    private int currentProjectPosition;

    private TextView itemTitleTV;
    
    private LinearLayout returnButton;
    
    private ImageView showMenuButton;
    
    private ListAdapter adapter;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.order_list);
	    
        projectItemList = (ListView)this.findViewById(R.id.projectItemList);
        currentProjectPosition = (Integer)this.getIntent().getExtras().get("project");
        project = GlobalHolder.getInstance().getProject(currentProjectPosition);

        mContext = this;
        moduleApi = new AuguaModuleImpl(mContext);
        adapter = new ListAdapter(this);
        projectItemList.setAdapter(adapter);

        itemTitleTV = (TextView)this.findViewById(R.id.itemTitle);
        returnButton = (LinearLayout) this.findViewById(R.id.order_list_return_button);
        
        showMenuButton = (ImageView) findViewById(R.id.imgShowDeleteProjectMenu);
        
        returnButton.setOnClickListener(returnButtonListener);

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
        
        showMenuButton.setOnClickListener(showMenuListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        itemTitleTV.setText(this.project.getName());
        adapter.notifyDataSetChanged();
    }

    
    private OnClickListener showMenuListener = new OnClickListener() {

		public void onClick(View arg0) {
			PopupMenu popupMenu = new PopupMenu(mContext, showMenuButton);
			popupMenu.getMenuInflater().inflate(
					R.layout.menu_item_order_list, popupMenu.getMenu());
			popupMenu
					.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

						public boolean onMenuItemClick(MenuItem item) {

							if (item.getItemId() == R.id.delete_project) {
								doDeleteProject();
							}
							return true;
						}
					});

			popupMenu.show();
		}

	};
    
    private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			finish();
		}
    	
    };
    
    
    
    private void doDeleteProject() {
    	Message.obtain(handler, DELETE_PROJECT).sendToTarget();
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
                    Toast.makeText(dialog.getContext(), "errorr ---------------",
                            Toast.LENGTH_SHORT).show();
                    break;
                case END_WAITING_END_SESSION:
                	if (dialog != null) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                	finish();
                	break;
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
//                        if(!project.isLoadOrderFromDB()){
//                            loadOrderFromDB();
//                        }
//                        if (project.getOrderCount() <= 0) {
//                            l = api.queryProjectOrderList(project.getId());
//                            project.addProjectOrder(l);
//                        }
                        Message.obtain(uiHandler, END_WAITING).sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message.obtain(uiHandler, END_WAITING_WITH_ERROR).sendToTarget();
                    }
                    break;
                case DELETE_PROJECT:
                	Message.obtain(uiHandler, START_WAITING).sendToTarget();
                	removeProjectFromDB();
                	GlobalHolder.getInstance().getPl().removeProject(project);
                	Message.obtain(uiHandler, END_WAITING_END_SESSION).sendToTarget();
                	break;
            }
        }

    }
    
    
    private void removeProjectFromDB() {
			int ret = this.moduleApi.deleteFromDB(ProjectCheckpoint.class,
					ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID + "=?",
					new String[] { project.getId() });
			Log.i(Constants.TAG, "-------delete project checkpoint " + ret);
			ret = this.moduleApi.deleteFromDB(ProjectOrder.class,
					ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID + "=?",
					new String[] { project.getId() });
			Log.i(Constants.TAG, "-------delete order" + ret);
			ret = this.moduleApi.deleteFromDB(Project.class,
					ContentDescriptor.ProjectDesc.Cols.PRO_ID + "=?",
					new String[] { project.getId() });
			Log.i(Constants.TAG, "-------delete  project" + ret);
			ret = this.moduleApi.deleteFromDB(UpdateRecord.class,
					ContentDescriptor.UpdateDesc.Cols.PRO_ID + "=?",
					new String[] { project.getId() });
			Log.i(Constants.TAG, "-------delete  project" + ret);
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
           //load checkpoint
           loadCheckpointFromDB(po);
       }

       project.setLoadOrderFromDB(true);
       c.close();
   }
    
    
    
    private void loadCheckpointFromDB(ProjectOrder order) {
		ContentResolver cr = this.getContentResolver();
		Cursor c = null;
		c = cr.query(ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
				ContentDescriptor.ProjectCheckpointDesc.Cols.ALL_COLS,
				ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID
						+ "=? and "
						+ ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG
						+ "<>?", new String[] { order.getId(),
						ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_DELETE },
				null);
		

		while (c.moveToNext()) {
			ProjectCheckpoint pcp = new ProjectCheckpoint();
			pcp.setnID(c.getInt(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.ID)));
			pcp.setId(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID)));
			pcp.setName(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME)));

			pcp.setPhotoPath(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH)));

			pcp.setPhotoName(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME)));

			pcp
					.setDescription(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));

			pcp
					.setCategory(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY)));

			pcp
					.setQcComments(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
			pcp
					.setQcStatus(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS)));
			pcp
					.setDescription(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
			pcp
					.setNumberDefect(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT)));
			pcp
					.setCheckType(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE)));
			pcp
					.setQcAction(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION)));
			
			pcp.setUploadPhotoAbsPath(c
					.getString(c
							.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_PATH)));
			order.addOrderCheckpoint(pcp);
		}

		order.setLoadedCheckpointFromDB(true);
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
            } else {
            	((ItemView)convertView).updateView(project.getOrder(position));
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
        
        private View parent;

        public ItemView(Context context) {
            super(context);
            this.mContext = context;
            initilize(context);
        }

        public void initilize(Context c) {
            this.mContext = c;
            View view = LayoutInflater.from(this.mContext).inflate(R.layout.component_order, null);
            addView(view);
            parent = findViewById(R.id.componentOrder);
            itemNameTV = (TextView)this.findViewById(R.id.itemName);
            qcStatusTV = (TextView)this.findViewById(R.id.qcStatus);
            quantityTV = (TextView)this.findViewById(R.id.itemQuntity);
            itemPhotoIV = (ImageView)this.findViewById(R.id.projectItemPhoto);            
            itemOperationIV = (ImageView)this.findViewById(R.id.imagesOp);
            
        }

        public void updateView(ProjectOrder pi) {
            itemNameTV.setText(pi.getName());
            qcStatusTV.setText("QC Status :" + pi.getQcStatus());
            quantityTV.setText("Quantity :" + pi.getQuantity());
            itemPhotoIV.setImageURI(Uri.fromFile(new File(GlobalHolder.getInstance().getStoragePath()
                    + pi.getPhotoPath())));
            itemPhotoIV.setOnClickListener(orderPhotoClickListener);
            photoPath = pi.getOriginPhotoPath();
            
            if (pi.isCompleted()) {
				//itemOperationIV.setImageResource(R.drawable.completed);
            	parent.setBackgroundColor(mContext.getResources().getColor(R.color.white_background));
			} else {
				//itemOperationIV.setImageResource(R.drawable.missing);
				parent.setBackgroundColor(mContext.getResources().getColor(R.color.checkpoint_incomplete_bg));
			}
            
//            if(pi.isCompleted()) {
//            	itemOperationIV.setImageResource(R.drawable.completed);
//            } else {
//            	itemOperationIV.setImageResource(R.drawable.missing);
//            }
        }
        
        private OnClickListener orderPhotoClickListener = new OnClickListener() {

    		public void onClick(View arg0) {
    			Intent intent = new Intent(Intent.ACTION_VIEW);
    			intent
    					.setDataAndType(Uri.fromFile(new File(
    							GlobalHolder.getInstance().getStoragePath()
    									+ photoPath)),
    							"image/*");
    			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(Intent.createChooser(intent, "Select Picture"));
    		}
        };

    }

}
