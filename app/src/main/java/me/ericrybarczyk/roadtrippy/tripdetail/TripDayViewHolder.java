package me.ericrybarczyk.roadtrippy.tripdetail;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.ericrybarczyk.roadtrippy.R;

public class TripDayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private OnTripDayListClickListener onTripDayListClickListener;
    private OnNavigationClickListener onNavigationClickListener;

    @BindView(R.id.trip_day_item_container) protected ConstraintLayout layoutContainer;
    @BindView(R.id.day_number) protected TextView dayNumber;
    @BindView(R.id.day_primary_description) protected TextView dayPrimaryDescription;
    @BindView(R.id.day_user_notes) protected TextView dayUserNotes;
    @BindView(R.id.icon_navigate_trip_day) protected ImageView iconNavigation;

    public TripDayViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    // TODO: integrate with Presenter?
    public void setTripDayListClickListener(OnTripDayListClickListener listClickistener) {
        onTripDayListClickListener = listClickistener;
    }
    public void setNavigationClickListener(OnNavigationClickListener navigationListener) {
        onNavigationClickListener = navigationListener;
    }

    @Override
    @OnClick
    public void onClick(View v) {

    }

    interface OnTripDayListClickListener {
        void onTripDayListItemClick();
    }

    interface OnNavigationClickListener {
        void onNavigationClick();
    }
}
