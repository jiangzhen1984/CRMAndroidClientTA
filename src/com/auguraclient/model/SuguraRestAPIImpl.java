
package com.auguraclient.model;

import com.auguraclient.http.HttpWrapper;
import com.auguraclient.util.GlobalHolder;
import com.auguraclient.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
        System.out.println(restData);

        // TODO send request
        String url = Util.getLoginUrl();
        String response;

        try {
            response = http.sendHttpGetRequest(url
                    + URLEncoder.encode(restData.toString(), "UTF-8"));
            if (response != null) {
                JSONObject resp = new JSONObject(response);
                return Util.parserUserJson(resp);
            }
        } catch (UnsupportedEncodingException e) {
            throw new APIException(" can't find UTF-8 encder instance");
        } catch (JSONException e) {
            throw new APIException(" can't parse API response");
        }

        return null;
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
            System.out.println(restData.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        String url = Util.getQueryProjectUrl();
        String response;

        try {
            response = http.sendHttpGetRequest(url + URLEncoder.encode(restData.toString()));
            if (response != null) {
                JSONObject resp = new JSONObject(response);
                return Util.parserProjectListJson(resp);
            } else {
                return null;
            }
        } catch (JSONException e) {
            throw new APIException(" can't parse API response");
        }
    }


    public List<ProjectItem> queryProjectItemList(String projectID) throws APIException {
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
            System.out.println(restData.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        String url = Util.getQueryProjectItemUrl();
        String response;

        try {
            response = http.sendHttpGetRequest(url + URLEncoder.encode(restData.toString()));
            if (response != null) {
                JSONObject resp = new JSONObject(response);
                return Util.parserProjectItemListJson(resp);
            }
        } catch (JSONException e) {
            throw new APIException(" can't parse API response");
        }

        return null;

    }

    public List<ProjectItem> queryProjectItemOrderList(String orderId) throws APIException {
        //{"session":"9467257e6ce3e29f46082b473c9e3554","module_name":"AGR_OrderDetails","module_id":"d82e0333-5e06-df53-7695-515d29c81443","link_field_name":"agr_orderdetails_agr_qccheckpoints","related_module_query":"","related_fields":["id","name","category","checktype","description","qc_status","executed_date","number_defect","qc_comment","qc_action","visual","date_modified","photo_c"],"deleted":"0"}

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
            restData.put("link_field_name", "related_module_query");
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
            System.out.println(restData.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        String url = Util.getQueryProjectItemOrderUrl();
        String response;

        try {
            response = http.sendHttpGetRequest(url + URLEncoder.encode(restData.toString()));
            if (response != null) {
                JSONObject resp = new JSONObject(response);
                return Util.parserProjectItemListJson(resp);
            }
        } catch (JSONException e) {
            throw new APIException(" can't parse API response");
        }

        return null;
    }





}
