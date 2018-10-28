package me.ericrybarczyk.roadtrippy.triplist;


import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface TripListContract {
    interface View extends BaseView<Presenter> {
        void showCreateTrip();
    }


    interface Presenter extends BasePresenter {
        void createTrip();
    }
}

