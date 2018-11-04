package me.ericrybarczyk.roadtrippy.triplist;

import android.support.annotation.NonNull;
import com.google.firebase.auth.FirebaseUser;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import static com.google.common.base.Preconditions.checkNotNull;

public class TripListPresenter implements TripListContract.Presenter {

    private TripDataSource tripDataSource;
    private TripListContract.View tripListView;


    public TripListPresenter(@NonNull TripDataSource dataSource, @NonNull TripListContract.View tripListView) {
        this.tripDataSource = checkNotNull(dataSource);
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
