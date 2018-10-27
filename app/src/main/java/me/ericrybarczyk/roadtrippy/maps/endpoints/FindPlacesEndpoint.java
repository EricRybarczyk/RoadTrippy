package me.ericrybarczyk.roadtrippy.maps.endpoints;

import me.ericrybarczyk.roadtrippy.maps.places.PlacesResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FindPlacesEndpoint {

    @GET("place/findplacefromtext/json") // ?key={apikey}&inputtype={input}&fields={fieldList}
    Call<PlacesResponse> findPlaces(@Query("key") String apikey, @Query("inputtype") String inputType, @Query("input") String searchTerm, @Query("fields") String fields);
}
