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

package me.ericrybarczyk.roadtrippy.viewmodels;

import me.ericrybarczyk.roadtrippy.dto.TripLocation;

public class TripLocationViewModel {

    private double latitude;
    private double longitude;
    private String description;
    private String placeId;

    public TripLocationViewModel() {
    }

    public TripLocationViewModel(double latitude, double longitude, String description, String placeId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.placeId = placeId;
    }

    public static TripLocationViewModel from(TripLocation tripLocation) {
        TripLocationViewModel viewModel = new TripLocationViewModel();

        viewModel.setLatitude(tripLocation.getLatitude());
        viewModel.setLongitude(tripLocation.getLongitude());
        viewModel.setDescription(tripLocation.getDescription());
        viewModel.setPlaceId(tripLocation.getPlaceId());

        return viewModel;
    }

    public TripLocation asTripLocation() {
        TripLocation tripLocation = new TripLocation();
        tripLocation.setLatitude(this.getLatitude());
        tripLocation.setLongitude(this.getLongitude());
        tripLocation.setDescription(this.getDescription());
        tripLocation.setPlaceId(this.getPlaceId());
        return tripLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
