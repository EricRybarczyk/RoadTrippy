package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.GoogleMapFragment;
import me.ericrybarczyk.roadtrippy.maps.MapDisplayRequestListener;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.triplist.TripListActivity;
import me.ericrybarczyk.roadtrippy.util.ActivityUtils;

public class AddEditTripActivity extends AppCompatActivity implements MapDisplayRequestListener, GoogleMapFragment.LocationSelectedListener {

    private AddEditTripPresenter addEditTripPresenter;
    private FirebaseUser firebaseUser;

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.drawer_layout) protected DrawerLayout drawer;
    @BindView(R.id.nav_view) protected NavigationView navigationView;
    @BindView(R.id.content_container) protected FrameLayout contentFrameLayout;

    private static final String TAG = AddEditTripActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_trip);
        ButterKnife.bind(this);

        // configure toolbar and navigation drawer
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setupNavigationDrawer();

        verifyFirebaseUser();

        AddEditTripFragment addEditTripFragment = (AddEditTripFragment) getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (addEditTripFragment == null) {
            addEditTripFragment = AddEditTripFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), addEditTripFragment, R.id.content_container);
        }

        addEditTripPresenter = new AddEditTripPresenter(new TripRepository(), firebaseUser, addEditTripFragment);

        // TODO: load saved instance state if it exists

    }

    private void verifyFirebaseUser() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // if no user, go to starting point where login will be required
            Intent intent = new Intent(AddEditTripActivity.this, TripListActivity.class);
            startActivity(intent);
        }
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_trip_list:
                                Intent intent = new Intent(AddEditTripActivity.this, TripListActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.nav_create_trip:
                                // current screen, no action
                                break;
                            case R.id.nav_trip_history:
                                // trip history activity
                                break;
                            case R.id.nav_settings:
                                // settings activity
                                break;
                        }

                        drawer.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapDisplayRequested(int requestCode, String returnToFragmentTag) {
        Fragment fragment = GoogleMapFragment.newInstance(requestCode, returnToFragmentTag);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.content_container);
    }

    @Override
    public void onMapDisplayRequested(int requestCode, String returnToFragmentTag, LatLng displayLocation, String locationDescription) {

    }

    @Override
    public void onLocationSelected(LatLng location, int requestCode, String locationDescription) {

    }

    @Override
    public void onTripDayDestinationSelected(LatLng location, int requestCode, String locationDescription) {

    }
}
