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

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.ericrybarczyk.roadtrippy.R;
import timber.log.Timber;

public class TripDayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TripDayListClickListener tripDayListClickListener;
    private NavigationClickListener navigationClickListener;

    @BindView(R.id.trip_day_item_container) protected ConstraintLayout layoutContainer;
    @BindView(R.id.day_number) protected TextView dayNumber;
    @BindView(R.id.day_primary_description) protected TextView dayPrimaryDescription;
    @BindView(R.id.day_user_notes) protected TextView dayUserNotes;
    @BindView(R.id.icon_navigate_trip_day) protected ImageView iconNavigation;

    public TripDayViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setTripDayListClickListener(TripDayListClickListener listClickListener) {
        tripDayListClickListener = listClickListener;
    }
    public void setNavigationClickListener(NavigationClickListener navigationListener) {
        navigationClickListener = navigationListener;
    }

    @Override
    @OnClick
    public void onClick(View v) {
        if (tripDayListClickListener == null) {
            Timber.e("onTripDayListClickListener is null");
            return;
        }
        tripDayListClickListener.onTripDayListItemClick();
    }

    @OnClick(R.id.icon_navigate_trip_day)
    public void onNavigationIconClick() {
        navigationClickListener.onNavigationClick();
    }

    interface TripDayListClickListener {
        void onTripDayListItemClick();
    }

    interface NavigationClickListener {
        void onNavigationClick();
    }
}
