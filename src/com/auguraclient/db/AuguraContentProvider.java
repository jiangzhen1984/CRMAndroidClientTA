
package com.auguraclient.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class AuguraContentProvider extends ContentProvider {

    private AuguraDatabaseHelper auguraHelper;

    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        auguraHelper = new AuguraDatabaseHelper(ctx);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        SQLiteDatabase db = auguraHelper.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);
        switch (token) {
            case ContentDescriptor.ProjectDesc.TOKEN:
                id = db.insert(ContentDescriptor.ProjectDesc.NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentDescriptor.ProjectDesc.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();

            case ContentDescriptor.ProjectOrderDesc.TOKEN:
                id = db.insert(ContentDescriptor.ProjectOrderDesc.NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentDescriptor.ProjectOrderDesc.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();

            case ContentDescriptor.ProjectCheckpointDesc.TOKEN:
                id = db.insert(ContentDescriptor.ProjectCheckpointDesc.NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = auguraHelper.getWritableDatabase();
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
