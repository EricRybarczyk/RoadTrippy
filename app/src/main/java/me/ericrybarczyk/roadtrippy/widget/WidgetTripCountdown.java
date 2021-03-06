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

package me.ericrybarczyk.roadtrippy.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDate;

import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.tripdetail.TripDetailActivity;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;
import timber.log.Timber;

public class WidgetTripCountdown extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String userId = WidgetTripCountdownConfigActivity.getUserIdPref(context, appWidgetId);
        String tripId = WidgetTripCountdownConfigActivity.getTripIdPref(context, appWidgetId);
        String tripNodeKey = WidgetTripCountdownConfigActivity.getTripNodeKeyPref(context, appWidgetId);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_trip_countdown);

        // get trip data from firebase
        TripRepository tripRepository = new TripRepository();
        DatabaseReference reference = tripRepository.getTrip(userId, tripNodeKey);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip == null) {
                    Timber.e("updateAppWidget - onDataChange: Trip object is null from Firebase");
                    return;
                }
                TripViewModel viewModel = TripViewModel.from(trip);
                String daysToGo = String.valueOf(viewModel.getDaysUntilDeparture());
                String subtitle = daysToGo + " " + context.getString(R.string.widget_trip_countdown_subtitle);
                remoteViews.setTextViewText(R.id.widget_trip_countdown_days_to_go, daysToGo);
                remoteViews.setTextViewText(R.id.widget_trip_countdown_title, viewModel.getDescription());
                remoteViews.setTextViewText(R.id.widget_trip_countdown_subtitle, subtitle);

                Intent intent = new Intent(context, TripDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(ArgumentKeys.WIDGET_REQUEST_TRIP_ID, tripId);
                intent.putExtra(ArgumentKeys.WIDGET_REQUEST_TRIP_NODE_KEY, tripNodeKey);
                boolean tripIsArchived = (viewModel.getIsArchived() || viewModel.getReturnDate().compareTo(LocalDate.now()) < 0); // also treat any past trip as Archived when accessing from widget
                intent.putExtra(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, tripIsArchived);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_trip_countdown_layout_container, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.getMessage());
            }
        });

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There can be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetTripCountdownConfigActivity.deleteWidgetPref(context, appWidgetId);
        }
    }
}