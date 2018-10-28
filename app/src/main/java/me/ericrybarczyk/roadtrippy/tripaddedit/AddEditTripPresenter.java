package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTripPresenter implements AddEditTripContract.Presenter {

    private TripDataSource tripDataSource;
    private FirebaseUser firebaseUser;
    private AddEditTripContract.View addEditTripView;

    public AddEditTripPresenter(@NonNull TripDataSource dataSource, @NonNull FirebaseUser firebaseUser, @NonNull AddEditTripContract.View view) {
        this.tripDataSource = checkNotNull(dataSource);
        this.firebaseUser = checkNotNull(firebaseUser);
        this.addEditTripView = checkNotNull(view);

        this.addEditTripView.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
