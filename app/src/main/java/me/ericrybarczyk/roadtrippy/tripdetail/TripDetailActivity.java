package me.ericrybarczyk.roadtrippy.tripdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.settings.SettingsActivity;
import me.ericrybarczyk.roadtrippy.tripaddedit.AddEditTripActivity;
import me.ericrybarczyk.roadtrippy.triplist.TripListActivity;
import me.ericrybarczyk.roadtrippy.util.ActivityUtils;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

public class TripDetailActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.drawer_layout) protected DrawerLayout drawer;
    @BindView(R.id.nav_view) protected NavigationView navigationView;
    @BindView(R.id.content_container) protected FrameLayout contentFrameLayout;

    private FirebaseUser firebaseUser;
    private TripDetailPresenter tripDetailPresenter;
    private static final String TAG = TripDetailActivity.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        ButterKnife.bind(this);

        // configure toolbar and navigation drawer
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setupNavigationDrawer();

        verifyFirebaseUser();

        String tripId = getIntent().getStringExtra(ArgumentKeys.KEY_TRIP_ID);
        String tripNodeKey = getIntent().getStringExtra(ArgumentKeys.KEY_TRIP_NODE_KEY);

        TripDetailFragment tripDetailFragment = (TripDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (tripDetailFragment == null) {
            tripDetailFragment = TripDetailFragment.newInstance(tripId, tripNodeKey);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), tripDetailFragment, R.id.content_container);
        }
        tripDetailPresenter = new TripDetailPresenter(new TripRepository(), tripDetailFragment);
    }


    private void verifyFirebaseUser() {
        firebaseUser = AuthenticationManager.getCurrentUser();
        if (firebaseUser == null) {
            // if no user, go to starting point where login will be required
            Intent intent = new Intent(TripDetailActivity.this, TripListActivity.class);
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
                                Intent intent = new Intent(TripDetailActivity.this, TripListActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.nav_create_trip:
                                Intent intentCreateTrip = new Intent(TripDetailActivity.this, AddEditTripActivity.class);
                                startActivity(intentCreateTrip);
                                break;
                            case R.id.nav_trip_history:
                                // trip history activity
                                break;
                            case R.id.nav_settings:
                                Intent intentSettings = new Intent(TripDetailActivity.this, SettingsActivity.class);
                                startActivity(intentSettings);
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
}
