package com.auguraclient.view;

import java.io.File;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.APIException;
import com.auguraclient.model.IAuguraRestAPI;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SessionAPIException;
import com.auguraclient.model.AuguraRestAPIImpl;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;

public class CreateUpdateCheckpoint extends Activity {

	// private EditText categoryEditText;

	// private EditText checkpointEditText;

	private Spinner categorySpinner;

	private Spinner checkTypeSpinner;

	private Spinner qcActionSpinner;

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

	private IAuguraRestAPI api;

	private UiHandler uiHandler;

	private CmdHanlder cmdHandler;

	private TextView returnButton;

	private ImageView showMenuButton;

	private TextView selectPhotoBUtton;

	private TextView[] qcStatusValue;

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

		api = new AuguraRestAPIImpl();

		uiHandler = new UiHandler();

		HandlerThread ut = new HandlerThread("ut");
		ut.start();
		cmdHandler = new CmdHanlder(ut.getLooper());
	}

	@Override
	protected void onResume() {
		super.onResume();
		// categoryEditText.setText(projectCheckpoint.getCategory());
		// checkpointEditText.setText(projectCheckpoint.getCheckType());
		nameEditText.setText(projectCheckpoint.getName());
		detailEditText.setText(projectCheckpoint.getDescription());
		qcCommentEditText.setText(projectCheckpoint.getQcComments());
		qcDefectEditText.setText(projectCheckpoint.getNumberDefect());
		if (projectCheckpoint.getPhotoPath() != null) {
			checkpointPhoto.setImageURI(Uri.fromFile(new File(
					GlobalHolder.GLOBAL_STORAGE_PATH
							+ projectCheckpoint.getPhotoPath())));
			checkpointPhoto.setOnClickListener(onOpenPhotoClickListener);
		} else if (projectCheckpoint.getUploadPhotoAbsPath() != null
				&& !projectCheckpoint.getUploadPhotoAbsPath().equals("")) {
			final Uri photo = Uri.fromFile(new File(projectCheckpoint
					.getUploadPhotoAbsPath()));
			checkpointPhoto.setImageURI(photo);
		}
		setQcStatus();

		categorySpinner.setAdapter(new ArrayAdapter(this, R.layout.spinner_ite,
				GlobalHolder.CATEGORY_ENUM));
		if (this.projectCheckpoint.getCategory() != null) {
			for (int i = 0; i < GlobalHolder.CATEGORY_ENUM_VALUE.length; i++) {
				if (this.projectCheckpoint.getCategory().equals(
						GlobalHolder.CATEGORY_ENUM_VALUE[i])) {
					categorySpinner.setSelection(i);
				}
			}
		}

		checkTypeSpinner.setAdapter(new ArrayAdapter(this,
				R.layout.spinner_ite, GlobalHolder.CHECK_TYPE_ENUM));
		if (this.projectCheckpoint.getCheckType() != null) {
			for (int i = 0; i < GlobalHolder.CHECK_TYPE_ENUM_VALUE.length; i++) {
				if (this.projectCheckpoint.getCheckType().equals(
						GlobalHolder.CHECK_TYPE_ENUM_VALUE[i])) {
					checkTypeSpinner.setSelection(i);
				}
			}
		}

		qcActionSpinner.setAdapter(new ArrayAdapter(this, R.layout.spinner_ite,
				GlobalHolder.QC_ACTION_ENUM));
		if (this.projectCheckpoint.getQcAction() != null) {
			for (int i = 0; i < GlobalHolder.QC_ACTION_ENUM_VALUE.length; i++) {
				if (this.projectCheckpoint.getQcAction().equals(
						GlobalHolder.QC_ACTION_ENUM_VALUE[i])) {
					qcActionSpinner.setSelection(i);
				}
			}
		}

	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent();
		if (projectCheckpoint.getId() != null)
			i.putExtra("checkpoint", projectCheckpoint);
		setResult(4, i);
		super.onBackPressed();
		finish();
	}

	private void initListener() {
		returnButton.setOnClickListener(returnButtonListener);
		submitButton.setOnClickListener(submitListener);
		showMenuButton.setOnClickListener(showMenuListener);
		selectPhotoBUtton.setOnClickListener(selectPhotoListener);
	}

	private void initView() {
		categorySpinner = (Spinner) this.findViewById(R.id.categoryEditText);
		checkTypeSpinner = (Spinner) this.findViewById(R.id.checkpointEditText);
		qcActionSpinner = (Spinner) this.findViewById(R.id.qcActionSpinner);
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

		showMenuButton = (ImageView) this
				.findViewById(R.id.showUpdateCheckpointMenu);

		selectPhotoBUtton = (TextView) this.findViewById(R.id.selectPhoto);

		qcStatusValue = new TextView[] {
				(TextView) this.findViewById(R.id.acStatusEFailed),
				(TextView) this.findViewById(R.id.tvStatusEAlert),
				(TextView) this.findViewById(R.id.tvStatusEPassed),
				(TextView) this.findViewById(R.id.tvStatusEReady),
				(TextView) this.findViewById(R.id.acStatusEEmpty) };
		for (int i = 0; i < qcStatusValue.length; i++) {
			qcStatusValue[i].setOnClickListener(selectQcStatusListener);
		}
	}

	private void setQcStatus() {
		for (int i = 0; i < qcStatusValue.length; i++) {
			String qcStatus = this.projectCheckpoint.getQcStatus();
			if (qcStatus == null) {
				return;
			}
			if (qcStatus
					.equalsIgnoreCase(qcStatusValue[i].getText().toString())) {
				// qcStatusValue[i].setTextColor(R.color.white_background);
				qcStatusValue[i].setBackgroundResource(R.color.red_background);
			} else {
				// qcStatusValue[i].setTextColor(R.color.blank_background);
				qcStatusValue[i]
						.setBackgroundResource(R.color.select_background);
			}
		}
	}

	private OnClickListener selectQcStatusListener = new OnClickListener() {

		public void onClick(View view) {
			projectCheckpoint.setQcStatus(((TextView) view).getText()
					.toString().trim());
			setQcStatus();
		}

	};

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
			popupMenu.getMenuInflater().inflate(
					R.layout.update_checkpoint_title_menu, popupMenu.getMenu());
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
			intent.setDataAndType(Uri.fromFile(new File(
					GlobalHolder.GLOBAL_STORAGE_PATH
							+ projectCheckpoint.getPhotoPath())), "image/*");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(Intent.createChooser(intent, "Select Picture"));
		}

	};

	private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent();
			if (projectCheckpoint.getId() != null)
				i.putExtra("checkpoint", projectCheckpoint);
			setResult(4, i);
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
		projectCheckpoint
				.setCategory(GlobalHolder.CATEGORY_ENUM_VALUE[categorySpinner
						.getSelectedItemPosition()]);
		projectCheckpoint
				.setCheckType(GlobalHolder.CHECK_TYPE_ENUM_VALUE[this.checkTypeSpinner
						.getSelectedItemPosition()]);
		projectCheckpoint
				.setQcAction(GlobalHolder.QC_ACTION_ENUM_VALUE[this.qcActionSpinner
						.getSelectedItemPosition()]);
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
			projectCheckpoint
					.setUploadPhotoAbsPath(getRealPathFromURI(selectedImageUri));
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(mContext, contentUri, proj,
				null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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

	private void submit() throws APIException, SessionAPIException {

		if (this.createFlag) {
			// api.createCheckpoint(this.projectOrder, projectCheckpoint);
			Uri uri = this
					.getContentResolver()
					.insert(
							ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
							getContentValues(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE));
			long checkpointID = ContentUris.parseId(uri);
			this.projectCheckpoint.setnID((int) checkpointID);
			Log.i(Constants.TAG, " create checkpoint id:" + checkpointID);
			if (checkpointID == -1) {
				throw new APIException("Can't save data to database");
			}
			// this.projectOrder.addOrderCheckpoint(projectCheckpoint);
			update(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE);
		} else {
			// api.updateCheckpoint(projectCheckpoint);
			int ret = this
					.getContentResolver()
					.update(
							ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
							getContentValues(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE),
							ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID
									+ "=?",
							new String[] { projectCheckpoint.getId() });
			Log.i(Constants.TAG, " update checkpoint count:" + ret);
			update(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_UPDATE);
		}
		projectCheckpoint.getProjectItem().getProject().setNeededUpdate(true);
		//

	}

	private void update(String flag) {
		
		int ret =this.getContentResolver().delete(
				ContentDescriptor.UpdateDesc.CONTENT_URI,
				ContentDescriptor.UpdateDesc.Cols.RELATE_ID + "=? and "
						+ ContentDescriptor.UpdateDesc.Cols.FLAG + "=?",
				new String[] { projectCheckpoint.getId(), ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE});

		
		ContentValues cv = new ContentValues();

		cv.put(ContentDescriptor.UpdateDesc.Cols.TYPE,
				ContentDescriptor.UpdateDesc.TYPE_ENUM_CHECKPOINT);
		cv.put(ContentDescriptor.UpdateDesc.Cols.PRO_ID, projectCheckpoint
				.getProjectItem().getProject().getId());
		cv.put(ContentDescriptor.UpdateDesc.Cols.PRO_ORDER_ID,
				projectCheckpoint.getProjectItem().getId());
		cv.put(ContentDescriptor.UpdateDesc.Cols.RELATE_ID, projectCheckpoint
				.getId());
		cv.put(ContentDescriptor.UpdateDesc.Cols.FLAG, flag);
		Uri uri = this.getContentResolver().insert(
				ContentDescriptor.UpdateDesc.CONTENT_URI, cv);

		ContentValues update = new ContentValues();
		update.put(ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG, flag);
		this.getContentResolver().update(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
				update,
				ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID
						+ "=?", new String[] { projectCheckpoint.getId() });

	}

	private ContentValues getContentValues(String flag) {
		ContentValues cv = new ContentValues();
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME,
				this.projectCheckpoint.getName());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION,
				this.projectCheckpoint.getDescription());
		if (this.projectCheckpoint.getId() == null
				|| this.projectCheckpoint.getId().equals("")) {
			this.projectCheckpoint.setId(UUID.randomUUID().toString());
		}
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID,
				this.projectCheckpoint.getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT,
				this.projectCheckpoint.getQcComments());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS,
				this.projectCheckpoint.getQcStatus());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID,
				projectOrder.getProject().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID,
				projectOrder.getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY,
				this.projectCheckpoint.getCategory());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE,
				this.projectCheckpoint.getCheckType());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION,
				this.projectCheckpoint.getQcAction());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT,
				this.projectCheckpoint.getNumberDefect());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME,
				this.projectCheckpoint.getPhotoName());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG, flag);
		cv
				.put(
						ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
						this.projectCheckpoint.getPhotoPath());
		cv
				.put(
						ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_BIG_PATH,
						this.projectCheckpoint.getPhotoPath());

		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_PATH,
				this.projectCheckpoint.getUploadPhotoAbsPath());
		return cv;
	}

	private void deleteCheckpoint() throws APIException, SessionAPIException {
		// TODO select add first
		int ret =this.getContentResolver().delete(
				ContentDescriptor.UpdateDesc.CONTENT_URI,
				ContentDescriptor.UpdateDesc.Cols.RELATE_ID + "=? and "
						+ ContentDescriptor.UpdateDesc.Cols.FLAG + "=?",
				new String[] { projectCheckpoint.getId(), ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE});
		
		//this record it's not new record which it's already exist on website
		if(ret ==0) {
			update(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_DELETE);
		}
		/*
		 * api.deleteCheckpoint(projectCheckpoint.getId()); ContentResolver cr =
		 * this.getContentResolver(); int ret = cr.delete(
		 * ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
		 * ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID + "=?",
		 * new String[] { projectCheckpoint.getId() });
		 */
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
				} catch (Exception e) {
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
				} catch (Exception e) {
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
