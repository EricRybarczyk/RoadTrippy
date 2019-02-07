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
    DatabaseReference getArchivedTrip(String userId, String tripNodeKey);
    DatabaseReference getDestinationsForTripDay(String userId, String tripId, String dayNodeKey);
    void saveTrip(Trip trip, List<TripDay> tripDays);
    void updateTripDayHighlight(String userId, String tripId, String dayNodeKey, boolean isHighlight);
    void updateTripDay(String userId, String tripId, String dayNodeKey, TripDay tripDay);
    void archiveTrip(String userId, String tripNodeKey);
    void removeTripDayDestination(String userId, String tripId, String dayNodeKey, int destinationIndex);
    void saveUserInfo(FirebaseUser firebaseUser);
}
