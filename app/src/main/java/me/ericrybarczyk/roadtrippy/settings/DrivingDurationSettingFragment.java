package me.ericrybarczyk.roadtrippy.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;

public class DrivingDurationSettingFragment extends DialogFragment {

    @BindView(R.id.preference_value_driving_hours) protected EditText drivingHours;
    @BindView(R.id.button_save_driving_hours) protected Button saveButton;
    private int currentDrivingHoursPreference;
    private static final String TAG = DrivingDurationSettingFragment.class.getSimpleName();

    public static DrivingDurationSettingFragment newInstance(int currentDrivingHoursPreference) {
        DrivingDurationSettingFragment drivingDurationSettingFragment = new DrivingDurationSettingFragment();
        Bundle args = new Bundle();
        args.putInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE, currentDrivingHoursPreference);
        drivingDurationSettingFragment.setArguments(args);
        return drivingDurationSettingFragment;
    }

    public DrivingDurationSettingFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentDrivingHoursPreference = getArguments().getInt(ArgumentKeys.KEY_DRIVING_DURATION_PREFERENCE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_settings_driving_hours, container, false);
        ButterKnife.bind(this, rootView);

        DrivingDurationPreferenceSaveListener listener = (DrivingDurationPreferenceSaveListener) getTargetFragment();
        if (listener == null) {
            throw new RuntimeException(TAG + ": TargetFragment of this dialog must implement DrivingDurationPreferenceSaveListener");
        }

        drivingHours.setText(String.valueOf(currentDrivingHoursPreference));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = drivingHours.getText().toString().trim();
                if (!value.isEmpty()) {
                    int hours = Integer.parseInt(value);
                    if (hours > 48 || hours < 1) {
                        Toast.makeText(getContext(), getString(R.string.error_driving_duration_value_invalid), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onDrivingDurationPreferenceSave(hours);
                    dismiss();
                }
            }
        });

        return rootView;
    }

    public interface DrivingDurationPreferenceSaveListener {
        void onDrivingDurationPreferenceSave(int drivingDuration);
    }


}
