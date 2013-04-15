package com.auguraclient.view;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.auguraclient.R;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SuguraRestAPIImpl;

public class CreateUpdateCheckpoint extends Activity {

	private EditText categoryEditText;

	private EditText checkpointEditText;

	private EditText nameEditText;

	private EditText detailEditText;

	private EditText qcDefectEditText;

	private EditText qcCommentEditText;

	private ImageView checkpointPhoto;

	private ProjectOrder projectOrder;

	private ProjectCheckpoint projectCheckpoint;

	private TextView addOrUpdateTextView;

	private LinearLayout submitButton;

	private Context mContext;

	private ISuguraRestAPI api;

	private UiHandler uiHandler;
	
	private CmdHanlder cmdHandler;

	private boolean createFlag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.setContentView(R.layout.checkpoint_update);

		categoryEditText = (EditText) this.findViewById(R.id.categoryEditText);
		checkpointEditText = (EditText) this
				.findViewById(R.id.checkpointEditText);
		nameEditText = (EditText) this.findViewById(R.id.nameEditText);
		detailEditText = (EditText) this.findViewById(R.id.detailEditText);
		qcCommentEditText = (EditText) this
				.findViewById(R.id.qcCommentEditText);
		checkpointPhoto = (ImageView) this.findViewById(R.id.checkpointPhoto);
		qcDefectEditText = (EditText) this.findViewById(R.id.qcDefectEditText);

		addOrUpdateTextView = (TextView) this
				.findViewById(R.id.bottom_tab_add_or_update_checkpoint);

		submitButton = (LinearLayout) findViewById(R.id.bottom_tab_add_or_update_checkpoint_layout);

		submitButton.setOnClickListener(submitListener);

		createFlag = this.getIntent().getBooleanExtra("create", true);
		mContext = this;
		projectOrder = (ProjectOrder) this.getIntent().getSerializableExtra(
				"projectOrder");
		if (createFlag) {
			projectCheckpoint = new ProjectCheckpoint();
			projectOrder.addOrderCheckpoint(projectCheckpoint);
			addOrUpdateTextView.setText(R.string.create_checkpoint);
		} else {
			addOrUpdateTextView.setText(R.string.update_checkpoint);
			projectCheckpoint = (ProjectCheckpoint) this.getIntent()
					.getSerializableExtra("projectCheckpoint");
		}

		api = new SuguraRestAPIImpl();

		uiHandler = new UiHandler();
		
		HandlerThread ut = new HandlerThread("ut");
		ut.start();
		cmdHandler = new CmdHanlder(ut.getLooper());
	}

	@Override
	protected void onResume() {
		super.onResume();
		categoryEditText.setText(projectCheckpoint.getCategory());
		checkpointEditText.setText(projectCheckpoint.getCheckType());
		nameEditText.setText(projectCheckpoint.getName());
		detailEditText.setText(projectCheckpoint.getDescription());
		qcCommentEditText.setText(projectCheckpoint.getQcComments());
		qcDefectEditText.setText(projectCheckpoint.getNumberDefect());
		if (projectCheckpoint.getPhotoPath() != null) {
			checkpointPhoto.setImageURI(Uri.fromFile(new File(projectCheckpoint
					.getPhotoPath())));
		}

	}

	private OnClickListener submitListener = new OnClickListener() {

		public void onClick(View v) {
			projectCheckpoint
					.setCategory(categoryEditText.getText().toString());
			projectCheckpoint.setCheckType(checkpointEditText.getText()
					.toString());
			projectCheckpoint.setName(nameEditText.getText().toString());
			projectCheckpoint.setDescription(detailEditText.getText()
					.toString());
			projectCheckpoint.setQcComments(qcCommentEditText.getText()
					.toString());
			projectCheckpoint.setNumberDefect(qcDefectEditText.getText()
					.toString());
			Message.obtain(cmdHandler, UI_COMMIT).sendToTarget();
		}

	};

	public static final int UI_START_SUBMIT = 1;

	public static final int UI_COMMIT = 2;

	public static final int UI_END_COMMIT = 3;

	private void submit() throws APIException {
		if (this.createFlag) {
			api.createCheckpoint(this.projectOrder, projectCheckpoint);
		} else {
			api.updateCheckpoint(projectCheckpoint);
		}
	}

	private void showDialog() {
		dialog = ProgressDialog.show(mContext, "", "Please wait...", true);
		dialog.setIcon(R.drawable.logo_24_24);
		dialog.show();
	}

	private void closeDialog() {
		dialog.cancel();
		dialog.dismiss();
		dialog = null;
	}

	private ProgressDialog dialog;

	class CmdHanlder extends Handler {

		public CmdHanlder(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_COMMIT:
				Message.obtain(uiHandler, UI_START_SUBMIT).sendToTarget();
				try {
					submit();
				} catch (APIException e) {
					e.printStackTrace();
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
				Message.obtain(uiHandler, UI_END_COMMIT).sendToTarget();
				break;
			}
		}

	}

	class UiHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_START_SUBMIT:
				showDialog();
				break;
			case UI_END_COMMIT:
				closeDialog();

			}
		}

	}

}
