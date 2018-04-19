package com.example.finalproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainPage extends AppCompatActivity
{
    //Weather provided by WeatherUnderground
    //API key for wunderground
    public static final String WUNDERGROUNG_API_KEY = "7ee59bf996ed0a7f";
    //Forecast Example
    // http://api.wunderground.com/api/API-KEY/forecast/q/CA/ZIP-CODE.json
    // http://api.wunderground.com/api/API-KEY/forecast/q/CA/LATTIDUE, LONGITUDE.json

    public static final int SETUP_ACTIVITY_RESULT = 1;
    private SQLiteDatabase theDB;
    private SharedPreferences sharedPref = null;
    private Location location;

    //User stuff is in sharedprefs now.
    // sharedPref.getBoolean("location_access", false)
    // sharedPref.getString("name", "@string/default_name")
    // sharedPref.getInt("zip", 0)
    // sharedPref.getBoolean("male", false);

    //Get location - Done
    //Get weather based on location
    //Get information out of the weather once found. Make a callback in the once weather is found.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //Find preferences file
        sharedPref = getSharedPreferences("com.example.finalProject", MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        //Uncomment this line to set up the page for the first time.
        //        sharedPref.edit().putBoolean("firstRun", true).apply();

        //        //https://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run
        if (sharedPref.getBoolean("firstRun", true))
        {
            startActivityForResult(new Intent(MainPage.this, FirstLaunchSetup.class), SETUP_ACTIVITY_RESULT);
            sharedPref.edit().putBoolean("firstRun", false).apply();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_page, menu);
        return true;
    }

    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        super.onActivityResult(requestCode, resultCode, data);
    //    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_about:
                startActivity(new Intent(this, Acknowledgments.class));
                return true;
            case R.id.menu_refresh_weather:
                refreshWeather();
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected((item));
        }
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
            }
        });

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
                    //                    findWeather();
                }
            });
    }

    //    //Pulls user stuff from database and drops it into the instance variables
    //    public void refreshUserInformationFromDB() {
    //        if (theDB == null) {
    //            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
    //        } else {
    //            //Retrieve data from the db.
    //            String[] columns = {"name", "location_access", "zip", "gender"};
    //            String where = "_id = " + 1;
    //            Cursor c = theDB.query("user", columns, where, null, null, null, null);
    //
    //            c.moveToFirst();
    //
    //            name = c.getString(c.getColumnIndex("name"));
    //
    //            String locationAccessInt = c.getString(c.getColumnIndex("location_access"));
    //            locationAccess = locationAccessInt.equals("true");
    //
    //            if (!locationAccess)
    //                zip = Integer.parseInt(c.getString(c.getColumnIndex("zip")));
    //
    //            String maleGenderInt = c.getString(c.getColumnIndex("gender"));
    //            maleGender = maleGenderInt.equals("true");
    //
    //            c.close();
    //        }
    //    }


    public void findWeather()
    {
        //this will find the weather. Only called after you have the location.
        if (sharedPref.getBoolean("location_access", false))
        {
            if (location == null)
            {
                Toast.makeText(this, "Please enter in your zip. We can't find your location automatically", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(this, "The weather is shit.", Toast.LENGTH_SHORT).show();

                //Find weather based on the precise location. We'll see how this is done. what api and shit.

                String latitude = Double.toString(location.getLatitude());
                String longitude = Double.toString(location.getLongitude());

                String JSONString =
                        "http://api.wunderground.com/api/" +
                                WUNDERGROUNG_API_KEY +
                                "/forecast/q/CA/" +
                                latitude +
                                ", " +
                                longitude +
                                ".json";

                findWeatherHelper(JSONString);
            }
        } else //Use the provided zipcode. it will have been parsed in any entry acitivity, so it should be at least 5 digits of integers.
        //Its on the user to make sure that its typed in correctly
        {
            Toast.makeText(this, "The weather is shit and the zip is here.", Toast.LENGTH_SHORT).show();

            String JSONString =
                    "http://api.wunderground.com/api/" +
                            WUNDERGROUNG_API_KEY +
                            "/forecast/q/CA/" +
                            Integer.toString(sharedPref.getInt("zip", 0)) +
                            ".json";
            findWeatherHelper(JSONString);
        }
    }

    //Async method to call down the weather and parse it.
    private void findWeatherHelper(String JSONString)
    {
        //Make async call and get json object
        //parse json object
        //Create global variables and save it there.
    }

    //Put a link to this in the action bar
    public void refreshWeather()
    {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Refresh Weather");
        alertDialog.setMessage("Would you like to refresh the weather");
        // Alert dialog button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
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