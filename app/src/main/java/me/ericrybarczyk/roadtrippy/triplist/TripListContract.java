package me.ericrybarczyk.roadtrippy.triplist;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.dto.Trip;

public interface TripListContract {
    interface View extends BaseView<Presenter> {
        void showTripList();
    }


    interface Presenter extends BasePresenter {
        FirebaseRecyclerOptions<Trip> getTripListDataOptions();
        void loadTripList();
    }
}

