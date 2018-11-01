package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.engine.TripManager;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.persistence.TripRepository;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTripPresenter implements AddEditTripContract.Presenter {

    private TripDataSource tripDataSource;
    private FirebaseUser firebaseUser;
    private AddEditTripContract.View addEditTripView;
    private int preferenceDrivingHours;
    private static final String TAG = AddEditTripPresenter.class.getSimpleName();

    public AddEditTripPresenter(@NonNull TripDataSource dataSource, @NonNull FirebaseUser firebaseUser, @NonNull AddEditTripContract.View view) {
        this.tripDataSource = checkNotNull(dataSource);
        this.firebaseUser = checkNotNull(firebaseUser);
        this.addEditTripView = checkNotNull(view);

        //TODO: decide how to provide user preference to this class
        preferenceDrivingHours = 12;

        this.addEditTripView.setPresenter(this);
    }

    @Override
    public void saveTrip(Context context, TripViewModel tripViewModel) {
        TripManager tripManager = new TripManager();
        Trip trip = tripManager.buildTrip(tripViewModel, firebaseUser.getUid());
        List<TripDay> tripDays = tripManager.buildInitialTripDays(context, tripViewModel, preferenceDrivingHours);

        TripRepository repository = new TripRepository();
        repository.saveTrip(trip, tripDays);

        // reset the state of the ViewModel to default
        tripViewModel.reset();

        // tell the view the work is done
        addEditTripView.showTripList();
    }

    @Override
    public void start() {

    }
}
