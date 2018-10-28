package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

public class TripLocationPickerFragment extends DialogFragment
        implements View.OnClickListener {

    private TripViewModel tripViewModel;
    private AddEditTripContract.Presenter presenter;
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

    @BindView(R.id.search_button_trip_location_picker) protected Button searchButton;
    @BindView(R.id.set_location_button_trip_location_picker) protected Button setLocationButton;
    @BindView(R.id.description_text_trip_location_picker) protected EditText locationDescription;
    @BindView(R.id.search_text_trip_location_picker) protected EditText searchText;

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
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        searchButton.setOnClickListener(this);

        if (argumentLocationDescription != null) {
            locationDescription.setText(argumentLocationDescription);
            argumentLocationDescription = null; // clear this out so it doesn't persist if the fragment is loaded again
        }


        rootView.clearFocus();
        return rootView;
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


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putInt(ArgumentKeys.KEY_REQUEST_CODE, requestCode);
        savedInstanceState.putFloat(ArgumentKeys.KEY_LAST_MAP_ZOOM_LEVEL, lastMapZoomLevel);
        savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)mapLocation.latitude);
        savedInstanceState.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)mapLocation.longitude);
        super.onSaveInstanceState(savedInstanceState);
    }
}
