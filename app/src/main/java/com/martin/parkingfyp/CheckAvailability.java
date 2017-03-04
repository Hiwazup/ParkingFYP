package com.martin.parkingfyp;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.martin.parkingfyp.model.CorkCarPark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Martin on 22/01/2017.
 */

public class CheckAvailability extends BaseActivity {
    ArrayList<CarParks> carParks;
    ArrayList<CorkCarPark.Result.Records> corky;
    private static final String BASE_URL = "http://data.corkcity.ie/";
    private CorkParkingAPI corkCarPark;
    private CorkCarPark c;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_availability);
        setToolbarText("Availability");
        //swipe = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        //swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /*@Override
            public void onRefresh() {
                requestData();
            }
        });*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setUpRecyclerView(){
        RecyclerView carPark = (RecyclerView) findViewById(R.id.rvCarPark);
        //carParks = CarParks.createList(getResources());
        CarParksAdapter adapter = new CarParksAdapter(getApplicationContext(), corky);
        carPark.setAdapter(adapter);
        carPark.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    public void requestData() {
        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        corkCarPark = retrofit.create(CorkParkingAPI.class);
        //CorkCarPark c;
        Call<CorkCarPark> cork = corkCarPark.getCarParks();
        cork.enqueue(new Callback<CorkCarPark>() {
             @Override
            public void onResponse(Call<CorkCarPark> call, Response<CorkCarPark> response) {
                int statusCode = response.code();
                CorkCarPark corkPark = response.body();
                ArrayList<CorkCarPark.Result.Records> carParks = corkPark.getRecords();
                corky = carParks;
                Log.d("JSON", "Response" + statusCode);
                setUpRecyclerView();
                 swipe.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<CorkCarPark> call, Throwable t) {
                Log.d("JSON", "Response" + t.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_check_availability:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
