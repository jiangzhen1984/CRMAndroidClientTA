package com.auguraclient.util;

public class Constants {

    public static final String TAG = "AUGURACLIENT";

    public static final String API_URL = "http://crm.augura.net/service/v4_1/rest.php";
    public static final String[] API_TABLE = {
        // first one is login
        API_URL+"?"+"method=login&input_type=JSON&response_type=JSON&rest_data=",
        // second is get project
        // http://crm.augura.net/service/v4_1/rest.php?method=get_entry_list&input_type=JSON&response_type=JSON&rest_data={"session":"XXXXXXXX","module_name":"Project","query":"num_c='0556'","order_by":"","offset":"0","select_fields":["id","name","num_c"],"deleted":"0"}
        API_URL+"?" +"method=get_entry_list&input_type=JSON&response_type=JSON&rest_data="
    };

    public static final int LOGIN_URL_INDEX = 0;

    public static final int QUERY_PROJECT_URL_INDEX = 1;
}
