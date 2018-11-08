package me.ericrybarczyk.roadtrippy.settings;

import com.google.android.gms.maps.model.LatLng;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface SettingsContract {

    interface View extends BaseView<Presenter> {
    }


    interface Presenter extends BasePresenter {
        int getCurrentDrivingDurationPreference(int defaultDuration);
        void saveDrivingDurationPreference(int drivingDuration);
        void saveHomeLocationPreference(LatLng homeLocation);
        LatLng getHomeLocationPreference();
    }
}
