package me.ericrybarczyk.roadtrippy;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class BaseActivity extends AppCompatActivity {

    protected void logScreenInfo(String tag) {
        Configuration configuration = getResources().getConfiguration();
        Log.d(tag, "Screen Orientation: " + configuration.orientation);
        Log.d(tag, "Screen Density: " + configuration.densityDpi);
        Log.d(tag, "Screen Height: " + configuration.screenHeightDp);
        Log.d(tag, "Screen Width: " + configuration.screenWidthDp);
        Log.d(tag, "Screen Smallest Width: " + configuration.smallestScreenWidthDp);
    }
}
