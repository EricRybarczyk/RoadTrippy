package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.util.InputUtils;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTripFragment extends Fragment
        implements  AddEditTripContract.View,
                    DatePickerFragment.TripDateSelectedListener,
                    TripOriginPickerFragment.TripOriginSelectedListener,
                    TripLocationPickerFragment.TripLocationSelectedListener,
                    TripOverviewMapFragment.TripConfirmationListener {

    private TripViewModel tripViewModel;
    private AddEditTripContract.Presenter presenter;

    @BindView(R.id.trip_name_text) protected EditText tripNameText;
    @BindView(R.id.departure_date_button) protected Button departureDateButton;
    @BindView(R.id.return_date_button) protected Button returnDateButton;
    @BindView(R.id.origin_button) protected Button originButton;
    @BindView(R.id.destination_button) protected Button destinationButton;
    @BindView(R.id.option_return_directions) protected CheckBox optionReturnDirections;
    @BindView(R.id.create_trip_next_button) protected Button nextStepButton;

    private static final String TAG = AddEditTripFragment.class.getSimpleName();

    public static AddEditTripFragment newInstance() {
        return new AddEditTripFragment();
    }

    public AddEditTripFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TripViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_create_trip, container, false);
        ButterKnife.bind(this, rootView);

        if (tripViewModel.isDescriptionEdited()) {
            tripNameText.setText(tripViewModel.getDescription());
        }
        if (tripViewModel.isDepartureDateEdited()) {
            departureDateButton.setText(tripViewModel.getDepartureDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        }
        if (tripViewModel.isReturnDateEdited()) {
            returnDateButton.setText(tripViewModel.getReturnDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        }
        if (tripViewModel.isOriginLatLngEdited()) {
            originButton.setText(tripViewModel.getOriginDescription());
        }
        if (tripViewModel.isDestinationLatLngEdited()) {
            destinationButton.setText(tripViewModel.getDestinationDescription());
        }
        if (tripViewModel.isIncludeReturnEdited()) {
            optionReturnDirections.setChecked(tripViewModel.isIncludeReturn());
        }
//        tripNameText.getText().clear();

        departureDateButton.setOnClickListener(v -> {
            saveTripName();
            departureDateButton.requestFocus();
            InputUtils.hideKeyboardFrom(getContext(), rootView);
            DatePickerFragment datePickerDialog = new DatePickerFragment();

            Bundle args = new Bundle();
            args.putSerializable(ArgumentKeys.KEY_CALENDAR_FOR_DISPLAY, tripViewModel.getDepartureDate());
            datePickerDialog.setArguments(args);
            datePickerDialog.show(getChildFragmentManager(), FragmentTags.TAG_DEPARTURE_DATE_DIALOG);
        });

        returnDateButton.setOnClickListener(v -> {
            saveTripName();
            returnDateButton.requestFocus();
            InputUtils.hideKeyboardFrom(getContext(), rootView);
            DatePickerFragment datePickerDialog = new DatePickerFragment();

            Bundle args = new Bundle();
            args.putSerializable(ArgumentKeys.KEY_CALENDAR_FOR_DISPLAY, tripViewModel.getReturnDate());
            datePickerDialog.setArguments(args);
            datePickerDialog.show(getChildFragmentManager(), FragmentTags.TAG_RETURN_DATE_DIALOG);
        });

        originButton.setOnClickListener(v -> {
            saveTripName();
            TripOriginPickerFragment pickerFragment = TripOriginPickerFragment.newInstance();
            pickerFragment.setTargetFragment(AddEditTripFragment.this, RequestCodes.TRIP_ORIGIN_REQUEST_CODE);
            pickerFragment.show(Objects.requireNonNull(getFragmentManager()), ArgumentKeys.TAG_PICK_ORIGIN_DIALOG);
        });

        destinationButton.setOnClickListener(v -> {
            saveTripName();
            TripLocationPickerFragment tripLocationPickerFragment = TripLocationPickerFragment.newInstance(RequestCodes.TRIP_DESTINATION_REQUEST_CODE);
            // if trip already has a destination set we need to show that on the map
            if (tripViewModel.getDestinationLatLng() != null) {
                Bundle args = new Bundle();
                args.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)tripViewModel.getDestinationLatLng().latitude);
                args.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)tripViewModel.getDestinationLatLng().longitude);
                args.putString(MapSettings.KEY_MAP_DISPLAY_LOCATION_DESCRIPTION, tripViewModel.getDestinationDescription());
                if (tripLocationPickerFragment.getArguments() != null) {
                    tripLocationPickerFragment.getArguments().putAll(args);
                } else {
                    tripLocationPickerFragment.setArguments(args);
                }
            }
            tripLocationPickerFragment.setTargetFragment(AddEditTripFragment.this, RequestCodes.TRIP_DESTINATION_REQUEST_CODE);
            tripLocationPickerFragment.show(Objects.requireNonNull(getFragmentManager()), FragmentTags.TAG_CREATE_TRIP);
        });

        nextStepButton.setOnClickListener(v -> {
            saveTripName();
            if (tripViewModel.isValidForSave()) {
                TripOverviewMapFragment tripOverviewMapFragment = TripOverviewMapFragment.newInstance();
                tripOverviewMapFragment.setTargetFragment(AddEditTripFragment.this, RequestCodes.TRIP_OVERVIEW_MAP_REQUEST_CODE);
                tripOverviewMapFragment.show(Objects.requireNonNull(getFragmentManager()), FragmentTags.TAG_TRIP_OVERVIEW_MAP);
            } else {
                Toast.makeText(getContext(), R.string.error_create_trip_data_validation, Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    private void saveTripName() {
        if (tripNameText.getText().length() > 0) {
            tripViewModel.setDescription(tripNameText.getText().toString().trim());
        }
    }

    @Override
    public void onTripDateSelected(int year, int month, int dayOfMonth, String tag) {
        if (tag.equals(FragmentTags.TAG_DEPARTURE_DATE_DIALOG)) {
            tripViewModel.setDepartureDate(LocalDate.of(year, month, dayOfMonth));
            departureDateButton.setText(tripViewModel.getDepartureDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        } else if (tag.equals(FragmentTags.TAG_RETURN_DATE_DIALOG)) {
            tripViewModel.setReturnDate(LocalDate.of(year, month, dayOfMonth));
            returnDateButton.setText(tripViewModel.getReturnDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        }
    }

    @Override
    public void onTripOriginSelected(String key) {
        if (key.equals(ArgumentKeys.KEY_HOME_ORIGIN)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()).getApplicationContext());
            if (preferences.contains(ArgumentKeys.KEY_HOME_LOCATION_LATITUDE_PREFERENCE) && preferences.contains(ArgumentKeys.KEY_HOME_LOCATION_LONGITUDE_PREFERENCE)) {
                tripViewModel.setOriginLatLng(
                        new LatLng(
                                (double) preferences.getFloat(ArgumentKeys.KEY_HOME_LOCATION_LATITUDE_PREFERENCE, 0.0f),
                                (double) preferences.getFloat(ArgumentKeys.KEY_HOME_LOCATION_LONGITUDE_PREFERENCE, 0.0f)
                        )
                );
                String home = getString(R.string.word_for_HOME);
                tripViewModel.setOriginDescription(home);
                originButton.setText(home);
            } else {
                Toast.makeText(getContext(), R.string.error_home_location_preference_not_found, Toast.LENGTH_LONG).show();
            }

        } else if (key.equals(ArgumentKeys.KEY_PICK_ORIGIN)) {

            TripLocationPickerFragment tripLocationPickerFragment = TripLocationPickerFragment.newInstance(RequestCodes.TRIP_ORIGIN_REQUEST_CODE);
            tripLocationPickerFragment.setTargetFragment(AddEditTripFragment.this, RequestCodes.TRIP_ORIGIN_REQUEST_CODE);

            if (tripViewModel.getOriginLatLng() != null) {
                // request a map centered on the location already selected
                Bundle args = new Bundle();
                args.putFloat(MapSettings.KEY_MAP_DISPLAY_LATITUDE, (float)tripViewModel.getOriginLatLng().latitude);
                args.putFloat(MapSettings.KEY_MAP_DISPLAY_LONGITUDE, (float)tripViewModel.getOriginLatLng().longitude);
                args.putString(MapSettings.KEY_MAP_DISPLAY_LOCATION_DESCRIPTION, tripViewModel.getOriginDescription());

                if (tripLocationPickerFragment.getArguments() != null) {
                    tripLocationPickerFragment.getArguments().putAll(args);
                } else {
                    tripLocationPickerFragment.setArguments(args);
                }
            }
            tripLocationPickerFragment.show(Objects.requireNonNull(getFragmentManager()), FragmentTags.TAG_CREATE_TRIP);
        }
    }

    @Override
    public void setPresenter(AddEditTripContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void onTripLocationSelected(LatLng location, String description, int requestCode) {
        switch (requestCode) {
            case RequestCodes.TRIP_ORIGIN_REQUEST_CODE:
                tripViewModel.setOriginLatLng(location);
                tripViewModel.setOriginDescription(description);
                originButton.setText(description);
                break;
            case RequestCodes.TRIP_DESTINATION_REQUEST_CODE:
                tripViewModel.setDestinationLatLng(location);
                tripViewModel.setDestinationDescription(description);
                destinationButton.setText(description);
                break;
        }
    }

    @Override
    public void onTripConfirmation() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        int defaultHours = Integer.parseInt(getResources().getString(R.string.pref_daily_driving_hours_default));
        int drivingHoursPreference = preferences.getInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE, defaultHours);
        presenter.saveTrip(getContext(), tripViewModel, drivingHoursPreference);
    }

    @Override
    public void showTripList() {
        Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
