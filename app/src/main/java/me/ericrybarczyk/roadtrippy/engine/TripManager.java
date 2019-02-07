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

package me.ericrybarczyk.roadtrippy.engine;

import android.content.Context;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.dto.TripLocation;
import me.ericrybarczyk.roadtrippy.persistence.PersistenceFormats;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

public class TripManager {

    public TripManager() {
    }

    public Trip buildTrip(TripViewModel tripViewModel, String userId) {
        Trip trip = new Trip();

        trip.setTripId(tripViewModel.getTripId());
        trip.setUserId(userId);
        trip.setDescription(tripViewModel.getDescription());

        trip.setOriginLatitude(tripViewModel.getOriginLatLng().latitude);
        trip.setOriginLongitude(tripViewModel.getOriginLatLng().longitude);
        trip.setOriginDescription(tripViewModel.getOriginDescription());

        trip.setDestinationLatitude(tripViewModel.getDestinationLatLng().latitude);
        trip.setDestinationLongitude(tripViewModel.getDestinationLatLng().longitude);
        trip.setDestinationDescription(tripViewModel.getDestinationDescription());

        trip.setIncludeReturn(tripViewModel.isIncludeReturn());
        trip.setIsArchived(false);

        trip.setDepartureDate(PersistenceFormats.toDateString(tripViewModel.getDepartureDate()));
        trip.setReturnDate(PersistenceFormats.toDateString(tripViewModel.getReturnDate()));

        trip.setDurationMinutes(tripViewModel.getDurationMinutes());

        String rightNow = PersistenceFormats.toDateString(LocalDate.now());
        trip.setCreateDate(rightNow);
        trip.setModifiedDate(rightNow);

        return trip;
    }

    public List<TripDay> buildInitialTripDays(Context context, TripViewModel tripViewModel, int prefDrivingDuration) {
        // build the list of trip days based on DepartureDate and ReturnDate (inclusive)

        LocalDateTime start = LocalDateTime.of(
                tripViewModel.getDepartureDate().getYear(),
                tripViewModel.getDepartureDate().getMonthValue(),
                tripViewModel.getDepartureDate().getDayOfMonth(),
                0, 0, 0);

        LocalDateTime end = LocalDateTime.of(
                tripViewModel.getReturnDate().getYear(),
                tripViewModel.getReturnDate().getMonthValue(),
                tripViewModel.getReturnDate().getDayOfMonth(),
                23,59,59);

        Duration duration = Duration.between(start, end);
        int numberOfTripDays = Math.abs((int)duration.toDays()) + 1; // add one to make it inclusive of last day

        ArrayList<TripDay> tripDays = new ArrayList<>();

        int daysOfDriving = getDaysOfDriving(tripViewModel.getDurationMinutes(), prefDrivingDuration);

        // zero-based loop adjusts for fact that numberOfTripDays is not inclusive of last day
        for (int day = 0; day < numberOfTripDays; day++) {
            TripDay td = new TripDay();
            td.setTripId(tripViewModel.getTripId());
            int tripDayNumber = day + 1; // 1-based for display purposes
            td.setDayNumber(tripDayNumber);

            if (isBeginDrivingDay(tripDayNumber, daysOfDriving) || isReturnDrivingDay(tripDayNumber, daysOfDriving, numberOfTripDays, tripViewModel.isIncludeReturn())) {
                td.setIsDrivingDay(true);
            }

            td.setPrimaryDescription(getInitialDayPrimaryDescription(context, tripDayNumber, daysOfDriving, numberOfTripDays, tripViewModel.isIncludeReturn()));
            td.setUserNotes(getInitialUserNotes(context, tripDayNumber, daysOfDriving, numberOfTripDays, tripViewModel.isIncludeReturn()));
            td.setIsDefaultText(true);

            td.setTripDayDate(PersistenceFormats.toDateString(tripViewModel.getDepartureDate().plusDays(day)));
            tripDays.add(td);
        }

        // set TripLocation on appropriate driving day
        // for start of trip, main trip location is destination for the last driving day
        tripDays.get(daysOfDriving - 1).getDestinations().add(
                new TripLocation(
                        tripViewModel.getDestinationLatLng().latitude,
                        tripViewModel.getDestinationLatLng().longitude,
                        tripViewModel.getDestinationDescription(),
                        null
                )
        );
        // if return driving is included in trip plans, trip origin is destination for last day of trip
        if (tripViewModel.isIncludeReturn()) {
            tripDays.get(tripDays.size()-1).getDestinations().add(
                    new TripLocation(
                            tripViewModel.getOriginLatLng().latitude,
                            tripViewModel.getOriginLatLng().longitude,
                            tripViewModel.getOriginDescription(),
                            null
                    )
            );
        }

        return tripDays;
    }

    private String getInitialDayPrimaryDescription(Context context, int dayNumber, int daysOfDriving, int totalDays, boolean includeReturnDrivingDays) {

        String drivingDay = context.getString(R.string.phrase_for_DrivingDay);

        if (isBeginDrivingDay(dayNumber, daysOfDriving)) {
            return drivingDay + " " + String.valueOf(dayNumber);
        }
        if (isReturnDrivingDay(dayNumber, daysOfDriving, totalDays, includeReturnDrivingDays)) {
            return context.getString(R.string.phrase_for_ReturnDrive);
        }
        return context.getString(R.string.phrase_for_PlanYourDay);
    }

    private String getInitialUserNotes(Context context, int dayNumber, int daysOfDriving, int totalDays, boolean includeReturnDrivingDays) {
        if (isBeginDrivingDay(dayNumber, daysOfDriving) || isReturnDrivingDay(dayNumber, daysOfDriving, totalDays, includeReturnDrivingDays)) {
            return context.getString(R.string.phrase_for_EnjoyYourDrive);
        }
        return context.getString(R.string.phrase_for_EnjoyYourDay);
    }

    private boolean isBeginDrivingDay(int dayNumber, int daysOfDriving) {
        return dayNumber <= daysOfDriving; // covers N days to begin trip
    }

    private boolean isReturnDrivingDay(int dayNumber, int daysOfDriving, int totalDays, boolean includeReturnDrivingDays) {
        return includeReturnDrivingDays && totalDays - dayNumber < daysOfDriving;
    }

    // determine how many days to allocate for driving
    // goal: consider the max driving hours preference as approximate...
    // this is only for initial setup, user can always edit a day and drive more/less
    private int getDaysOfDriving(int tripDurationMinutes, int prefDrivingDuration) {
        int drivingHours = tripDurationMinutes / 60;
        int drivingDays = drivingHours / prefDrivingDuration;
        int remainder = drivingHours % prefDrivingDuration;
        if (remainder > 2) {
            drivingDays++;
        }
        return drivingDays > 0 ? drivingDays : 1;
    }
}
