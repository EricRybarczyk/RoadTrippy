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

package me.ericrybarczyk.roadtrippy.tripday;

import com.google.android.gms.maps.model.LatLng;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.viewmodels.TripDayViewModel;

public interface TripDayContract {

    interface View extends BaseView<Presenter> {
        void displayTripDay(TripDay tripDay);
        void setTripDestination(LatLng tripDestination);
        void showTripDetail();
    }

    interface Presenter extends BasePresenter {
        void getTripDay(String userId, String tripId, String dayNodeKey);
        void getTripDestination(String userId, String tripNodeKey);
        void updateTripDay(String userId, String tripId, String dayNodeKey, TripDayViewModel tripDayViewModel);
        void updateTripDayHighlight(String userId, String tripId, String dayNodeKey, boolean isHighlight);
    }

}
