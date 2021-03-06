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

package me.ericrybarczyk.roadtrippy.triplist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.persistence.DataOptions;
import me.ericrybarczyk.roadtrippy.tripaddedit.AddEditTripActivity;
import me.ericrybarczyk.roadtrippy.tripdetail.TripDetailActivity;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;
import me.ericrybarczyk.roadtrippy.util.FontManager;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripListFragment extends Fragment implements TripListContract.View {

    private TripListContract.Presenter presenter;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private String keyTripListDisplayType;
    private static final int GRID_LAYOUT_SCREEN_WIDTH_MIN = 720;
    private static final int GRID_LAYOUT_SPAN_COUNT = 2;

    @BindView(R.id.trip_list) protected RecyclerView tripListRecyclerView;
    @BindView(R.id.fab) protected FloatingActionButton fab;

    public static TripListFragment newInstance(String tripListDisplayKey) {
        TripListFragment tripListFragment = new TripListFragment();
        Bundle args = new Bundle();
        args.putString(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE, tripListDisplayKey);
        tripListFragment.setArguments(args);
        return tripListFragment;
    }

    public TripListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            keyTripListDisplayType = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE);
        } else if (getArguments() != null) {
            keyTripListDisplayType = getArguments().getString(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE);
        } else {
            Timber.e("Missing expected value for KEY_TRIP_LIST_DISPLAY_TYPE, using default.");
            keyTripListDisplayType = ArgumentKeys.TRIP_LIST_DISPLAY_DEFAULT_INDICATOR;
        }

        DataOptions dataOptions = new DataOptions(AuthenticationManager.getCurrentUser().getUid());

        firebaseRecyclerAdapter = this.getFirebaseRecyclerAdapter(dataOptions.getTripListDataOptions(keyTripListDisplayType));
    }

    private FirebaseRecyclerAdapter<Trip, TripViewHolder> getFirebaseRecyclerAdapter(FirebaseRecyclerOptions<Trip> options) {
        return new FirebaseRecyclerAdapter<Trip, TripViewHolder>(options) {
            @NonNull
            @Override
            public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_item, parent, false);
                return new TripViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TripViewHolder holder, int position, @NonNull Trip model) {
                TripViewModel viewModel = TripViewModel.from(this.getItem(position));
                String joinWord = getString(R.string.word_for_TO);
                String hours = getString(R.string.word_for_HOURS);
                String h = getString(R.string.abbreviation_for_HOURS);
                String minutes = getString(R.string.word_for_MINUTES);
                String m = getString(R.string.abbreviation_for_MINUTES);
                String unknown = getString(R.string.word_for_UNKNOWN);

                String tripNodeKey = this.getRef(position).getKey();

                holder.setTripId(viewModel.getTripId());
                holder.setTripNodeKey(tripNodeKey);
                holder.setTripListClickListener((tripId, tripNodeKey1) -> showTripDetail(tripId, tripNodeKey1));

                File imageDir = Objects.requireNonNull(getContext()).getDir(MapSettings.DESTINATION_MAP_IMAGE_DIRECTORY, Context.MODE_PRIVATE);
                String tripImageFilename = MapSettings.DESTINATION_MAP_SLICED_PREFIX + viewModel.getTripId() + MapSettings.DESTINATION_MAP_IMAGE_EXTENSION;
                File mapImage = new File(imageDir, tripImageFilename);
                Picasso.with(getContext())
                        .load(mapImage)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.map_placeholder)
                        .error(R.drawable.map_placeholder)
                        .into(holder.tripImage);

                holder.tripName.setText(viewModel.getDescription());
                holder.tripDirectionsOverview.setText(viewModel.getOriginDestinationSummaryText(joinWord));
                holder.tripDateRange.setText(viewModel.getDateRangeSummaryText(joinWord));
                holder.highlightOne.setText(viewModel.getOriginDescription());
                holder.highlightTwo.setText(viewModel.getDestinationDescription());

                String sb = getString(R.string.label_for_DRIVING_TIME) +
                        " " +
                        viewModel.getDurationDescription(hours, minutes, h, m, unknown);

                holder.drivingDuration.setText(sb);

                holder.iconHighlightOne.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME_SOLID));
                holder.iconHighlightTwo.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME_SOLID));
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (getItemCount() == 0) {
                    String message;
                    if (keyTripListDisplayType.equals(ArgumentKeys.TRIP_LIST_DISPLAY_ARCHIVE_INDICATOR)) {
                        message = getString(R.string.message_no_archived_trip_list_data);
                    } else {
                        message = getString(R.string.message_no_default_trip_list_data);
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);
        ButterKnife.bind(this, rootView);

        RecyclerView.LayoutManager layoutManager;
        if (getResources().getConfiguration().screenWidthDp >= GRID_LAYOUT_SCREEN_WIDTH_MIN) {
            layoutManager = new GridLayoutManager(getActivity(), GRID_LAYOUT_SPAN_COUNT, LinearLayoutManager.VERTICAL, false);
        } else {
            layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }
        tripListRecyclerView.setLayoutManager(layoutManager);
        tripListRecyclerView.setHasFixedSize(true);
        tripListRecyclerView.setAdapter(firebaseRecyclerAdapter);

        fab.setOnClickListener(v -> presenter.createTrip());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE, keyTripListDisplayType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showCreateTrip() {
        Intent intent = new Intent(getContext(), AddEditTripActivity.class);
        startActivityForResult(intent, RequestCodes.CREATE_TRIP_REQUEST_CODE);
    }

    @Override
    public void showTripDetail(String tripId, String tripNodeKey) {
        Intent intent = new Intent(getContext(), TripDetailActivity.class);
        intent.putExtra(ArgumentKeys.KEY_TRIP_ID, tripId);
        intent.putExtra(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
        intent.putExtra(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, (keyTripListDisplayType.equals(ArgumentKeys.TRIP_LIST_DISPLAY_ARCHIVE_INDICATOR)));
        startActivity(intent);
    }

    @Override
    public void showArchivedTripList() {
        if (!this.keyTripListDisplayType.equals(ArgumentKeys.TRIP_LIST_DISPLAY_ARCHIVE_INDICATOR)) {
            changeRecyclerAdapter(ArgumentKeys.TRIP_LIST_DISPLAY_ARCHIVE_INDICATOR);
        }
    }

    @Override
    public void showDefaultTripList() {
        if (!this.keyTripListDisplayType.equals(ArgumentKeys.TRIP_LIST_DISPLAY_DEFAULT_INDICATOR)) {
            changeRecyclerAdapter(ArgumentKeys.TRIP_LIST_DISPLAY_DEFAULT_INDICATOR);
        }
    }

    private void changeRecyclerAdapter(String tripListDisplayType) {
        firebaseRecyclerAdapter.stopListening();
        tripListRecyclerView.setAdapter(null);
        DataOptions dataOptions = new DataOptions(AuthenticationManager.getCurrentUser().getUid());
        firebaseRecyclerAdapter = getFirebaseRecyclerAdapter(dataOptions.getTripListDataOptions(tripListDisplayType));
        tripListRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        this.keyTripListDisplayType = tripListDisplayType;
    }

    @Override
    public void setPresenter(TripListContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

}
