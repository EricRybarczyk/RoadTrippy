package me.ericrybarczyk.roadtrippy.settings;

import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;

import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;

import static com.google.common.base.Preconditions.checkNotNull;

public class SettingsPresenter implements SettingsContract.Presenter {

    private FirebaseUser firebaseUser;
    private SharedPreferences sharedPreferences;
    private SettingsContract.View settingsView;


    public SettingsPresenter(FirebaseUser firebaseUser, SharedPreferences sharedPreferences, SettingsContract.View view) {
        this.firebaseUser = checkNotNull(firebaseUser);
        this.sharedPreferences = checkNotNull(sharedPreferences);
        this.settingsView = view;

        this.settingsView.setPresenter(this);
    }

    @Override
    public int getCurrentDrivingDurationPreference() {
        int defaultHours = 12;
        return sharedPreferences.getInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE, defaultHours);
    }

    @Override
    public void saveDrivingDurationPreference(int drivingDuration) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE, drivingDuration);
        editor.apply();
    }

    @Override
    public void start() {

    }
}
