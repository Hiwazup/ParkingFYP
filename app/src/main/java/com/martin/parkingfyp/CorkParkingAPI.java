package com.martin.parkingfyp;

import com.martin.parkingfyp.model.CorkCarPark;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CorkParkingAPI {

    @GET("api/action/datastore_search?resource_id=6cc1028e-7388-4bc5-95b7-667a59aa76dc&fields=\"name\"&fields=\"free_spaces\"")
    Call<CorkCarPark> getCarParks();
}
