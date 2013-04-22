package com.auguraclient.view;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.User;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;

public class LogoView extends Activity {

	private static final int INIT = 1;

	private LogoHandler handler;

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.logo_view);
		handler = new LogoHandler();
		mContext = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Message m = Message.obtain(handler, INIT);
		handler.sendMessageDelayed(m, 1000);
	}

	class LogoHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT:
				initConfig();
				doFinish();
				break;
			}
		}

	}

	private void initConfig() {
		GlobalHolder.GLOBAL_STORAGE_PATH = Environment
				.getExternalStorageDirectory().getAbsolutePath();
		String path = GlobalHolder.GLOBAL_STORAGE_PATH
				+ Constants.CommonConfig.PIC_DIR;
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Toast.makeText(mContext, R.string.error_create_pic_dir,
						Toast.LENGTH_SHORT).show();
			}
		}
		SharedPreferences sp = mContext.getSharedPreferences(
				Constants.SaveConfig.CONFIG, MODE_PRIVATE);
		String userName = sp.getString(Constants.SaveConfig.USER_NAME, "");
		String password = sp.getString(Constants.SaveConfig.PASSWORD, "");
		String session = sp.getString(Constants.SaveConfig.SESSION, "");
		String userID = sp.getString(Constants.SaveConfig.USER_ID, "");

		Constants.HOST = sp.getString(Constants.SaveConfig.API,
				"crm.augura.net");
		Intent i = new Intent();
		if (userName == null || userName.equals("") || session == null
				|| session.equals("") || userID == null || userID.equals("")) {
			i.setAction("com.auguraclient.view.login");
			i.addCategory("com.auguraclient.view");
		} else {
			User u = new User();
			u.setUserName(userName);
			u.setmSessionID(session);
			u.setPassword(password);
			u.setUseID(userID);
			GlobalHolder.setCurrentUser(u);
			loadProjectFromDb();
			i.setAction("com.auguraclient.view.projectList");
			i.addCategory("com.auguraclient.view");
		}
		mContext.startActivity(i);
	}

	private void loadProjectFromDb() {
		ProjectList pl = new ProjectList();
		ContentResolver cr = this.getContentResolver();
		Cursor c = null;
		c = cr.query(ContentDescriptor.ProjectDesc.CONTENT_URI,
				ContentDescriptor.ProjectDesc.Cols.ALL_CLOS, null, null, null);
		pl.setResultCount(c.getCount());
		while (c.moveToNext()) {
			Project p = new Project();
			p.setnID(c.getInt(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectDesc.Cols.ID)));
			p.setName(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectDesc.Cols.NAME)));
			p.setText(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectDesc.Cols.TEXT)));
			p.setId(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectDesc.Cols.PRO_ID)));
			loadOderFromDB(p);
			pl.addProject(p);

			Cursor updateCursor = cr.query(
					ContentDescriptor.UpdateDesc.CONTENT_URI,
					new String[] { ContentDescriptor.UpdateDesc.Cols.FLAG },
					ContentDescriptor.UpdateDesc.Cols.PRO_ID + "=?",
					new String[] { p.getId() }, null);
			if (updateCursor.getCount() > 0) {
				p.setNeededUpdate(true);
			} else {
				p.setNeededUpdate(false);
			}
			updateCursor.close();
		}
		c.close();
		GlobalHolder.setPl(pl);
	}

	private void loadOderFromDB(Project p) {
		ContentResolver cr = this.getContentResolver();
		Cursor c = null;
		c = cr.query(ContentDescriptor.ProjectOrderDesc.CONTENT_URI,
				ContentDescriptor.ProjectOrderDesc.Cols.ALL_COLS,
				ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID + "=?",
				new String[] { p.getId() }, null);
		DateFormat dp = new SimpleDateFormat("yyyy-MM-dd");
		while (c.moveToNext()) {
			ProjectOrder po = new ProjectOrder();
			po.setnID(c.getInt(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.ID)));
			po.setId(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.ORD_ID)));
			po.setName(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.NAME)));
			po.setDescription(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.DESCRIPTION)));
			po.setPhotoBigPath(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_BIG_PATH)));
			po.setPhotoPath(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_SMALL_PATH)));
			po.setPhotoName(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_NAME)));
			po.setQcComment(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT)));
			po.setQcStatus(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS)));
			po.setQuantity(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY)));
			po.setQuantityChecked(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED)));
			String dateString  = c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectOrderDesc.Cols.DATE_MODIFIED));
			if(dateString != null) {
				try {
					po.setDateDodified(dp.parse(dateString));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			p.addProjectOrder(po);
			loadCheckpointFromDB(po);
			po.setLoadedCheckpointFromDB(true);
		}

		p.setLoadOrderFromDB(true);
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

			pcp.setDescription(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));

			pcp.setCategory(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY)));

			pcp.setQcComments(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
			pcp.setQcStatus(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS)));
			pcp.setDescription(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
			pcp.setNumberDefect(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT)));
			pcp.setCheckType(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE)));
			pcp.setQcAction(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION)));

			pcp.setUploadPhotoAbsPath(c.getString(c
					.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_PATH)));
			order.addOrderCheckpoint(pcp);
		}

		order.setLoadedCheckpointFromDB(true);
		c.close();
	}

	public void doFinish() {
		finish();
	}

}
