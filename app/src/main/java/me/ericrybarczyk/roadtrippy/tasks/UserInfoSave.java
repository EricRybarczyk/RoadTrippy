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

package me.ericrybarczyk.roadtrippy.tasks;

import android.os.AsyncTask;
import com.google.firebase.auth.FirebaseUser;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;


public class UserInfoSave extends AsyncTask<FirebaseUser, Void, Void> {

    @Override
    protected Void doInBackground(FirebaseUser... firebaseUsers) {
        TripRepository repository = new TripRepository();
        repository.saveUserInfo(firebaseUsers[0]);
        return null;
    }
}
