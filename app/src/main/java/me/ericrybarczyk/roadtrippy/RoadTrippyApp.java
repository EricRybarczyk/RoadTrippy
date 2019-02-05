package me.ericrybarczyk.roadtrippy;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.threetenabp.AndroidThreeTen;

import timber.log.Timber;

public class RoadTrippyApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(); // plant an empty facade Tree
        }

        // call setPersistenceEnabled() in Application class: https://stackoverflow.com/a/37766261/798642
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
