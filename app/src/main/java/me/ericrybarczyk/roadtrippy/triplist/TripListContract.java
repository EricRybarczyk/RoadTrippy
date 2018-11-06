package me.ericrybarczyk.roadtrippy.triplist;


import com.firebase.ui.database.FirebaseRecyclerOptions;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.dto.Trip;

public interface TripListContract {

    interface View extends BaseView<Presenter> {
        void showCreateTrip();
        void showTripDetail(String tripId, String tripNodeKey);
    }

    interface Presenter extends BasePresenter {
        void createTrip();
        //FirebaseRecyclerOptions<Trip> getTripListDataOptions(String tripListDisplayKey);
    }
}

