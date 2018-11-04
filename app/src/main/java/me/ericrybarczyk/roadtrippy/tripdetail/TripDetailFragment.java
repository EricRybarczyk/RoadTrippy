package me.ericrybarczyk.roadtrippy.tripdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import me.ericrybarczyk.roadtrippy.dto.TripDay;
import me.ericrybarczyk.roadtrippy.maps.endpoints.NavigationIntentService;
import me.ericrybarczyk.roadtrippy.persistence.DataOptions;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;
import me.ericrybarczyk.roadtrippy.util.FileSystemUtil;
import me.ericrybarczyk.roadtrippy.viewmodels.TripDayViewModel;

public class TripDetailFragment extends Fragment implements TripDetailContract.View {

    @BindView(R.id.trip_detail_image) protected ImageView tripImage;
    @BindView(R.id.trip_description) protected TextView tripDescription;
    @BindView(R.id.trip_days_list) protected RecyclerView tripDaysListRecyclerView;

    private TripDetailContract.Presenter presenter;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private String tripId;
    private String tripNodeKey;
    private String tripDescriptionForDisplay;
    private boolean tripIsArchived = false;

    private static final String TAG = TripDetailFragment.class.getSimpleName();

    public static TripDetailFragment newInstance(String tripId, String tripNodeKey) {
        TripDetailFragment tripDetailFragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        args.putString(ArgumentKeys.KEY_TRIP_NODE_KEY, tripNodeKey);
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
            }
        } else if (getArguments() != null) {
            if (getArguments().containsKey(ArgumentKeys.KEY_TRIP_ID)) {
                tripId = getArguments().getString(ArgumentKeys.KEY_TRIP_ID);
                tripNodeKey = getArguments().getString(ArgumentKeys.KEY_TRIP_NODE_KEY);
            }
            if (getArguments().containsKey(ArgumentKeys.TRIP_IS_ARCHIVED_KEY)) {
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
                TripDayViewModel viewModel = TripDayViewModel.from(model);
                String dayNodeKey = this.getRef(position).getKey();

                holder.dayNumber.setText(String.valueOf(viewModel.getDayNumber()));
                holder.dayPrimaryDescription.setText(viewModel.getPrimaryDescription());
                holder.dayUserNotes.setText(viewModel.getUserNotes());
                if (isToday(viewModel.getTripDayDate())) {
                    holder.layoutContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                }

                // TODO: handle tripIsArchived with different click result?
                holder.setTripDayListClickListener(new TripDayViewHolder.OnTripDayListClickListener() {
                    @Override
                    public void onTripDayListItemClick() {
                        Toast.makeText(getContext(), "Not Implemented Yet", Toast.LENGTH_LONG).show();
//                        fragmentNavigationRequestListener.onTripDayEditFragmentRequest(
//                                FragmentTags.TAG_TRIP_DAY,
//                                viewModel.getTripId(),
//                                tripNodeKey,
//                                viewModel.getDayNumber(),
//                                dayNodeKey);
                    }
                });

                if (viewModel.getDestinations().size() == 0) {
                    holder.iconNavigation.setVisibility(View.INVISIBLE);
                } else {
                    holder.setNavigationClickListener(new TripDayViewHolder.OnNavigationClickListener() {
                        @Override
                        public void onNavigationClick() {
                            switch (viewModel.getDestinations().size()) {
                                case 0:
                                    Log.e(TAG, "onNavigationIconClick with no destinations. Code flow prevents this. git blame!");
                                    return;
                                case 1:
                                    // navigate directly to the single destination;
                                    Intent navigationIntent = NavigationIntentService.getNavigationIntent(viewModel.getDestinations().get(0));
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
        super.onSaveInstanceState(outState);
    }


    @Override
    public void setPresenter(TripDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
