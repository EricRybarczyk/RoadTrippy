package me.ericrybarczyk.roadtrippy.triplist;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface TripListContract {

    interface View extends BaseView<Presenter> {
        void showCreateTrip();
        void showTripDetail(String tripId, String tripNodeKey);
        void showArchivedTripList();
        void showDefaultTripList();
    }

    interface Presenter extends BasePresenter {
        void createTrip();
    }
}

