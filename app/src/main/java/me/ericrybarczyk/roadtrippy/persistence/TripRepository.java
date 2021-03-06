/*
 * Copyright (C) 2018 Eric Rybarczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.ericrybarczyk.roadtrippy.persistence;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.dto.TripLocation;
import me.ericrybarczyk.roadtrippy.dto.User;
import timber.log.Timber;

public class TripRepository implements TripDataSource {

    private FirebaseDatabase firebaseDatabase;

    public TripRepository() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }


    @Override
    public void saveTrip(Trip trip, List<TripDay> tripDays) {
        try {
            DatabaseReference tripsDatabaseReference = firebaseDatabase.getReference()
                    .child(DatabasePaths.BASE_PATH_TRIPS + trip.getUserId());

            // get the pushId to use in path when storing the child TripDay records
            String tripPushId = tripsDatabaseReference.push().getKey();

            // save the Trip object under the pushId
            tripsDatabaseReference.child(Objects.requireNonNull(tripPushId)).setValue(trip);

            // build a path for the TripDay child objects so they can be associated with the saved Trip object
            DatabaseReference daysDatabaseReference = firebaseDatabase.getReference()
                    .child(DatabasePaths.BASE_PATH_TRIPDAYS + trip.getUserId() + "/" + trip.getTripId());

            // save all TripDay objects
            for (TripDay day : tripDays) {
                day.setTripNodeKey(tripPushId);
                daysDatabaseReference.push().setValue(day);
            }

        } catch (Exception e) {
            Timber.e("Firebase Exception: %s", e.getMessage());
            throw e;
        }
    }

    @Override
    public DatabaseReference getTripList(String userId) {
        DatabaseReference reference = firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_TRIPS + userId + "/");
        reference.orderByChild(DatabasePaths.KEY_TRIP_LIST_DEFAULT_SORT);
        return reference;
    }

    @Override
    public DatabaseReference getArchivedTripList(String userId) {
        DatabaseReference reference = firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_ARCHIVE + userId + "/");
        reference.orderByChild(DatabasePaths.KEY_TRIP_LIST_DEFAULT_SORT);
        return reference;
    }

    @Override
    public DatabaseReference getTripDaysList(String userId, String tripId) {
        return firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_TRIPDAYS + userId + "/" + tripId);
    }

    @Override
    public DatabaseReference getTripDay(String userId, String tripId, String dayNodeKey) {
        return firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_TRIPDAYS + userId + "/" + tripId + "/" + dayNodeKey);
    }

    @Override
    public DatabaseReference getTrip(String userId, String tripNodeKey) {
        return firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_TRIPS + userId + "/" + tripNodeKey);
    }

    @Override
    public DatabaseReference getArchivedTrip(String userId, String tripNodeKey) {
        return firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_ARCHIVE + userId + "/" + tripNodeKey);
    }

    @Override
    public DatabaseReference getDestinationsForTripDay(String userId, String tripId, String dayNodeKey) {
        return firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_TRIPDAYS + userId + "/" + tripId + "/"
                + dayNodeKey + "/" + DatabasePaths.KEY_TRIPDAY_DESTINATIONS_CHILD);
    }

    @Override
    public void updateTripDayHighlight(String userId, String tripId, String dayNodeKey, boolean isHighlight) {
        DatabaseReference reference = getTripDay(userId, tripId, dayNodeKey);
        reference.child(DatabasePaths.KEY_TRIPDAY_HIGHLIGHT_CHILD).setValue(isHighlight);
    }

    @Override
    public void updateTripDay(String userId, String tripId, String dayNodeKey, TripDay tripDay) {
        DatabaseReference reference = getTripDay(userId, tripId, dayNodeKey);
        reference.setValue(tripDay);
    }

    @Override
    public void archiveTrip(String userId, String tripNodeKey) {
        DatabaseReference reference = getTrip(userId, tripNodeKey);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip != null) {
                    archiveTrip(userId, tripNodeKey, trip);
                    deleteActiveTrip(userId, tripNodeKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.getMessage());
            }
        });
    }

    private void archiveTrip(String userId, String tripNodeKey, Trip trip) {
        DatabaseReference reference = firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_ARCHIVE + userId);
        trip.setIsArchived(true);
        reference.child(tripNodeKey).setValue(trip);
    }

    private void deleteActiveTrip(String userId, String tripNodeKey) {
        DatabaseReference reference = getTrip(userId, tripNodeKey);
        reference.removeValue();
    }

    @Override
    public void removeTripDayDestination(String userId, String tripId, String dayNodeKey, int destinationIndex) {
        // to maintain the destinations with expected array-like indexing, need to re-save the list after item removal.
        // Using ArrayList to avoid manually re-indexing.
        DatabaseReference reference = getTripDay(userId, tripId, dayNodeKey)
                .child(DatabasePaths.KEY_TRIPDAY_DESTINATIONS_CHILD); // + "/" + destinationIndex);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<TripLocation> destinations = new ArrayList<>();
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    destinations.add(item.getValue(TripLocation.class));
                }
                destinations.remove(destinationIndex);
                reference.setValue(destinations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e("Firebase DatabaseError: %s", databaseError.getMessage());
            }
        });
    }

    @Override
    public void saveUserInfo(FirebaseUser firebaseUser) {
        DatabaseReference reference = firebaseDatabase.getReference().child(DatabasePaths.BASE_PATH_USERS + firebaseUser.getUid());

        // see if user already exists
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String now = PersistenceFormats.toDateTimeString(LocalDateTime.now());
                User existingUser = dataSnapshot.getValue(User.class);
                User user;
                if (existingUser != null) {
                    // update existing user
                    user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail(), existingUser.getCreateDate(), now);
                } else {
                    // save new user
                    user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail(), now, now);
                }
                Task<Void> result = reference.setValue(user);
                Timber.i("save user operation success = %s", String.valueOf(result.isSuccessful()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.getMessage());
            }
        });
    }
}
