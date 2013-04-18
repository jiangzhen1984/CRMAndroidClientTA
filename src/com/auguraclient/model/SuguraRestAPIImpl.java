package com.auguraclient.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.auguraclient.http.HttpWrapper;
import com.auguraclient.util.Constants;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.Util;

public class SuguraRestAPIImpl implements ISuguraRestAPI {

	private final static String API_VERSION = "1";

	private final static String APPLICATION_NAME = "Android-Client";

	private HttpWrapper http;

	public SuguraRestAPIImpl() {
		http = new HttpWrapper();
	}

	/**
	 * http://crm.augura.net/service/v4_1/rest.php?method=login&input_type=JSON&
	 * response_type
	 * =JSON&rest_data={%22user_auth%22:{%22user_name%22:%22android%
	 * 22,%22password
	 * %22:%22c31b32364ce19ca8fcd150a417ecce58%22,%22version%22:%221
	 * %22},%22application_name%22:%22RestTest%22}
	 */
	public User login(String userName, String password) throws APIException {
		String pwdMd5 = Util.encryptMD5(password);

		JSONObject restData = new JSONObject();
		JSONObject userAuth = new JSONObject();
		try {
			userAuth.put("user_name", userName);
			userAuth.put("password", pwdMd5);
			userAuth.put("version", API_VERSION);
			restData.put("user_auth", userAuth);
			restData.put("application_name", APPLICATION_NAME);

		} catch (JSONException e) {
			throw new APIException(" can't wrpe json data", e);
		}
		Log.i(Constants.TAG, restData.toString());

		String url = Util.getLoginUrl();
		String response;

		try {
			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString(), "UTF-8"));
			if (response != null) {
				User user = Util.parserUserJson(parseResponseError(response));
				return user;
			}
		} catch (UnsupportedEncodingException e) {
			throw new APIException(" can't find UTF-8 encder instance");
		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}

		return null;
	}

	public ProjectList loadProject(String name) throws APIException {
		ProjectList pl = this.queryProjectList(name);
		List<Project> l = pl.getList();
		for (int i = 0; i < l.size(); i++) {
			Project p = l.get(i);
			List<ProjectOrder> poList = this.queryProjectOrderList(p.getId());
			p.addProjectOrder(poList);
			for (int j = 0; j < poList.size(); j++) {
				ProjectOrder po = poList.get(j);
				po.addOrderCheckpoint(this.queryProjectOrderCheckpointList(po
						.getId()));
			}
		}
		return pl;
	}

	public ProjectList queryProjectList(String name) throws APIException {
		String sessionId = GlobalHolder.getSessionId();
		if (sessionId == null || sessionId.isEmpty()) {
			return null;
		}

		// {"session":"XXXXXXXX","module_name":"Project","query":"num_c='0556'","order_by":"","offset":"0","select_fields":["id","name","num_c"],"deleted":"0"}

		JSONObject restData = new JSONObject();
		JSONArray selectFields = new JSONArray();
		try {
			restData.put("session", sessionId);
			restData.put("module_name", "Project");
			restData.put("query", "num_c='" + name + "'");
			restData.put("order_by", "");
			restData.put("offset", "0");
			selectFields.put("id");
			selectFields.put("name");
			selectFields.put("num_c");
			restData.put("select_fields", selectFields);
			restData.put("deleted", "0");
			Log.i(Constants.TAG, restData.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		String url = Util.getQueryProjectUrl();
		String response;

		try {
			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString()));
			if (response != null) {
				return Util.parserProjectListJson(parseResponseError(response));
			} else {
				return null;
			}
		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}
	}

	public List<ProjectOrder> queryProjectOrderList(String projectID)
			throws APIException {
		// {"session":"9467257e6ce3e29f46082b473c9e3554","module_name":"Project","module_id":"a3c3613d-cdc5-703a-55af-513945799b60","link_field_name":"agr_orderdetails_project","related_module_query":"","related_fields":["id","name","quantity","photo_c","qc_status","qc_date","quantity_checked","qc_comment","date_modified"],"deleted":"0"}
		String sessionId = GlobalHolder.getSessionId();
		if (sessionId == null || sessionId.isEmpty()) {
			return null;
		}

		JSONObject restData = new JSONObject();
		JSONArray selectFields = new JSONArray();
		try {
			restData.put("session", sessionId);
			restData.put("module_name", "Project");
			restData.put("module_id", projectID);
			restData.put("link_field_name", "agr_orderdetails_project");
			restData.put("related_module_query", "");

			selectFields.put("id");
			selectFields.put("name");
			selectFields.put("photo_c");
			selectFields.put("qc_status");
			selectFields.put("qc_date");
			selectFields.put("quantity_checked");
			selectFields.put("qc_comment");
			selectFields.put("date_modified");
			selectFields.put("quantity");
			restData.put("related_fields", selectFields);
			restData.put("deleted", "0");
			Log.i(Constants.TAG, restData.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		String url = Util.getQueryProjectItemUrl();
		String response;

		try {
			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString()));
			if (response != null) {
				return Util
						.parserProjectItemListJson(parseResponseError(response));
			}
		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}

		return null;

	}

	public List<ProjectCheckpoint> queryProjectOrderCheckpointList(
			String orderId) throws APIException {
		// {"session":"9467257e6ce3e29f46082b473c9e3554","module_name":"AGR_OrderDetails","module_id":"d82e0333-5e06-df53-7695-515d29c81443","link_field_name":"agr_orderdetails_agr_qccheckpoints","related_module_query":"","related_fields":["id","name","category","checktype","description","qc_status","executed_date","number_defect","qc_comment","qc_action","visual","date_modified","photo_c"],"deleted":"0"}

		String sessionId = GlobalHolder.getSessionId();
		if (sessionId == null || sessionId.isEmpty()) {
			return null;
		}

		JSONObject restData = new JSONObject();
		JSONArray selectFields = new JSONArray();
		try {
			restData.put("session", sessionId);
			restData.put("module_name", "AGR_OrderDetails");
			restData.put("module_id", orderId);
			restData.put("link_field_name",
					"agr_orderdetails_agr_qccheckpoints");
			restData.put("related_module_query", "");

			selectFields.put("id");
			selectFields.put("name");
			selectFields.put("category");
			selectFields.put("checktype");
			selectFields.put("description");
			selectFields.put("qc_status");
			selectFields.put("executed_date");
			selectFields.put("number_defect");
			selectFields.put("qc_comment");
			selectFields.put("qc_action");
			selectFields.put("visual");
			selectFields.put("date_modified");

			restData.put("related_fields", selectFields);
			restData.put("deleted", "0");
			Log.i(Constants.TAG, restData.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		String url = Util.getQueryProjectItemOrderUrl();
		String response;

		try {
			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString()));
			if (response != null) {
				return Util
						.parserProjectItemOrderListJson(parseResponseError(response));
			}
		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}

		return null;
	}

	// http://crm.augura.net/service/v4_1/rest.php?method=set_entry&input_type=JSON&response_type=JSON&rest_data={"session":"XXXXXXXX","module_name":"AGR_QCCheckpoints","name_value_list":[{"name":"id","value":"48287499-7ee5-a933-e687-515e7dc74bf5"},{"name":"deleted","value":1}]}
	public void deleteCheckpoint(String checkpointId) throws APIException {
		String sessionId = GlobalHolder.getSessionId();
		if (sessionId == null || sessionId.isEmpty()) {
			return;
		}

		JSONObject restData = new JSONObject();
		JSONArray entryArray = new JSONArray();
		try {
			restData.put("session", sessionId);
			restData.put("module_name", "AGR_QCCheckpoints");

			JSONObject entry = new JSONObject();
			entry.put("name", "id");
			entry.put("value", checkpointId);
			entryArray.put(entry);

			JSONObject delete = new JSONObject();
			delete.put("name", "deleted");
			delete.put("value", "1");
			entryArray.put(delete);
			restData.put("name_value_list", entryArray);

			Log.i(Constants.TAG, restData.toString());
			String url = Util.getDeleteCheckpointUrl();
			String response;

			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString()));
			parseResponseError(response);
			Log.i(Constants.TAG, response);

		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}

	}

	public void createCheckpoint(ProjectOrder order,
			ProjectCheckpoint checkpoint) throws APIException {
		String sessionId = GlobalHolder.getSessionId();
		if (sessionId == null || sessionId.isEmpty()) {
			return;
		}

		
		JSONObject restData = new JSONObject();
		JSONArray entryArray = new JSONArray();
		try {
			restData.put("session", sessionId);
			restData.put("module_name", "AGR_QCCheckpoints");

			JSONObject entry = new JSONObject();
			entry.put("name", "assigned_user_id");
			entry.put("value", GlobalHolder.getCrrentUser().getUseID());
			entryArray.put(entry);

			JSONObject visual = new JSONObject();
			visual.put("name", "visual");
			visual.put("value", "");
			entryArray.put(visual);

			JSONObject name = new JSONObject();
			name.put("name", "name");
			name.put("value", checkpoint.getName());
			entryArray.put(name);

			JSONObject category = new JSONObject();
			category.put("name", "category");
			category.put("value", checkpoint.getCategory());
			entryArray.put(category);

			JSONObject checktype = new JSONObject();
			checktype.put("name", "checktype");
			checktype.put("value", checkpoint.getCheckType());
			entryArray.put(checktype);

			JSONObject description = new JSONObject();
			description.put("name", "description");
			description.put("value", checkpoint.getDescription());
			entryArray.put(description);

			JSONObject qc_status = new JSONObject();
			qc_status.put("name", "qc_status");
			qc_status.put("value", checkpoint.getQcStatus());
			entryArray.put(qc_status);

			JSONObject number_defect = new JSONObject();
			number_defect.put("name", "number_defect");
			number_defect.put("value", checkpoint.getNumberDefect());
			entryArray.put(number_defect);

			JSONObject qc_comment = new JSONObject();
			qc_comment.put("name", "qc_comment");
			qc_comment.put("value", checkpoint.getQcComments());
			entryArray.put(qc_comment);

			JSONObject qc_action = new JSONObject();
			qc_action.put("name", "qc_action");
			qc_action.put("value", checkpoint.getQcAction());
			entryArray.put(qc_action);

			// JSONObject executed_date = new JSONObject();
			// executed_date.put("name", "executed_date");
			// executed_date.put("value", checkpoint.get);
			// entryArray.put(executed_date.put);

			restData.put("name_value_list", entryArray);

			Log.i(Constants.TAG, restData.toString());
			String url = Util.getCreateCheckpointUrl();
			String response;

			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString()));
			Log.i(Constants.TAG, response);
			JSONObject resJson = parseResponseError(response);
			String id  =resJson.getString("id");
			checkpoint.setId(id);
			
			String relationShipUrl = Util.getCreateCheckpointRelUrl();
			JSONObject rel = new JSONObject();
			//,"module_id":"d82e0333-5e06-df53-7695-515d29c81443","link_field_name":"agr_orderdetails_agr_qccheckpoints","related_ids":["c53ab7d0-2530-46bb-030e-515d320a730f","6fe37f88-6148-80ad-4d26-5168d0e5c8f1"]}
			
			rel.put("session", sessionId);
			rel.put("module_name", "AGR_OrderDetails");
			rel.put("module_id", order.getId());
			rel.put("link_field_name", "agr_orderdetails_agr_qccheckpoints");
				
			JSONArray rIDS = new JSONArray();
			rIDS.put(id);
			rel.put("related_ids", rIDS);
			
			response = http.sendHttpGetRequest(relationShipUrl
					+ URLEncoder.encode(rel.toString()));
			Log.i(Constants.TAG, response);
			
			//TODO mv 
			String photoPath = checkpoint.getUploadPhotoAbsPath();
			String localPath = GlobalHolder.GLOBAL_STORAGE_PATH + Constants.CommonConfig.PIC_DIR + id +"_visual.jpg";
			File f = new File(photoPath);
			if(! f.renameTo(new File(localPath))) {
				Log.e(Constants.TAG, " can't rename file to local dir");
			}
			
			http.sendUploadPhotoRequest(Constants.UPLOAD_PHOTO_URL, localPath);

		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}
	}

	public void updateCheckpoint(ProjectCheckpoint checkpoint)
			throws APIException {
		String sessionId = GlobalHolder.getSessionId();
		if (sessionId == null || sessionId.isEmpty()) {
			return;
		}

		// "name_value_list":[{"name":"visual","value":""},{"name":"id",
		// "value":"673349f6-d189-5d9a-24cd-516bb6412204"}]}

		JSONObject restData = new JSONObject();
		JSONArray entryArray = new JSONArray();
		try {
			restData.put("session", sessionId);
			restData.put("module_name", "AGR_QCCheckpoints");

			JSONObject entry = new JSONObject();
			entry.put("name", "assigned_user_id");
			entry.put("value", GlobalHolder.getCrrentUser().getUseID());
			entryArray.put(entry);

			JSONObject visual = new JSONObject();
			visual.put("name", "visual");
			visual.put("value", "");
			entryArray.put(visual);

			JSONObject id = new JSONObject();
			id.put("name", "id");
			id.put("value", checkpoint.getId());
			entryArray.put(id);

			JSONObject name = new JSONObject();
			name.put("name", "name");
			name.put("value", checkpoint.getName());
			entryArray.put(name);

			JSONObject category = new JSONObject();
			category.put("name", "category");
			category.put("value", checkpoint.getCategory());
			entryArray.put(category);

			JSONObject checktype = new JSONObject();
			checktype.put("name", "checktype");
			checktype.put("value", checkpoint.getCheckType());
			entryArray.put(checktype);

			JSONObject description = new JSONObject();
			description.put("name", "description");
			description.put("value", checkpoint.getDescription());
			entryArray.put(description);

			JSONObject qc_status = new JSONObject();
			qc_status.put("name", "qc_status");
			qc_status.put("value", checkpoint.getQcStatus());
			entryArray.put(qc_status);

			JSONObject number_defect = new JSONObject();
			number_defect.put("name", "number_defect");
			number_defect.put("value", checkpoint.getNumberDefect());
			entryArray.put(number_defect);

			JSONObject qc_comment = new JSONObject();
			qc_comment.put("name", "qc_comment");
			qc_comment.put("value", checkpoint.getQcComments());
			entryArray.put(qc_comment);

			JSONObject qc_action = new JSONObject();
			qc_action.put("name", "qc_action");
			qc_action.put("value", checkpoint.getQcAction());
			entryArray.put(qc_action);

			// JSONObject executed_date = new JSONObject();
			// executed_date.put("name", "executed_date");
			// executed_date.put("value", checkpoint.get);
			// entryArray.put(executed_date.put);

			restData.put("name_value_list", entryArray);

			Log.i(Constants.TAG, restData.toString());
			String url = Util.getUpdateCheckpointUrl();
			String response;

			response = http.sendHttpGetRequest(url
					+ URLEncoder.encode(restData.toString()));
			parseResponseError(response);
			Log.i(Constants.TAG, response);

		} catch (JSONException e) {
			throw new APIException(" can't parse API response");
		}

	}

	private JSONObject parseResponseError(String response)
			throws JSONException, APIException {

		JSONObject object = new JSONObject(response);

		if (object.has("name") && object.has("number")) {
			Object errorName = object.get("name");
			int errorNumber = object.getInt("number");

			if (errorName != null
					&& errorName.toString().equalsIgnoreCase(
							"Invalid Session ID") && errorNumber == 11) {
				throw new APIException(" Invalid session, please re-log in");
			}

			if (errorName != null
					&& errorName.toString().equalsIgnoreCase("Access Denied")
					&& errorNumber == 40) {
				throw new APIException(" No permission to do!");
			}
		}

		return object;
	}

}
