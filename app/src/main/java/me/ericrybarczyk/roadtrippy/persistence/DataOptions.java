package me.ericrybarczyk.roadtrippy.persistence;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;

public class DataOptions {
    private TripRepository repository;
    private String userId;

    public DataOptions(String userId) {
        repository = new TripRepository();
        this.userId = userId;
    }

    public FirebaseRecyclerOptions<Trip> getTripListDataOptions(String tripListDisplayKey) {
        DatabaseReference reference;
        if (tripListDisplayKey.equals(ArgumentKeys.TRIP_LIST_DISPLAY_ARCHIVE_INDICATOR)) {
            reference = repository.getArchivedTripList(this.userId);
        } else {
            reference = repository.getTripList(this.userId);
        }
        return new FirebaseRecyclerOptions.Builder<Trip>()
                .setQuery(reference, Trip.class)
                .build();
    }

    public FirebaseRecyclerOptions<TripDay> getTripDayDataOptions(String tripId) {
        DatabaseReference reference = repository.getTripDaysList(userId, tripId);
        return new FirebaseRecyclerOptions.Builder<TripDay>()
                .setQuery(reference, TripDay.class)
                .build();
    }
}
