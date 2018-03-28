package com.example.finalproject;

//    tools:context="com.example.finalproject.FirstLaunchSetup"

//Location and permission information were pulled from the Android Dev training page

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class FirstLaunchSetup extends AppCompatActivity
{
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    public static final int RESULT_SETUP_COMPLETE = 10;

    SQLiteDatabase theDB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch_setup);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //Create a custom callback for adding the location permission in the check box widget.
        CheckBox chkLocation = findViewById(R.id.chkUseLocation);
        chkLocation.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        enableLocation();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
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

    }

    public void enableLocation()
    {
        //If the box is now checked, disable the zipcode box, as they wont need it.
        //If the box is being unchecked, reenable the zipcode box, because now they need to manually type it in.
        CheckBox chk = findViewById(R.id.chkUseLocation);
        if (chk.isChecked())
        {
            findViewById(R.id.txtZipcode).setEnabled(false);
        } else
        {
            findViewById(R.id.txtZipcode).setEnabled(true);
        }

        //Make the call to enable the location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
    }

    //This method is a callback from calling the permission pop up. Once they either accept or decline it the permission, this method is called.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        //Request code is the code that we hard coded into the request to ask for the permission.
        //The constant. This is used so we can do something if they hit yes or no.
        //What we do is pop a toast if it was enabled and disable the text box, or pop a toast saying the opposite and reenable it.
        switch (requestCode)
        {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
            {
                //If the permissions request was denied, reenable the zipcode text box and then pop a toast stating it will not use location
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //Once finished, display a toast saying successful
                    Toast.makeText(getApplicationContext(), "Location Allowed...", Toast.LENGTH_LONG).show();
                    findViewById(R.id.txtZipcode).setEnabled(false);
                } else if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED)
                {
                    Toast.makeText(getApplicationContext(), "Location NOT Allowed...", Toast.LENGTH_LONG).show();
                    findViewById(R.id.txtZipcode).setEnabled(true);
                }

                break;
            }
        }
    }

    /*
    public void onClick(final View view)
    {
        final AlertDialog alertDialog = new AlertDialog.Builder(FirstLaunchSetup.this).create();
        alertDialog.setTitle("Not working yet");
        alertDialog.setMessage("This app is not configured to pull weather data yet. This is unfinished");
        // Alert dialog button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Continue Anyway",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // user chose to continue, move to next activity
                        launchNextActivity(view);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // user cancels action, pop up box is removed
                        alertDialog.dismiss();
                    }
                });
        alertDialog.show();
    }
    */

    public void launchNextActivity(View view)
    {
        if (theDB == null)
        {
            Toast.makeText(this, "Try again in a few seconds.", Toast.LENGTH_SHORT).show();
        } else
        {
            ContentValues values = new ContentValues();

            Intent intent = new Intent(this, MainPage.class);

            //Create references to the different widgets to pull information
            EditText txtName = findViewById(R.id.txtName);
            CheckBox chkLocation = findViewById(R.id.chkUseLocation);
            EditText txtZipcode = findViewById(R.id.txtZipcode);
            RadioButton rdbMale = findViewById(R.id.rdbMale);

            //Add the information to the intent for now. Will maybe replace this with the lasting database.
            //Hopefully

            //If they did not enter a name, warn them and dont let them continue
            if (txtName.getText().toString().equals(""))
            {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
                return;
            }
            //Add name
            values.put("name", txtName.getText().toString());

            if (!chkLocation.isChecked())
            {
                if (txtZipcode.getText().toString().equals(""))
                {
                    //We also need to add a check to see if its a 5 digit number and have it fail if its not.
                    Toast.makeText(this, "Please enter a valid zipcode", Toast.LENGTH_LONG).show();
                    return;
                } else
                {
                    //Add zipcode
                    values.put("zip", txtZipcode.getText().toString());
                }
            }

            //Add a flag if they enable location of not yet. We will see if this stays.  <--- Its staying
            if (chkLocation.isChecked())
                values.put("location_access", "true");
            else
                values.put("location_access", "false");

            //Add gender. True == male, false == female
            if (rdbMale.isChecked())
                values.put("gender", "male");
            else
                values.put("gender", "female");

            //Eventually will start the new activity
            setResult(RESULT_SETUP_COMPLETE);
            finish();
        }
    }


}


