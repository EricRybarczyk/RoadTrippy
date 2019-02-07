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

package me.ericrybarczyk.roadtrippy.maps.endpoints;

import android.content.Intent;
import android.net.Uri;

import me.ericrybarczyk.roadtrippy.viewmodels.TripLocationViewModel;

public class NavigationIntentService {
    public static Intent getNavigationIntent(TripLocationViewModel tripLocationViewModel) {
        String destination = tripLocationViewModel.getLatitude() + "," + tripLocationViewModel.getLongitude();
        String uri = "https://www.google.com/maps/dir/?api=1&destination=" + destination + "&travelmode=driving&dir_action=navigate";
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        return mapIntent;
    }
}
