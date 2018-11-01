package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;

public class TripOriginPickerFragment extends DialogFragment  {

    @BindView(R.id.home_origin_button) protected Button homeOriginButton;
    @BindView(R.id.pick_origin_button) protected Button pickOriginButton;
    private static final String TAG = TripOriginPickerFragment.class.getSimpleName();

    public TripOriginPickerFragment() {
    }

    public static TripOriginPickerFragment newInstance() {
        TripOriginPickerFragment pickerFragment = new TripOriginPickerFragment();
        pickerFragment.setStyle(STYLE_NO_TITLE, 0);
        return pickerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_select_trip_origin, container);
        ButterKnife.bind(this, rootView);

        TripOriginSelectedListener listener = (TripOriginSelectedListener) getTargetFragment();
        if (listener == null) {
            throw new RuntimeException(TAG + ": TargetFragment of this dialog must implement TripOriginSelectedListener");
        }

        homeOriginButton.setOnClickListener(v -> {
            listener.onTripOriginSelected(ArgumentKeys.KEY_HOME_ORIGIN);
            this.dismiss();
        });
        pickOriginButton.setOnClickListener(v -> {
            listener.onTripOriginSelected(ArgumentKeys.KEY_PICK_ORIGIN);
            this.dismiss();
        });

        return rootView;
    }

    public interface TripOriginSelectedListener {
        void onTripOriginSelected(String key);
    }

}