package com.auguraclient.view;

import com.auguraclient.R;
import com.auguraclient.model.APIException;
import com.auguraclient.model.ISuguraRestAPI;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.SuguraRestAPIImpl;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class OrderView extends Activity {

	private static final int START_WAITING = 1;

	private static final int END_WAITING = 2;

	private static final int END_WAITING_WITH_ERROR = 3;

	private Context mContext;

	private ListView projectOrderList;

	private Project project;

	private ListAdapter adapter;

	private ISuguraRestAPI api;

	private static final int LOAD_PROJECT_ITEM_ORDER = 1;

	private LoaderHandler handler;

	private UIHandler uiHandler;

	private ProjectOrder projectItem;

	private ImageView projectItemPhotoIV;

	private TextView itemOrderQuntityTV;

	private TextView itemOrderDescriptionTV;

	private TextView itemOrderCommentTV;

	private TextView itemOrderQTTV;

	private TextView orderItemTitleTV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setfullScreen();
		this.setContentView(R.layout.project_order_list);
		projectOrderList = (ListView) this.findViewById(R.id.projectOrderList);

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

		Integer position = (Integer) this.getIntent().getExtras()
				.get("project");
		Integer itemPosition = (Integer) this.getIntent().getExtras().get(
				"itemPosition");

		project = GlobalHolder.getProject(position);

		projectItem = project.getOrder(itemPosition);

		mContext = this;

		adapter = new ListAdapter(this);
		projectOrderList.setAdapter(adapter);

		api = new SuguraRestAPIImpl();

		HandlerThread ht = new HandlerThread("it");
		ht.start();
		handler = new LoaderHandler(ht.getLooper());
		uiHandler = new UIHandler();
		Message.obtain(handler, LOAD_PROJECT_ITEM_ORDER).sendToTarget();

		projectOrderList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Intent i = new Intent();
				// i.setClass(mContext, ProjectItemView.class);
				// i.putExtra("project", position);
				// mContext.startActivity(i);
			}

		});
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
		itemOrderQTTV.setText("Q777C Date:     " + "Quantity Checked:"
				+ projectItem.getQuantityChecked() + "  QC Status:"
				+ projectItem.getQcStatus());
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
				dialog = ProgressDialog.show(mContext, "Loading",
						"Please wait...", true);
				dialog.setIcon(R.drawable.logo_24_24);
				dialog.show();
				break;
			case END_WAITING:
				if (dialog != null) {
					dialog.cancel();
					dialog.dismiss();
				}
				adapter.notifyDataSetChanged();
				projectItemPhotoIV.setImageDrawable(getDrawable(projectItem
						.getPhotoBigPath()));
				break;
			case END_WAITING_WITH_ERROR:
				if (dialog != null) {
					dialog.cancel();
					dialog.dismiss();
				}
				// TODO show toast
				Toast.makeText(dialog.getContext(), "errorr ---------------",
						Toast.LENGTH_SHORT).show();
			}
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
					if (projectItem.getItemOrderCount() <= 0) {
						l = api.queryProjectOrderCheckpointList(projectItem
								.getId());
						projectItem.addOrderCheckpoint(l);
					}
					Message.obtain(uiHandler, END_WAITING).sendToTarget();
				} catch (APIException e) {
					e.printStackTrace();
					Message.obtain(uiHandler, END_WAITING_WITH_ERROR)
							.sendToTarget();
				}
				break;
			}
		}

	}

	class ListAdapter extends BaseAdapter {

		private Context context;

		public ListAdapter(Context context) {
			this.context = context;
		}

		public int getCount() {
			return projectItem.getItemOrderCount();
		}

		public Object getItem(int position) {
			return projectItem.getItemOrderByIndex(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				ItemView appView = new ItemView(context);
				appView.updateView(projectItem.getItemOrderByIndex(position));
				convertView = appView;
			}
			return convertView;
		}

	}

	class ItemView extends LinearLayout {

		private Context mContext;

		private TextView itemOrderCategoryCheckType;

		private TextView itemOrderDefectAlert;

		private TextView itemOrderQcComments;

		private ImageView projectItemOrderPhoto;

		private ImageView itemOperationIV;

		public ItemView(Context context) {
			super(context);
			this.mContext = context;
			initilize(context);
		}

		public void initilize(Context c) {
			this.mContext = c;
			View view = LayoutInflater.from(this.mContext).inflate(
					R.xml.project_item_order, null);
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

		public void updateView(ProjectCheckpoint pi) {
			if (pi == null) {
				Log
						.e(Constants.TAG,
								" can't update view for order view ProjectItemOrder is null");
				return;
			}
			itemOrderCategoryCheckType.setText(pi.getCategory() + "   >> "
					+ pi.getCheckType() + "   >>" + pi.getName());
			itemOrderDefectAlert.setText(pi.getNumberDefect() + "   "
					+ pi.getQcAction());
			itemOrderQcComments.setText(pi.getQcComments() == null ? "" : pi
					.getQcComments());
			if (pi.getPhotoPath() != null && !pi.getPhotoPath().isEmpty()) {
				projectItemOrderPhoto.setImageDrawable(getDrawable(pi
						.getPhotoPath()));
			}
		}

	}

	private Drawable getDrawable(String url) {
		Log.e(Constants.TAG, url);
		InputStream is = null;
		try {
			is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
