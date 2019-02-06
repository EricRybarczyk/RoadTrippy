package me.ericrybarczyk.roadtrippy.triplist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.ericrybarczyk.roadtrippy.R;
import timber.log.Timber;

public class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private OnTripListClickListener onTripListClickListener;
    private String tripId;
    private String tripNodeKey;

    @BindView(R.id.trip_item_image) protected ImageView tripImage;
    @BindView(R.id.trip_name_text) protected TextView tripName;
    @BindView(R.id.trip_directions_overview_text) protected TextView tripDirectionsOverview;
    @BindView(R.id.trip_date_range_text) protected TextView tripDateRange;
    @BindView(R.id.trip_highlight_one_text) protected TextView highlightOne;
    @BindView(R.id.trip_highlight_two_text) protected TextView highlightTwo;
    @BindView(R.id.driving_duration_text) protected TextView drivingDuration;
    @BindView(R.id.icon_highlight_one) protected TextView iconHighlightOne;
    @BindView(R.id.icon_highlight_two) protected TextView iconHighlightTwo;


    public TripViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setTripNodeKey(String tripNodeKey) {
        this.tripNodeKey = tripNodeKey;
    }

    void setTripListClickListener(OnTripListClickListener listener) {
        onTripListClickListener = listener;
    }

    @Override
    @OnClick
    public void onClick(View v) {
        if (onTripListClickListener == null) {
            Timber.e("onTripListClickListener is null");
            return;
        }
        if (tripId == null) {
            Timber.e("String tripId is null");
            return;
        }
        onTripListClickListener.onTripListItemClick(tripId, tripNodeKey);
    }

    interface OnTripListClickListener {
        void onTripListItemClick(String tripId, String tripDescription);
    }

}
