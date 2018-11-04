package me.ericrybarczyk.roadtrippy.tripday;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.viewmodels.TripDayViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripDayPresenter implements TripDayContract.Presenter {

    private TripDataSource tripDataSource;
    private TripDayContract.View tripDayView;
    public static final String TAG = TripDayPresenter.class.getSimpleName();

    public TripDayPresenter(@NonNull TripDataSource dataSource, @NonNull TripDayContract.View view) {
        tripDataSource = checkNotNull(dataSource);
        tripDayView = checkNotNull(view);

        view.setPresenter(this);
    }

    @Override
    public void getTripDay(String userId, String tripId, String dayNodeKey) {
        DatabaseReference tripDayReference = tripDataSource.getTripDay(userId, tripId, dayNodeKey);
        tripDayReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tripDayView.displayTripDay(dataSnapshot.getValue(TripDay.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                throw databaseError.toException();
            }
        });
    }

    @Override
    public void getTripDestination(String userId, String tripNodeKey) {
        DatabaseReference tripReference = tripDataSource.getTrip(userId, tripNodeKey);
        tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip == null) {
                    return;
                }
                tripDayView.setTripDestination(new LatLng(trip.getDestinationLatitude(), trip.getDestinationLongitude()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                throw databaseError.toException();
            }
        });
    }

    @Override
    public void updateTripDay(String userId, String tripId, String dayNodeKey, TripDayViewModel tripDayViewModel) {
        tripDataSource.updateTripDay(userId, tripId, dayNodeKey, tripDayViewModel.asTripDay());
    }

    @Override
    public void updateTripDayHighlight(String userId, String tripId, String dayNodeKey, boolean isHighlight) {
        tripDataSource.updateTripDayHighlight(userId, tripId, dayNodeKey, isHighlight);
    }

    @Override
    public void start() {

    }
}
