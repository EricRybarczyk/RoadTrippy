package me.ericrybarczyk.roadtrippy.settings;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.triplist.TripListContract;

public interface SettingsContract {

    interface View extends BaseView<TripListContract.Presenter> {
        void doSomething();
    }


    interface Presenter extends BasePresenter {
        void doSomething();
    }
}
