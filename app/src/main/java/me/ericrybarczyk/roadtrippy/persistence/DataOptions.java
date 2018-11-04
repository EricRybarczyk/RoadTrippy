package me.ericrybarczyk.roadtrippy.persistence;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;

public class DataOptions {
    private TripRepository repository;
    private String userId;

    public DataOptions(String userId) {
        repository = new TripRepository();
        this.userId = userId;
    }

    public FirebaseRecyclerOptions<Trip> getTripListDataOptions() {
        DatabaseReference reference = repository.getTripList(this.userId);
        FirebaseRecyclerOptions<Trip> options = new FirebaseRecyclerOptions.Builder<Trip>()
                .setQuery(reference, Trip.class)
                .build();
        return options;
    }

    public FirebaseRecyclerOptions<TripDay> getTripDayDataOptions(String tripId) {
        DatabaseReference reference = repository.getTripDaysList(userId, tripId);
        FirebaseRecyclerOptions<TripDay> options = new FirebaseRecyclerOptions.Builder<TripDay>()
                .setQuery(reference, TripDay.class)
                .build();
        return options;
    }
}
