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
		matcher.addURI(augura, ProjectCheckpointDesc.PATH,
				ProjectCheckpointDesc.TOKEN);
		matcher.addURI(augura, UpdateDesc.PATH, UpdateDesc.TOKEN);

		return matcher;

	}

	public static class ProjectDesc {

		public static final String PATH = "project";

		public static final String NAME = PATH;

		public static final int TOKEN = 1;

		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(
				PATH).build();

		public static class Cols {
			public static final String ID = BaseColumns._ID;

			public static final String NAME = "name";

			public static final String PRO_ID = "pro_id";
			public static final String TEXT = "pro_text";

			public static final String SYNC_FLAG = "sync_flag";
			public static final String[] ALL_CLOS = { ID, NAME, PRO_ID,
					SYNC_FLAG, TEXT };
		}
	}

	public static class ProjectOrderDesc {

		public static final String PATH = "project_order";

		public static final int TOKEN = 2;

		public static final String NAME = PATH;

		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(
				PATH).build();

		public static class Cols {
			public static final String ID = BaseColumns._ID;

			public static final String NAME = "name";

			public static final String PRO_ID = ProjectDesc.Cols.PRO_ID;

			public static final String ORD_ID = "ord_id";

			public static final String QUANTITY = "quantity";

			public static final String QC_STATUS = "qc_status";

			public static final String QUANTITY_CHECKED = "quantity_checked";

			public static final String QC_COMMENT = "qc_comment";

			public static final String DATE_MODIFIED = "date_modified";

			public static final String PHOTO_NAME = "photo_name";

			public static final String PHOTO_LOCAL_SMALL_PATH = "small_path";

			public static final String PHOTO_LOCAL_BIG_PATH = "big_path";

			public static final String DESCRIPTION = "description";

			public static final String[] ALL_COLS = { ID, NAME, PRO_ID, ORD_ID,
					QUANTITY, QC_STATUS, QUANTITY_CHECKED, QC_COMMENT,
					DATE_MODIFIED, PHOTO_NAME, PHOTO_LOCAL_SMALL_PATH,
					PHOTO_LOCAL_BIG_PATH, DESCRIPTION };
		}
	}

	public static class ProjectCheckpointDesc {

		public static final String PATH = "project_order_checkpoint";

		public static final int TOKEN = 3;

		public static final String NAME = PATH;

		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(
				PATH).build();

		public static class Cols {
			public static final String ID = BaseColumns._ID;

			public static final String NAME = "name";

			public static final String PRO_ID = ProjectDesc.Cols.PRO_ID;

			public static final String PRO_ORDER_ID = ProjectOrderDesc.Cols.ORD_ID;

			public static final String CHECKPOINT_ID = "checkpoint_id";

			public static final String CATEGORY = "category";

			public static final String CHECK_TYPE = "check_type";

			public static final String QC_STATUS = "qc_status";

			public static final String NUMBER_DEFECT = "number_defect";

			public static final String QC_ACTION = "qc_action";

			public static final String PHOTO_NAME = "photo_name";

			public static final String PHOTO_LOCAL_SMALL_PATH = "small_path";

			public static final String PHOTO_LOCAL_BIG_PATH = "big_path";
			
			public static final String PHOTO_LOCAL_PATH = "abs_big_path";

			public static final String DESCRIPTION = "description";

			public static final String QC_COMMENT = "qc_comment";

			public static final String EXECTUED_DATE = "executed_date";

			public static final String LAST_DATE = "last_date";

			public static final String FLAG = "flag";

			public static final String[] ALL_COLS = { ID, NAME, PRO_ID,
					PRO_ORDER_ID, CHECKPOINT_ID, CATEGORY, CHECK_TYPE,					
					PHOTO_LOCAL_SMALL_PATH, PHOTO_LOCAL_BIG_PATH, DESCRIPTION,
					QC_STATUS, NUMBER_DEFECT, QC_ACTION, PHOTO_NAME,
					QC_COMMENT, EXECTUED_DATE, LAST_DATE, PHOTO_LOCAL_PATH, FLAG };

		}
	}

	public static class UpdateDesc {
		
		public static final String PATH = "updated_record";

		public static final int TOKEN = 4;

		public static final String NAME = PATH;

		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(
				PATH).build();

		public static final String TYPE_ENUM_CHECKPOINT ="1";
		
		public static final String TYPE_ENUM_ORDER ="2";
		
		public static final String TYPE_ENUM_FLAG_CREATE ="1";
		
		public static final String TYPE_ENUM_FLAG_UPDATE ="2";
		
		public static final String TYPE_ENUM_FLAG_DELETE ="3";
		
		public static class Cols {
			public static final String ID = BaseColumns._ID;

			public static final String TYPE = "type";

			public static final String PRO_ID = ProjectDesc.Cols.PRO_ID;

			public static final String PRO_ORDER_ID = ProjectOrderDesc.Cols.ORD_ID;

			public static final String RELATE_ID = "related_id";

			public static final String FLAG = "flag";

			public static final String[] ALL_COLS = { ID, TYPE, PRO_ID,
					PRO_ORDER_ID, RELATE_ID, FLAG };
		}

	}

}
