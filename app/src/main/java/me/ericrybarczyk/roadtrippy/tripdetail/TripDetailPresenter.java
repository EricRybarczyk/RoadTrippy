package me.ericrybarczyk.roadtrippy.tripdetail;

import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.persistence.DataOptions;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripDetailPresenter implements TripDetailContract.Presenter {

    private TripDataSource tripDataSource;
    private TripDetailContract.View tripDetailView;
    private static final String TAG = TripDetailPresenter.class.getSimpleName();

    public TripDetailPresenter(@NonNull TripDataSource dataSource, @NonNull TripDetailContract.View view) {
        this.tripDataSource = checkNotNull(dataSource);
        this.tripDetailView = checkNotNull(view);

        this.tripDetailView.setPresenter(this);
    }

    @Override
    public void getTrip(String userId, String tripNodeKey, boolean tripIsArchived) {
        DatabaseReference tripReference;
        if (tripIsArchived) {
            tripReference = tripDataSource.getArchivedTrip(userId, tripNodeKey);
        } else {
            tripReference = tripDataSource.getTrip(userId, tripNodeKey);
        }

        tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tripDetailView.displayTrip(dataSnapshot.getValue(Trip.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                throw databaseError.toException();
            }
        });
    }

    @Override
    public void archiveTrip(String userId, String tripNodeKey) {
        tripDataSource.archiveTrip(userId, tripNodeKey);
    }

    @Override
    public void start() {

    }
}
