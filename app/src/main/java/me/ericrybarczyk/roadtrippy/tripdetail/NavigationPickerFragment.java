package me.ericrybarczyk.roadtrippy.tripdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.dto.TripLocation;
import me.ericrybarczyk.roadtrippy.maps.endpoints.NavigationIntentService;
import me.ericrybarczyk.roadtrippy.persistence.DataOptions;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;
import me.ericrybarczyk.roadtrippy.util.AuthenticationManager;
import me.ericrybarczyk.roadtrippy.viewmodels.TripLocationViewModel;

public class NavigationPickerFragment extends DialogFragment {

    private String dayNodeKey;
    private String tripId;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @BindView(R.id.navigation_destination_list) protected RecyclerView navigationDestinationsRecyclerView;

    public NavigationPickerFragment() {
    }

    public static NavigationPickerFragment newInstance(String tripId, String dayNodeKey) {
        NavigationPickerFragment navFragment = new NavigationPickerFragment();
        Bundle args = new Bundle();
        args.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        args.putString(ArgumentKeys.KEY_DAY_NODE_KEY, dayNodeKey);
        navFragment.setArguments(args);
        navFragment.setStyle(STYLE_NO_TITLE, 0);
        return navFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dayNodeKey = savedInstanceState.getString(ArgumentKeys.KEY_DAY_NODE_KEY);
            tripId = savedInstanceState.getString(ArgumentKeys.KEY_TRIP_ID);
        } else if (getArguments() != null) {
            dayNodeKey = getArguments().getString(ArgumentKeys.KEY_DAY_NODE_KEY);
            tripId = getArguments().getString(ArgumentKeys.KEY_TRIP_ID);
        }


        DataOptions dataOptions = new DataOptions(AuthenticationManager.getCurrentUser().getUid());
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TripLocation, NavigationDestinationHolder>(dataOptions.getDestinationsForTripDay(tripId, dayNodeKey)) {

            @NonNull
            @Override
            public NavigationDestinationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_destination_list_item, parent, false);
                return new NavigationDestinationHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NavigationDestinationHolder holder, int position, @NonNull TripLocation model) {
                TripLocationViewModel viewModel = TripLocationViewModel.from(model);
                holder.navigateButton.setText(viewModel.getDescription());
                holder.navigateButton.setOnClickListener(v -> {
                    Intent navigationIntent = NavigationIntentService.getNavigationIntent(viewModel);
                    if (navigationIntent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(navigationIntent);
                    } else {
                        Toast.makeText(getContext(), R.string.error_message_system_missing_google_maps, Toast.LENGTH_LONG).show();
                    }
                    dismiss();
                });
            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_select_navigation_destination, container);
        ButterKnife.bind(this, rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        navigationDestinationsRecyclerView.setLayoutManager(layoutManager);
        navigationDestinationsRecyclerView.setAdapter(firebaseRecyclerAdapter);

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
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        outState.putString(ArgumentKeys.KEY_DAY_NODE_KEY, dayNodeKey);
        super.onSaveInstanceState(outState);
    }
}
