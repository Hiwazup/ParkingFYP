package com.martin.parkingfyp;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CheckAvailability extends BaseActivity {
    private RecyclerView mCarParks;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<CorkCarParkDetails, AvailabilityHolder> mAdapter;
    private SwipeRefreshLayout swipe;

    OpeningTimes openingTime = new OpeningTimes();

    Query query;
    boolean defaultSort = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_availability);
        setToolbarText(getString(R.string.check_availability_toolbar_text));

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
                swipe.setRefreshing(false);
            }
        });

        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(false);

        mCarParks = (RecyclerView) findViewById(R.id.rvCarPark);
        mCarParks.setHasFixedSize(false);
        mCarParks.setLayoutManager(mManager);

        mRef.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    private void attachRecyclerViewAdapter() {
        final String dayOfWeek = new SimpleDateFormat(getString(R.string.day_of_week_format))
                .format(Calendar.getInstance().getTime());
        final String timeS = new SimpleDateFormat(getString(R.string.time_format))
                .format(Calendar.getInstance().getTime());
        try {
            final double time = Double.parseDouble(timeS);
            if (defaultSort) {
                query = mRef_Parks.orderByChild(getString(R.string.alpha_order));
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
                            try {
                                openingTime = dataSnapshot.getValue(OpeningTimes.class);
                                if (time < openingTime.getOpen() || time > openingTime.getClose()) {
                                    viewHolder.setSpaces(getString(R.string.closed));
                                    viewHolder.setStatus(getString(R.string.closed));
                                    viewHolder.setStatusColor(false);
                                } else {
                                    viewHolder.setSpaces(model.getFree_spaces());
                                    viewHolder.setStatus(getString(R.string.open));
                                    viewHolder.setStatusColor(true);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public AvailabilityHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
                    AvailabilityHolder viewHolder = super.onCreateViewHolder(parent, viewType);
                    viewHolder.setOnClickListener(new AvailabilityHolder.ClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            TextView title = (TextView) view.findViewById(R.id.title);
                            Intent intent = new Intent(view.getContext(), CarParkDetails.class);
                            intent.putExtra(getString(R.string.maps_extra), title.getText());
                            view.getContext().startActivity(intent);
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
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
}
