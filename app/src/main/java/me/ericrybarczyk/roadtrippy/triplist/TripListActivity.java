package me.ericrybarczyk.roadtrippy.triplist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.settings.SettingsActivity;
import me.ericrybarczyk.roadtrippy.tasks.UserInfoSave;
import me.ericrybarczyk.roadtrippy.tripaddedit.AddEditTripActivity;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.util.ActivityUtils;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;

public class TripListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.drawer_layout) protected DrawerLayout drawer;
    @BindView(R.id.nav_view) protected NavigationView navigationView;
    @BindView(R.id.content_container) protected FrameLayout contentFrameLayout;

    private static final String TAG = TripListActivity.class.getSimpleName();

    public static final String ANONYMOUS = "anonymous";
    private String activeUsername = ANONYMOUS;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String keyTripListDisplayType;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        ButterKnife.bind(this);

        // configure toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setupNavigationDrawer();

        if (savedInstanceState != null) {
            keyTripListDisplayType = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE);
        } else if (getIntent().hasExtra(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE)) {
            keyTripListDisplayType = getIntent().getStringExtra(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE);
        } else {
            keyTripListDisplayType = ArgumentKeys.TRIP_LIST_DISPLAY_DEFAULT_INDICATOR;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        verifyPermissions();
        updateLastKnownLocation();
        setupFirebaseAuth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeDisplay() {
        TripListFragment tripListFragment = (TripListFragment) getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (tripListFragment == null) {
            tripListFragment = TripListFragment.newInstance(keyTripListDisplayType);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), tripListFragment, R.id.content_container);
        }
        // Presenter must still be initialized because the Presenter links itself to the View
        TripListPresenter tripListPresenter = new TripListPresenter(tripListFragment);
    }

    private void verifyPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted - request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    RequestCodes.LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCodes.LOCATION_PERMISSIONS_REQUEST_CODE: {
                updateLastKnownLocation();
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // no need to use the location now, but this helps make sure location is current when user accesses map functionality
                    Log.i(TAG, "Location updated");
                })
                .addOnFailureListener(this, e -> Log.e(TAG, "fusedLocationProviderClient onFailure: " + e.getMessage()));
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(
                item -> {
                    TripListContract.View tripListView;
                    switch (item.getItemId()) {
                        case R.id.nav_trip_list:
                            // current screen, no action
                            tripListView = (TripListContract.View) getSupportFragmentManager().findFragmentById(R.id.content_container);
                            tripListView.showDefaultTripList();
                            break;
                        case R.id.nav_create_trip:
                            Intent intentCreateTrip = new Intent(TripListActivity.this, AddEditTripActivity.class);
                            startActivity(intentCreateTrip);
                            break;
                        case R.id.nav_trip_history:
                            // trip history activity
                            tripListView = (TripListContract.View) getSupportFragmentManager().findFragmentById(R.id.content_container);
                            tripListView.showArchivedTripList();
                            break;
                        case R.id.nav_settings:
                            Intent intentSettings = new Intent(TripListActivity.this, SettingsActivity.class);
                            startActivity(intentSettings);
                            break;
                    }

                    drawer.closeDrawers();
                    return true;
                });
    }

    private void setupFirebaseAuth() {
        /* SOURCE: FirebaseAuth code is directly adapted from Udacity & Google materials,
           including Udacity's Firebase extracurricular module in the Android Developer Nanodegree program,
           and https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md */
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                onSignedInInitialize(firebaseUser);
            } else {

                onSignedOutCleanup();

                // configure supported sign-in providers
                List<AuthUI.IdpConfig> providers = Collections.singletonList(
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                // Create and launch sign-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(true)
                                .setAvailableProviders(providers)
                                .build(),
                        RequestCodes.SIGN_IN_REQUEST_CODE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void onSignedInInitialize(FirebaseUser firebaseUser) {
        this.activeUsername = firebaseUser.getDisplayName();
        View header = navigationView.getHeaderView(0);
        TextView usernameText = header.findViewById(R.id.username_display_text);
        usernameText.setText(activeUsername);
        initializeDisplay();
        new UserInfoSave().execute(firebaseUser);
    }

    private void onSignedOutCleanup() {
        this.activeUsername = ANONYMOUS;
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ArgumentKeys.KEY_TRIP_LIST_DISPLAY_TYPE, keyTripListDisplayType);
        super.onSaveInstanceState(outState);
    }
}
