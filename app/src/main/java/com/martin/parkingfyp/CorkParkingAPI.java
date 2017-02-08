package com.martin.parkingfyp;

import com.martin.parkingfyp.model.CorkCarPark;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Martin on 30/01/2017.
 */

public interface CorkParkingAPI {

    @GET("api/action/datastore_search?resource_id=6cc1028e-7388-4bc5-95b7-667a59aa76dc&fields=\"name\"&fields=\"free_spaces\"")
    Call<CorkCarPark> getCarParks();
}
