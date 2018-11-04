package me.ericrybarczyk.roadtrippy.tripdetail;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripDetailPresenter implements TripDetailContract.Presenter {

    private TripDataSource tripDataSource;
    private FirebaseUser firebaseUser;
    private TripDetailContract.View tripDetailView;
    private static final String TAG = TripDetailPresenter.class.getSimpleName();


    public TripDetailPresenter(@NonNull TripDataSource dataSource, @NonNull TripDetailContract.View view) {
        this.tripDataSource = checkNotNull(dataSource);
        this.firebaseUser = AuthenticationManager.getCurrentUser();
        this.tripDetailView = checkNotNull(view);

        this.tripDetailView.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
