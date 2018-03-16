package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainPage extends AppCompatActivity {

    private String name;
    private boolean locationAccess;
    private Integer zip;
    private boolean maleGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        //Create a callback onclick for the picture that will refresh the picture maybe? Use the dialog box to confirm this.


        //Retrieve data from the intent that launched this activity.
        Intent intentFromLaunch = getIntent();
        name = intentFromLaunch.getStringExtra("name");
        locationAccess = intentFromLaunch.getBooleanExtra("location", false);
        zip = intentFromLaunch.getIntExtra("zipcode", 0);
        maleGender = intentFromLaunch.getBooleanExtra("male", true);

        //Get location

        //Get weather based on location

        //Talk about what sort of stuff they need to do.
    }

    public void onAboutLaunchClick(View view)
    {
        startActivity(new Intent(this, Acknowledgments.class));
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
