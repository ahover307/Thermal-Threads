package com.example.finalproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainPage extends AppCompatActivity
{

    SQLiteDatabase theDB;

    private long rowId;

    private Location location;

    private String name;
    private boolean locationAccess;
    private Integer zip;
    private boolean maleGender;

    //Get location - Done
    //Get weather based on location
    //Get information out of the weather once found. Make a callback in the once weather is found.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        //Create a callback onclick for the picture that will refresh the picture maybe? Use the dialog box to confirm this.

        Intent intent = getIntent();
        rowId = intent.getLongExtra("rowId", 1);

        //Create Location object
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Begin searching for the location. Create callback which will then begin the search for the weather once the location is found.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>()
            {
                @Override
                public void onSuccess(Location l)
                {
                    //This can be null. Deal with that.
                    location = l;
                    findWeather();
                }
            });
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
                refreshUserInformationFromDB();
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
            locationAccess = locationAccessInt.equals("true");

            if (!locationAccess)
                zip = Integer.parseInt(c.getString(c.getColumnIndex("zip")));

            String maleGenderInt = c.getString(c.getColumnIndex("gender"));
            maleGender = maleGenderInt.equals("true");

            c.close();
        }
    }

    public void findWeather()
    {
        //this will find the weather. Only called after you have the location.
        if (locationAccess)
        {
            if (location == null)
            {
                Toast.makeText(this, "Please try again. Your phone needs to have found the location already for this to work.", Toast.LENGTH_SHORT).show();
            } else
            {
                //Find weather based on the precise location. We'll see how this is done. what api and shit.
            }
        } else //Use the provided zipcode. it will have been parsed in any entry acitivity, so it should be at least 5 digits of integers.
        //Its on the user to make sure that its typed in correctly
        {
            boolean x = zip.equals(10);
        }
    }

    //Put a link to this in the action bar
    public void refreshWeather()
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
                        //Begin searching for the location. Create callback which will then begin the search for the weather once the location is found.
                        findWeather();
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

    public void onAboutLaunchClick(View view)
    {
        startActivity(new Intent(this, Acknowledgments.class));
    }

}
