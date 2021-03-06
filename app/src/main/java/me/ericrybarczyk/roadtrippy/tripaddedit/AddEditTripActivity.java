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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.util.ActivityUtils;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

public class AddEditTripActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.content_container) protected FrameLayout contentFrameLayout;

    private static final String TAG = AddEditTripActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_trip);
        ButterKnife.bind(this);

        // configure toolbar and navigation drawer
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AuthenticationManager.verifyAuthentication(this);

        AddEditTripFragment addEditTripFragment = (AddEditTripFragment) getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (addEditTripFragment == null) {
            addEditTripFragment = AddEditTripFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), addEditTripFragment, R.id.content_container);
        }
        // Presenter must still be initialized because the Presenter links itself to the View
        AddEditTripPresenter addEditTripPresenter = new AddEditTripPresenter(new TripRepository(), addEditTripFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
