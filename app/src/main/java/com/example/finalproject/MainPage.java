package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainPage extends AppCompatActivity
{

    SQLiteDatabase theDB;

    private long rowId;


    private String name;
    private boolean locationAccess;
    private Integer zip;
    private boolean maleGender;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        //Create a callback onclick for the picture that will refresh the picture maybe? Use the dialog box to confirm this.

        Intent intent = getIntent();
        rowId = intent.getLongExtra("rowId", 1);

        //Get location

        //Get weather based on location

        //Talk about what sort of stuff they need to do.
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        InformationDB.getInstance(this).asyncWritableDatabase(new InformationDB.OnDBReadyListener()
        {
            @Override
            public void onDBReady(SQLiteDatabase db)
            {
                theDB = db;
//                refreshUserInformationFromDB();
            }
        });
    }

    public void refreshUserInformationFromDB()
    {
        if (theDB == null)
        {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        } else
        {
            //Retrieve data from the db.
            String[] columns = {"name", "location_access", "zip", "gender"};
            String where = "_id = " + Long.toString(rowId);
            Cursor c = theDB.query("user", columns, where, null, null, null, null);

            c.moveToFirst();

            name = c.getString(c.getColumnIndex("name"));

            String locationAccessInt = c.getString(c.getColumnIndex("location_access"));
            if (locationAccessInt.equals("true"))
                locationAccess = true;
            else
                locationAccess = false;

            zip = Integer.parseInt(c.getString(c.getColumnIndex("zip")));

            String maleGenderInt = c.getString(c.getColumnIndex("gender"));
            if (maleGenderInt.equals("true"))
                maleGender = true;
            else
                maleGender = false;
        }
    }


    public void onAboutLaunchClick(View view)
    {
        refreshUserInformationFromDB();
        //startActivity(new Intent(this, Acknowledgments.class));
    }

    public void refreshWeather(final View view)
    {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Refresh Weather");
        alertDialog.setMessage("Would you like to refresh the weather");
        // Alert dialog button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Continue Anyway",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //findWeather();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        alertDialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
