package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
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
import me.ericrybarczyk.roadtrippy.util.FileSystemUtil;
import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.util.InputUtils;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class TripLocationPickerFragment extends FullScreenDialogFragment
        implements  OnMapReadyCallback, GoogleMap.OnMapClickListener,
                    GoogleMap.OnCameraMoveStartedListener, View.OnClickListener {

    private TripViewModel tripViewModel;
    private SupportMapFragment mapFragment;
    private String googleMapsApiKey;
    private GoogleMap googleMap;
    private LatLng mapLocation;
    private boolean displayForUserCurrentLocation;
    private float lastMapZoomLevel;
    private int requestCode; // passed in from caller to be returned with map location
    private String argumentLocationDescription;
    private static final String TAG = TripLocationPickerFragment.class.getSimpleName();

    @BindView(R.id.search_button_tlp) protected Button searchButton;
    @BindView(R.id.set_location_button_tlp) protected Button setLocationButton;
    @BindView(R.id.description_text_tlp) protected EditText locationDescription;
    @BindView(R.id.search_text_tlp) protected EditText searchText;

    public static TripLocationPickerFragment newInstance(int requestCode) {
        TripLocationPickerFragment tripLocationPickerFragment = new TripLocationPickerFragment();
        Bundle args = new Bundle();
        args.putInt(ArgumentKeys.KEY_REQUEST_CODE, requestCode);
        tripLocationPickerFragment.setArguments(args);
        return tripLocationPickerFragment;
    }

    public TripLocationPickerFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleMapsApiKey = getString(R.string.google_maps_key);
        lastMapZoomLevel = MapSettings.MAP_DEFAULT_ZOOM;
        displayForUserCurrentLocation = true; // by default map will show user's current location

        if (savedInstanceState != null) {
            requestCode = savedInstanceState.getInt(ArgumentKeys.KEY_REQUEST_CODE);
            if (savedInstanceState.containsKey(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL)) {
                lastMapZoomLevel = savedInstanceState.getFloat(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL);
            } else if (savedInstanceState.containsKey(MapSettings.KEY_MAP_DISPLAY_LATITUDE)) {
                displayForUserCurrentLocation = false; // flag request to show a requested location instead of user current location
                double latitude = (double) savedInstanceState.getFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE);
                double longitude = (double) savedInstanceState.getFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE);
                mapLocation = new LatLng(latitude, longitude);
            }
        } else if (getArguments() != null) {
            requestCode = getArguments().getInt(ArgumentKeys.KEY_REQUEST_CODE);
            if (getArguments().containsKey(MapSettings.KEY_MAP_DISPLAY_LATITUDE)) {
                displayForUserCurrentLocation = false; // flag request to show a requested location instead of user current location
                double latitude = (double) getArguments().getFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE);
                double longitude = (double) getArguments().getFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE);
                mapLocation = new LatLng(latitude, longitude);
            }
            if (getArguments().containsKey(MapSettings.KEY_MAP_DISPLAY_LOCATION_DESCRIPTION)) {
                argumentLocationDescription = getArguments().getString(MapSettings.KEY_MAP_DISPLAY_LOCATION_DESCRIPTION);
            }
        }

        tripViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TripViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_trip_location, container, false);
        ButterKnife.bind(this, rootView);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FragmentTags.TAG_CREATE_TRIP_MAP);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
        }
        getChildFragmentManager().beginTransaction().replace(R.id.map_container_tlp, mapFragment, FragmentTags.TAG_CREATE_TRIP_MAP).commitNow();

        rootView.clearFocus();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchButton.setOnClickListener(this);

        if (argumentLocationDescription != null) {
            locationDescription.setText(argumentLocationDescription);
            argumentLocationDescription = null; // clear this out so it doesn't persist if the fragment is loaded again
        }

        setLocationButton.setOnClickListener(v -> {
            if (v.getId() == setLocationButton.getId()) {
                if (requestCode == RequestCodes.TRIP_DESTINATION_REQUEST_CODE) {
                    updateMapView(MapSettings.MAP_SEARCH_RESULT_ZOOM); // make sure the map is displayed in a way that works well for the snapshot
                    // save a bitmap of the Google Map
                    // code based on https://stackoverflow.com/a/26946907/798642 and https://stackoverflow.com/a/17674787/798642
                    googleMap.snapshot(bitmap -> FileSystemUtil.saveMapSnapshotImage(getTargetFragment().getContext(), bitmap, tripViewModel.getTripId()));
                }
                InputUtils.hideKeyboardFrom(getContext(), searchText);

                TripLocationSelectedListener listener = (TripLocationSelectedListener) getTargetFragment();
                if (listener == null) {
                    throw new RuntimeException(TAG + ": TargetFragment of this dialog must implement TripLocationSelectedListener");
                }
                listener.onTripLocationSelected(mapLocation, locationDescription.getText().toString(), requestCode);
                dismiss();
            }
        });

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
                    if (location != null && displayForUserCurrentLocation) {
                        mapLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                    mapFragment.getMapAsync(TripLocationPickerFragment.this);
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapClickListener(this);
        googleMap.setMyLocationEnabled(true);

        UiSettings uiSettings = googleMap.getUiSettings();
        //uiSettings.setMyLocationButtonEnabled(true); // when something is selected on map, this shows two Maps Intents button icons (Directions, Map)
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);

        updateMapView();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mapLocation = latLng;
        updateMapView(MapSettings.MAP_CLICK_ZOOM);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        InputUtils.hideKeyboardFrom(getContext(), getView());
    }

    private void updateMapView() {
        updateMapView(lastMapZoomLevel);
    }

    private void updateMapView(float zoomLevel) {
        if (mapLocation == null) return;
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(mapLocation));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mapLocation)
                .zoom(zoomLevel)
                .bearing(MapSettings.MAP_DEFAULT_BEARING)
                .tilt(MapSettings.MAP_DEFAULT_TILT)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
        lastMapZoomLevel = zoomLevel;
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
                            mapLocation = new LatLng(place.getGeometry().getLocation().getLat(), place.getGeometry().getLocation().getLng());
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putInt(ArgumentKeys.KEY_REQUEST_CODE, requestCode);
        if (googleMap != null) {
            savedInstanceState.putFloat(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL, googleMap.getCameraPosition().zoom);
        }
        savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)mapLocation.latitude);
        savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)mapLocation.longitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    public interface TripLocationSelectedListener {
        void onTripLocationSelected(LatLng location, String description, int requestCode);
    }
}
