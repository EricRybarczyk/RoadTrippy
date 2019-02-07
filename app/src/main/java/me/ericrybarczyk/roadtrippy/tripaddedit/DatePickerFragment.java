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

/*  Non-plagiarism statement: this class is directly adapted from
 *  https://developer.android.com/guide/topics/ui/controls/pickers#DatePicker
 */

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import org.threeten.bp.LocalDate;

import java.util.Objects;

import me.ericrybarczyk.roadtrippy.util.FragmentTags;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;
import timber.log.Timber;

public class DatePickerFragment extends DialogFragment
        implements  DatePickerDialog.OnDateSetListener {

    private TripDateSelectedListener tripDateSelectedListener;
    private LocalDate date;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        TripViewModel tripViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(TripViewModel.class);

        assert getTag() != null;
        switch (getTag()) {
            case FragmentTags.TAG_DEPARTURE_DATE_DIALOG:
                date = tripViewModel.getDepartureDate();
                break;
            case FragmentTags.TAG_RETURN_DATE_DIALOG:
                if (tripViewModel.getReturnDate().compareTo(tripViewModel.getDepartureDate()) < 1) {
                    date = tripViewModel.getDepartureDate().plusDays(1);
                } else {
                    date = tripViewModel.getReturnDate();
                }
                break;
        }

        int year = date.getYear();
        int month = date.getMonthValue() - 1; // DatePickerDialog uses 0-based Month so decrement from LocalDate which uses 1-based Month
        int day = date.getDayOfMonth();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tripDateSelectedListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.tripDateSelectedListener == null) {
            Fragment parent = this.getParentFragment();
            if (parent instanceof TripDateSelectedListener) {
                this.tripDateSelectedListener = (TripDateSelectedListener) parent;
            } else {
                throw new RuntimeException("Parent Fragment must implement TripDateSelectedListener");
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // DatePicker uses 0-based month so +1 for org.threeten.bp.LocalDate
        int localDateMonth = month + 1;
        date = LocalDate.of(year, localDateMonth, dayOfMonth);
        try {
            if (tripDateSelectedListener == null) {
                Timber.e("TripDateSelectedListener is null");
                return;
            }
            tripDateSelectedListener.onTripDateSelected(year, localDateMonth, dayOfMonth, this.getTag());
        } catch (ClassCastException e) {
            Timber.e("Containing fragment must implement DatePickerFragment.TripDateSelectedListener");
            throw e;
        }
    }

    public void setTripDateSelectedListener(TripDateSelectedListener listener) {
        this.tripDateSelectedListener = listener;
    }

    public interface TripDateSelectedListener {
        void onTripDateSelected(int year, int month, int dayOfMonth, String tag);
    }
}
