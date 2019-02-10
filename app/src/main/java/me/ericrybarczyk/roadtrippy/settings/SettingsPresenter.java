/*
 * Copyright (C) 2018 Eric Rybarczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.ericrybarczyk.roadtrippy.settings;

import android.content.SharedPreferences;
import com.google.android.gms.maps.model.LatLng;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import static com.google.common.base.Preconditions.checkNotNull;

public class SettingsPresenter implements SettingsContract.Presenter {

    private SharedPreferences sharedPreferences;


    SettingsPresenter(SharedPreferences sharedPreferences, SettingsContract.View view) {
        this.sharedPreferences = checkNotNull(sharedPreferences);
        view.setPresenter(this);
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
