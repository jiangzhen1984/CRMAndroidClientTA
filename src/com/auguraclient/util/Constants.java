
package com.auguraclient.util;

public class Constants {

    public class CommonConfig {
        public static final String PIC_DIR = "/augura/pic/";
    }

    public class SaveConfig {
        public static final String CONFIG = "config";

        public static final String USER_NAME = "user_name";

        public static final String PASSWORD = "password";

        public static final String SESSION = "session";

        public static final String USER_ID = "user_id";
    }

    public static final String TAG = "AUGURACLIENT";

    public static final String API_URL = "http://crm.augura.net/service/v4_1/rest.php";

    public static final String PHTOT_API_URL = "http://crm.augura.net/Augura/photos/";

    public static final String PHTOT_COMPRESSED_API_URL = "http://crm.augura.net/AuguraClasses/phpThumb/phpThumb.php?src=/Augura/photos/";

    public static final String[] API_TABLE = {
            // first one is login
            API_URL + "?" + "method=login&input_type=JSON&response_type=JSON&rest_data=",
            // second is get project
            // http://crm.augura.net/service/v4_1/rest.php?method=get_entry_list&input_type=JSON&response_type=JSON&rest_data={"session":"XXXXXXXX","module_name":"Project","query":"num_c='0556'","order_by":"","offset":"0","select_fields":["id","name","num_c"],"deleted":"0"}
            API_URL + "?" + "method=get_entry_list&input_type=JSON&response_type=JSON&rest_data=",
            // http://crm.augura.net/service/v4_1/rest.php?method=get_relationships&input_type=JSON&response_type=JSON&rest_data={%22session%22:%229467257e6ce3e29f46082b473c9e3554%22,%22module_name%22:%22Project%22,%22module_id%22:%22a3c3613d-cdc5-703a-55af-513945799b60%22,%22link_field_name%22:%22agr_orderdetails_project%22,%22related_module_query%22:%22%22,%22related_fields%22:[%22id%22,%22name%22,%22quantity%22,%22photo_c%22,%22qc_status%22,%22qc_date%22,%22quantity_checked%22,%22qc_comment%22,%22date_modified%22],%22deleted%22:%220%22}
            API_URL + "?"
                    + "method=get_relationships&input_type=JSON&response_type=JSON&rest_data=",
            // http://crm.augura.net/service/v4_1/rest.php?method=get_relationships&input_type=JSON&response_type=JSON&rest_data={%22session%22:%229467257e6ce3e29f46082b473c9e3554%22,%22module_name%22:%22AGR_OrderDetails%22,%22module_id%22:%221414d8fc-3f73-6adb-bbad-515d296ca383%22,%22link_field_name%22:%22agr_orderdetails_agr_qccheckpoints%22,%22related_module_query%22:%22%22,%22related_fields%22:[%22id%22,%22name%22,%22category%22,%22checktype%22,%22description%22,%22qc_status%22,%22executed_date%22,%22number_defect%22,%22qc_comment%22,%22qc_action%22,%22visual%22,%22date_modified%22],%22deleted%22:%220%22}
            API_URL + "?"
                    + "method=get_relationships&input_type=JSON&response_type=JSON&rest_data=",
            // http://crm.augura.net/service/v4_1/rest.php?method=set_entry&input_type=JSON&response_type=JSON&rest_data=
            API_URL + "?" + "method=set_entry&input_type=JSON&response_type=JSON&rest_data="
    };

    public static final int LOGIN_URL_INDEX = 0;

    public static final int QUERY_PROJECT_URL_INDEX = 1;

    public static final int QUERY_PROJECT_ITEM_URL_INDEX = 2;

    public static final int QUERY_PROJECT_ORDER_URL_INDEX = 3;

    public static final int DELETE_CHECK_POINT_URL_INDEX = 4;

    public static final int BUFFER_SIZE = 5000;
}
