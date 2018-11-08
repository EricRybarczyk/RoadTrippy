package me.ericrybarczyk.roadtrippy.tripdetail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;

public class NavigationDestinationHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.navigate_to_destination_button) protected Button navigateButton;

    public NavigationDestinationHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}