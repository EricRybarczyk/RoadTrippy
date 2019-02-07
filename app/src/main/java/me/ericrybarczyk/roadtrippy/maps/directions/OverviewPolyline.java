// This class was generated from Google Maps Directions API JSON using http://www.jsonschema2pojo.org/

package me.ericrybarczyk.roadtrippy.maps.directions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class OverviewPolyline {

    @SerializedName("points")
    @Expose
    private String points;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

}
