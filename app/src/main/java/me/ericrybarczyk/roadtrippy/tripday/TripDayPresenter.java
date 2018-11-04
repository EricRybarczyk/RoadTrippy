package me.ericrybarczyk.roadtrippy.tripday;

import android.support.annotation.NonNull;

import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripDayPresenter implements TripDayContract.Presenter {

    private TripDataSource tripDataSource;
    private TripDayContract.View tripDayView;

    public TripDayPresenter(@NonNull TripDataSource dataSource, @NonNull TripDayContract.View view) {
        tripDataSource = checkNotNull(dataSource);
        tripDayView = checkNotNull(view);

        view.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
