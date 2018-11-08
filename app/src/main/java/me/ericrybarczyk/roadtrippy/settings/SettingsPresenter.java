package me.ericrybarczyk.roadtrippy.settings;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

import static com.google.common.base.Preconditions.checkNotNull;

public class SettingsPresenter implements SettingsContract.Presenter {

    private FirebaseUser firebaseUser;
    private SharedPreferences sharedPreferences;
    private SettingsContract.View settingsView;


    public SettingsPresenter(SharedPreferences sharedPreferences, SettingsContract.View view) {
        this.firebaseUser = AuthenticationManager.getCurrentUser();
        this.sharedPreferences = checkNotNull(sharedPreferences);
        this.settingsView = view;

        this.settingsView.setPresenter(this);
    }

    @Override
    public int getCurrentDrivingDurationPreference(int defaultHours) {
        return sharedPreferences.getInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE, defaultHours);
    }

    @Override
    public void saveDrivingDurationPreference(int drivingDuration) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE, drivingDuration);
        editor.apply();
    }

    @Override
    public void saveHomeLocationPreference(LatLng homeLocation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(ArgumentKeys.KEY_HOME_LOCATION_LATITUDE_PREFERENCE, (float)homeLocation.latitude);
        editor.putFloat(ArgumentKeys.KEY_HOME_LOCATION_LONGITUDE_PREFERENCE, (float)homeLocation.longitude);
        editor.apply();
    }

    @Override
    public LatLng getHomeLocationPreference() {
        if (sharedPreferences.contains(ArgumentKeys.KEY_HOME_LOCATION_LATITUDE_PREFERENCE)) {
            return new LatLng(
                    (double) sharedPreferences.getFloat(ArgumentKeys.KEY_HOME_LOCATION_LATITUDE_PREFERENCE, 0.0f),
                    (double) sharedPreferences.getFloat(ArgumentKeys.KEY_HOME_LOCATION_LONGITUDE_PREFERENCE, 0.0f)
            );
        }
        return null;
    }

    @Override
    public void start() {

    }
}
