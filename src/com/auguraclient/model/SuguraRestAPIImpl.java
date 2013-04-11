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

    private final static  String API_VERSION = "1" ;

    private final static String APPLICATION_NAME ="Android-Client" ;

    private HttpWrapper http;




    public SuguraRestAPIImpl() {
        http = new HttpWrapper();
    }


    /**
     *     http://crm.augura.net/service/v4_1/rest.php?method=login&input_type=JSON&response_type=JSON&rest_data={%22user_auth%22:{%22user_name%22:%22android%22,%22password%22:%22c31b32364ce19ca8fcd150a417ecce58%22,%22version%22:%221%22},%22application_name%22:%22RestTest%22}
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
                response = http.sendHttpGetRequest(url+URLEncoder.encode(restData.toString(), "UTF-8"));
                if(response != null) {
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
        if(sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        //{"session":"XXXXXXXX","module_name":"Project","query":"num_c='0556'","order_by":"","offset":"0","select_fields":["id","name","num_c"],"deleted":"0"}

        JSONObject restData = new JSONObject();
        JSONArray selectFields =  new JSONArray();
        try {
            restData.put("session", sessionId);
            restData.put("module_name", "Project");
            restData.put("query", "num_c='"+name+"'");
            restData.put("order_by", "");
            restData.put("offset", "0");
            selectFields.put("id");
            selectFields.put("name");
            selectFields.put("num_c");
            restData.put("select_fields", selectFields);
            restData.put("deleted", "0");
            System.out.println(restData.toString());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        String url = Util.getQueryProjectUrl();
        String response;

            try {
                response = http.sendHttpGetRequest(url+URLEncoder.encode(restData.toString()));
                if(response != null) {
                    JSONObject resp = new JSONObject(response);
                    return Util.parserProjectListJson(resp);
                }
            } catch (JSONException e) {
                throw new APIException(" can't parse API response");
            }



        return null;
    }

}
