package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class InformationDB extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "userInformation.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE user (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT, " +
                    "zip TEXT, " +
                    "location_access TEXT, " +
                    "gender TEXT);";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS user";
    @SuppressLint("StaticFieldLeak")
    private static InformationDB theDB;

    private InformationDB(Context context)
    {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        //Context appContext = context.getApplicationContext();
    }

    static synchronized InformationDB getInstance(Context context)
    {
        if (theDB == null)
        {
            theDB = new InformationDB(context.getApplicationContext());
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

    void asyncWritableDatabase(OnDBReadyListener listener)
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
