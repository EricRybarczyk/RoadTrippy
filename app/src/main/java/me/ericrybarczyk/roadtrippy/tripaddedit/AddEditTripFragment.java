package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.maps.MapDisplayRequestListener;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.util.InputUtils;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTripFragment extends Fragment
        implements  AddEditTripContract.View,
                    TripOriginPickerFragment.TripOriginSelectedListener,
                    DatePickerFragment.TripDateSelectedListener {

    private TripViewModel tripViewModel;
    private AddEditTripContract.Presenter presenter;
    private MapDisplayRequestListener mapDisplayRequestListener;

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
        tripViewModel = ViewModelProviders.of(getActivity()).get(TripViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_create_trip, container, false);
        ButterKnife.bind(this, rootView);

        if (tripViewModel.isEdited()) {
            tripNameText.setText(tripViewModel.getDescription());
            departureDateButton.setText(tripViewModel.getDepartureDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            returnDateButton.setText(tripViewModel.getReturnDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            originButton.setText(tripViewModel.getOriginDescription());
            destinationButton.setText(tripViewModel.getDestinationDescription());
            optionReturnDirections.setChecked(tripViewModel.isIncludeReturn());
        } else {
            tripNameText.getText().clear();
        }

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
            TripOriginPickerFragment pickerFragment = TripOriginPickerFragment.newInstance(this);
            pickerFragment.show(getChildFragmentManager(), ArgumentKeys.TAG_PICK_ORIGIN_DIALOG);
        });

        destinationButton.setOnClickListener(v -> {
            saveTripName();
            if (tripViewModel.getDestinationLatLng() != null) {
                // request a map centered on the location already selected
                mapDisplayRequestListener.onMapDisplayRequested(RequestCodes.TRIP_DESTINATION_REQUEST_CODE, FragmentTags.TAG_CREATE_TRIP, tripViewModel.getDestinationLatLng(), tripViewModel.getDestinationDescription());
            } else {
                mapDisplayRequestListener.onMapDisplayRequested(RequestCodes.TRIP_DESTINATION_REQUEST_CODE, FragmentTags.TAG_CREATE_TRIP);
            }
        });

        nextStepButton.setOnClickListener(v -> {
            saveTripName();
            if (isValidForSave()) {
                //fragmentNavigationRequestListener.onFragmentNavigationRequest(FragmentTags.TAG_TRIP_OVERVIEW_MAP);
            } else {
                Toast.makeText(getContext(), R.string.error_create_trip_data_validation, Toast.LENGTH_LONG).show();
            }
        });


        rootView.clearFocus();
        InputUtils.hideKeyboardFrom(getContext(), rootView);
        return rootView;
    }

    private void saveTripName() {
        if (tripNameText.getText().length() > 0) {
            tripViewModel.setDescription(tripNameText.getText().toString().trim());
        }
    }

    private boolean isValidForSave() {
        return (!tripViewModel.getDescription().isEmpty())
                && (!tripViewModel.getOriginDescription().isEmpty())
                && (!tripViewModel.getDestinationDescription().isEmpty())
                && (tripViewModel.getDepartureDate().compareTo(tripViewModel.getReturnDate()) <= 0)
                && (tripViewModel.getOriginLatLng() != null)
                && (tripViewModel.getDestinationLatLng() != null);
    }

    @Override
    public void onTripDateSelected(int year, int month, int dayOfMonth, String tag) {
        if (tag.equals(FragmentTags.TAG_DEPARTURE_DATE_DIALOG)) {
            tripViewModel.setDepartureDate(LocalDate.of(year, month, dayOfMonth));
            if (tripViewModel.isEdited()) {
                departureDateButton.setText(tripViewModel.getDepartureDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            }

        } else if (tag.equals(FragmentTags.TAG_RETURN_DATE_DIALOG)) {
            tripViewModel.setReturnDate(LocalDate.of(year, month, dayOfMonth));
            if (tripViewModel.isEdited()) {
                returnDateButton.setText(tripViewModel.getReturnDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            }
        }
    }

    @Override
    public void onTripOriginSelected(String key) {

    }@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapDisplayRequestListener) {
            mapDisplayRequestListener = (MapDisplayRequestListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MapDisplayRequestListener");
        }
    }

    @Override
    public void setPresenter(AddEditTripContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }
}
