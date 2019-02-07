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

package me.ericrybarczyk.roadtrippy.settings;

import com.google.android.gms.maps.model.LatLng;

import me.ericrybarczyk.roadtrippy.BasePresenter;
import me.ericrybarczyk.roadtrippy.BaseView;

public interface SettingsContract {

    interface View extends BaseView<Presenter> {
    }


    interface Presenter extends BasePresenter {
        int getCurrentDrivingDurationPreference(int defaultDuration);
        void saveDrivingDurationPreference(int drivingDuration);
        void saveHomeLocationPreference(LatLng homeLocation);
        LatLng getHomeLocationPreference();
    }
}
