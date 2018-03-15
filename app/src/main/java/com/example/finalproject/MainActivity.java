package com.example.finalproject;

//    tools:context="com.example.finalproject.MainActivity"

//Location and permission information were pulled from the Android Dev training page

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create a custom callback for added the location permission in the check box widget.
        CheckBox chkLocation = findViewById(R.id.chkUseLocation);
        chkLocation.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Log.i("enable location","Made it to onclick");
                        enableLocation();
                    }
                });
    }

    public void enableLocation()
    {
        Log.i("enable location","outside the if");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.i("enable location","inside the if");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            Log.i("enable location","ending if");
        }
        Log.i("enable location","finsihed if");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        Log.i("enable location","callback");
        switch (requestCode)
        {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
            {
                Log.i("enable location","inside the case");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.i("enable location","inside the if and making toast");
                    //Once finished, display a toast saying successful
                    Toast toast = Toast.makeText(getApplicationContext(), "Location Enabled...", Toast.LENGTH_LONG);
                    toast.show();
                    Log.i("enable location","toast should be live");
                }
                return;
            }
        }
    }

    public void launchNextActivity()
    {
        Intent intent = new Intent(this, MainPage.class);

        //Create references to the different widgets to pull information
        EditText txtName = findViewById(R.id.txtEditName);
        CheckBox chkLocation = findViewById(R.id.chkUseLocation);
        EditText txtZipcode = findViewById(R.id.txtZipcode);
        RadioButton rdbMale = findViewById(R.id.rdbMale);

        //Add the information to the intent for now. Will maybe replace this with the lasting database.
        //Hopefully

        //Add name
        intent.putExtra("name", txtName.getText());

        //Add zipcode
        intent.putExtra("zipcode", Integer.parseInt(txtZipcode.getText().toString()));

        //Add a flag if they enable location of not yet. We will see if this stays.
        intent.putExtra("location", chkLocation.isChecked());

        //Add gender. True == male, false == female
        intent.putExtra("male", rdbMale.isChecked());

        //Eventually will start the new activity
        startActivity(intent);
    }


    }


