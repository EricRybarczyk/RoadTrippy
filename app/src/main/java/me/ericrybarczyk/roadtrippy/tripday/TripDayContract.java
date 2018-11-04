package me.ericrybarczyk.roadtrippy.tripday;

import com.google.android.gms.maps.model.LatLng;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.viewmodels.TripDayViewModel;

public interface TripDayContract {

    interface View extends BaseView<Presenter> {
        void displayTripDay(TripDay tripDay);
        void setTripDestination(LatLng tripDestination);
        void showTripDetail();
    }

    interface Presenter extends BasePresenter {
        void getTripDay(String userId, String tripId, String dayNodeKey);
        void getTripDestination(String userId, String tripNodeKey);
        void updateTripDay(String userId, String tripId, String dayNodeKey, TripDayViewModel tripDayViewModel);
        void updateTripDayHighlight(String userId, String tripId, String dayNodeKey, boolean isHighlight);
    }

}
