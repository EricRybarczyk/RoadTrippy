package me.ericrybarczyk.roadtrippy.triplist;

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripListPresenter implements TripListContract.Presenter {

    private TripListContract.View tripListView;


    public TripListPresenter(@NonNull TripListContract.View tripListView) {
        this.tripListView = checkNotNull(tripListView);

        this.tripListView.setPresenter(this);
    }

    @Override
    public void createTrip() {
        tripListView.showCreateTrip();
    }

    @Override
    public void start() {

    }
}
