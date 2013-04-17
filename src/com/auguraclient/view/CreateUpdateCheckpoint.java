package com.auguraclient.view;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SuguraRestAPIImpl;
import com.auguraclient.util.GlobalHolder;

@SuppressLint("NewApi")
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

	private TextView returnButton;

	private TextView showMenuButton;

	private TextView selectPhotoBUtton;

	private boolean createFlag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.checkpoint_update);
		initView();
		initListener();

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
			checkpointPhoto.setImageURI(Uri.fromFile(new File(
					GlobalHolder.GLOBAL_STORAGE_PATH
							+ projectCheckpoint.getPhotoPath())));
			checkpointPhoto.setOnClickListener(onOpenPhotoClickListener);
		}

	}

	private void initListener() {
		returnButton.setOnClickListener(returnButtonListener);
		submitButton.setOnClickListener(submitListener);
		showMenuButton.setOnClickListener(showMenuListener);
		selectPhotoBUtton.setOnClickListener(selectPhotoListener);
	}

	private void initView() {
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

		returnButton = (TextView) this
				.findViewById(R.id.updateCheckpointReturn);

		showMenuButton = (TextView) this
				.findViewById(R.id.showUpdateCheckpointMenu);

		selectPhotoBUtton = (TextView) this.findViewById(R.id.selectPhoto);
	}

	private OnClickListener selectPhotoListener = new OnClickListener() {

		public void onClick(View arg0) {
			recordData();
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, 100);
		}
	};

	private OnClickListener showMenuListener = new OnClickListener() {

		public void onClick(View arg0) {
			final PopupMenu popupMenu = new PopupMenu(mContext, showMenuButton);
			popupMenu.inflate(R.layout.update_checkpoint_title_menu);
			popupMenu
					.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

						public boolean onMenuItemClick(MenuItem item) {

							if (item.getItemId() == R.id.delete_checkpoint) {
								doDeleteCheckpoint();
							}
							popupMenu.dismiss();
							return true;
						}
					});

			popupMenu.show();
		}

	};

	private OnClickListener onOpenPhotoClickListener = new OnClickListener() {

		public void onClick(View v) {			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(
					Uri.fromFile(new File(GlobalHolder.GLOBAL_STORAGE_PATH
							+ projectCheckpoint.getPhotoPath())), "image/*");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(Intent.createChooser(intent, "Select Picture"));
		}

	};

	private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			finish();
		}

	};

	private OnClickListener submitListener = new OnClickListener() {

		public void onClick(View v) {
			Editable nameText = nameEditText.getText();
			if (nameText == null || nameText.toString() == null
					|| nameText.toString().equals("")) {
				Toast.makeText(mContext, "Name can't be empty",
						Toast.LENGTH_LONG).show();
				return;
			}
			recordData();
			Message.obtain(cmdHandler, UI_COMMIT).sendToTarget();
		}

	};

	private void recordData() {
		projectCheckpoint.setCategory(categoryEditText.getText().toString());
		projectCheckpoint.setCheckType(checkpointEditText.getText().toString());
		projectCheckpoint.setName(nameEditText.getText().toString());
		projectCheckpoint.setDescription(detailEditText.getText().toString());
		projectCheckpoint.setQcComments(qcCommentEditText.getText().toString());
		projectCheckpoint
				.setNumberDefect(qcDefectEditText.getText().toString());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == 100) {
			Uri selectedImageUri = data.getData();
			checkpointPhoto.setImageURI(selectedImageUri);
			projectCheckpoint.setUploadPhotoAbsPath(getRealPathFromURI(selectedImageUri));
		}
	}

	
	private String  getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(column_index);
        cursor.close();
        return url;
    }
	
	private void doDeleteCheckpoint() {
		Message.obtain(this.cmdHandler, UI_DELETE_CHECKPOINT).sendToTarget();
	}

	public static final int UI_START_SUBMIT = 1;

	public static final int UI_COMMIT = 2;

	public static final int UI_END_COMMIT = 3;

	public static final int UI_DELETE_CHECKPOINT = 4;

	public static final int UI_END_THIS_SESSION = 5;

	private void submit() throws APIException {
		if (this.createFlag) {
			api.createCheckpoint(this.projectOrder, projectCheckpoint);
		} else {
			api.updateCheckpoint(projectCheckpoint);
		}
	}

	private void deleteCheckpoint() throws APIException {
		api.deleteCheckpoint(projectCheckpoint.getId());
		ContentResolver cr = this.getContentResolver();
		int ret = cr.delete(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
				ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID
						+ "=?", new String[] { projectCheckpoint.getId() });
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
					Toast.makeText(mContext, "Sumit successfully ",
							Toast.LENGTH_LONG).show();
				} catch (APIException e) {
					e.printStackTrace();
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
				Message.obtain(uiHandler, UI_END_COMMIT).sendToTarget();
				break;

			case UI_DELETE_CHECKPOINT:
				String message = null;
				Message.obtain(uiHandler, UI_START_SUBMIT).sendToTarget();
				try {
					deleteCheckpoint();
					message = "Delete successfully";
				} catch (APIException e) {
					e.printStackTrace();
					message = e.getMessage();
				}
				Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
				Message.obtain(uiHandler, UI_END_THIS_SESSION).sendToTarget();
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
				break;
			case UI_END_THIS_SESSION:
				closeDialog();
				Intent i = new Intent();
				i.putExtra("checkpoint", projectCheckpoint);
				i.putExtra("delete", true);
				setResult(3, i);
				finish();
				break;
			}
		}

	}

}
