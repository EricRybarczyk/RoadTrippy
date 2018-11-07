package me.ericrybarczyk.roadtrippy.tripaddedit;

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
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

public class AddEditTripActivity extends AppCompatActivity {

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
