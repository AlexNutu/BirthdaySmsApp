/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.com.example.alexnutu.birthdaysms.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class BirthdayProvider extends ContentProvider {

    private BirthdayDbHelper database;

    // used for the UriMacher
    private static final int BIRTHDAYS = 10;
    private static final int BIRTHDAY_ID = 20;

    private static final String AUTHORITY = "app.com.example.alexnutu.birthdaysms";

    private static final String BASE_PATH = "birthday";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/alarms";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/alarm";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, BIRTHDAYS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BIRTHDAY_ID);
    }

    @Override
    public boolean onCreate() {
        database = new BirthdayDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
//        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(BirthdayContract.BirthdayEntry.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BIRTHDAYS:
                break;
            case BIRTHDAY_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(BirthdayContract.BirthdayEntry.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case BIRTHDAYS:
                id = sqlDB.insert(BirthdayContract.BirthdayEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case BIRTHDAYS:
                rowsDeleted = sqlDB.delete(BirthdayContract.BirthdayEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case BIRTHDAY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(BirthdayContract.BirthdayEntry.TABLE_NAME,
                            BirthdayContract.BirthdayEntry.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(BirthdayContract.BirthdayEntry.TABLE_NAME,
                            BirthdayContract.BirthdayEntry.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case BIRTHDAYS:
                rowsUpdated = sqlDB.update(BirthdayContract.BirthdayEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case BIRTHDAY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(BirthdayContract.BirthdayEntry.TABLE_NAME,
                            values,
                            BirthdayContract.BirthdayEntry.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(BirthdayContract.BirthdayEntry.TABLE_NAME,
                            values,
                            BirthdayContract.BirthdayEntry.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { BirthdayContract.BirthdayEntry.COLUMN_NAME, BirthdayContract.BirthdayEntry.COLUMN_BIRTHDATE, BirthdayContract.BirthdayEntry.COLUMN_PHONE_NUMBER, BirthdayContract.BirthdayEntry.COLUMN_MESSAGE,
                BirthdayContract.BirthdayEntry.COLUMN_FACEBOOK_ACCOUNT,BirthdayContract.BirthdayEntry.COLUMN_NOTIFICATION};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
