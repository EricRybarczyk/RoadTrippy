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

package me.ericrybarczyk.roadtrippy;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {

    protected void logScreenInfo(String tag) {
        Configuration configuration = getResources().getConfiguration();
        Timber.tag(tag).d("Screen Orientation: %d", configuration.orientation);
        Timber.tag(tag).d("Screen Density: %d", configuration.densityDpi);
        Timber.tag(tag).d("Screen Height: %d", configuration.screenHeightDp);
        Timber.tag(tag).d("Screen Width: %d", configuration.screenWidthDp);
        Timber.tag(tag).d("Screen Smallest Width: %d", configuration.smallestScreenWidthDp);
    }

    // check saved preference for indicator the user has accepted terms and conditions
    protected boolean getUserHasAcceptedTermsConditions() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(ArgumentKeys.KEY_USER_ACCEPTED_TERMS_CONDITIONS, false);
    }
}
