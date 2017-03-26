package com.martin.parkingfyp;

import android.*;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.martin.parkingfyp.model.CorkCarPark;
import com.martin.parkingfyp.model.CorkCarParkDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout fullLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private static FirebaseDatabase mDatabase;
    protected DatabaseReference mRef = getDatabase().getReference();
    protected DatabaseReference mRef_Parks = mRef.child("carparks");
    protected DatabaseReference mRef_Hours = mRef.child("opening_hours");
    protected DatabaseReference mRef_Prices = mRef.child("prices");
    protected LatLng destinationLatLng;
    protected LatLng sourceLatLng;
    protected int position = -1;

    private static final String BASE_URL = "http://data.corkcity.ie/";
    private CorkParkingAPI corkCarPark;

    private String TAG = "Location";

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullLayout);
        toolbar = getToolbar();
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        if (useToolbar()) {setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        setUpNavView();
        requestData();
    }

    public static FirebaseDatabase getDatabase(){
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }



    public Toolbar getToolbar() {
        return (Toolbar)findViewById(R.id.app_bar);
    }

    protected boolean useToolbar() {
        return true;
    }

    protected void setUpNavView() {
        navigationView.setNavigationItemSelectedListener(this);
        if (useDrawerToggle()) {
            drawerToggle = new ActionBarDrawerToggle(this, fullLayout, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            drawerToggle.syncState();
        } else if (useToolbar() && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected boolean useDrawerToggle() {
        return true;
    }

    protected void setToolbarText(String text){
        TextView title = (TextView) findViewById(R.id.toolbar_title);
        title.setText(text);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        fullLayout.closeDrawer(GravityCompat.START);
        return onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.car_parks:
                startActivity(new Intent(this, CheckAvailability.class));
                return true;
            case R.id.map:
                startActivity(new Intent(this, MapsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //mMap.setMyLocationEnabled(true);
            } else{
                boolean showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
                if(!showRationale){
                    Toast.makeText(this, "Location Is Required", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void requestData() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
                corkCarPark = retrofit.create(CorkParkingAPI.class);
                Call<CorkCarPark> cork = corkCarPark.getCarParks();
                cork.enqueue(new Callback<CorkCarPark>() {
                    @Override
                    public void onResponse(Call<CorkCarPark> call, Response<CorkCarPark> response) {
                        ArrayList<CorkCarPark.Result.Records> carParks = response.body().getRecords();
                        for(CorkCarPark.Result.Records record : carParks){
                            mRef_Parks.child(record.getName()).child("free_spaces").setValue(record.getFree_spaces());
                        }
                    }

                    @Override
                    public void onFailure(Call<CorkCarPark> call, Throwable t) {
                        Log.d("JSON", "Response" + t.getMessage());
                    }
                });
                handler.postDelayed(this, 120000);
            }
        }, 1000);

    }
}
