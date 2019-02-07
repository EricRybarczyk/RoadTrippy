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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.dto.TripLocation;
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

    public FirebaseRecyclerOptions<TripLocation> getDestinationsForTripDay(String tripId, String dayNodeKey) {
        DatabaseReference reference = repository.getDestinationsForTripDay(userId, tripId, dayNodeKey);
        return new FirebaseRecyclerOptions.Builder<TripLocation>()
                .setQuery(reference, TripLocation.class)
                .build();
    }
}
