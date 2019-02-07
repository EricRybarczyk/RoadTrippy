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

public class DatabasePaths {
    public static final String BASE_PATH_TRIPS = "trips/";
    public static final String BASE_PATH_TRIPDAYS = "tripdays/";
    public static final String BASE_PATH_USERS = "users/";
    public static final String BASE_PATH_ARCHIVE = "tripArchive/";

    public static final String KEY_TRIP_LIST_DEFAULT_SORT = "departureDate";
    public static final String KEY_TRIPDAY_HIGHLIGHT_CHILD = "isHighlight";
    public static final String KEY_TRIPDAY_DESTINATIONS_CHILD = "destinations";

}
