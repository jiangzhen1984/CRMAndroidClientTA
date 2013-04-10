package com.auguraclient.util;

public class Constants {

    public static final String TAG = "AUGURACLIENT";

    public static final String API_URL = "http://crm.augura.net/service/v4_1/rest.php";
    public static final String[] API_TABLE = {
        // first one is login
        API_URL,
        // second is get project
        API_URL+"?" +"method=get_entry_list&input_type=JSON&response_type=JSON&rest_data="
    };

    public static final int LOGIN_URL_INDEX = 0;
}
