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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;

import static com.google.common.base.Preconditions.checkNotNull;

public class SettingsFragment extends Fragment implements SettingsContract.View,
                                                DrivingDurationSettingFragment.DrivingDurationPreferenceSaveListener,
                                                HomeLocationSettingFragment.HomeLocationPreferenceSaveListener {

    @BindView(R.id.home_location_preference) protected LinearLayout homeLocationPreference;
    @BindView(R.id.driving_hours_preference) protected LinearLayout drivingHoursPreference;

    private SettingsContract.Presenter presenter;

    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, rootView);

        homeLocationPreference.setOnClickListener(v -> {
            LatLng homeLocation = presenter.getHomeLocationPreference();
            HomeLocationSettingFragment settingFragment;
            if (homeLocation == null) {
                settingFragment = HomeLocationSettingFragment.newInstance();
            } else {
                settingFragment = HomeLocationSettingFragment.newInstance(homeLocation);
            }
            settingFragment.setTargetFragment(SettingsFragment.this, RequestCodes.PREFERENCE_HOME_LOCATION_REQUEST_CODE);
            settingFragment.show(getFragmentManager(), FragmentTags.TAG_SETTING_HOME_LOCATION);
        });
        drivingHoursPreference.setOnClickListener(v -> {
            int defaultHours = Integer.parseInt(getResources().getString(R.string.pref_daily_driving_hours_default));
            DrivingDurationSettingFragment settingFragment = DrivingDurationSettingFragment.newInstance(presenter.getCurrentDrivingDurationPreference(defaultHours));
            settingFragment.setTargetFragment(SettingsFragment.this, RequestCodes.PREFERENCE_DRIVING_HOURS_REQUEST_CODE);
            settingFragment.show(getFragmentManager(), FragmentTags.TAG_SETTING_DRIVING_DURATION);
        });

        return rootView;
    }

    @Override
    public void setPresenter(SettingsContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void onDrivingDurationPreferenceSave(int drivingDuration) {
        this.presenter.saveDrivingDurationPreference(drivingDuration);
    }

    @Override
    public void onHomeLocationPreferenceSave(LatLng homeLocation) {
        this.presenter.saveHomeLocationPreference(homeLocation);
    }
}
