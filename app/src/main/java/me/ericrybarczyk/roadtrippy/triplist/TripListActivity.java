package me.ericrybarczyk.roadtrippy.triplist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.createtrip.CreateTripActivity;

public class TripListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.drawer_layout) protected DrawerLayout drawer;
    @BindView(R.id.nav_view) protected NavigationView navigationView;
    @BindView(R.id.content_container) protected FrameLayout contentFrameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        ButterKnife.bind(this);

        // configure toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setupNavigationDrawer();
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_trip_list:
                                // current screen, no action
                                break;
                            case R.id.nav_create_trip:
                                Intent intent = new Intent(TripListActivity.this, CreateTripActivity.class);
                                startActivity(intent);
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
}
