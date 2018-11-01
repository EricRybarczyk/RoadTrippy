package me.ericrybarczyk.roadtrippy.triplist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.Trip;
import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.persistence.DataOptions;
import me.ericrybarczyk.roadtrippy.tripaddedit.AddEditTripActivity;
import me.ericrybarczyk.roadtrippy.util.FontManager;
import me.ericrybarczyk.roadtrippy.util.RequestCodes;
import me.ericrybarczyk.roadtrippy.viewmodels.TripViewModel;

import static com.google.common.base.Preconditions.checkNotNull;

public class TripListFragment extends Fragment implements TripListContract.View {

    private TripListContract.Presenter presenter;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @BindView(R.id.trip_list) protected RecyclerView tripListRecyclerView;
    @BindView(R.id.fab) protected FloatingActionButton fab;

    private static final String TAG = TripListFragment.class.getSimpleName();

    public static TripListFragment newInstance() {
        return new TripListFragment();
    }

    public TripListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataOptions dataOptions = new DataOptions(FirebaseAuth.getInstance().getCurrentUser().getUid());

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Trip, TripViewHolder>(dataOptions.getTripListDataOptions()) {

            @NonNull
            @Override
            public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_item, parent, false);
                return new TripViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TripViewHolder holder, int position, @NonNull Trip model) {
                TripViewModel viewModel = TripViewModel.from(this.getItem(position));
                String joinWord = getString(R.string.word_for_TO);
                String hours = getString(R.string.word_for_HOURS);
                String h = getString(R.string.abbreviation_for_HOURS);
                String minutes = getString(R.string.word_for_MINUTES);
                String m = getString(R.string.abbreviation_for_MINUTES);
                String unknown = getString(R.string.word_for_UNKNOWN);

                String tripNodeKey = this.getRef(position).getKey();

                holder.setTripId(viewModel.getTripId());
                holder.setTripNodeKey(tripNodeKey);
                holder.setTripListClickListener(new TripViewHolder.OnTripListClickListener() {
                    @Override
                    public void onTripListItemClick(String tripId, String tripNodeKey) {
                        // TODO - EVAL: fragmentNavigationRequestListener.onFragmentNavigationRequest(FragmentTags.TAG_TRIP_DETAIL, tripId, tripNodeKey, isTripListFromArchive);
                    }
                });

                File imageDir = getContext().getDir(MapSettings.DESTINATION_MAP_IMAGE_DIRECTORY, Context.MODE_PRIVATE);
                String tripImageFilename = MapSettings.DESTINATION_MAP_SLICED_PREFIX + viewModel.getTripId() + MapSettings.DESTINATION_MAP_IMAGE_EXTENSION;
                File mapImage = new File(imageDir, tripImageFilename);
                Picasso.with(getContext())
                        .load(mapImage)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.map_placeholder)
                        .error(R.drawable.map_placeholder)
                        .into(holder.tripImage);

                holder.tripName.setText(viewModel.getDescription());
                holder.tripDirectionsOverview.setText(viewModel.getOriginDestinationSummaryText(joinWord));
                holder.tripDateRange.setText(viewModel.getDateRangeSummaryText(joinWord));
                holder.highlightOne.setText(viewModel.getOriginDescription());
                holder.highlightTwo.setText(viewModel.getDestinationDescription());

                String sb = getString(R.string.label_for_DRIVING_TIME) +
                        " " +
                        viewModel.getDurationDescription(hours, minutes, h, m, unknown);

                holder.drivingDuration.setText(sb);

                holder.iconHighlightOne.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME_SOLID));
                holder.iconHighlightTwo.setTypeface(FontManager.getTypeface(getContext(), FontManager.FONTAWESOME_SOLID));
            }
        };

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);
        ButterKnife.bind(this, rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false); // getContext(), LinearLayoutManager.VERTICAL, false
        tripListRecyclerView.setLayoutManager(layoutManager);
        tripListRecyclerView.setHasFixedSize(true);
        tripListRecyclerView.setAdapter(firebaseRecyclerAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.createTrip();
            }
        });

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
    public void showCreateTrip() {
        Intent intent = new Intent(getContext(), AddEditTripActivity.class);
        startActivityForResult(intent, RequestCodes.CREATE_TRIP_REQUEST_CODE);
    }

    @Override
    public void setPresenter(TripListContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}
