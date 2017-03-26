package com.martin.parkingfyp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.martin.parkingfyp.model.CorkCarPark;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Martin on 02/03/2017.
 */

public class CheckAvailability extends BaseActivity{
    private RecyclerView mCarParks;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<CorkCarParkDetails, AvailabilityHolder> mAdapter;
    private SwipeRefreshLayout swipe;

    private String TAG = "Database";
    OpeningTimes openingTime = new OpeningTimes();

    protected DatabaseReference mRef_Parks = mRef.child("carparks");
    protected DatabaseReference mRef_Hours = mRef.child("opening_hours");
    Query query;
    boolean defaultSort = true;

    int currentVisiblePosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_availability);
        setToolbarText("Availability");

        swipe = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
                swipe.setRefreshing(false);
            }
        });

        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(false);

        mCarParks = (RecyclerView)findViewById(R.id.rvCarPark);
        mCarParks.setHasFixedSize(false);
        mCarParks.setLayoutManager(mManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAdapter != null){
            mAdapter.cleanup();
        }
    }

    private void attachRecyclerViewAdapter(){
        final String dayOfWeek = new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime());
        final String timeS = new SimpleDateFormat("HH.mm").format(Calendar.getInstance().getTime());
        try {
            final double time = Double.parseDouble(timeS);
            if(defaultSort) {
                query = mRef_Parks.orderByChild("rank");
                //query = mRef_Parks.orderByChild("free_spaces");
            } else {
                query = mRef_Parks;
            }
            mAdapter = new FirebaseRecyclerAdapter<CorkCarParkDetails, AvailabilityHolder>(CorkCarParkDetails.class, R.layout.list_item, AvailabilityHolder.class, query) {
                @Override
                protected void populateViewHolder(final AvailabilityHolder viewHolder, final CorkCarParkDetails model, int position) {
                    viewHolder.setTitle(model.getName());
                    mRef_Hours.child(model.getName()).child(dayOfWeek).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            openingTime =  dataSnapshot.getValue(OpeningTimes.class);
                            if(time < openingTime.getOpen() || time > openingTime.getClose()){
                                viewHolder.setSpaces("Closed");
                                viewHolder.setStatus("Closed");
                                viewHolder.setStatusColor(false);
                            } else {
                                viewHolder.setSpaces(model.getFree_spaces());
                                viewHolder.setStatus("Open");
                                viewHolder.setStatusColor(true);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public AvailabilityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    AvailabilityHolder viewHolder = super.onCreateViewHolder(parent, viewType);
                    viewHolder.setOnClickListener(new AvailabilityHolder.ClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            //recyclerViewPosition = position;
                            TextView title = (TextView)view.findViewById(R.id.title);
                            Intent intent = new Intent(view.getContext(), CarParkDetails.class);
                            intent.putExtra("Name", title.getText());
                            view.getContext().startActivity(intent);
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    });
                    return viewHolder;
                }

                @Override
                protected void onDataChanged() {
                    super.onDataChanged();
                }
            };
            mCarParks.setAdapter(mAdapter);
        } catch (Exception e){
            Log.d(TAG, timeS);
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.availability_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.default_sort:
                defaultSort = true;
                attachRecyclerViewAdapter();
                return true;
            case R.id.alpha_sort:
                defaultSort = false;
                attachRecyclerViewAdapter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentVisiblePosition = ((LinearLayoutManager)mCarParks.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        Log.d(TAG, "onPause: " + currentVisiblePosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  (mCarParks.getLayoutManager()).smoothScrollToPosition(mCarParks, null, currentVisiblePosition);
        //Log.d(TAG, "onResume: " + currentVisiblePosition);
       // currentVisiblePosition = 0;
    }
}
