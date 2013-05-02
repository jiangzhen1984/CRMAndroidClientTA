package com.auguraclient.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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
import com.auguraclient.model.AuguaModuleImpl;
import com.auguraclient.model.AuguraRestAPIImpl;
import com.auguraclient.model.IAuguraModule;
import com.auguraclient.model.IAuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.SessionAPIException;
import com.auguraclient.model.UpdateRecord;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.Util;

public class CreateUpdateCheckpoint extends Activity implements
		OnTouchListener, OnGestureListener {

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

	private IAuguraModule moduleApi;

	private UiHandler uiHandler;

	private CmdHanlder cmdHandler;

	private TextView returnButton;

	private ImageView showMenuButton;

	private TextView selectPhotoBUtton;

	private TextView[] qcStatusValue;

	private GestureDetector mGestureDetector;

	private boolean createFlag = true;

	private int qcActionSpinnPos = -1;

	private int categorySpinnPos = -1;

	private int checktypeSpinnPos = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.checkpoint_update);
		mGestureDetector = new GestureDetector((OnGestureListener) this);
		initView();
		initListener();

		createFlag = this.getIntent().getBooleanExtra("create", true);
		mContext = this;
		Project project = GlobalHolder.getInstance().getProjectById(
				this.getIntent().getStringExtra("project"));
		projectOrder = project.getOrder(this.getIntent().getStringExtra(
				"projectOrder"));

		api = new AuguraRestAPIImpl();
		moduleApi = new AuguaModuleImpl(this);

		uiHandler = new UiHandler();

		HandlerThread ut = new HandlerThread("ut");
		ut.start();
		cmdHandler = new CmdHanlder(ut.getLooper());

		if (createFlag) {
			projectCheckpoint = new ProjectCheckpoint();
			addOrUpdateTextView.setText(R.string.create_checkpoint);
			submitButton.setVisibility(View.VISIBLE);
		} else {
			addOrUpdateTextView.setText(R.string.update_checkpoint);
			projectCheckpoint = projectOrder.findProjectCheckpointById(this
					.getIntent().getStringExtra("projectCheckpoint"));
			submitButton.setVisibility(View.INVISIBLE);
		}
	}

	Bitmap photo;

	@Override
	protected void onResume() {
		super.onResume();
		// categoryEditText.setText(projectCheckpoint.getCategory());
		// checkpointEditText.setText(projectCheckpoint.getCheckType());
		nameEditText.setText(projectCheckpoint.getName());
		detailEditText.setText(projectCheckpoint.getDescription());
		qcCommentEditText.setText(projectCheckpoint.getQcComments());
		qcDefectEditText.setText(projectCheckpoint.getNumberDefect());
		if (photo != null) {
			photo.recycle();
		}

		// if (projectCheckpoint.getUploadPhotoAbsPath() != null
		// && !projectCheckpoint.getUploadPhotoAbsPath().equals("")) {
		// photo = Util.decodeFile(photo,
		// projectCheckpoint.getUploadPhotoAbsPath());
		// checkpointPhoto.setImageBitmap(photo);
		// } else
		if (projectCheckpoint.getPhotoPath() != null) {
			photo = Util.decodeFile(GlobalHolder.getInstance().getStoragePath()
					+ projectCheckpoint.getPhotoPath());
			checkpointPhoto.setImageBitmap(photo);
		}
		checkpointPhoto.setOnClickListener(onOpenPhotoClickListener);
		setQcStatus();

		categorySpinner.setAdapter(new ArrayAdapter(this, R.layout.spinner_ite,
				GlobalHolder.getInstance().getCategoryLabel()));
		if (this.projectCheckpoint.getCategory() != null) {
			for (int i = 0; i < GlobalHolder.getInstance().getCategoryValue().length; i++) {
				if (this.projectCheckpoint.getCategory().equals(
						GlobalHolder.getInstance().getCategoryValue()[i])) {
					categorySpinner.setSelection(i);
					this.categorySpinnPos = i;
				}
			}
		}

		checkTypeSpinner.setAdapter(new ArrayAdapter(this,
				R.layout.spinner_ite, GlobalHolder.getInstance()
						.getChecktypeLabel()));
		if (this.projectCheckpoint.getCheckType() != null) {
			for (int i = 0; i < GlobalHolder.getInstance().getChecktypeValue().length; i++) {
				if (this.projectCheckpoint.getCheckType().equals(
						GlobalHolder.getInstance().getChecktypeValue()[i])) {
					checkTypeSpinner.setSelection(i);
					this.checktypeSpinnPos = i;
				}
			}
		}

		qcActionSpinner.setAdapter(new ArrayAdapter(this, R.layout.spinner_ite,
				GlobalHolder.getInstance().getQcActionLabel()));
		if (this.projectCheckpoint.getQcAction() != null) {
			for (int i = 0; i < GlobalHolder.getInstance().getQcActionLabel().length; i++) {
				if (this.projectCheckpoint.getQcAction().equals(
						GlobalHolder.getInstance().getQcActionValue()[i])) {
					qcActionSpinner.setSelection(i);
					qcActionSpinnPos = i;
				}
			}
		}

		initAutoSaveListener();

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (photo != null) {
			photo.recycle();
			System.gc();
		}

		ProjectCheckpoint pc = projectOrder
				.findProjectCheckpointById(projectCheckpoint.getId());
		if (projectCheckpoint.getId() != null) {
			if (pc == null) {
				projectOrder.addOrderCheckpoint(projectCheckpoint);
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent();
		if (projectCheckpoint.getId() != null)
			i.putExtra("checkpoint", projectCheckpoint.getId());
		setResult(4, i);
		recordData();
		try {
			submit();
		} catch (APIException e) {
			e.printStackTrace();
		} catch (SessionAPIException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onBackPressed();
		finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int pos = projectOrder.getProjectCheckpointPos(projectCheckpoint);
		if (pos == -1) {
			return true;
		}
		boolean toRight = false;
		boolean startSwitch = false;
		if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 100) {
			if (pos < projectOrder.getCheckpointCount() - 1) {
				startSwitch = true;
				pos++;

				toRight = true;
			} else {
				Toast.makeText(mContext, "This is last one", Toast.LENGTH_SHORT)
						.show();
			}

		} else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
			if (pos > 0) {
				pos--;
				startSwitch = true;
				toRight = false;
			} else {
				Toast.makeText(mContext, "This is first one1",
						Toast.LENGTH_SHORT).show();
			}
		}
		if (startSwitch) {
			Intent i = new Intent();
			i.putExtra("create", false);
			i.putExtra("direct", toRight);
			i.putExtra("project", projectOrder.getProject().getId());
			i.putExtra("projectOrder", projectOrder.getId());
			i.putExtra("projectCheckpoint", projectOrder
					.getOrderCheckpointrByIndex(pos).getId());
			i.setClass(mContext, CreateUpdateCheckpoint.class);
			startActivity(i);
			if (toRight) {
				CreateUpdateCheckpoint.this.overridePendingTransition(
						R.anim.view_checkpoint_in, R.anim.view_checkpoint_out);
			} else {
				CreateUpdateCheckpoint.this.overridePendingTransition(
						R.anim.to_right_view_checkpoint_in,
						R.anim.to_right_view_checkpoint_out);
			}
			finish();
		}

		return false;
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private void initListener() {
		returnButton.setOnClickListener(returnButtonListener);
		submitButton.setOnClickListener(submitListener);
		showMenuButton.setOnClickListener(showMenuListener);
		selectPhotoBUtton.setOnClickListener(selectPhotoListener);

	}

	private void initAutoSaveListener() {

		nameEditText.setOnFocusChangeListener(textChangeListener);
		detailEditText.setOnFocusChangeListener(textChangeListener);
		qcCommentEditText.setOnFocusChangeListener(textChangeListener);
		qcDefectEditText.setOnFocusChangeListener(textChangeListener);
		categorySpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
		checkTypeSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
		qcActionSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
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
			autoSave();
		}

	};


	private OnClickListener selectPhotoListener = new OnClickListener() {

		public void onClick(View arg0) {
			recordData();
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			File tempFile = new File(GlobalHolder.getInstance().getStoragePath()
					+ Constants.CommonConfig.PIC_DIR + File.separator
					+ "temp.jpg");
			try {
				if (!tempFile.exists()) {
					boolean b = tempFile.createNewFile();

					System.out.println("========================" + b);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
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
			if (projectCheckpoint.getUploadPhotoAbsPath() != null) {
				intent.setDataAndType(Uri.fromFile(new File(projectCheckpoint
						.getUploadPhotoAbsPath())), "image/*");
			} else if (projectCheckpoint.getPhotoPath() != null) {
				intent.setDataAndType(
						Uri.fromFile(new File(GlobalHolder.getInstance().getStoragePath() + projectCheckpoint.getPhotoPath())), "image/*");
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(Intent.createChooser(intent, "Select Picture"));
		}

	};

	private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			Intent i = new Intent();
			if (projectCheckpoint.getId() != null)
				i.putExtra("checkpoint", projectCheckpoint.getId());
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

	private OnItemSelectedListener spinnerItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> viewAdapter, View view,
				int pos, long arg3) {
			if (viewAdapter.getId() == R.id.categoryEditText) {
				if (categorySpinnPos == pos) {
					Log.i(Constants.TAG, "no chage");
					return;
				}
				categorySpinnPos = pos;

			} else if (viewAdapter.getId() == R.id.checkpointEditText) {
				if (checktypeSpinnPos == pos) {
					Log.i(Constants.TAG, "no chage");
					return;
				}
				checktypeSpinnPos = pos;

			} else if (viewAdapter.getId() == R.id.qcActionSpinner) {
				if (qcActionSpinnPos == pos) {
					Log.i(Constants.TAG, "no chage");
					return;
				}
				qcActionSpinnPos = pos;
			} else {
				Log.e(Constants.TAG, " Invalid id ");
				return;
			}
			autoSave();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			return;
		}

	};

	private OnFocusChangeListener textChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				autoSave();
			}
		}

	};

	private Timer timer;

	private void autoSave() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				recordData();
				Message.obtain(cmdHandler, UI_COMMIT).sendToTarget();
			}

		}, 200);

	}

	private void recordData() {
		projectCheckpoint.setCategory(GlobalHolder.getInstance()
				.getCategoryValue()[categorySpinner.getSelectedItemPosition()]);
		projectCheckpoint.setCheckType(GlobalHolder.getInstance()
				.getChecktypeValue()[this.checkTypeSpinner
				.getSelectedItemPosition()]);
		projectCheckpoint.setQcAction(GlobalHolder.getInstance()
				.getQcActionValue()[this.qcActionSpinner
				.getSelectedItemPosition()]);
		projectCheckpoint.setName(nameEditText.getText().toString());
		projectCheckpoint.setDescription(detailEditText.getText().toString());
		projectCheckpoint.setQcComments(qcCommentEditText.getText().toString());
		projectCheckpoint
				.setNumberDefect(qcDefectEditText.getText().toString());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			if (resultCode == Activity.RESULT_OK && requestCode == 100) {
				if (photo != null) {
					photo.recycle();
				}
				FileOutputStream fo = null;
				if (data == null) {
					File tempFile = new File(GlobalHolder.getInstance().getStoragePath()
							+ Constants.CommonConfig.PIC_DIR + File.separator
							+ "temp.jpg");
					photo = Util.decodeBitmapFromUri(mContext,
							tempFile.getAbsolutePath(), 2000, 2000);
				} else {
					photo = Util.decodeBitmapFromUri(mContext, data.getData(),
							2000, 2000);
				}
				if (photo == null) {
					Toast.makeText(mContext, "can't load photo from camera!",
							Toast.LENGTH_LONG).show();
					return;
				}
				checkpointPhoto.setImageBitmap(photo);

				// photo = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

				Random randomGenerator = new Random();
				String newimagename = randomGenerator.nextInt() + ".jpg";
				File f = new File(GlobalHolder.getInstance().getStoragePath()
						+ Constants.CommonConfig.PIC_DIR + File.separator
						+ newimagename);

				try {
					f.createNewFile();
					fo = new FileOutputStream(f.getAbsoluteFile());
					fo.write(bytes.toByteArray());
					projectCheckpoint
							.setPhotoPath(Constants.CommonConfig.PIC_DIR
									+ newimagename);
					projectCheckpoint
							.setUploadPhotoAbsPath(f.getAbsolutePath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fo != null)
							fo.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				autoSave();

			}
		} catch (Exception e) {
			Toast.makeText(mContext, "can't catch photo from camera",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	private void doDeleteCheckpoint() {
		Message.obtain(this.cmdHandler, UI_DELETE_CHECKPOINT).sendToTarget();
	}

	public static final int UI_START_SUBMIT = 1;

	public static final int UI_COMMIT = 2;

	public static final int UI_END_COMMIT = 3;

	public static final int UI_DELETE_CHECKPOINT = 4;

	public static final int UI_END_THIS_SESSION = 5;

	private void submit() throws Exception {

		if (projectCheckpoint.getName() == null
				|| projectCheckpoint.getName().equals("")) {
			return;
		}

		if (this.createFlag) {
			projectCheckpoint
					.setFlag(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE);
			if (this.projectCheckpoint.getId() == null
					|| this.projectCheckpoint.getId().equals("")) {
				this.projectCheckpoint.setId(UUID.randomUUID().toString());
			}
			moduleApi.saveCheckpoint(projectCheckpoint);

			projectOrder.addOrderCheckpoint(projectCheckpoint);
			update(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE);
			this.createFlag = false;
		} else {

			projectCheckpoint
					.setFlag(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE);
			int ret = moduleApi.updateCheckpoint(projectCheckpoint);
			Log.i(Constants.TAG, " update checkpoint count:" + ret);
			update(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_UPDATE);
		}
		projectCheckpoint.getProjectItem().getProject().setNeededUpdate(true);
		//

	}

	private void update(String flag) throws Exception {
		if (!flag.equals(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE)) {
			Cursor cur = this
					.getContentResolver()
					.query(ContentDescriptor.UpdateDesc.CONTENT_URI,
							ContentDescriptor.UpdateDesc.Cols.ALL_COLS,
							ContentDescriptor.UpdateDesc.Cols.RELATE_ID
									+ "=? and "
									+ ContentDescriptor.UpdateDesc.Cols.FLAG
									+ "=?",
							new String[] {
									projectCheckpoint.getId(),
									ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE },
							null);
			int count = cur.getCount();
			cur.close();
			if (count > 0) {
				return;
			}

		}

		moduleApi.saveUpdateReocrd(new UpdateRecord(
				ContentDescriptor.UpdateDesc.TYPE_ENUM_CHECKPOINT,
				projectCheckpoint.getProjectItem().getProject().getId(),
				projectCheckpoint.getProjectItem().getId(), projectCheckpoint
						.getId(), flag

		));

		ContentValues update = new ContentValues();
		update.put(ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG, flag);
		this.getContentResolver().update(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
				update,
				ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID
						+ "=?", new String[] { projectCheckpoint.getId() });

	}

	private void deleteCheckpoint() throws APIException, SessionAPIException,
			Exception {
		// TODO select add first
		int ret = this.getContentResolver().delete(
				ContentDescriptor.UpdateDesc.CONTENT_URI,
				ContentDescriptor.UpdateDesc.Cols.RELATE_ID + "=? and "
						+ ContentDescriptor.UpdateDesc.Cols.FLAG + "=?",
				new String[] { projectCheckpoint.getId(),
						ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_CREATE });

		// this record it's not new record which it's already exist on website
		if (ret == 0) {
			update(ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_DELETE);
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
					// Toast.makeText(mContext, "Saved",
					// Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT)
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
				i.putExtra("checkpoint", projectCheckpoint.getId());
				i.putExtra("delete", true);
				setResult(3, i);
				finish();
				break;
			}
		}

	}

}
