package com.example.jsw.mail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DBFILE_CONTACT = "contact.db";

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE TEST_T (SENDER TEXT, PW TEXT, RECEIVER TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_TABLE = "DROP TABLE IF EXISTS TEST_T";
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
