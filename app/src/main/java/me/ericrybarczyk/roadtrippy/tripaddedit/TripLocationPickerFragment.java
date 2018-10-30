package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.maps.endpoints.FindPlacesEndpoint;
import me.ericrybarczyk.roadtrippy.maps.endpoints.SearchService;
import me.ericrybarczyk.roadtrippy.maps.places.Candidate;
import me.ericrybarczyk.roadtrippy.maps.places.PlacesResponse;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.InputUtils;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class TripLocationPickerFragment extends DialogFragment
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraMoveStartedListener, View.OnClickListener {

    private TripViewModel tripViewModel;
    private SupportMapFragment mapFragment;
    private String googleMapsApiKey;
    private GoogleMap googleMap;
    private LatLng mapLocation;
    private boolean displayForUserCurrentLocation;
    private CameraPosition cameraPosition;
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
        Log.d(TAG, "Using Maps API key: " + googleMapsApiKey);
        lastMapZoomLevel = MapSettings.MAP_DEFAULT_ZOOM;
        displayForUserCurrentLocation = true; // by default map will show users current location

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ArgumentKeys.KEY_START_LAT)) {
                requestCode = savedInstanceState.getInt(ArgumentKeys.KEY_REQUEST_CODE);
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

        tripViewModel = ViewModelProviders.of(getActivity()).get(TripViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_trip_location, container, false);
        ButterKnife.bind(this, rootView);
        rootView.clearFocus();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_tlp);
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
                    googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                        @Override
                        public void onSnapshotReady(Bitmap bitmap) {
                            saveMapSnapshotImage(bitmap);
                        }
                    });
                }
                InputUtils.hideKeyboardFrom(getContext(), searchText);

                TripLocationSelectedListener listener = (TripLocationSelectedListener) getTargetFragment();
                if (listener == null) {
                    throw new RuntimeException(TAG + " must implement TripLocationSelectedListener");
                }
                listener.onTripLocationSelected(mapLocation, locationDescription.getText().toString(), requestCode);
                dismiss();
            }
        });

        int permission = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PERMISSION_GRANTED) {
            updateLocation();
        } else {
            // Permission is not granted - request the permission
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    RequestCodes.LOCATION_PERMISSIONS_REQUEST_CODE);
        }

        view.clearFocus();
    }

    private void saveMapSnapshotImage(Bitmap bitmap) {

        // calculations to slice a part of the image for use in trip list view
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();
        // original is slightly portrait. Remove 20% horizontal 60% vertical, keep centered remainder. Looks good in List View, Picasso centers to fit.
        int startX = Math.round(currentWidth * 0.1f);
        int startY = Math.round(currentHeight * 0.3f);
        int resizedWidth = currentWidth - (startX * 2);
        int resizedHeight = currentHeight - (startY * 2);

        FileOutputStream fos = null;
        try {
            File imageDir = getTargetFragment().getContext().getDir(MapSettings.DESTINATION_MAP_IMAGE_DIRECTORY, Context.MODE_PRIVATE);

            // main image is used on trip detail screen
            File mainFile = new File(imageDir, MapSettings.DESTINATION_MAP_MAIN_PREFIX + tripViewModel.getTripId() + MapSettings.DESTINATION_MAP_IMAGE_EXTENSION);
            fos = new FileOutputStream(mainFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, MapSettings.MAP_IMAGE_SAVE_QUALITY, fos);

            // resized is a narrow image for use in main trip list screen
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, startX, startY, resizedWidth, resizedHeight);
            File resizedFile = new File(imageDir, MapSettings.DESTINATION_MAP_SLICED_PREFIX + tripViewModel.getTripId() + MapSettings.DESTINATION_MAP_IMAGE_EXTENSION);
            fos = new FileOutputStream(resizedFile);
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, MapSettings.MAP_IMAGE_SAVE_QUALITY, fos);

        } catch (FileNotFoundException e) {
            // in case of file errors, placeholder images will be acceptable
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        // Technique for full-size dialog display is from https://guides.codepath.com/android/Using-DialogFragment#sizing-dialogs
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        LocationServices.getFusedLocationProviderClient(getContext())
                .getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (displayForUserCurrentLocation) {
                            mapLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                        mapFragment.getMapAsync(TripLocationPickerFragment.this);
                    }
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
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(mapLocation));
        cameraPosition = new CameraPosition.Builder().target(mapLocation)
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
                public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                    PlacesResponse placesResponse = response.body();
                    if ((placesResponse != null && placesResponse.getCandidates() != null ? placesResponse.getCandidates().size() : 0) > 0) {

                        // if one result, set that as the location and update UI
                        if (placesResponse.getCandidates().size() == 1) {
                            Candidate place = placesResponse.getCandidates().get(0);
                            mapLocation = new LatLng(place.getGeometry().getLocation().getLat(), place.getGeometry().getLocation().getLng());
                            locationDescription.setText(place.getName());
                            updateMapView(MapSettings.MAP_SEARCH_RESULT_ZOOM);
                        } else {
                            Log.d(TAG, "Too many Places Search results. Result count = " + String.valueOf(placesResponse.getCandidates().size()));
                            Toast.makeText(getContext(), R.string.map_search_too_many_results_message, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Log.d(TAG, "Retrofit onResponse: No Places Search results.");
                        Toast.makeText(getContext(), R.string.map_search_no_results_message, Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<PlacesResponse> call, Throwable t) {
                    Log.e(TAG, "Failed to call Places API. Error: " + t.getMessage());
                    Toast.makeText(getContext(), R.string.map_search_call_error_message, Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putInt(ArgumentKeys.KEY_REQUEST_CODE, requestCode);
        savedInstanceState.putFloat(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL, lastMapZoomLevel);
        savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)mapLocation.latitude);
        savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)mapLocation.longitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    public interface TripLocationSelectedListener {
        void onTripLocationSelected(LatLng location, String description, int requestCode);
    }
}
