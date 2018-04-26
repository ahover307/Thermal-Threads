package com.example.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainPage extends AppCompatActivity
{
    //Weather provided by WeatherUnderground
    //API key for wunderground
    public static final String WUNDERGROUND_API_KEY = "7ee59bf996ed0a7f";
    public static final int SETUP_ACTIVITY_RESULT = 1;
    //Forecast Example
    // http://api.wunderground.com/api/API-KEY/forecast/q/CA/ZIP-CODE.json
    // http://api.wunderground.com/api/API-KEY/forecast/q/CA/LATTIDUE, LONGITUDE.json
    //Weather object that holds all the information given
    WeatherObject weatherObject;
    private SQLiteDatabase theDB;
    private SharedPreferences sharedPref = null;
    private Location location;

    //User stuff is in shared-prefs now.
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
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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
                refreshWeatherFromDatabase();
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
                    findWeather();
                }
            });
        else
            findWeather();
    }

    private void refreshWeatherFromDatabase()
    {
        if (theDB == null)
        {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        } else
        {
            //Retrieve data from the db.
            String[] columns = {"weather"};
            String where = "_id = (SELECT MAX(_id) FROM weather)";
            Cursor c = theDB.query("weather", columns, where, null, null, null, null);

            c.moveToFirst();

            ((TextView) findViewById(R.id.lblOldWeather)).setText(c.getString(c.getColumnIndex("weather")));

            c.close();
        }
    }

    public void findWeather()
    {
        //this will find the weather. Only called after you have the location.
        if (sharedPref.getBoolean("location_access", true))
        {
            if (location == null)
            {
                Toast.makeText(this, "Please enter in your zip. We can't find your location automatically", Toast.LENGTH_SHORT).show();
            } else
            {
                //Find weather based on the precise location. We'll see how this is done. what api and shit.

                String latitude = Double.toString(location.getLatitude());
                String longitude = Double.toString(location.getLongitude());

                String JSONString =
                        "http://api.wunderground.com/api/" +
                                WUNDERGROUND_API_KEY +
                                "/forecast/q/" +
                                latitude +
                                "," +
                                longitude +
                                ".json";

                findWeatherHelper(JSONString);
            }
        } else //Use the provided zipcode. it will have been parsed in any entry acitivity, so it should be at least 5 digits of integers.
        //Its on the user to make sure that its typed in correctly
        {
            String JSONString =
                    "http://api.wunderground.com/api/" +
                            WUNDERGROUND_API_KEY +
                            "/forecast/q/" +
                            Integer.toString(sharedPref.getInt("zip", 1)) +
                            ".json";
            findWeatherHelper(JSONString);
        }
    }

    //Async method to call down the weather and parse it.
    @SuppressLint("StaticFieldLeak")
    private void findWeatherHelper(String JSONString)
    {
        //Make async call and get json object
        //parse json object
        //Create global variables and save it there.
        new AsyncTask<String, Void, WeatherObject>()
        {
            @Override
            protected void onPostExecute(WeatherObject weatherObject)
            {
                super.onPostExecute(weatherObject);

                MainPage.this.weatherObject = weatherObject;

                ((TextView) findViewById(R.id.lblWeatherLastPulled)).setText(String.format("%s%s", getString(R.string.weather_last_pulled), weatherObject.getTimeWeatherWasPulled()));

                findOutfits();
            }

            @Override
            protected WeatherObject doInBackground(String... strings)
            {
                String wUndergroundURL = strings[0];
                URL url;
                URLConnection urlConnection;

                JSONObject jsonObject;

                WeatherObject weatherObject = new WeatherObject();
                try
                {
                    url = new URL(wUndergroundURL);

                    urlConnection = url.openConnection();
                } catch (IOException e)
                {
                    Log.e("JSON Background Task", "Connection Failed to site " + e.getMessage());
                    return null;
                }

                StringBuilder json = new StringBuilder();
                try
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        json.append(line);
                    }
                    reader.close();
                } catch (Exception e)
                {
                    Log.e("JSON Background Task", "Error converting result " + e.toString());
                    return null;
                }

                try
                {
                    jsonObject = new JSONObject(json.toString());
                } catch (JSONException e)
                {
                    Log.e("JSON Background Task", "Error parsing data " + e.toString());
                    return null;
                }

                try
                {
                    jsonObject = jsonObject.getJSONObject("forecast");

                    //Before going into the specifics for the day, go into the forecast, and pull out the forecast text
                    weatherObject.setForecast(jsonObject.getJSONObject("txt_forecast").getJSONArray("forecastday").getJSONObject(0).getString("fcttext"));

                    jsonObject = jsonObject.getJSONObject("simpleforecast");
                    JSONArray jsonDayArray = jsonObject.getJSONArray("forecastday");
                    jsonObject = jsonDayArray.getJSONObject(0);

                    //At this point we are in the json of today.
                    //Pull and give it to the weatherObject

                    weatherObject.setTempHigh(jsonObject.getJSONObject("high").getInt("fahrenheit"));
                    weatherObject.setTempLow(jsonObject.getJSONObject("low").getInt("fahrenheit"));
                    weatherObject.setHumidityAvg(jsonObject.getInt("avehumidity"));
                    weatherObject.setTimeWeatherWasPulled(jsonObject.getJSONObject("date").getString("pretty"));
                    weatherObject.setWindAvg(jsonObject.getJSONObject("avewind").getInt("mph"));
                    weatherObject.setWindMax(jsonObject.getJSONObject("maxwind").getInt("mph"));
                    weatherObject.setWeatherType(jsonObject.getString("conditions"));

                } catch (JSONException e)
                {
                    Log.e("JSON Background Task", "Error parsing JSON: " + e.getMessage());
                    Log.e("JSON Background Task", url.toString());
                }

                //Put the weather into the database
                ContentValues values = new ContentValues();
                values.put("weather", weatherObject.getForecast());

                theDB.insert("weather", null, values);

                return weatherObject;
            }
        }.execute(JSONString);
    }

    public void findOutfits(View view)
    {
        findOutfits();
    }

    private void findOutfits()
    {
        ((TextView) findViewById(R.id.lblWeatherIntro)).setText(weatherObject.getForecast());
        RadioButton outfitSetWork = findViewById(R.id.rdbWork);
        TextView labelClothes = findViewById(R.id.lblClothes);


        if (sharedPref.getBoolean("male", true))
        {
            if (weatherObject.getTempHigh() < 45)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.mwork1);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.mcasual1);
                    }
                } else // precipitation
                {
                    //work
                    if (outfitSetWork.isChecked())
                    {
                        labelClothes.setText(R.string.mwork2);
                    }
                    //casual
                    else
                    {
                        labelClothes.setText(R.string.mcasual2);
                    }
                }
            }
            if (weatherObject.getTempHigh() < 60)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.mwork3);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.mcasual3);
                    }
                } else // precipitation
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.mwork4);
                    } else
                    //casual
                    {
                        labelClothes.setText(R.string.mcasual4);
                    }
                }

            }
            if (weatherObject.getTempHigh() < 70)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {

                        //work
                        labelClothes.setText(R.string.mwork5);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.mcasual5);
                    }
                } else // precipitation
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.mwork6);
                    } else
                    //casual
                    {
                        labelClothes.setText(R.string.mcasual6);
                    }
                }

            }

            if (weatherObject.getTempHigh() > 70)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {

                        //work
                        labelClothes.setText(R.string.mwork7);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.mcasual7);
                    }
                } else // precipitation
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.mwork8);
                    } else
                    //casual
                    {
                        labelClothes.setText(R.string.mcasual8);
                    }
                }

            }
        } else
        // female options
        {
            if (weatherObject.getTempHigh() < 45)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.fwork1);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.fcasual1);
                    }
                } else // precipitation
                {
                    //work
                    if (outfitSetWork.isChecked())
                    {
                        labelClothes.setText(R.string.fwork2);
                    }
                    //casual
                    else
                    {
                        labelClothes.setText(R.string.fcasual2);
                    }
                }
            }
            if (weatherObject.getTempHigh() < 60)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.fwork3);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.fcasual3);
                    }
                } else // precipitation
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.fwork4);
                    } else
                    //casual
                    {
                        labelClothes.setText(R.string.fcasual4);
                    }
                }

            }
            if (weatherObject.getTempHigh() < 70)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {

                        //work
                        labelClothes.setText(R.string.fwork5);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.fcasual5);
                    }
                } else // precipitation
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.fwork6);
                    } else
                    //casual
                    {
                        labelClothes.setText(R.string.fcasual6);
                    }
                }

            }

            if (weatherObject.getTempHigh() > 70)
            {
                if (weatherObject.getWeatherType().equals("Overcast") ||
                        weatherObject.getWeatherType().equals("Clear") ||
                        weatherObject.getWeatherType().equals("Scattered Clouds") ||
                        weatherObject.getWeatherType().equals("Partly Cloudy") ||
                        weatherObject.getWeatherType().equals("Mostly Cloudy"))
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.fwork7);
                    } else
                    {
                        //casual
                        labelClothes.setText(R.string.fcasual7);
                    }
                } else // precipitation
                {
                    if (outfitSetWork.isChecked())
                    {
                        //work
                        labelClothes.setText(R.string.fwork8);
                    } else
                    //casual
                    {
                        labelClothes.setText(R.string.fcasual8);
                    }
                }

            }

        }
        //This will run as soon as the weather was found
        //Find your outfits here.
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
                        refreshWeatherFromDatabase();
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
}