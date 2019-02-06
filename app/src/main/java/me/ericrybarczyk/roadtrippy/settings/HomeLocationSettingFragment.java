package me.ericrybarczyk.roadtrippy.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.FullScreenDialogFragment;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.maps.endpoints.FindPlacesEndpoint;
import me.ericrybarczyk.roadtrippy.maps.endpoints.SearchService;
import me.ericrybarczyk.roadtrippy.maps.places.Candidate;
import me.ericrybarczyk.roadtrippy.maps.places.PlacesResponse;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.util.InputUtils;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class HomeLocationSettingFragment extends FullScreenDialogFragment
                implements  OnMapReadyCallback, GoogleMap.OnMapClickListener,
                            GoogleMap.OnCameraMoveStartedListener, View.OnClickListener {

    private SupportMapFragment mapFragment;
    private String googleMapsApiKey;
    private GoogleMap googleMap;
    private float lastMapZoomLevel;
    private LatLng homeLocation;

    @BindView(R.id.search_button_tlp) protected Button searchButton;
    @BindView(R.id.set_location_button_tlp) protected Button setLocationButton;
    @BindView(R.id.description_text_tlp) protected EditText locationDescription;
    @BindView(R.id.search_text_tlp) protected EditText searchText;

    private static final String TAG = HomeLocationSettingFragment.class.getSimpleName();

    public static HomeLocationSettingFragment newInstance() {
        return new HomeLocationSettingFragment();
    }

    public static HomeLocationSettingFragment newInstance(LatLng homeLocation) {
        HomeLocationSettingFragment settingFragment = new HomeLocationSettingFragment();
        Bundle args = new Bundle();
        args.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)homeLocation.latitude);
        args.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)homeLocation.longitude);
        settingFragment.setArguments(args);
        return settingFragment;
    }

    public HomeLocationSettingFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleMapsApiKey = getString(R.string.google_maps_key);
        lastMapZoomLevel = MapSettings.MAP_DEFAULT_ZOOM;

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL)) {
                lastMapZoomLevel = savedInstanceState.getFloat(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL);
            }
            if (savedInstanceState.containsKey(MapSettings.KEY_MAP_DISPLAY_LATITUDE)) {
                homeLocation = getHomeLocation(savedInstanceState);
            }
        } else if (getArguments() != null) {
            if (getArguments().containsKey(MapSettings.KEY_MAP_DISPLAY_LATITUDE)) {
                homeLocation = getHomeLocation(getArguments());
            }
        }
    }

    private LatLng getHomeLocation(Bundle bundle) {
        double latitude = (double) bundle.getFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE);
        double longitude = (double) bundle.getFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE);
        return new LatLng(latitude, longitude);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_settings_home_location, container, false);
        ButterKnife.bind(this, rootView);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FragmentTags.TAG_SETTING_HOME_LOCATION_MAP);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
        }
        getChildFragmentManager().beginTransaction().replace(R.id.map_container_tlp, mapFragment, FragmentTags.TAG_SETTING_HOME_LOCATION_MAP).commitNow();

        rootView.clearFocus();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchButton.setOnClickListener(this);
        setLocationButton.setOnClickListener(this);
        locationDescription.setText(getString(R.string.word_for_HOME));
        locationDescription.setEnabled(false);

        int permission = ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PERMISSION_GRANTED) {
            updateLocation();
        } else {
            // Permission is not granted - request the permission
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    RequestCodes.LOCATION_PERMISSIONS_REQUEST_CODE);
        }

        view.clearFocus();
    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()))
                .getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null &&     homeLocation == null) {
                        // if homeLocation already set, start the map with that saved home location
                        homeLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                    mapFragment.getMapAsync(HomeLocationSettingFragment.this);
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapClickListener(this);
        googleMap.setMyLocationEnabled(true);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);

        updateMapView(lastMapZoomLevel);
    }

    private void updateMapView(float zoomLevel) {
        if (homeLocation == null) return;
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(homeLocation));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(homeLocation)
                .zoom(zoomLevel)
                .bearing(MapSettings.MAP_DEFAULT_BEARING)
                .tilt(MapSettings.MAP_DEFAULT_TILT)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
        lastMapZoomLevel = zoomLevel;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        homeLocation = latLng;
        updateMapView(MapSettings.MAP_CLICK_ZOOM);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        InputUtils.hideKeyboardFrom(getContext(), getView());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == searchButton.getId()) {
            String searchValue;
            if (searchText.getText() == null) { return; }
            searchValue = searchText.getText().toString();

            InputUtils.hideKeyboardFrom(getContext(), getView());
            FindPlacesEndpoint endpoint = SearchService.getClient().create(FindPlacesEndpoint.class);
            Call<PlacesResponse> findPlacesCall = endpoint.findPlaces(googleMapsApiKey, SearchService.PLACES_API_TEXT_QUERY_INPUT_TYPE, searchValue, SearchService.PLACES_API_QUERY_FIELDS);

            findPlacesCall.enqueue(new Callback<PlacesResponse>() {
                @Override
                public void onResponse(@NonNull Call<PlacesResponse> call, @NonNull Response<PlacesResponse> response) {
                    PlacesResponse placesResponse = response.body();
                    if ((placesResponse != null && placesResponse.getCandidates() != null ? placesResponse.getCandidates().size() : 0) > 0) {

                        // if one result, set that as the location and update UI
                        if (placesResponse.getCandidates().size() == 1) {
                            Candidate place = placesResponse.getCandidates().get(0);
                            homeLocation = new LatLng(place.getGeometry().getLocation().getLat(), place.getGeometry().getLocation().getLng());
                            locationDescription.setText(place.getName());
                            updateMapView(MapSettings.MAP_SEARCH_RESULT_ZOOM);
                        } else {
                            Timber.d("Too many Places Search results. Result count = %s", String.valueOf(placesResponse.getCandidates().size()));
                            Toast.makeText(getContext(), R.string.map_search_too_many_results_message, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Timber.d("Retrofit onResponse: No Places Search results.");
                        Toast.makeText(getContext(), R.string.map_search_no_results_message, Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<PlacesResponse> call, @NonNull Throwable t) {
                    Timber.e("Failed to call Places API. Error: %s", t.getMessage());
                    Toast.makeText(getContext(), R.string.map_search_call_error_message, Toast.LENGTH_LONG).show();
                }
            });
        }
        if (v.getId() == setLocationButton.getId()) {
            if (homeLocation == null) {
                Timber.e(getString(R.string.error_no_location_specified));
                Toast.makeText(getContext(), R.string.error_no_location_specified, Toast.LENGTH_LONG).show();
                return;
            }
            HomeLocationPreferenceSaveListener listener = (HomeLocationPreferenceSaveListener) getTargetFragment();
            if (listener == null) {
                throw new RuntimeException(TAG + ": TargetFragment of this dialog must implement HomeLocationPreferenceSaveListener");
            }
            listener.onHomeLocationPreferenceSave(homeLocation);
            dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (googleMap != null) {
            savedInstanceState.putFloat(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL, googleMap.getCameraPosition().zoom);
        }
        if (homeLocation != null) {
            savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)homeLocation.latitude);
            savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)homeLocation.longitude);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public interface HomeLocationPreferenceSaveListener {
        void onHomeLocationPreferenceSave(LatLng homeLocation);
    }
}

