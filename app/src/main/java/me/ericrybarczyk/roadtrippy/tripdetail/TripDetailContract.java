package me.ericrybarczyk.roadtrippy.tripdetail;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;

public interface TripDetailContract {

    interface View extends BaseView<Presenter> {
        void displayTrip(Trip trip);
    }

    interface Presenter extends BasePresenter {
        void getTrip(String userId, String tripNodeKey);
        FirebaseRecyclerOptions<TripDay> getTripDayDataOptions(String tripId);
    }
}
