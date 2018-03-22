package com.example.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

/**
 * Created by ahove on 3/22/2018.
 */

public class InformationDB extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "userInformation.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE user (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT, " +
                    "zip INTEGER, " +
                    "location_access INTEGER, " +
                    "gender INTEGER);";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS user";
    private static InformationDB theDB;

    private InformationDB(Context context)
    {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized InformationDB getInstance(Context context)
    {
        if (theDB == null)
        {
            theDB = new InformationDB(context);
        }
        return theDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void getWritableDatabase(OnDBReadyListener listener)
    {
        new OpenDBAsyncTask().execute(listener);
    }

    interface OnDBReadyListener
    {
        void onDBReady(SQLiteDatabase db);
    }

    private static class OpenDBAsyncTask extends AsyncTask<OnDBReadyListener, Void, SQLiteDatabase>
    {
        OnDBReadyListener listener;

        @Override
        protected SQLiteDatabase doInBackground(OnDBReadyListener... params)
        {
            listener = params[0];
            return InformationDB.theDB.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db)
        {
            //Make that callback
            listener.onDBReady(db);
        }
    }
}
