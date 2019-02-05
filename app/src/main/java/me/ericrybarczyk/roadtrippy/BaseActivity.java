package me.ericrybarczyk.roadtrippy;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {

    protected void logScreenInfo(String tag) {
        Configuration configuration = getResources().getConfiguration();
        Timber.tag(tag).d("Screen Orientation: %d", configuration.orientation);
        Timber.tag(tag).d("Screen Density: %d", configuration.densityDpi);
        Timber.tag(tag).d("Screen Height: %d", configuration.screenHeightDp);
        Timber.tag(tag).d("Screen Width: %d", configuration.screenWidthDp);
        Timber.tag(tag).d("Screen Smallest Width: %d", configuration.smallestScreenWidthDp);
    }
}
