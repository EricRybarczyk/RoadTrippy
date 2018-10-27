package me.ericrybarczyk.roadtrippy.persistence;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;

public interface TripDataSource {
    DatabaseReference getTripList(String userId);
    DatabaseReference getArchivedTripList(String userId);
    DatabaseReference getTripDaysList(String userId, String tripId);
    DatabaseReference getTripDay(String userId, String tripId, String dayNodeKey);
    DatabaseReference getTrip(String userId, String tripNodeKey);
    DatabaseReference getDestinationsForTripDay(String userId, String tripId, String dayNodeKey);
    void saveTrip(Trip trip, List<TripDay> tripDays);
    void updateTripDayHighlight(String userId, String tripId, String dayNodeKey, boolean isHighlight);
    void updateTripDay(String userId, String tripId, String dayNodeKey, TripDay tripDay);
    void archiveFinishedTrips(String userId);
    void archiveTrip(String userId, String tripNodeKey);
    void removeTripDayDestination(String userId, String tripId, String dayNodeKey, int destinationIndex);
    void saveUserInfo(FirebaseUser firebaseUser);
}
