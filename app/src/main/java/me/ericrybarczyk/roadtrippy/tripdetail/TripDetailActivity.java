package me.ericrybarczyk.roadtrippy.tripdetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

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

        AuthenticationManager.verifyAuthentication(this);

        String tripId = getIntent().getStringExtra(ArgumentKeys.KEY_TRIP_ID);
        String tripNodeKey = getIntent().getStringExtra(ArgumentKeys.KEY_TRIP_NODE_KEY);
        boolean tripIsArchived = getIntent().getBooleanExtra(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, false);

        TripDetailFragment tripDetailFragment = (TripDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (tripDetailFragment == null) {
            tripDetailFragment = TripDetailFragment.newInstance(tripId, tripNodeKey, tripIsArchived);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), tripDetailFragment, R.id.content_container);
        }
        // Presenter must still be initialized because the Presenter links itself to the View
        TripDetailPresenter tripDetailPresenter = new TripDetailPresenter(new TripRepository(), tripDetailFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
