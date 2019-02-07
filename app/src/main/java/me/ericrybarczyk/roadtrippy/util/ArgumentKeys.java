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

package me.ericrybarczyk.roadtrippy.util;

public class ArgumentKeys {
    public static final String WIDGET_REQUEST_TRIP_ID = "widget_trip_id";
    public static final String WIDGET_REQUEST_TRIP_NODE_KEY = "widget_trip_node_key";
    public static final String TRIP_LIST_DISPLAY_DEFAULT_INDICATOR = "display_default_trips_list";
    public static final String TRIP_LIST_DISPLAY_ARCHIVE_INDICATOR = "display_archived_trips_list";
    public static final String KEY_TRIP_LIST_DISPLAY_TYPE = "display_archived_trips_list";
    public static final String TRIP_IS_ARCHIVED_KEY = "requested_trip_is_archived";

    public static final String KEY_REQUEST_CODE = "request_code_from_caller";
    public static final String KEY_LAST_MAP_ZOOM_LEVEL = "last_map_zoom_level";

    public static final String TAG_PICK_ORIGIN_DIALOG= "pick_origin_dialog";
    public static final String KEY_CALENDAR_FOR_DISPLAY = "calendar_for_display";

    public static final String KEY_TRIP_ID = "trip_id_key";
    public static final String KEY_DAY_NODE_KEY = "trip_day_node_key";
    public static final String KEY_TRIP_DAY_NUMBER = "trip_day_number_key";
    public static final String KEY_TRIP_DESTINATION_LATITUDE  = "trip_destination_latitude_key";
    public static final String KEY_TRIP_DESTINATION_LONGITUDE  = "trip_destination_longitude_key";
    public static final String KEY_TRIP_NODE_KEY = "trip_node_key";
    public static final String TAG_PICK_NAVIGATION_DIALOG = "pick_navigation_dialog";

    public static final String KEY_HOME_ORIGIN = "home_origin";
    public static final String KEY_PICK_ORIGIN = "pick_origin";
    public static final String KEY_DRIVING_DURATION_PREFERENCE = "driving_duration_preference_key";
    public static final String KEY_HOME_LOCATION_LATITUDE_PREFERENCE = "home_location_latitude_preference_key";
    public static final String KEY_HOME_LOCATION_LONGITUDE_PREFERENCE = "home_location_longitude_preference_key";
}
