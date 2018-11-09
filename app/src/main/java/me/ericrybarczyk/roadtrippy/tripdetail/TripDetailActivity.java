package me.ericrybarczyk.roadtrippy.tripdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.util.ActivityUtils;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

public class TripDetailActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.drawer_layout) protected DrawerLayout drawer;
    @BindView(R.id.nav_view) protected NavigationView navigationView;
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
