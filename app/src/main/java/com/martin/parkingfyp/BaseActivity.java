package com.martin.parkingfyp;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.martin.parkingfyp.model.CorkCarPark;

import java.util.ArrayList;

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
    private static final String BASE_URL = "http://data.corkcity.ie/";
    private CorkParkingAPI corkCarPark;
    private static FirebaseDatabase mDatabase;
    protected DatabaseReference mRef = getDatabase().getReference();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullLayout);
        toolbar = getToolbar();
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        if (useToolbar()) {
            setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }
        setUpNavView();
    }

    public static FirebaseDatabase getDatabase(){
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public void requestData() {
        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        corkCarPark = retrofit.create(CorkParkingAPI.class);
        Call<CorkCarPark> cork = corkCarPark.getCarParks();
        cork.enqueue(new Callback<CorkCarPark>() {
            @Override
            public void onResponse(Call<CorkCarPark> call, Response<CorkCarPark> response) {
                int statusCode = response.code();
                CorkCarPark corkPark = response.body();
                ArrayList<CorkCarPark.Result.Records> carParks = corkPark.getRecords();
                for(CorkCarPark.Result.Records record : carParks){
                    Log.d("JSON", record.getName() + " : " + record.getFree_spaces());
                    mRef.child(record.getName()).child("free_spaces").setValue(record.getFree_spaces());
                }
                //Log.d("JSON", "Response" + statusCode);
            }

            @Override
            public void onFailure(Call<CorkCarPark> call, Throwable t) {
                Log.d("JSON", "Response" + t.getMessage());
            }
        });
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
                startActivity(new Intent(this, Avail.class));
                return true;
            case R.id.map:
                startActivity(new Intent(this, MapsActivity.class));
                //Toast.makeText(getApplicationContext(), "To Be Done", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
