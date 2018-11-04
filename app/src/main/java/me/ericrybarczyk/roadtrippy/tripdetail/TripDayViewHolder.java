package me.ericrybarczyk.roadtrippy.tripdetail;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.ericrybarczyk.roadtrippy.R;

public class TripDayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TripDayListClickListener tripDayListClickListener;
    private NavigationClickListener navigationClickListener;

    @BindView(R.id.trip_day_item_container) protected ConstraintLayout layoutContainer;
    @BindView(R.id.day_number) protected TextView dayNumber;
    @BindView(R.id.day_primary_description) protected TextView dayPrimaryDescription;
    @BindView(R.id.day_user_notes) protected TextView dayUserNotes;
    @BindView(R.id.icon_navigate_trip_day) protected ImageView iconNavigation;
    private static final String TAG = TripDayViewHolder.class.getSimpleName();

    public TripDayViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setTripDayListClickListener(TripDayListClickListener listClickListener) {
        tripDayListClickListener = listClickListener;
    }
    public void setNavigationClickListener(NavigationClickListener navigationListener) {
        navigationClickListener = navigationListener;
    }

    @Override
    @OnClick
    public void onClick(View v) {
        if (tripDayListClickListener == null) {
            Log.e(TAG, "onTripDayListClickListener is null");
            return;
        }
        tripDayListClickListener.onTripDayListItemClick();
    }

    @OnClick(R.id.icon_navigate_trip_day)
    public void onNavigationIconClick() {
        navigationClickListener.onNavigationClick();
    }

    interface TripDayListClickListener {
        void onTripDayListItemClick();
    }

    interface NavigationClickListener {
        void onNavigationClick();
    }
}
