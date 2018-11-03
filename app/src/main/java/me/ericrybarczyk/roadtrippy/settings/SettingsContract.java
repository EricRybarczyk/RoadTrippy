package me.ericrybarczyk.roadtrippy.settings;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface SettingsContract {

    interface View extends BaseView<Presenter> {
        void doSomething();
    }


    interface Presenter extends BasePresenter {
        int getCurrentDrivingDurationPreference();
        void saveDrivingDurationPreference(int drivingDuration);
    }
}
