/*
 * Copyright (C) 2018 Eric Rybarczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.directions.DirectionsResponse;
import me.ericrybarczyk.roadtrippy.maps.directions.Leg;
import me.ericrybarczyk.roadtrippy.maps.directions.Route;
import me.ericrybarczyk.roadtrippy.maps.directions.Step;
import me.ericrybarczyk.roadtrippy.maps.endpoints.GetDirectionsEndpoint;
import me.ericrybarczyk.roadtrippy.maps.endpoints.SearchService;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class TripOverviewMapFragment extends DialogFragment implements OnMapReadyCallback {

    private TripViewModel tripViewModel;
    private SupportMapFragment mapFragment;
    private String googleMapsApiKey;
    private GoogleMap googleMap;
    private static final String TAG = TripOverviewMapFragment.class.getSimpleName();

    @BindView(R.id.heading_create_trip_tom) protected TextView headingText;
    @BindView(R.id.trip_confirm_button_tom) protected Button tripConfirmButton;

    public static TripOverviewMapFragment newInstance() {
        return new TripOverviewMapFragment();
    }

    public TripOverviewMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleMapsApiKey = getString(R.string.google_maps_key);
        tripViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TripViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_trip_overview_map, container, false);
        ButterKnife.bind(this, rootView);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
        }
        getChildFragmentManager().beginTransaction().replace(R.id.map_container_tom, mapFragment).commitNow();

        headingText.setText(tripViewModel.getDescription());

        tripConfirmButton.setOnClickListener(v -> {
            TripConfirmationListener listener = (TripConfirmationListener) getTargetFragment();
            if (listener == null) {
                throw new RuntimeException(TAG + ": TargetFragment of this dialog must implement TripConfirmationListener");
            }
            listener.onTripConfirmation();
            dismiss();
        });

        mapFragment.getMapAsync(this);

        return rootView;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.clear();
        googleMap.setMyLocationEnabled(true);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);

        // get Directions for the trip origin to destination to show user the route overview
        String origin = tripViewModel.getOriginLatLng().latitude + "," + tripViewModel.getOriginLatLng().longitude;
        String destination = tripViewModel.getDestinationLatLng().latitude + "," + tripViewModel.getDestinationLatLng().longitude;
        String alternatives = "true";

        GetDirectionsEndpoint endpoint = SearchService.getClient().create(GetDirectionsEndpoint.class);
        Call<DirectionsResponse> directionsCall = endpoint.getDirections(googleMapsApiKey, origin, destination, alternatives);
        directionsCall.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                DirectionsResponse directionsResponse = response.body();

                // Convert overview_polyline into list of LatLng and add as Polyline
                if ((directionsResponse != null && directionsResponse.getRoutes() != null ? directionsResponse.getRoutes().size() : 0) > 0) {

                    // use the first route for simple overview. Actual navigation will be in Google Maps and user can make changes
                    Route selectedRoute = null;
                    if (directionsResponse.getRoutes().size() == 1) {
                        selectedRoute = directionsResponse.getRoutes().get(0);
                    } else {
                        // evaluate routes, take the first one with no ferries
                        // Route->Legs->Steps->maneuver
                        boolean hasFerry;
                        String ferryIndicatorWord = getString(R.string.maps_api_indicator_ferry);
                        for (Route route: directionsResponse.getRoutes()) {
                            hasFerry = false;
                            evaluateRoute:
                            for (Leg leg : route.getLegs()) {
                                for (Step step : leg.getSteps()) {
                                    if (step.getManeuver() != null) {
                                        if (step.getManeuver().toLowerCase().contains(ferryIndicatorWord)) {
                                            hasFerry = true;
                                            break evaluateRoute;
                                        }
                                    }
                                }
                            }
                            if (!hasFerry) {
                                selectedRoute = route;
                                break; // done with outer-most loop
                            }
                        }
                    }
                    // if still no route then all routes have ferries so we take the first option
                    if (selectedRoute == null) {
                        selectedRoute = directionsResponse.getRoutes().get(0);
                    }

                    int durationMinutes = 0;
                    for (Leg leg: selectedRoute.getLegs()) {
                        durationMinutes += (leg.getDuration().getValue()) / 60; // value is in seconds, so divide by 60 for minutes
                    }
                    tripViewModel.setDurationMinutes(durationMinutes);

                    // add a marker at the starting & ending points
                    LatLng start = new LatLng(selectedRoute.getLegs().get(0).getStartLocation().getLat(), selectedRoute.getLegs().get(0).getStartLocation().getLng());
                    LatLng end = new LatLng(selectedRoute.getLegs().get(0).getEndLocation().getLat(), selectedRoute.getLegs().get(0).getEndLocation().getLng());
                    googleMap.addMarker(new MarkerOptions().position(start));
                    googleMap.addMarker(new MarkerOptions().position(end));

                    // get the polyline to show
                    String encodedPolyline = selectedRoute.getOverviewPolyline().getPoints();
                    List<LatLng> overviewPoints = PolyUtil.decode(encodedPolyline);

                    // source for TypedValue technique: https://stackoverflow.com/a/8780360/798642
                    TypedValue widthValue = new TypedValue();
                    TypedValue offsetValue = new TypedValue();
                    getResources().getValue(R.dimen.map_polyline_width, widthValue, true);
                    getResources().getValue(R.dimen.map_bounds_expand_amount, offsetValue, true);
                    float width = widthValue.getFloat();
                    float offset = offsetValue.getFloat();

                    // Polyline overviewPolyline =
                    googleMap.addPolyline(new PolylineOptions()
                            .clickable(false)
                            .color(getResources().getColor(R.color.colorMapPolyline))
                            .width(width)
                            .addAll(overviewPoints)
                    );

                    // get the bounds for the map view, expand by offset to make it all fit in view
                    LatLngBounds overviewBounds = new LatLngBounds(
                            new LatLng(selectedRoute.getBounds().getSouthwest().getLat() - offset, selectedRoute.getBounds().getSouthwest().getLng() - offset),
                            new LatLng(selectedRoute.getBounds().getNortheast().getLat() + offset, selectedRoute.getBounds().getNortheast().getLng() + offset)
                    );
                    // Set the camera to the greatest possible zoom level that includes the bounds
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(overviewBounds, 0));

                    // another option: center and set zoom level
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(overviewBounds.getCenter(), 5));

                } else {
                    Timber.d("Retrofit onResponse: No Directions search result.");
                    Toast.makeText(getContext(), R.string.map_directions_failure, Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                Timber.e("Retrofit Callback onFailure: %s", t.getMessage());
                Toast.makeText(getActivity(), R.string.map_directions_call_error_message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onResume() {
        // Technique for full-size dialog display is from https://guides.codepath.com/android/Using-DialogFragment#sizing-dialogs
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = Objects.requireNonNull(getDialog().getWindow()).getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    public interface TripConfirmationListener {
        void onTripConfirmation();
    }

}
