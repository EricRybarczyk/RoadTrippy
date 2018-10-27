package me.ericrybarczyk.roadtrippy;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class RoadTrippyApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        // call setPersistenceEnabled() in Application class: https://stackoverflow.com/a/37766261/798642
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
