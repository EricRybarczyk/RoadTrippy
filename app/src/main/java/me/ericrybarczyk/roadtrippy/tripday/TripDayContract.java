package me.ericrybarczyk.roadtrippy.tripday;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.dto.TripDay;

public interface TripDayContract {

    interface View extends BaseView<Presenter> {
        void displayTripDay(TripDay tripDay);
    }

    interface Presenter extends BasePresenter {
        void getTripDay(String userId, String tripId, String dayNodeKey);
    }

}
