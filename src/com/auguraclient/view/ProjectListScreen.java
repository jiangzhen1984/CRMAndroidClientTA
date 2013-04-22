package com.auguraclient.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.APIException;
import com.auguraclient.model.AuguraRestAPIImpl;
import com.auguraclient.model.IAuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SessionAPIException;
import com.auguraclient.model.User;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.SoundEngine;

public class ProjectListScreen extends Activity {

	private static final int CMD_QUERY_PROJECT = 1;

	private static final int CMD_LOAD_PROJECT_FROM_DB = 2;

	private static final int CMD_SYNC_PROJECT = 3;

	private static final int UI_START_WAITING = 1;

	private static final int UI_END_WAITING = 2;

	private static final int UI_END_WAITING_WITH_ERROR = 3;

	private LinearLayout addProjectLayout = null;

	private Context context;

	private IAuguraRestAPI api;

	private CmdHandler handler;

	private UIHandler uiHandler;

	private ListView projectList;

	private ListAdapter projectAdapter;

	private ImageView showMenuButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setfullScreen();
		setContentView(R.layout.projectlist);

		projectList = (ListView) findViewById(R.id.projectList);

		addProjectLayout = (LinearLayout) findViewById(R.id.tab_add_project_layout);
		addProjectLayout.setOnClickListener(addProjectListener);
		showMenuButton = (ImageView) findViewById(R.id.imgShowMenu);

		ActionBar bar = getActionBar();
		if (bar != null)
			bar.hide();

		showMenuButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				PopupMenu popupMenu = new PopupMenu(context, showMenuButton);
				popupMenu.getMenuInflater().inflate(
						R.layout.project_list_title_menu, popupMenu.getMenu());
				popupMenu
						.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

							public boolean onMenuItemClick(MenuItem item) {

								if (item.getItemId() == R.id.menu_quit) {
									finish();
								} else if (item.getItemId() == R.id.menu_setting) {
									showSettingDialg();
								}
								return true;
							}
						});

				popupMenu.show();
			}

		});

		context = this;
		api = new AuguraRestAPIImpl();
		HandlerThread th = new HandlerThread("project");
		th.start();
		handler = new CmdHandler(th.getLooper());

		uiHandler = new UIHandler();

		projectAdapter = new ListAdapter(context);
		projectList.setAdapter(projectAdapter);
		projectAdapter.notifyDataSetChanged();

	}

	@Override
	protected void onResume() {
		super.onResume();
		projectAdapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	private void showSettingDialg() {
		final Dialog settingDlg = new Dialog(this);
		settingDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		settingDlg.setCanceledOnTouchOutside(false);
		settingDlg.setContentView(R.layout.setting_dialog);
		final EditText etName = (EditText) settingDlg
				.findViewById(R.id.ed_name);
		final EditText etPwd = (EditText) settingDlg.findViewById(R.id.ed_pwd);
		final EditText etAPI = (EditText) settingDlg.findViewById(R.id.ed_api);
		LinearLayout setButton = (LinearLayout) settingDlg
				.findViewById(R.id.ok_button_lin);
		final SharedPreferences sp = this.context.getSharedPreferences(
				Constants.SaveConfig.CONFIG, MODE_PRIVATE);
		etName.setText(sp.getString(Constants.SaveConfig.USER_NAME, ""));
		etPwd.setText(sp.getString(Constants.SaveConfig.PASSWORD, ""));
		etAPI
				.setText(sp.getString(Constants.SaveConfig.API,
						Constants.API_URL));
		setButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (etName.getText() == null
						|| etName.getText().toString().equals("")) {
					Toast.makeText(context, " Name can't be empty",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (etPwd.getText() == null
						|| etPwd.getText().toString().equals("")) {
					Toast.makeText(context, " Password can't be empty",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (etAPI.getText() == null
						|| etAPI.getText().toString().equals("")) {
					Toast.makeText(context, " API can't be empty",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Editor ed = sp.edit();
				ed.putString(Constants.SaveConfig.USER_NAME, etName.getText()
						.toString());
				ed.putString(Constants.SaveConfig.PASSWORD, etPwd.getText()
						.toString());
				ed.putString(Constants.SaveConfig.API, etAPI.getText()
						.toString());
				ed.commit();
				showRestartDialog();
				settingDlg.cancel();
				settingDlg.dismiss();
			}

		});
		settingDlg.show();
	}

	private void showRestartDialog() {

	}

	OnClickListener addProjectListener = new OnClickListener() {
		public void onClick(View v) {
			SoundEngine.sharedEngine().playEffect(getApplicationContext(),
					R.raw.button);
			// custom dialog
			final Dialog dialog = new Dialog(context);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setContentView(R.layout.add_project_dialog);
			dialog.setTitle(context.getText(R.string.add_project));
			LinearLayout dialogButton = (LinearLayout) dialog
					.findViewById(R.id.ok_button_lin);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					EditText projectText = (EditText) dialog
							.findViewById(R.id.project_numc);
					if (projectText.getText() == null
							|| projectText.getText().toString() == null
							|| projectText.getText().toString().equals("")) {
						Toast.makeText(dialog.getContext(),
								context.getText(R.string.not_null_query),
								Toast.LENGTH_LONG).show();
					} else {
						Message.obtain(handler, CMD_QUERY_PROJECT,
								projectText.getText().toString())
								.sendToTarget();
						

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

	private void saveToDatabase(ProjectList pl) {
		for (int i = 0; i < pl.getList().size(); i++) {
			Project p = pl.getList().get(i);
			ContentValues cv = new ContentValues();
			cv.put(ContentDescriptor.ProjectDesc.Cols.PRO_ID, p.getId());
			cv.put(ContentDescriptor.ProjectDesc.Cols.NAME, p.getName());
			cv.put(ContentDescriptor.ProjectDesc.Cols.TEXT, p.getText());
			cv.put(ContentDescriptor.ProjectDesc.Cols.SYNC_FLAG, "1");
			Uri uri = this.getContentResolver().insert(
					ContentDescriptor.ProjectDesc.CONTENT_URI, cv);
			long perojectId = ContentUris.parseId(uri);
			p.setnID((int) perojectId);
			p.setLoadOrderFromDB(true);

			for (int j = 0; j < p.getOrderCount(); j++) {
				cv.clear();
				ProjectOrder po = p.getOrder(j);
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.NAME, po
						.getName());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.DESCRIPTION, po
						.getDescription());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.ORD_ID, po
						.getId());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID, p
						.getId());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT, po
						.getQcComment());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS, po
						.getQcStatus());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY, po
						.getQuantity());
				cv
						.put(
								ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED,
								po.getQuantityChecked());
				cv.put(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_NAME, po
						.getPhotoName());
				cv
						.put(
								ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
								po.getPhotoPath());
				cv
						.put(
								ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_BIG_PATH,
								po.getPhotoBigPath());
				uri = this.getContentResolver().insert(
						ContentDescriptor.ProjectOrderDesc.CONTENT_URI, cv);
				long projectOrderId = ContentUris.parseId(uri);
				po.setnID((int) projectOrderId);
				po.setLoadedCheckpointFromDB(true);

				for (int z = 0; z < po.getCheckpointCount(); z++) {
					cv.clear();
					ProjectCheckpoint pcp = po.getOrderCheckpointrByIndex(z);
					saveCheckpoint(pcp);
				}
			}

		}
	}

	private void removeProjectFromDB(ProjectList pl) {
		ContentResolver cr = this.getContentResolver();
		for (int i = 0; i < pl.getList().size(); i++) {
			Project p = pl.getList().get(i);
			// delete project_order_checkpoint table records
			int ret = cr.delete(
					ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
					ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID + "=?",
					new String[] { p.getId() });
			Log.i(Constants.TAG, "-------delete " + ret);
			ret = cr.delete(ContentDescriptor.ProjectOrderDesc.CONTENT_URI,
					ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID + "=?",
					new String[] { p.getId() });
			Log.i(Constants.TAG, "-------delete " + ret);
			ret = cr.delete(ContentDescriptor.ProjectDesc.CONTENT_URI,
					ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID + "=?",
					new String[] { p.getId() });
			Log.i(Constants.TAG, "-------delete " + ret);
		}
	}

	private void updateCheckpoint(ProjectCheckpoint pcp) {
		ContentValues cv = new ContentValues();
		cv
				.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME, pcp
						.getName());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION, pcp
				.getDescription());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID, pcp
				.getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT, pcp
				.getQcComments());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS, pcp
				.getQcStatus());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID, pcp
				.getProjectItem().getProject().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID, pcp
				.getProjectItem().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY, pcp
				.getCategory());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE, pcp
				.getCheckType());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION, pcp
				.getQcAction());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT, pcp
				.getNumberDefect());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME, pcp
				.getPhotoName());
		cv
				.put(
						ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
						pcp.getPhotoPath());
		cv
				.put(
						ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_BIG_PATH,
						pcp.getPhotoPath());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG, "0");
		int ret = this.getContentResolver().update(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI, cv,
				ContentDescriptor.ProjectCheckpointDesc.Cols.ID + "=?",
				new String[] { pcp.getnID() + "" });
		Log.i(Constants.TAG, "======================" + ret);
	}

	private void saveCheckpoint(ProjectCheckpoint pcp) {
		ContentValues cv = new ContentValues();

		cv
				.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME, pcp
						.getName());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION, pcp
				.getDescription());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID, pcp
				.getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT, pcp
				.getQcComments());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS, pcp
				.getQcStatus());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID, pcp
				.getProjectItem().getProject().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID, pcp
				.getProjectItem().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY, pcp
				.getCategory());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE, pcp
				.getCheckType());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION, pcp
				.getQcAction());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT, pcp
				.getNumberDefect());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME, pcp
				.getPhotoName());
		cv
				.put(
						ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
						pcp.getPhotoPath());
		cv
				.put(
						ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_BIG_PATH,
						pcp.getPhotoPath());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG, "0");
		Uri uri = this.getContentResolver().insert(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI, cv);
		long checkpointID = ContentUris.parseId(uri);
		pcp.setnID((int) checkpointID);

	}

	private void doSync(Project p) throws APIException, SessionAPIException {
		Cursor c = null;

		try {
			c = this.getContentResolver().query(
					ContentDescriptor.UpdateDesc.CONTENT_URI,
					ContentDescriptor.UpdateDesc.Cols.ALL_COLS,
					ContentDescriptor.UpdateDesc.Cols.PRO_ID + "=?",
					new String[] { p.getId() }, null);
			while (c.moveToNext()) {
				String relatedId = c
						.getString(c
								.getColumnIndex(ContentDescriptor.UpdateDesc.Cols.RELATE_ID));
				String orderId = c
						.getString(c
								.getColumnIndex(ContentDescriptor.UpdateDesc.Cols.PRO_ORDER_ID));
				String flag = c
						.getString(c
								.getColumnIndex(ContentDescriptor.UpdateDesc.Cols.FLAG));
				if (flag
						.equals(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_DELETE)) {
					try {
						api.deleteCheckpoint(relatedId);
					} catch (Exception e) {
						e.printStackTrace();
					}

					this
							.getContentResolver()
							.delete(
									ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
									ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID
											+ "=?", new String[] { relatedId });

				} else if (flag
						.equals(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE)) {
					ProjectOrder po = p.getOrder(orderId);
					if (po == null) {
						Log.e(Constants.TAG, "Can't find ProjectOrder data:"
								+ orderId);
						continue;
					}
					ProjectCheckpoint pc = po
							.findProjectCheckpointById(relatedId);
					if (po == null || pc == null) {
						Log.e(Constants.TAG, "Can't find checkpoint data:"
								+ relatedId);
						continue;
					}
					// Clear temporary id to get new Id
					pc.setId(null);
					api.createCheckpoint(po, pc);
					updateCheckpoint(pc);
				} else if (flag
						.equals(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_UPDATE)) {
					ProjectOrder po = p.getOrder(orderId);
					ProjectCheckpoint pc = po
							.findProjectCheckpointById(relatedId);
					if (pc == null) {
						Log.e(Constants.TAG, "Can't find checkpoint data:"
								+ relatedId);
						continue;
					}
					api.updateCheckpoint(pc);
				} else {
					Log.e(Constants.TAG, "Incorrect type:" + flag);
				}
			}
		} catch (APIException e) {
			throw e;
		} catch (SessionAPIException e) {
			throw e;
		} finally {
			if (c != null)
				c.close();
		}

		this.getContentResolver().delete(
				ContentDescriptor.UpdateDesc.CONTENT_URI,
				ContentDescriptor.UpdateDesc.Cols.PRO_ID + "=?",
				new String[] { p.getId() });

		p.setNeededUpdate(false);
		// TODO update UI
	}

	private ProgressDialog dialog;

	class UIHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_START_WAITING:
				dialog = ProgressDialog.show(context, "Loading",
						"Please wait...", true);
				dialog.setIcon(R.drawable.logo_24_24);
				dialog.show();
				break;
			case UI_END_WAITING:
				if (dialog != null) {
					dialog.cancel();
					dialog.dismiss();
				}
				projectAdapter.notifyDataSetChanged();
				break;
			case UI_END_WAITING_WITH_ERROR:
				if (dialog != null) {
					dialog.cancel();
					dialog.dismiss();
				}
				Toast.makeText(dialog.getContext(),
						R.string.error_load_project, Toast.LENGTH_LONG).show();
				if (msg.obj != null && ((Integer) msg.obj) == -1) {
					Intent i = new Intent();
					i.setAction("com.auguraclient.view.login");
					i.addCategory("com.auguraclient.view");
					startActivity(i);
					finish();

				}
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
			case CMD_QUERY_PROJECT:

				try {
					Message.obtain(uiHandler, UI_START_WAITING)
					.sendToTarget();
					//TODO login fist
					SharedPreferences sp = context.getSharedPreferences(
							Constants.SaveConfig.CONFIG, MODE_PRIVATE);
					String userName = sp.getString(Constants.SaveConfig.USER_NAME, "");
					String password = sp.getString(Constants.SaveConfig.PASSWORD, "");
					if(userName == null || userName.equals("") || password ==null || password.equals("")) {
						Message.obtain(uiHandler, UI_END_WAITING)
						.sendToTarget();
						Toast.makeText(context, "please set user name and password first!",
								Toast.LENGTH_SHORT).show();
						return;
					}
					 User user = api.login(userName, password);
					 if(user ==null) {
						 Message.obtain(uiHandler, UI_END_WAITING_WITH_ERROR)
							.sendToTarget();
						 return;
					 }
					 GlobalHolder.setCurrentUser(user);
					
					ProjectList pl = api.loadProject((String) msg.obj);
					if (pl == null) {
						Message.obtain(uiHandler, UI_END_WAITING_WITH_ERROR)
								.sendToTarget();
					} else if (pl.getResultCount() == 0) {
						Toast.makeText(context, R.string.can_not_find_project,
								Toast.LENGTH_SHORT).show();
					} else {

						// FIXME remove project from cache and database
						removeProjectFromDB(pl);
						GlobalHolder.addProject(pl);
						saveToDatabase(pl);
					}
					Message.obtain(uiHandler, UI_END_WAITING).sendToTarget();
				} catch (SessionAPIException e) {
					e.printStackTrace();
					Message.obtain(uiHandler, UI_END_WAITING_WITH_ERROR, -1)
							.sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					Message.obtain(uiHandler, UI_END_WAITING_WITH_ERROR)
							.sendToTarget();
				}
				break;

			case CMD_LOAD_PROJECT_FROM_DB:
				Message.obtain(uiHandler, UI_START_WAITING).sendToTarget();
				Message.obtain(uiHandler, UI_END_WAITING).sendToTarget();
				break;
			case CMD_SYNC_PROJECT:
				String id = (String) msg.obj;
				Project p = GlobalHolder.getProjectById(id);
				if (id == null || id.equals("") || p == null) {
					Toast.makeText(context, "Can't get project information",
							Toast.LENGTH_SHORT);
					return;
				}
				Message.obtain(uiHandler, UI_START_WAITING).sendToTarget();
				try {
					
					//TODO login fist
					SharedPreferences sp = context.getSharedPreferences(
							Constants.SaveConfig.CONFIG, MODE_PRIVATE);
					String userName = sp.getString(Constants.SaveConfig.USER_NAME, "");
					String password = sp.getString(Constants.SaveConfig.PASSWORD, "");
					
					 User user = api.login(userName, password);
					 if(user ==null) {
						 Message.obtain(uiHandler, UI_END_WAITING_WITH_ERROR)
							.sendToTarget();
						 return;
					 }
					 GlobalHolder.setCurrentUser(user);
					
					doSync(p);
				} catch (SessionAPIException e) {
					Toast.makeText(context, "sync error " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					Message.obtain(uiHandler, UI_END_WAITING_WITH_ERROR, -1)
							.sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "sync error " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
				Message.obtain(uiHandler, UI_END_WAITING).sendToTarget();

			}
		}

	}

	class ListAdapter extends BaseAdapter {

		private Context context;

		public ListAdapter(Context context) {
			this.context = context;
		}

		public int getCount() {
			return GlobalHolder.getPl() == null ? 0 : GlobalHolder.getPl()
					.getResultCount();
		}

		public Object getItem(int position) {
			return GlobalHolder.getProject(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				ItemView appView = new ItemView(context);
				appView.updateView(GlobalHolder.getProject(position));
				convertView = appView;
			} else {
				((ItemView) convertView).updateView(GlobalHolder
						.getProject(position));
			}
			return convertView;
		}

	}

	class ItemView extends LinearLayout {

		private Context mContext;

		private TextView tv;

		private ImageView operation;

		public ItemView(Context context) {
			super(context);
			this.mContext = context;
			initilize(context);
		}

		public void initilize(Context c) {
			this.mContext = c;
			View view = LayoutInflater.from(this.mContext).inflate(
					R.layout.component_project, null);
			addView(view);
			tv = (TextView) this.findViewById(R.id.projectListItemName);
			operation = (ImageView) this.findViewById(R.id.project_state);
		}

		public void updateView(final Project p) {
			tv.setText(p.getText());
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.setClass(context, ProjectOrderListView.class);
					i.putExtra("project", GlobalHolder.getIndex(p));
					context.startActivity(i);
				}

			});
			if (p.isNeededUpdate()) {
				operation.setImageResource(R.drawable.project_sync);
				operation.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Message.obtain(handler, CMD_SYNC_PROJECT, p.getId())
								.sendToTarget();
					}

				});
			} else {
				operation.setImageResource(R.drawable.project_ok);
			}
		}
	}
}
