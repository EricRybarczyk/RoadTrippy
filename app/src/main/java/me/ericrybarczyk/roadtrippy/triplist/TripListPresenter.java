package me.ericrybarczyk.roadtrippy.triplist;

import android.support.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripListPresenter implements TripListContract.Presenter {

    private TripDataSource tripDataSource;
    private TripListContract.View tripListView;
    private FirebaseUser firebaseUser;


    public TripListPresenter(@NonNull TripDataSource dataSource, @NonNull TripListContract.View tripListView, @NonNull FirebaseUser firebaseUser) {
        this.tripDataSource = checkNotNull(dataSource);
        this.tripListView = checkNotNull(tripListView);
        this.firebaseUser = checkNotNull(firebaseUser);
    }

    @Override
    public FirebaseRecyclerOptions<Trip> getTripListDataOptions() {
        DatabaseReference reference = tripDataSource.getTripList(firebaseUser.getUid());
        FirebaseRecyclerOptions<Trip> options = new FirebaseRecyclerOptions.Builder<Trip>()
                .setQuery(reference, Trip.class)
                .build();
        return options;
    }

    @Override
    public void loadTripList() {

    }

    @Override
    public void start() {

    }
}
