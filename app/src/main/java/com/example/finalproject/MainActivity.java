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

                        enableLocation();
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
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

    public void launchNextActivity(View view)
    {
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
        intent.putExtra("name", txtName.getText());

        if (txtZipcode.getText().toString().equals(""))
        {
            //We also need to add a check to see if its a 5 digit number and have it fail if its not.
            Toast.makeText(this, "Please enter a valid zipcode", Toast.LENGTH_LONG).show();
            return;
        } else
        {
            //Add zipcode
            intent.putExtra("zipcode", Integer.parseInt(txtZipcode.getText().toString()));
        }

        //Add a flag if they enable location of not yet. We will see if this stays.  <--- Its staying
        intent.putExtra("location", chkLocation.isChecked());

        //Add gender. True == male, false == female
        intent.putExtra("male", rdbMale.isChecked());

        //Eventually will start the new activity
        startActivity(intent);
    }
}


