package com.auguraclient.view;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.auguraclient.R;
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.logo_view);
		handler = new LogoHandler();
		mContext = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Message m = Message.obtain(handler, INIT);
		handler.sendMessageDelayed(m, 3000);
		GlobalHolder.GLOBAL_STORAGE_PATH = Environment
				.getExternalStorageDirectory().getAbsolutePath();
		String path = GlobalHolder.GLOBAL_STORAGE_PATH
				+ Constants.CommonConfig.PIC_DIR;
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Toast.makeText(this, R.string.error_create_pic_dir,
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	class LogoHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Intent i = new Intent();
			switch (msg.what) {
			case INIT:
				SharedPreferences sp = mContext.getSharedPreferences(
						Constants.SaveConfig.CONFIG, MODE_PRIVATE);
				String userName = sp.getString(Constants.SaveConfig.USER_NAME,
						"");
				String password = sp.getString(Constants.SaveConfig.PASSWORD,
						"");
				String session = sp.getString(Constants.SaveConfig.SESSION, "");
				String userID = sp
						.getString(Constants.SaveConfig.USER_NAME, "");
				if (userName == null || userName.isEmpty() || password == null
						|| password.isEmpty() || session == null
						|| session.isEmpty() || userID == null
						|| userID.isEmpty()) {
					i.setAction("com.auguraclient.view.login");
					i.addCategory("com.auguraclient.view");
				} else {
					User u = new User();
					u.setUserName(userName);
					u.setmSessionID(session);
					u.setUseID(userID);
					GlobalHolder.setCurrentUser(u);
					i.setAction("com.auguraclient.view.projectList");
					i.addCategory("com.auguraclient.view");
				}
				mContext.startActivity(i);
				doFinish();
				break;
			}
		}

	}

	public void doFinish() {
		finish();
	}

}
