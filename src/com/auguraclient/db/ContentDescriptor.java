
package com.auguraclient.db;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

public class ContentDescriptor {

    public static final String AUGURA = "com.augura.client";

    private static final Uri BASE_URI = Uri.parse("content://" + AUGURA);

    public static final UriMatcher URI_MATCHER = buildUriMatcher();

    private ContentDescriptor() {
    };

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String augura = AUGURA;

        matcher.addURI(augura, ProjectDesc.PATH, ProjectDesc.TOKEN);
        matcher.addURI(augura, ProjectOrderDesc.PATH, ProjectOrderDesc.TOKEN);
        matcher.addURI(augura, ProjectCheckpointDesc.PATH, ProjectCheckpointDesc.TOKEN);

        return matcher;

    }

    public static class ProjectDesc {

        public static final String PATH = "project";

        public static final String NAME = PATH;

        public static final int TOKEN = 1;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();

        public static class Cols {
            public static final String ID = BaseColumns._ID;

            public static final String NAME = "name";

            public static final String PRO_ID = "pro_id";

            public static final String SYNC_FLAG ="sync_flag";
        }
    }

    public static class ProjectOrderDesc {

        public static final String PATH = "project_order";

        public static final int TOKEN = 2;

        public static final String NAME = PATH;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath("project_order")
                .build();

        public static class Cols {
            public static final String ID = BaseColumns._ID;

            public static final String NAME = "name";

            public static final String PRO_ID = ProjectDesc.Cols.PRO_ID;

            public static final String ORD_ID = "ord_id";

            public static final String QUANTITY = "quantity";

            public static final String QC_STATUS = "qc_status;";

            public static final String QUANTITY_CHECKED = " quantity_checked";

            public static final String QC_COMMENT = " qc_comment";

            public static final String DATE_MODIFIED = "date_modified";

            public static final String PHOTO_NAME = "photo_name";

            public static final String PHOTO_LOCAL_SMALL_PATH = "small_path";

            public static final String PHOTO_LOCAL_BIG_PATH = "big_path";

            public static final String DESCRIPTION = "description";
        }
    }

    public static class ProjectCheckpointDesc {

        public static final String PATH = "project_order_checkpoing";

        public static final int TOKEN = 3;
        public static final String NAME = PATH;

        public static final Uri CONTENT_URI = BASE_URI.buildUpon()
                .appendPath("project_order_checkpoing").build();

        public static class Cols {
            public static final String ID = BaseColumns._ID;

            public static final String NAME = "name";

            public static final String PRO_ID = ProjectDesc.Cols.PRO_ID;

            public static final String PRO_ORDER_ID = ProjectOrderDesc.Cols.ORD_ID;

            public static final String CATEGORY = "category";

            public static final String CHECK_TYPE = "check_type;";

            public static final String QC_STATUS = " qc_status";

            public static final String NUMBER_DEFECT = " number_defect";

            public static final String QC_ACTION = "qc_action";

            public static final String PHOTO_NAME = "photo_name";

            public static final String PHOTO_LOCAL_SMALL_PATH = "small_path";

            public static final String PHOTO_LOCAL_BIG_PATH = "big_path";

            public static final String DESCRIPTION = "description";

            public static final String QC_COMMENT = "qc_comment";

            public static final String EXECTUED_DATE = "executed_date";

            public static final String LAST_DATE = "last_date";

        }

    }

}