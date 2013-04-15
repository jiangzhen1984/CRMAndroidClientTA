package com.auguraclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AuguraDatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "augura.db";
    private static final int DATABASE_VERSION = 1;

    public AuguraDatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public AuguraDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ContentDescriptor.ProjectDesc.NAME+ " ( " +
                ContentDescriptor.ProjectDesc.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContentDescriptor.ProjectDesc.Cols.PRO_ID   + " TEXT NOT NULL,  " +
                ContentDescriptor.ProjectDesc.Cols.NAME + " TEXT NOT NULL, " +
                ContentDescriptor.ProjectDesc.Cols.TEXT + " TEXT NOT NULL, " +
                ContentDescriptor.ProjectDesc.Cols.SYNC_FLAG + " TEXT, " +
                "UNIQUE (" +
                    ContentDescriptor.ProjectDesc.Cols.ID +
                ") ON CONFLICT REPLACE)"
            );


        db.execSQL("CREATE TABLE " + ContentDescriptor.ProjectOrderDesc.NAME+ " ( " +
                ContentDescriptor.ProjectOrderDesc.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID   + " TEXT NOT NULL,  " +
                ContentDescriptor.ProjectOrderDesc.Cols.NAME + " TEXT NOT NULL, " +
                ContentDescriptor.ProjectOrderDesc.Cols.ORD_ID + " TEXT NOT NULL, " +
                ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.DATE_MODIFIED + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_NAME + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.DESCRIPTION + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_BIG_PATH + " TEXT , " +
                ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_SMALL_PATH + " TEXT , " +
                "UNIQUE (" +
                    ContentDescriptor.ProjectOrderDesc.Cols.ID +
                ") ON CONFLICT REPLACE)"
            );


        db.execSQL("CREATE TABLE " + ContentDescriptor.ProjectCheckpointDesc.NAME+ " ( " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID   + " TEXT NOT NULL,  " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID + " TEXT NOT NULL, " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID + " TEXT NOT NULL, " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.NAME + " TEXT NOT NULL," +
                ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.LAST_DATE + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.EXECTUED_DATE + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME + " TEXT , " +

                ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_BIG_PATH + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH + " TEXT , " +
                ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG + " TEXT , " +
                "UNIQUE (" +
                    ContentDescriptor.ProjectOrderDesc.Cols.ID +
                ") ON CONFLICT REPLACE)"
            );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + ContentDescriptor.ProjectDesc.NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ContentDescriptor.ProjectOrderDesc.NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ContentDescriptor.ProjectCheckpointDesc.NAME);
            onCreate(db);
        }
    }





}
