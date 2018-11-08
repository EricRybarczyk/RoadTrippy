package me.ericrybarczyk.roadtrippy.tripaddedit;


import android.content.Context;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

public interface AddEditTripContract {

    interface View extends BaseView<Presenter> {
        void showTripList();
    }

    interface Presenter extends BasePresenter {
        void saveTrip(Context context, TripViewModel tripViewModel, int drivingHoursPreference);
    }
}
