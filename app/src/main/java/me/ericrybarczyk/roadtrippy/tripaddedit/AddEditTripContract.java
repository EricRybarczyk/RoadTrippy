package me.ericrybarczyk.roadtrippy.tripaddedit;


import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface AddEditTripContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        //void saveTripName();
    }
}
