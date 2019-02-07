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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.BaseActivity;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.util.ActivityUtils;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

public class TripDetailActivity extends BaseActivity {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.content_container) protected FrameLayout contentFrameLayout;

    private TripDetailPresenter tripDetailPresenter;
    private String tripNodeKey;
    private boolean tripIsArchived;
    private static final String TAG = TripDetailActivity.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        ButterKnife.bind(this);

        // configure toolbar and navigation drawer
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AuthenticationManager.verifyAuthentication(this);

        Intent starter = getIntent();
        String tripId;
        if (starter.hasExtra(ArgumentKeys.WIDGET_REQUEST_TRIP_ID)) {
            tripId = starter.getStringExtra(ArgumentKeys.WIDGET_REQUEST_TRIP_ID);
            tripNodeKey = starter.getStringExtra(ArgumentKeys.WIDGET_REQUEST_TRIP_NODE_KEY);
        } else {
            tripId = starter.getStringExtra(ArgumentKeys.KEY_TRIP_ID);
            tripNodeKey = starter.getStringExtra(ArgumentKeys.KEY_TRIP_NODE_KEY);
        }
        tripIsArchived = starter.getBooleanExtra(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, false);

        TripDetailFragment tripDetailFragment = (TripDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (tripDetailFragment == null) {
            tripDetailFragment = TripDetailFragment.newInstance(tripId, tripNodeKey, tripIsArchived);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), tripDetailFragment, R.id.content_container);
        }

        tripDetailPresenter = new TripDetailPresenter(new TripRepository(), tripDetailFragment);

        logScreenInfo(TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (tripIsArchived) {
            return true; // no menu to archive if already archived
        }
        getMenuInflater().inflate(R.menu.menu_trip_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_archive_trip:
                tripDetailPresenter.archiveTrip(AuthenticationManager.getCurrentUser().getUid(), tripNodeKey);
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
