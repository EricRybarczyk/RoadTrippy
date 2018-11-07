package me.ericrybarczyk.roadtrippy.tripdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

import org.threeten.bp.LocalDate;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.maps.endpoints.NavigationIntentService;
import me.ericrybarczyk.roadtrippy.persistence.DataOptions;
import me.ericrybarczyk.roadtrippy.tripday.TripDayActivity;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;
import me.ericrybarczyk.roadtrippy.util.FileSystemUtil;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripDayViewModel;

public class TripDetailFragment extends Fragment implements TripDetailContract.View {

    @BindView(R.id.trip_detail_image) protected ImageView tripImage;
    @BindView(R.id.trip_description) protected TextView tripDescription;
    @BindView(R.id.trip_days_list) protected RecyclerView tripDaysListRecyclerView;

    private TripDetailContract.Presenter presenter;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private String tripId;
    private String tripNodeKey;
    private boolean tripIsArchived = false;
    private static final String TAG = TripDetailFragment.class.getSimpleName();

    public static TripDetailFragment newInstance(String tripId, String tripNodeKey, boolean tripIsArchived) {
        TripDetailFragment tripDetailFragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        args.putString(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
        args.putBoolean(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, tripIsArchived);
        tripDetailFragment.setArguments(args);
        return tripDetailFragment;
    }

    public TripDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ArgumentKeys.KEY_TRIP_ID)) {
                tripId = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_ID);
                tripNodeKey = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_NODE_KEY);
                tripIsArchived = savedInstanceState.getBoolean(ArgumentKeys.TRIP_IS_ARCHIVED_KEY);
            }
        } else if (getArguments() != null) {
            if (getArguments().containsKey(ArgumentKeys.KEY_TRIP_ID)) {
                tripId = getArguments().getString(ArgumentKeys.KEY_TRIP_ID);
                tripNodeKey = getArguments().getString(ArgumentKeys.KEY_TRIP_NODE_KEY);
                tripIsArchived = getArguments().getBoolean(ArgumentKeys.TRIP_IS_ARCHIVED_KEY);
            }
        }

        DataOptions dataOptions = new DataOptions(AuthenticationManager.getCurrentUser().getUid());

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TripDay, TripDayViewHolder>(dataOptions.getTripDayDataOptions(tripId)) {
            @NonNull
            @Override
            public TripDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_day_list_item, parent, false);
                return new TripDayViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TripDayViewHolder holder, int position, @NonNull TripDay model) {
                TripDayViewModel tripDayViewModel = TripDayViewModel.from(model);
                String dayNodeKey = this.getRef(position).getKey();

                holder.dayNumber.setText(String.valueOf(tripDayViewModel.getDayNumber()));
                holder.dayPrimaryDescription.setText(tripDayViewModel.getPrimaryDescription());
                holder.dayUserNotes.setText(tripDayViewModel.getUserNotes());
                if (isToday(tripDayViewModel.getTripDayDate())) {
                    holder.layoutContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                } else { // must reset color or else recycled items could retain the highlight color
                    holder.layoutContainer.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                }

                // TODO: handle tripIsArchived with different click result?
                holder.setTripDayListClickListener(new TripDayViewHolder.TripDayListClickListener() {
                    @Override
                    public void onTripDayListItemClick() {
                        Intent intent = new Intent(getContext(), TripDayActivity.class);
                        intent.putExtra(ArgumentKeys.KEY_TRIP_ID, tripDayViewModel.getTripId());
                        intent.putExtra(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
                        intent.putExtra(ArgumentKeys.KEY_TRIP_DAY_NUMBER, tripDayViewModel.getDayNumber());
                        intent.putExtra(ArgumentKeys.KEY_DAY_NODE_KEY, dayNodeKey);
                        intent.putExtra(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, tripIsArchived);
                        startActivityForResult(intent, RequestCodes.TRIP_DAY_REQUEST_CODE);
                    }
                });

                if (tripDayViewModel.getDestinations().size() == 0) {
                    holder.iconNavigation.setVisibility(View.INVISIBLE);
                } else {
                    holder.iconNavigation.setVisibility(View.VISIBLE);
                    holder.setNavigationClickListener(new TripDayViewHolder.NavigationClickListener() {
                        @Override
                        public void onNavigationClick() {
                            switch (tripDayViewModel.getDestinations().size()) {
                                case 0:
                                    Log.e(TAG, "onNavigationIconClick with no destinations. Code flow prevents this. git blame!");
                                    return;
                                case 1:
                                    // navigate directly to the single destination;
                                    Intent navigationIntent = NavigationIntentService.getNavigationIntent(tripDayViewModel.getDestinations().get(0));
                                    if (navigationIntent.resolveActivity(getContext().getPackageManager()) != null) {
                                        startActivity(navigationIntent);
                                    } else {
                                        Toast.makeText(getContext(), R.string.error_message_system_missing_google_maps, Toast.LENGTH_LONG).show();
                                    }
                                    return;
                                default:
                                    // show the picker
                                    NavigationPickerFragment pickerFragment = NavigationPickerFragment.newInstance(tripId, dayNodeKey, TripDetailFragment.this.presenter);
                                    pickerFragment.show(getChildFragmentManager(), ArgumentKeys.TAG_PICK_NAVIGATION_DIALOG);
                            }
                        }
                    });
                }

            }

            private boolean isToday(LocalDate tripDayDate) {
                return LocalDate.now().equals(tripDayDate);
            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_trip_detail, container, false);
        ButterKnife.bind(this, rootView);

        try {
            presenter.getTrip(AuthenticationManager.getCurrentUser().getUid(), tripNodeKey, tripIsArchived);
        } catch (Exception e) {
            Snackbar.make(tripDaysListRecyclerView, R.string.error_message_data_lookup_exception, Snackbar.LENGTH_SHORT).show();
            return rootView;
        }

        File mapImage = FileSystemUtil.getPrimaryTripImageFile(getContext(), tripId);
        Picasso.with(getContext())
                .load(mapImage)
                .placeholder(R.drawable.map_placeholder)
                .error(R.drawable.map_placeholder)
                .into(tripImage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        tripDaysListRecyclerView.setLayoutManager(layoutManager);
        tripDaysListRecyclerView.setHasFixedSize(true);
        tripDaysListRecyclerView.setAdapter(firebaseRecyclerAdapter);

        return rootView;
    }

    @Override
    public void displayTrip(Trip trip) {
        if (trip == null) {
            tripDescription.setText(getString(R.string.phrase_for_YourTrip));
            return;
        }
        tripDescription.setText(trip.getDescription());
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        outState.putString(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
        outState.putBoolean(ArgumentKeys.TRIP_IS_ARCHIVED_KEY, tripIsArchived);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setPresenter(TripDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

}
