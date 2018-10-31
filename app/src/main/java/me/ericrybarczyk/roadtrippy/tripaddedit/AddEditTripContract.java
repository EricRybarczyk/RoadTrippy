package me.ericrybarczyk.roadtrippy.tripaddedit;


import android.content.Context;
import android.graphics.Bitmap;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface AddEditTripContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        void saveMapSnapshotImage(Context context, Bitmap bitmap, String tripId);
        //void saveTripName();
    }
}
