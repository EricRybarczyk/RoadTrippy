package me.ericrybarczyk.roadtrippy.tripday;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.tripaddedit.TripLocationPickerFragment;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;
import me.ericrybarczyk.roadtrippy.util.FontManager;
import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.util.InputUtils;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripDayViewModel;
import me.ericrybarczyk.roadtrippy.viewmodels.TripLocationViewModel;

public class TripDayFragment extends Fragment implements TripDayContract.View, TripLocationPickerFragment.TripLocationSelectedListener {

    private TripDayContract.Presenter presenter;
    private TripDayViewModel tripDayViewModel;
    private String userId;
    private String tripId;
    private String tripNodeKey;
    private String dayNodeKey;
    private int dayNumber;
    private LatLng tripDestination;

    @BindView(R.id.day_number_header) protected TextView dayNumberHeader;
    @BindView(R.id.icon_highlight) protected TextView iconHighlight;
    @BindView(R.id.day_primary_description) protected EditText dayPrimaryDescription;
    @BindView(R.id.search_destination_button) protected Button searchDestinationButton;
    @BindView(R.id.destination_list_label) protected TextView destinationListLabel;
    @BindView(R.id.day_destination_list) protected RecyclerView dayDestinationRecyclerView;
    @BindView(R.id.day_user_notes) protected EditText dayUserNotes;
    @BindView(R.id.save_trip_day_button) protected Button saveTripDayButton;

    private static final String TAG = TripDayFragment.class.getSimpleName();

    public static TripDayFragment newInstance(String tripId, String tripNodeKey, int dayNumber, String dayNodeKey) {
        TripDayFragment tripDayFragment = new TripDayFragment();
        Bundle args = new Bundle();
        args.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        args.putString(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
        args.putInt(ArgumentKeys.KEY_TRIP_DAY_NUMBER, dayNumber);
        args.putString(ArgumentKeys.KEY_DAY_NODE_KEY, dayNodeKey);
        tripDayFragment.setArguments(args);
        return tripDayFragment;
    }

    public TripDayFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = AuthenticationManager.getCurrentUser().getUid();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ArgumentKeys.KEY_TRIP_ID)) {
                tripId = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_ID);
                tripNodeKey = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_NODE_KEY);
                dayNodeKey = savedInstanceState.getString(ArgumentKeys.KEY_DAY_NODE_KEY);
                dayNumber = savedInstanceState.getInt(ArgumentKeys.KEY_TRIP_DAY_NUMBER);
                if (savedInstanceState.containsKey(ArgumentKeys.KEY_TRIP_DESTINATION_LATITUDE)) {
                    tripDestination = new LatLng(
                            savedInstanceState.getDouble(ArgumentKeys.KEY_TRIP_DESTINATION_LATITUDE),
                            savedInstanceState.getDouble(ArgumentKeys.KEY_TRIP_DESTINATION_LONGITUDE)
                    );
                }
            }
        } else if (getArguments() != null) {
            tripId = getArguments().getString(ArgumentKeys.KEY_TRIP_ID);
            tripNodeKey = getArguments().getString(ArgumentKeys.KEY_TRIP_NODE_KEY);
            dayNodeKey = getArguments().getString(ArgumentKeys.KEY_DAY_NODE_KEY);
            dayNumber = getArguments().getInt(ArgumentKeys.KEY_TRIP_DAY_NUMBER);
        }
        if (tripDestination == null) {
            presenter.getTripDestination(userId, tripNodeKey);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_trip_day, container, false);
        ButterKnife.bind(this, rootView);
        InputUtils.hideKeyboardFrom(getContext(), rootView);

        presenter.getTripDay(userId, tripId, dayNodeKey);

        return rootView;
    }

    @Override
    public void displayTripDay(TripDay tripDay) {
        tripDayViewModel = TripDayViewModel.from(tripDay);
        setHighlightIndicator(tripDayViewModel.getIsHighlight());
        dayNumber = tripDayViewModel.getDayNumber();
        String headerText = getString(R.string.word_for_Day) + " " + String.valueOf(dayNumber);
        dayNumberHeader.setText(headerText);
        if (!tripDayViewModel.getIsDefaultText()) {
            dayPrimaryDescription.setText(tripDayViewModel.getPrimaryDescription());
            dayUserNotes.setText(tripDayViewModel.getUserNotes());
        }
        if (tripDayViewModel.getDestinations().size() > 0) {
            dayDestinationRecyclerView.setVisibility(View.VISIBLE);
            destinationListLabel.setVisibility(View.VISIBLE);
            TripLocationAdapter adapter = new TripLocationAdapter(
                    tripDayViewModel.getDestinations(),
                    userId,
                    tripId,
                    dayNodeKey
            );
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            dayDestinationRecyclerView.setLayoutManager(layoutManager);
            dayDestinationRecyclerView.setAdapter(adapter);
        } else {
            dayDestinationRecyclerView.setVisibility(View.INVISIBLE);
            destinationListLabel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setTripDestination(LatLng tripDestination) {
        if (tripDestination == null) {
            // should not be possible, can't get to this screen until trip data is created
            Log.e(TAG, "Trip destination missing for tripId: " + tripId + " , tripNodeKey: " + tripNodeKey);
        }
        this.tripDestination = tripDestination;
    }

    private void setHighlightIndicator(boolean isHighlight) {
        if (isHighlight) {
            iconHighlight.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME_SOLID));
            iconHighlight.setTextColor(ContextCompat.getColor(getContext(), R.color.colorControlHighlight));
        } else {
            iconHighlight.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME_REGULAR));
            iconHighlight.setTextColor(ContextCompat.getColor(getContext(), R.color.colorControlHighlightOff));
        }
    }

    @OnClick(R.id.icon_highlight)
    public void onHighlightClick() {
        tripDayViewModel.setIsHighlight(!tripDayViewModel.getIsHighlight());
        presenter.updateTripDayHighlight(userId, tripId, dayNodeKey, tripDayViewModel.getIsHighlight());
        setHighlightIndicator(tripDayViewModel.getIsHighlight());
    }

    @OnClick(R.id.search_destination_button)
    public void onClick(View v) {
        saveTripDay();
        TripLocationPickerFragment tripLocationPickerFragment = TripLocationPickerFragment.newInstance(RequestCodes.TRIP_DAY_DESTINATION_REQUEST_CODE);
        // start this map on the main trip destination
        Bundle args = new Bundle();
        args.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)tripDestination.latitude);
        args.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)tripDestination.longitude);
        if (tripLocationPickerFragment.getArguments() != null) {
            tripLocationPickerFragment.getArguments().putAll(args);
        } else {
            tripLocationPickerFragment.setArguments(args);
        }
        tripLocationPickerFragment.setTargetFragment(TripDayFragment.this, RequestCodes.TRIP_DAY_DESTINATION_REQUEST_CODE);
        tripLocationPickerFragment.show(getFragmentManager(), FragmentTags.TAG_TRIP_DAY_DESTINATION);
    }

    @Override
    public void onTripLocationSelected(LatLng location, String description, int requestCode) {
        if (requestCode == RequestCodes.TRIP_DAY_DESTINATION_REQUEST_CODE) {
            TripLocationViewModel tripLocationViewModel = new TripLocationViewModel(location.latitude, location.longitude, description, null);
            tripDayViewModel.getDestinations().add(tripLocationViewModel);
            saveTripDay();
        }
    }

    @OnClick(R.id.save_trip_day_button)
    public void onSaveClick() {
        if (dayPrimaryDescription.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), R.string.error_save_trip_day_validation, Toast.LENGTH_LONG).show();
            return;
        }
        saveTripDay();
        showTripDetail();
    }

    private void saveTripDay() {
        tripDayViewModel.setPrimaryDescription(dayPrimaryDescription.getText().toString().trim());
        tripDayViewModel.setUserNotes(dayUserNotes.getText().toString().trim());
        tripDayViewModel.setIsDefaultText(false);
        tripDayViewModel.setTripDayNodeKey(dayNodeKey);
        presenter.updateTripDay(userId, tripId, dayNodeKey, tripDayViewModel);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        outState.putString(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
        outState.putString(ArgumentKeys.KEY_DAY_NODE_KEY, dayNodeKey);
        outState.putInt(ArgumentKeys.KEY_TRIP_DAY_NUMBER, dayNumber);
        if (tripDestination != null) {
            outState.putDouble(ArgumentKeys.KEY_TRIP_DESTINATION_LATITUDE, tripDestination.latitude);
            outState.putDouble(ArgumentKeys.KEY_TRIP_DESTINATION_LONGITUDE, tripDestination.longitude);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showTripDetail() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void setPresenter(TripDayContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
