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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, rootView);

        homeLocationPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng homeLocation = presenter.getHomeLocationPreference();
                HomeLocationSettingFragment settingFragment;
                if (homeLocation == null) {
                    settingFragment = HomeLocationSettingFragment.newInstance();
                } else {
                    settingFragment = HomeLocationSettingFragment.newInstance(homeLocation);
                }
                settingFragment.setTargetFragment(SettingsFragment.this, RequestCodes.PREFERENCE_HOME_LOCATION_REQUEST_CODE);
                settingFragment.show(getFragmentManager(), FragmentTags.TAG_SETTING_HOME_LOCATION);
            }
        });
        drivingHoursPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrivingDurationSettingFragment settingFragment = DrivingDurationSettingFragment.newInstance(presenter.getCurrentDrivingDurationPreference());
                settingFragment.setTargetFragment(SettingsFragment.this, RequestCodes.PREFERENCE_DRIVING_HOURS_REQUEST_CODE);
                settingFragment.show(getFragmentManager(), FragmentTags.TAG_SETTING_DRIVING_DURATION);
            }
        });

        return rootView;
    }

    @Override
    public void doSomething() {
        
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
