package com.auguraclient.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class OrderView extends Activity {

	private static final int START_WAITING = 1;

	private static final int END_WAITING = 2;

	private static final int END_WAITING_WITH_ERROR = 3;

	private static final int LOAD_PROJECT_ITEM_ORDER = 1;

	private static final int DELETE_CHECKPOINT = 2;

	private Context mContext;

	private LinearLayout projectOrderCheckpointList;

	private Project project;

	private IAuguraRestAPI api;

	private LoaderHandler handler;

	private UIHandler uiHandler;

	private ProjectOrder projectItem;

	private ImageView projectItemPhotoIV;

	private TextView itemOrderQuntityTV;

	private TextView itemOrderDescriptionTV;

	private TextView itemOrderCommentTV;

	private TextView itemOrderQTTV;

	private TextView orderItemTitleTV;

	private LinearLayout addCheckpointButton;

	private LinearLayout checkpointReturn;

	private List<View> tView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setfullScreen();
		this.setContentView(R.layout.order_detail);
		projectOrderCheckpointList = (LinearLayout) this
				.findViewById(R.id.projectOrderCheckpointList);

		projectItemPhotoIV = (ImageView) this
				.findViewById(R.id.projectItemPhoto);

		itemOrderQuntityTV = (TextView) this
				.findViewById(R.id.itemOrderQuntity);

		itemOrderDescriptionTV = (TextView) this
				.findViewById(R.id.itemOrderDescription);

		itemOrderCommentTV = (TextView) this
				.findViewById(R.id.itemOrderComment);

		itemOrderQTTV = (TextView) this.findViewById(R.id.itemOrderQT);

		orderItemTitleTV = (TextView) this.findViewById(R.id.orderItemTitle);

		addCheckpointButton = (LinearLayout) this
				.findViewById(R.id.bottom_add_checpoint_layout);

		checkpointReturn = (LinearLayout) this
				.findViewById(R.id.checkpointReturn);

		// set listener
		initListener();

		Integer position = (Integer) this.getIntent().getExtras()
				.get("project");
		Integer itemPosition = (Integer) this.getIntent().getExtras().get(
				"itemPosition");

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
		itemOrderDescriptionTV.setText(projectItem.getDescription());
		itemOrderCommentTV.setText("QC Comments:\n"
				+ projectItem.getQcComment() == null ? "" : projectItem
				.getQcComment());
		itemOrderQTTV.setText("QC Date:     "
				+ "Quantity Checked:"
				+ (projectItem.getQuantityChecked() == null ? "" : projectItem
						.getQuantityChecked())
				+ "  QC Status:"
				+ (projectItem.getQcStatus() == null ? "" : projectItem
						.getQcStatus()));

		projectItemPhotoIV.setImageURI(Uri.fromFile(new File(
				GlobalHolder.GLOBAL_STORAGE_PATH
						+ projectItem.getPhotoBigPath())));

	}

	private void initListener() {
		checkpointReturn.setOnClickListener(returnButtonListener);

		addCheckpointButton.setOnClickListener(addCheckpointListener);
		projectItemPhotoIV.setOnClickListener(onOrderPhotoClickListener);
	}

	private OnClickListener returnButtonListener = new OnClickListener() {

		public void onClick(View arg0) {
			finish();
		}

	};

	private OnClickListener addCheckpointListener = new OnClickListener() {

		public void onClick(View v) {
			Intent i = new Intent();
			i.putExtra("create", true);
			i.putExtra("projectOrder", projectItem);
			i.setClass(mContext, CreateUpdateCheckpoint.class);
			startActivityForResult(i, 200);
		}

	};

	private OnClickListener onOrderPhotoClickListener = new OnClickListener() {

		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(
					GlobalHolder.GLOBAL_STORAGE_PATH
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
				ProjectCheckpoint pc = (ProjectCheckpoint) data
						.getSerializableExtra("checkpoint");
				projectItem.removeCheckpoint(pc);
				for (int i = 0; i < tView.size(); i++) {
					if (((ItemView) tView.get(i)).getCheckpointId().equals(
							pc.getId()))
						projectOrderCheckpointList.removeView(tView.get(i));
				}
			}
		} else if (resultCode == 4) {
			ProjectCheckpoint pc = (ProjectCheckpoint) data
					.getSerializableExtra("checkpoint");
			if (pc != null) {
				if (projectItem.findProjectCheckpointById(pc.getId()) == null) {
					this.projectItem.addOrderCheckpoint(pc);
					ItemView appView = new ItemView(mContext);
					appView.updateView(pc);
					projectOrderCheckpointList.addView(appView,
							new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.WRAP_CONTENT,
									LinearLayout.LayoutParams.WRAP_CONTENT));
					tView.add(appView);
				} else {
					//TODO update view
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
		for (int i = 0; i < tView.size(); i++) {
			ItemView appView = new ItemView(mContext);
			appView.updateView(projectItem.getOrderCheckpointrByIndex(i));
			projectOrderCheckpointList.addView(appView,
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT));
			tView.add(appView);
		}
	}

	private void showConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.alert_icon);
		builder.setMessage(this.getResources().getText(
				R.string.delete_checkpoint_confirm));
		builder.setTitle(this.getResources().getText(
				R.string.delete_checkpoint_confirm_title));

		builder.setPositiveButton(R.string.delete_checkpoint_confirm_button,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Message.obtain(handler, DELETE_CHECKPOINT)
								.sendToTarget();
						dialog.cancel();
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(
				R.string.delete_checkpoint_confirm_button_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						dialog.dismiss();
					}
				});

		final AlertDialog configDialog = builder.create();
		configDialog.show();
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
					if (!projectItem.isLoadedCheckpointFromDB()) {
						loadCheckpointFromDB();
					}
					if (projectItem.getCheckpointCount() <= 0) {
						l = api.queryProjectOrderCheckpointList(projectItem
								.getId());
						projectItem.addOrderCheckpoint(l);
						saveDataToDB();
					}
					Message.obtain(uiHandler, END_WAITING).sendToTarget();
				} catch (Exception e) {
					Log.e(Constants.TAG, e.getMessage(), e);
					Message.obtain(uiHandler, END_WAITING_WITH_ERROR,
							e.getMessage()).sendToTarget();
				}
				break;
			}
		}

	}

	private void saveDataToDB() {
		ContentValues cv = new ContentValues();
		for (int z = 0; z < projectItem.getCheckpointCount(); z++) {
			cv.clear();
			ProjectCheckpoint pcp = projectItem.getOrderCheckpointrByIndex(z);
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME, pcp
					.getName());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION,
					pcp.getDescription());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID,
					pcp.getId());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT, pcp
					.getQcComments());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS, pcp
					.getQcStatus());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID,
					projectItem.getProject().getId());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID,
					projectItem.getId());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY, pcp
					.getCategory());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE, pcp
					.getCheckType());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION, pcp
					.getQcAction());
			cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT,
					pcp.getNumberDefect());
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
			pcp
					.setnID(c
							.getInt(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.ID)));
			pcp
					.setId(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID)));
			pcp
					.setName(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME)));

			pcp
					.setPhotoPath(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH)));

			pcp
					.setPhotoName(c
							.getString(c
									.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME)));

			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION) > 0) {
				pcp
						.setDescription(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
			}

			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY) >= 0) {
				pcp
						.setCategory(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY)));
			}

			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT) >= 0) {
				pcp
						.setQcComments(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
			}
			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT) >= 0) {
				pcp
						.setQcComments(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT)));
			}
			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS) >= 0) {
				pcp
						.setQcStatus(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS)));
			}
			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION) >= 0) {
				pcp
						.setDescription(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION)));
			}
			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT) >= 0) {
				pcp
						.setNumberDefect(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT)));
			}
			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE) >= 0) {
				pcp
						.setCheckType(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE)));
			}
			if (c
					.getColumnIndex(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION) >= 0) {
				pcp
						.setQcAction(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION)));
				pcp.setUploadPhotoAbsPath(c
								.getString(c
										.getColumnIndexOrThrow(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_PATH)));
			}
			projectItem.addOrderCheckpoint(pcp);
		}

		projectItem.setLoadedCheckpointFromDB(true);
		c.close();
	}

	class ItemView extends LinearLayout {

		private Context mContext;

		private TextView itemOrderCategoryCheckType;

		private TextView itemOrderDefectAlert;

		private TextView itemOrderQcComments;

		private ImageView projectItemOrderPhoto;

		private ImageView itemOperationIV;

		private String id;

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
			itemOrderCategoryCheckType = (TextView) this
					.findViewById(R.id.itemOrderCategoryCheckType);
			itemOrderDefectAlert = (TextView) this
					.findViewById(R.id.itemOrderDefectAlert);
			itemOrderQcComments = (TextView) this
					.findViewById(R.id.itemOrderQcComments);
			projectItemOrderPhoto = (ImageView) this
					.findViewById(R.id.projectItemOrderPhoto);
			itemOperationIV = (ImageView) this
					.findViewById(R.id.projectItemOrderOperation);
		}

		public String getCheckpointId() {
			return this.id;
		}

		public void updateView(final ProjectCheckpoint pi) {
			if (pi == null) {
				Log
						.e(Constants.TAG,
								" can't update view for order view ProjectItemOrder is null");
				return;
			}
			id = pi.getId();
			itemOrderCategoryCheckType.setText(pi.getCategory() + "   > "
					+ pi.getCheckType() + "   >" + pi.getName());
			itemOrderDefectAlert.setText(pi.getQcAction() == null ? "" : pi
					.getQcAction()
					+ pi.getNumberDefect() == null ? "   " : pi
					.getNumberDefect());
			itemOrderQcComments.setText(pi.getQcComments() == null ? "" : pi
					.getQcComments());
			if (pi.isCompleted()) {
				itemOperationIV.setImageResource(R.drawable.completed);
			} else {
				itemOperationIV.setImageResource(R.drawable.missing);
			}
			if (pi.getPhotoPath() != null && !pi.getPhotoPath().equals("")) {
				final Uri photo = Uri.fromFile(new File(
						GlobalHolder.GLOBAL_STORAGE_PATH + pi.getPhotoPath()));
				projectItemOrderPhoto.setImageURI(photo);
			} else if(pi.getUploadPhotoAbsPath() != null  && !pi.getUploadPhotoAbsPath().equals("")) {
				final Uri photo = Uri.fromFile(new File(pi.getUploadPhotoAbsPath()));
				projectItemOrderPhoto.setImageURI(photo);
			}
			this.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					Intent i = new Intent();
					i.putExtra("create", false);
					i.putExtra("projectOrder", projectItem);
					i.putExtra("projectCheckpoint", pi);
					i.setClass(mContext, CreateUpdateCheckpoint.class);
					startActivityForResult(i, 100);

				}

			});
		}

	}

}
