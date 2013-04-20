
package com.auguraclient.view;

import com.auguraclient.R;
import com.auguraclient.model.APIException;
import com.auguraclient.model.IAuguraRestAPI;
import com.auguraclient.model.AuguraRestAPIImpl;
import com.auguraclient.model.User;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.SoundEngine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class LoginScreen extends Activity {

    private static final int CMD_SEND_lOG_IN_REQUEST = 1;

    private static final int PROGRESS_START_TO_LOG_IN = 1;

    private static final int PROGRESS_START_TO_LOG_IN_ERROR = 2;

    private static final int PROGRESS_START_TO_LOG_IN_SUCCESSFUL = 3;

    private static final int PROGRESS_START_TO_LOG_IN_ERROR_WITH_USER_INVALID = 4;


    private EditText edit_login = null;

    private EditText edit_password = null;

    private LinearLayout ok_button = null;

    private CmdHandler handler;

    private ProgressHandler progressHandler;

    private IAuguraRestAPI api;


    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);


        edit_login = (EditText)findViewById(R.id.login_text);

        edit_login.setOnTouchListener(login_touch);

        edit_password = (EditText)findViewById(R.id.password_text);

        edit_password.setOnTouchListener(password_touch);

        ok_button = (LinearLayout)findViewById(R.id.ok_button);

        ok_button.setOnClickListener(check_login);

        HandlerThread ht = new HandlerThread("cmdHandler");
        ht.start();

        handler = new CmdHandler(ht.getLooper());

        progressHandler = new ProgressHandler();

        api = new AuguraRestAPIImpl();

        context = this;
    }

    OnTouchListener login_touch = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            edit_login.setText("");
            edit_login.setOnTouchListener(null);
            return false;
        }
    };

    OnTouchListener password_touch = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            edit_password.setText("");
            edit_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            edit_password.setOnTouchListener(null);
            return false;
        }
    };

    OnClickListener check_login = new OnClickListener() {
        public void onClick(View v) {
            SoundEngine.sharedEngine().playEffect(getApplicationContext(), R.raw.button);

             if(edit_login.getText() ==null || edit_password.getText() == null) {
                 showErrorDialog(R.string.app_name, Resources.getSystem().getString(R.string.login_error_1));
                 return;
             }
            String username = edit_login.getText().toString();

            String password = edit_password.getText().toString();

            if (username == null || username.trim().equals("Login")
                    || username.trim().length() == 0) {
                showErrorDialog(R.string.app_name, context.getString(R.string.login_error_1));
                return;
            }

            // send login progress
            Message.obtain(progressHandler, PROGRESS_START_TO_LOG_IN).sendToTarget();

            // send login request
            Message.obtain(handler, CMD_SEND_lOG_IN_REQUEST, new String[] {
                    username, password
            }).sendToTarget();

        }
    };

    public void setfullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }



    private void showErrorDialog(int resID, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen.this);
        builder.setIcon(R.drawable.alert_icon);
        builder.setMessage(msg);
        builder.setTitle(resID);
        final AlertDialog error_dialog = builder.create();
        builder.setPositiveButton(R.string.login_screen_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        error_dialog.cancel();
                        error_dialog.dismiss();
                    }
                });
        error_dialog.show();
    }

    private  AlertDialog loggingInDialog;
    class ProgressHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_START_TO_LOG_IN:

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen.this);
                    builder.setIcon(R.drawable.logo_24_24);
                    builder.setMessage(R.string.login_waiting_login);
                    builder.setTitle(R.string.app_name);
                    loggingInDialog = builder.create();
                    loggingInDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                        public boolean onKey(DialogInterface arg0, int keyCode,
                                KeyEvent event) {
                            return true;
                        }
                    });
                    loggingInDialog.setCanceledOnTouchOutside(false);
                    loggingInDialog.show();

                    break;
                case PROGRESS_START_TO_LOG_IN_ERROR_WITH_USER_INVALID:
                case PROGRESS_START_TO_LOG_IN_ERROR:
                    if(loggingInDialog!= null) {
                        loggingInDialog.cancel();
                        loggingInDialog.dismiss();
                    }
                    showErrorDialog(R.string.app_name, (String)msg.obj);
                    break;
                case PROGRESS_START_TO_LOG_IN_SUCCESSFUL:
                    if(loggingInDialog!= null) {
                        loggingInDialog.cancel();
                        loggingInDialog.dismiss();
                    }
                    Intent i = new Intent();
                    i.setAction("com.auguraclient.view.projectList");
                    i.addCategory("com.auguraclient.view");
                    startActivity(i);
                    finish();
                    break;
            }
        }

    }

    class CmdHandler extends Handler {

        public CmdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CMD_SEND_lOG_IN_REQUEST:
                    if (msg.obj == null) {
                        Log.e(Constants.TAG, "login parameter is null ");
                        return;
                    }
                    String username = ((String[])msg.obj)[0];
                    String password = ((String[])msg.obj)[1];
                    try {
                        User user = api.login(username, password);
                        user.setPassword(password);
                        if(user == null ) {
                            Message.obtain(progressHandler, PROGRESS_START_TO_LOG_IN_ERROR_WITH_USER_INVALID, " Log in failed")
                            .sendToTarget();
                        } else {
                            GlobalHolder.setCurrentUser(user);
                            SharedPreferences sp = context.getSharedPreferences(Constants.SaveConfig.CONFIG, MODE_PRIVATE);
                            Editor edit = sp.edit();
                            edit.putString(Constants.SaveConfig.USER_ID, user.getUseID());
                            edit.putString(Constants.SaveConfig.USER_NAME, user.getUserName());
                            edit.putString(Constants.SaveConfig.SESSION, user.getmSessionID());
                            edit.putString(Constants.SaveConfig.PASSWORD, user.getPassword());
                            edit.commit();
                            Message.obtain(progressHandler, PROGRESS_START_TO_LOG_IN_SUCCESSFUL)
                            .sendToTarget();
                        }
                    } catch (Exception e) {
                        // send login progress
                        Message.obtain(progressHandler, PROGRESS_START_TO_LOG_IN_ERROR, e.getMessage())
                                .sendToTarget();
                    }
                    break;
            }
        }





    }

}
