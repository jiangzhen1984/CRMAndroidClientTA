package com.auguraclient.view;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.auguraclient.R;
import com.auguraclient.db.ContentDescriptor;
import com.auguraclient.model.AuguraRestAPIImpl;
import com.auguraclient.model.IAuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.Util;

public class OrderView extends Activity {

	private static final int START_WAITING = 1;

	private static final int END_WAITING = 2;

	private static final int END_WAITING_WITH_ERROR = 3;

	private static final int LOAD_PROJECT_ITEM_ORDER = 1;

	private static final int AUTO_SAVE_ORDER_DETAIL = 2;

	private Context mContext;

	private LinearLayout projectOrderCheckpointList;

	private Project project;

	private IAuguraRestAPI api;

	private LoaderHandler handler;

	private UIHandler uiHandler;

	private ProjectOrder projectItem;

	private ImageView projectItemPhotoIV;

	private TextView itemOrderQuntityTV;

	private TextView itemOrderDescriptionED;

	private EditText itemOrderCommentED;

	private TextView orderItemTitleTV;

	private LinearLayout addCheckpointButton;

	private LinearLayout checkpointReturn;

	private Spinner qcStatusSpinner;

	private EditText qcCheckedED;

	private EditText qcDateED;

	private List<View> tView;
	
	private int currentQcStatusPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setfullScreen();
		this.setContentView(R.layout.order_detail);
		this.overridePendingTransition(0, R.anim.view_checkpoint_out);

		initView();
		// set listener
		initListener();

		Integer position = (Integer) this.getIntent().getExtras()
				.get("project");
		Integer itemPosition = (Integer) this.getIntent().getExtras()
				.get("itemPosition");

		project = GlobalHolder.getProject(position);

		projectItem = project.getOrder(itemPosition);

		mContext = this;

		api = new AuguraRestAPIImpl();

		HandlerThread ht = new HandlerThread("it");
		ht.start();
		handler = new LoaderHandler(ht.getLooper());
		uiHandler = new UIHandler();
		Message.obtain(handler, LOAD_PROJECT_ITEM_ORDER).sendToTarget();

	}

	@Override
	protected void onResume() {

		super.onResume();
		orderItemTitleTV.setText(projectItem.getName());
		itemOrderQuntityTV.setText(" Quantity: " + projectItem.getQuantity());
		itemOrderDescriptionED.setText(projectItem.getDescription());
		itemOrderCommentED.setText(projectItem.getQcComment() == null ? ""
				: projectItem.getQcComment());
		qcCheckedED.setText(projectItem.getQuantityChecked());
		qcDateED.setText(projectItem.getModifiedDateString());

		projectItemPhotoIV.setImageURI(Uri.fromFile(new File(
				GlobalHolder.GLOBAL_STORAGE_PATH
						+ projectItem.getPhotoBigPath())));

		qcCheckedED.setText(projectItem.getQuantityChecked());

		if (this.projectItem.getQcStatus() != null) {
			for (int i = 0; i < GlobalHolder.QC_Status_ENUM.length; i++) {
				if (this.projectItem.getQcStatus().equals(
						GlobalHolder.QC_Status_ENUM[i])) {
					qcStatusSpinner.setSelection(i);
					currentQcStatusPos = i;
				}
			}
		}

	}

	
	
	@Override
	public void onBackPressed() {
		saveOrder();
		super.onBackPressed();
	}

	private void initView() {
		projectOrderCheckpointList = (LinearLayout) this
				.findViewById(R.id.projectOrderCheckpointList);

		projectItemPhotoIV = (ImageView) this
				.findViewById(R.id.projectItemPhoto);

		itemOrderQuntityTV = (TextView) this
				.findViewById(R.id.itemOrderQuntity);

		itemOrderDescriptionED = (TextView) this
				.findViewById(R.id.itemOrderDescription);

		itemOrderCommentED = (EditText) this
				.findViewById(R.id.itemOrderComment);

		orderItemTitleTV = (TextView) this.findViewById(R.id.orderItemTitle);

		addCheckpointButton = (LinearLayout) this
				.findViewById(R.id.bottom_add_checpoint_layout);

		checkpointReturn = (LinearLayout) this
				.findViewById(R.id.checkpointReturn);
		qcCheckedED = (EditText) this
				.findViewById(R.id.order_detal_qc_checked_edit);

		qcStatusSpinner = (Spinner) this.findViewById(R.id.qcStatusSpinner);

		qcStatusSpinner.setAdapter(new ArrayAdapter(this, R.layout.spinner_ite,
				GlobalHolder.QC_Status_ENUM));

		qcDateED = (EditText) this.findViewById(R.id.order_detal_qc_date_edit);

		qcDateED.setOnClickListener(datePickerListener);

	}

	private void initListener() {
		checkpointReturn.setOnClickListener(returnButtonListener);

		addCheckpointButton.setOnClickListener(addCheckpointListener);
		projectItemPhotoIV.setOnClickListener(onOrderPhotoClickListener);
		
		itemOrderCommentED.setOnFocusChangeListener(textChangeListener);
		itemOrderDescriptionED.setOnFocusChangeListener(textChangeListener);
		qcCheckedED.setOnFocusChangeListener(textChangeListener);
		qcStatusSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
		
	}


	private OnItemSelectedListener spinnerItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long arg3) {
			if(parent.getId() == R.id.qcStatusSpinner) {
				if(pos == currentQcStatusPos) {
					Log.e(Constants.TAG, "no Change");
					return;
				}
				currentQcStatusPos = pos;
			} else {
				Log.e(Constants.TAG, "Invalid id");
				return;
			}
			startAutoSaveTimer();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	};
	
	
	
	
	private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			finish();
		}

	};

	private OnClickListener datePickerListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Dialog dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.date_picker_dialog);
			dialog.setTitle(R.string.order_detal_date_picker_title);
			DatePicker dp = (DatePicker) dialog.findViewById(R.id.datePicker);
			int year = 0;
			int month = 0;
			int day = 0;
			Calendar c = Calendar.getInstance();
			DateFormat da = new SimpleDateFormat("yyyy-MM-dd");
			if (qcDateED.getText().toString() != null) {

				try {
					Date oldDate = da.parse(qcDateED.getText().toString());
					Calendar.getInstance().setTime(oldDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (projectItem.getDateDodified() != null) {
				Calendar.getInstance().setTime(projectItem.getDateDodified());
			}

			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);

			dp.init(year, month, day, new OnDateChangedListener() {
				@Override
				public void onDateChanged(DatePicker dp, int year, int month,
						int day) {
					qcDateED.setText(year + "-" + (month + 1) + "-" + day);
				}

			});
			dialog.show();
		}

	};

	private OnClickListener addCheckpointListener = new OnClickListener() {

		public void onClick(View v) {
			Intent i = new Intent();
			i.putExtra("create", true);
			i.putExtra("project", projectItem.getProject().getId());
			i.putExtra("projectOrder", projectItem.getId());
			i.setClass(mContext, CreateUpdateCheckpoint.class);
			startActivityForResult(i, 200);
		}

	};
	
	private OnFocusChangeListener textChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus) {
				startAutoSaveTimer();
			}
		}

		
		
	};

	private OnClickListener onOrderPhotoClickListener = new OnClickListener() {

		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(
					Uri.fromFile(new File(GlobalHolder.GLOBAL_STORAGE_PATH
							+ projectItem.getOriginPhotoPath())), "image/*");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(Intent.createChooser(intent, "Select Picture"));
		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 3) {
			boolean b = data.getBooleanExtra("delete", false);
			if (b) {
				String pcId = data.getStringExtra("checkpoint");
				ProjectCheckpoint pc = projectItem
						.findProjectCheckpointById(pcId);
				projectItem.removeCheckpoint(pc);
				for (int i = 0; i < tView.size(); i++) {
					if (((ItemView) tView.get(i)).getCheckpointId().equals(
							pc.getId()))
						projectOrderCheckpointList.removeView(tView.get(i));
				}
			}
		} else if (resultCode == 4) {
			String projectCheckpointId = data.getStringExtra("checkpoint");
			if (projectCheckpointId != null) {
				ProjectCheckpoint pc = projectItem
						.findProjectCheckpointById(projectCheckpointId);
				if (pc != null) {

					for (int i = 0; i < tView.size(); i++) {
						if (((ItemView) tView.get(i)).getCheckpointId().equals(
								projectCheckpointId)) {
							((ItemView) tView.get(i)).updateView(pc);
							return;
						}
					}
					ItemView appView = new ItemView(mContext);
					appView.updateView(pc);
					projectOrderCheckpointList.addView(appView,
							new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.WRAP_CONTENT,
									LinearLayout.LayoutParams.WRAP_CONTENT));
					tView.add(appView);
				}
			}
		}
	}

	public void setfullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private ProgressDialog dialog;

	class UIHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START_WAITING:
				dialog = ProgressDialog.show(mContext, "", "Please wait...",
						true);
				dialog.setIcon(R.drawable.logo_24_24);
				dialog.show();
				break;
			case END_WAITING:
				initAdapterView();
				if (dialog != null) {
					dialog.cancel();
					dialog.dismiss();
				}

				break;
			case END_WAITING_WITH_ERROR:
				if (dialog != null) {
					dialog.cancel();
					dialog.dismiss();
				}
				Toast.makeText(dialog.getContext(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;

			}
		}
	}

	private void initAdapterView() {
		tView = new ArrayList<View>(projectItem.getCheckpointCount());
		for (int i = 0; i < projectItem.getCheckpointCount(); i++) {
			ItemView appView = new ItemView(mContext);
			appView.updateView(projectItem.getOrderCheckpointrByIndex(i));
			projectOrderCheckpointList.addView(appView,
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT));
			tView.add(appView);
		}
	}


	class LoaderHandler extends Handler {

		public LoaderHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_PROJECT_ITEM_ORDER:
				Message.obtain(uiHandler, START_WAITING).sendToTarget();
				List<ProjectCheckpoint> l;
				try {
					// if (!projectItem.isLoadedCheckpointFromDB()) {
					// loadCheckpointFromDB();
					// }
					// if (projectItem.getCheckpointCount() <= 0) {
					// l = api.queryProjectOrderCheckpointList(projectItem
					// .getId());
					// projectItem.addOrderCheckpoint(l);
					// saveDataToDB();
					// }
					Message.obtain(uiHandler, END_WAITING).sendToTarget();
				} catch (Exception e) {
					Log.e(Constants.TAG, e.getMessage(), e);
					Message.obtain(uiHandler, END_WAITING_WITH_ERROR,
							e.getMessage()).sendToTarget();
				}
				break;
				
				
			case AUTO_SAVE_ORDER_DETAIL:
				saveOrder();
				Toast.makeText(mContext, "Order detail is auto saved", Toast.LENGTH_SHORT).show();
				break;
			}
		}

	}
	
	
	private Timer timer;
	private void startAutoSaveTimer() {
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		timer =  new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Message.obtain(handler, AUTO_SAVE_ORDER_DETAIL).sendToTarget();
			}
			
		}, 300);
	}
	
	private void saveOrder() {
		if(qcDateED.getText().toString()!=null && !qcDateED.getText().toString().equals("")) {
			DateFormat  df = new SimpleDateFormat("yyyy-MM-dd");
			Date d;
			try {
				d = df.parse(qcDateED.getText().toString());
				projectItem.setDateDodified(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		projectItem.setQcComment(itemOrderCommentED.getText().toString());
		projectItem.setDescription(itemOrderDescriptionED.getText().toString());
		projectItem.setQcStatus(GlobalHolder.QC_Status_ENUM[qcStatusSpinner.getSelectedItemPosition()]);
		projectItem.setQuantityChecked(qcCheckedED.getText().toString());
		
		ContentValues cv = new ContentValues();
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT, projectItem.getQcComment());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS, projectItem.getQcStatus());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.DATE_MODIFIED, qcDateED.getText().toString());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED, qcCheckedED.getText().toString());
		int ret = this.getContentResolver().update(
				ContentDescriptor.ProjectOrderDesc.CONTENT_URI, cv, ContentDescriptor.ProjectOrderDesc.Cols.ID+"=?", new String[]{projectItem.getnID()+""});
		Log.i(Constants.TAG, " update order count:"+ret);
		
		if(ret > 0) {
			ContentValues orderCV = new ContentValues();

			orderCV.put(ContentDescriptor.UpdateDesc.Cols.TYPE,
					ContentDescriptor.UpdateDesc.TYPE_ENUM_ORDER);
			orderCV.put(ContentDescriptor.UpdateDesc.Cols.PRO_ID, projectItem.getProject().getId());
			orderCV.put(ContentDescriptor.UpdateDesc.Cols.PRO_ORDER_ID,
					projectItem.getId());
			orderCV.put(ContentDescriptor.UpdateDesc.Cols.RELATE_ID,
					projectItem.getId());
			cv.put(ContentDescriptor.UpdateDesc.Cols.FLAG, ContentDescriptor.UpdateDesc.TYPE_ENUM_FLAG_UPDATE);
			Uri uri = this.getContentResolver().insert(
					ContentDescriptor.UpdateDesc.CONTENT_URI, cv);
		}
		
	}

	private void saveDataToDB() {
		ContentValues cv = new ContentValues();
		for (int z = 0; z < projectItem.getCheckpointCount(); z++) {
			cv.clear();
			ProjectCheckpoint pcp = projectItem.getOrderCheckpointrByIndex(z);
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME,
					pcp.getName());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION,
					pcp.getDescription());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID,
					pcp.getId());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT,
					pcp.getQcComments());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS,
					pcp.getQcStatus());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID,
					projectItem.getProject().getId());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID,
					projectItem.getId());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY,
					pcp.getCategory());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE,
					pcp.getCheckType());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION,
					pcp.getQcAction());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT,
					pcp.getNumberDefect());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME,
					pcp.getPhotoName());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
					pcp.getPhotoPath());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_BIG_PATH,
					pcp.getPhotoPath());
			Uri uri = this.getContentResolver().insert(
					ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI, cv);
			long checkpointID = ContentUris.parseId(uri);
			pcp.setnID((int) checkpointID);
		}
	}

	private void loadCheckpointFromDB() {
		ContentResolver cr = this.getContentResolver();
		Cursor c = null;
		c = cr.query(ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI,
				ContentDescriptor.ProjectCheckpointDesc.Cols.ALL_COLS,
				ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID
						+ "=? and "
						+ ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG
						+ "<>?", new String[] { this.projectItem.getId(),
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

			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION) > 0) {
				pcp.setDescription(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
			}

			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY) >= 0) {
				pcp.setCategory(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY)));
			}

			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT) >= 0) {
				pcp.setQcComments(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
			}
			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT) >= 0) {
				pcp.setQcComments(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
			}
			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS) >= 0) {
				pcp.setQcStatus(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS)));
			}
			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION) >= 0) {
				pcp.setDescription(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
			}
			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT) >= 0) {
				pcp.setNumberDefect(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT)));
			}
			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE) >= 0) {
				pcp.setCheckType(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE)));
			}
			if (c.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION) >= 0) {
				pcp.setQcAction(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION)));
				pcp.setUploadPhotoAbsPath(c.getString(c
						.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_PATH)));
			}
			projectItem.addOrderCheckpoint(pcp);
		}

		projectItem.setLoadedCheckpointFromDB(true);
		c.close();
	}

	static boolean a;
	class ItemView extends LinearLayout {

		private Context mContext;

		private TextView itemOrderCategoryCheckType;

		private TextView itemOrderDefectAlert;

		private TextView itemOrderQcComments;

		private ImageView projectItemOrderPhoto;

		private ImageView itemOperationIV;

		private String id;

		private Bitmap photo;
		
		private View parent;

		public ItemView(Context context) {
			super(context);
			this.mContext = context;
			initilize(context);
		}

		public void initilize(Context c) {
			this.mContext = c;
			View view = LayoutInflater.from(this.mContext).inflate(
					R.layout.component_checkpoint, null, false);
			addView(view);
			parent = findViewById(R.id.componentCheckpoint);
			itemOrderCategoryCheckType = (TextView) this
					.findViewById(R.id.itemOrderCategoryCheckType);
			itemOrderDefectAlert = (TextView) this
					.findViewById(R.id.itemOrderDefectAlert);
			itemOrderQcComments = (TextView) this
					.findViewById(R.id.itemOrderQcComments);
			projectItemOrderPhoto = (ImageView) this
					.findViewById(R.id.projectItemOrderPhoto);
			itemOperationIV = (ImageView) this
					.findViewById(R.id.projectItemOrderPhotoRight);
		}

		public String getCheckpointId() {
			return this.id;
		}

		public void updateView(final ProjectCheckpoint pi) {
			if (pi == null) {
				Log.e(Constants.TAG,
						" can't update view for order view ProjectItemOrder is null");
				return;
			}
			id = pi.getId();
			itemOrderCategoryCheckType.setText(pi.getCategoryLabel() + " > "
					 + pi.getName()+" ( "+ pi.getCheckTypeLabel()+" )");
			itemOrderDefectAlert.setText(
					(pi.getQcStatus() == null ? "" : pi.getQcStatus()) + 
					(pi.getNumberDefect() == null ? " " : (pi.getNumberDefect()+" defect "))+
					(pi.getQcAction()==null?"":pi.getQcAction()));
			itemOrderQcComments.setText(pi.getQcComments() == null ? "" : pi
					.getQcComments());
			
			if (pi.isCompleted()) {
				//itemOperationIV.setImageResource(R.drawable.completed);
				parent.setBackgroundColor(mContext.getResources().getColor(R.color.white_background));
			} else {
				//itemOperationIV.setImageResource(R.drawable.missing);
				parent.setBackgroundColor(mContext.getResources().getColor(R.color.checkpoint_incomplete_bg));
			}
			if (pi.getPhotoPath() != null && !pi.getPhotoPath().equals("")) {
				photo = Util.decodeFile(photo, GlobalHolder.GLOBAL_STORAGE_PATH
						+ pi.getPhotoPath());
				itemOperationIV.setImageBitmap(photo);
			} else if (pi.getUploadPhotoAbsPath() != null
					&& !pi.getUploadPhotoAbsPath().equals("")) {
				photo = Util.decodeFile(photo, pi.getUploadPhotoAbsPath());
				itemOperationIV.setImageBitmap(photo);
			}
			this.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					Intent i = new Intent();
					i.putExtra("create", false);
					i.putExtra("project", projectItem.getProject().getId());
					i.putExtra("projectOrder", projectItem.getId());
					i.putExtra("projectCheckpoint", pi.getId());
					i.setClass(mContext, CreateUpdateCheckpoint.class);
					startActivityForResult(i, 100);

				}

			});
		}

	}

}
