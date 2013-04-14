
package com.auguraclient.util;

import com.auguraclient.model.JSONParserException;
import com.auguraclient.model.Project;
import com.auguraclient.model.ProjectOrder;
import com.auguraclient.model.ProjectCheckpoint;
import com.auguraclient.model.ProjectList;
import com.auguraclient.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Util {

    private static MessageDigest digest = null;

    public static String encryptMD5(String sum) {
        if (sum == null || sum.isEmpty()) {
            Log.w("AUGURACLIENT", "can't encrype empty string ");
            return null;
        }
        if (digest == null) {
            try {
                digest = java.security.MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                Log.e("AUGURACLIENT", "can't get md5 instance  ", e);
                throw new RuntimeException(" can't get md5 instance ");
            }
        }
        digest.update(sum.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++) {
            String h = Integer.toHexString(0xFF & messageDigest[i]);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }

    public static String getLoginUrl() {
        return Constants.API_TABLE[Constants.LOGIN_URL_INDEX];
    }

    public static String getQueryProjectUrl() {
        return Constants.API_TABLE[Constants.QUERY_PROJECT_URL_INDEX];
    }

    public static String getQueryProjectItemUrl() {
        return Constants.API_TABLE[Constants.QUERY_PROJECT_ITEM_URL_INDEX];
    }

    public static String getQueryProjectItemOrderUrl() {
        return Constants.API_TABLE[Constants.QUERY_PROJECT_ORDER_URL_INDEX];
    }

    public static String getDeleteCheckpointUrl() {
        return Constants.API_TABLE[Constants.DELETE_CHECK_POINT_URL_INDEX];
    }


    public static User parserUserJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        User user = new User();
        Object obj;
        try {
            obj = object.get("id");
            if (obj == null) {
                return null;
            }
            user.setmSessionID(obj.toString());

            JSONObject valueObject = (JSONObject)object.get("name_value_list");
           if(valueObject != null) {
               user.setUseID(((JSONObject)valueObject.get("user_id")).getString("value"));
               user.setUserName(((JSONObject)valueObject.get("user_name")).getString("value"));
           }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return user;
    }

    public static ProjectList parserProjectListJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        ProjectList pl = new ProjectList();

        Object obj;
        try {
            obj = object.get("result_count");
            if (obj == null) {
                return null;
            }
            // FIXME check number format
            pl.setResultCount(Integer.parseInt(obj.toString()));

            obj = object.get("total_count");
            if (obj == null) {
                return null;
            }
            // FIXME check number format
            pl.setTotalCount(Integer.parseInt(obj.toString()));

            obj = object.get("next_offset");
            if (obj == null) {
                return null;
            }
            // FIXME check number format
            pl.setNextOffet(Integer.parseInt(obj.toString()));

            JSONArray entryList = object.getJSONArray("entry_list");
            for (int i = 0; i < entryList.length(); i++) {
                Project p = new Project();
                JSONObject it = (JSONObject)entryList.get(i);
                p.setId(it.get("id").toString());

                String text = ((JSONObject)((JSONObject)it.get("name_value_list")).get("name"))
                        .get("value").toString();
                p.setText(text);

                String name = ((JSONObject)((JSONObject)it.get("name_value_list")).get("num_c"))
                        .get("value").toString();
                p.setName(name);
                pl.addProject(p);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pl;
    }

    public static List<ProjectOrder> parserProjectItemListJson(JSONObject object) {
        try {
            List<ProjectOrder> l = new ArrayList<ProjectOrder>();
            JSONArray itemAr = object.getJSONArray("entry_list");
            for (int i = 0; i < itemAr.length(); i++) {
                ProjectOrder pi = new ProjectOrder();
                JSONObject it = (JSONObject)itemAr.get(i);
                pi.parser(it);
                l.add(pi);
            }
            return l;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (JSONParserException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static List<ProjectCheckpoint> parserProjectItemOrderListJson(JSONObject object) {
        try {
            List<ProjectCheckpoint> l = new ArrayList<ProjectCheckpoint>();
            JSONArray itemAr = object.getJSONArray("entry_list");
            for (int i = 0; i < itemAr.length(); i++) {
                ProjectCheckpoint pi = new ProjectCheckpoint();
                JSONObject it = (JSONObject)itemAr.get(i);
                pi.parser(it);
                l.add(pi);
            }
            return l;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (JSONParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadImageFromURL(URL url, String photoPath) throws IOException {
        Log.i(Constants.TAG, photoPath);
        InputStream input = null;
        OutputStream output = null;

        try {
            File f =  new  File(photoPath);
            if(!f.exists()) {
                f.createNewFile();
            }
            input = url.openStream();
            output = new FileOutputStream(f);

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int bytesRead = 0;
            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                output.write(buffer, 0, bytesRead);
            }

        } catch(IOException e){
        	e.printStackTrace();
        	throw e;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                throw e;
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                throw e;
            }

        }
    }

}
