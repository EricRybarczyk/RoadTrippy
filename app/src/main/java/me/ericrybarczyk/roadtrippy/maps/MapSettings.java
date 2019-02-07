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

package me.ericrybarczyk.roadtrippy.maps;

public class MapSettings {
    public static final float MAP_DEFAULT_ZOOM = 12.0f;
    public static final float MAP_SEARCH_RESULT_ZOOM = 13.0f;
    public static final float MAP_CLICK_ZOOM = 14.0f;
    public static final float MAP_DEFAULT_BEARING = 360.0f;
    public static final float MAP_DEFAULT_TILT = 0.0f;
    public static final int MAP_IMAGE_SAVE_QUALITY = 100;
    public static final String DESTINATION_MAP_IMAGE_DIRECTORY = "destMaps";
    public static final String DESTINATION_MAP_IMAGE_EXTENSION = ".png";
    public static final String DESTINATION_MAP_MAIN_PREFIX = "t_";
    public static final String DESTINATION_MAP_SLICED_PREFIX = "s_";
    public static final String KEY_MAP_DISPLAY_LATITUDE = "map_display_latitude";
    public static final String KEY_MAP_DISPLAY_LONGITUDE = "map_display_longitude";
    public static final String KEY_MAP_DISPLAY_LOCATION_DESCRIPTION = "map_display_location_name";
}
