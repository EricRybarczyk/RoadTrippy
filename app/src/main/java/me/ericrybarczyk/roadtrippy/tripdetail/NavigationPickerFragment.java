package me.ericrybarczyk.roadtrippy.tripdetail;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import butterknife.BindView;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;

public class NavigationPickerFragment extends DialogFragment {

    private String dayNodeKey;
    private String tripId;
    private TripDetailContract.Presenter presenter;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @BindView(R.id.navigation_destination_list) protected RecyclerView navigationDestinationsRecyclerView;

    public NavigationPickerFragment() {
    }

    public static NavigationPickerFragment newInstance(String tripId, String dayNodeKey, TripDetailContract.Presenter presenter) {
        NavigationPickerFragment navFragment = new NavigationPickerFragment();
        navFragment.presenter = presenter;
        Bundle args = new Bundle();
        args.putString(ArgumentKeys.KEY_TRIP_ID, tripId);
        args.putString(ArgumentKeys.KEY_DAY_NODE_KEY, dayNodeKey);
        navFragment.setArguments(args);
        navFragment.setStyle(STYLE_NO_TITLE, 0);
        return navFragment;
    }
}
