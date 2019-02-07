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

package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.engine.TripManager;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTripPresenter implements AddEditTripContract.Presenter {

    private FirebaseUser firebaseUser;
    private AddEditTripContract.View addEditTripView;
    private static final String TAG = AddEditTripPresenter.class.getSimpleName();

    public AddEditTripPresenter(@NonNull TripDataSource dataSource, @NonNull AddEditTripContract.View view) {
        TripDataSource tripDataSource = checkNotNull(dataSource);
        this.firebaseUser = AuthenticationManager.getCurrentUser();
        this.addEditTripView = checkNotNull(view);

        this.addEditTripView.setPresenter(this);
    }

    @Override
    public void saveTrip(Context context, TripViewModel tripViewModel, int preferenceDrivingHours) {
        TripManager tripManager = new TripManager();
        Trip trip = tripManager.buildTrip(tripViewModel, firebaseUser.getUid());
        List<TripDay> tripDays = tripManager.buildInitialTripDays(context, tripViewModel, preferenceDrivingHours);

        TripRepository repository = new TripRepository();
        repository.saveTrip(trip, tripDays);

        // reset the state of the ViewModel to default
        tripViewModel.reset();

        // tell the view the work is done
        addEditTripView.showTripList();
    }

    @Override
    public void start() {

    }
}
