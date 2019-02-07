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

package me.ericrybarczyk.roadtrippy.tripdetail;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripDetailPresenter implements TripDetailContract.Presenter {

    private TripDataSource tripDataSource;
    private TripDetailContract.View tripDetailView;

    public TripDetailPresenter(@NonNull TripDataSource dataSource, @NonNull TripDetailContract.View view) {
        this.tripDataSource = checkNotNull(dataSource);
        this.tripDetailView = checkNotNull(view);

        this.tripDetailView.setPresenter(this);
    }

    @Override
    public void getTrip(String userId, String tripNodeKey, boolean tripIsArchived) {
        DatabaseReference tripReference;
        if (tripIsArchived) {
            tripReference = tripDataSource.getArchivedTrip(userId, tripNodeKey);
        } else {
            tripReference = tripDataSource.getTrip(userId, tripNodeKey);
        }

        tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tripDetailView.displayTrip(dataSnapshot.getValue(Trip.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.getMessage());
                throw databaseError.toException();
            }
        });
    }

    @Override
    public void archiveTrip(String userId, String tripNodeKey) {
        tripDataSource.archiveTrip(userId, tripNodeKey);
    }

    @Override
    public void start() {

    }
}
