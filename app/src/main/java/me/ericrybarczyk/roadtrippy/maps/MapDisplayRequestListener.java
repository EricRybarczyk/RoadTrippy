package me.ericrybarczyk.roadtrippy.maps;

import com.google.android.gms.maps.model.LatLng;

public interface MapDisplayRequestListener {
    void onMapDisplayRequested(int requestCode, String returnToFragmentTag);
    void onMapDisplayRequested(int requestCode, String returnToFragmentTag, LatLng displayLocation, String locationDescription);
}
