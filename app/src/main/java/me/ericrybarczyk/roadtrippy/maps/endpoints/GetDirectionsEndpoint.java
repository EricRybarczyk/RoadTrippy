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


import me.ericrybarczyk.roadtrippy.maps.directions.DirectionsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDirectionsEndpoint {

    @GET("directions/json") // ?key={apikey}&origin={origin}&destination={destination}
    Call<DirectionsResponse> getDirections(@Query("key") String apikey, @Query("origin") String origin, @Query("destination") String destination, @Query("alternatives") String alternatives, @Query("avoid") String avoid);

    @GET("directions/json") // ?key={apikey}&origin={origin}&destination={destination}
    Call<DirectionsResponse> getDirections(@Query("key") String apikey, @Query("origin") String origin, @Query("destination") String destination, @Query("alternatives") String alternatives);
}

