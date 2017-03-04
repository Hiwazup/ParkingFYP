package com.martin.parkingfyp;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;

import java.util.ArrayList;

/**
 * Created by Martin on 01/02/2017.
 */

public class CarParkDetails  extends BaseActivity {
    private CorkCarParkDetails details;
    private TextView free_spaces;
    private TextView status;
    private TextView spaces;
    private TextView opening_hours;
    private LatLng location;
    private String TAG = "Database";
    private TableLayout days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_park_details);
        setToolbarText("Details");
        
        String carParkName = getIntent().getExtras().getString("Name");
        
        TextView title = (TextView)findViewById(R.id.details_title);
        free_spaces = (TextView)findViewById(R.id.details_free_spaces);
        spaces = (TextView)findViewById(R.id.details_spaces);
        status = (TextView)findViewById(R.id.details_status);
        //opening_hours = (TextView)findViewById(R.id.details_hours);
        days = (TableLayout)findViewById(R.id.Day_Table);


        mRef.child(carParkName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                details = dataSnapshot.getValue(CorkCarParkDetails.class);
                free_spaces.setText(details.getFree_spaces() + " Spaces Available");
                spaces.setText(details.getSpaces() + " Total Spaces");
                location = new LatLng(details.getLatitude(), details.getLongitude());
                Log.d("Database", "Spaces " + details.getFree_spaces());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Error" + databaseError.toException());
            }
        });

        mRef_Hours.child(carParkName).child("opening_times").orderByChild("rank").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<OpeningTimes> opening_times = new ArrayList<OpeningTimes>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    OpeningTimes open = snapshot.getValue(OpeningTimes.class);
                    opening_times.add(open);
                    Log.d(TAG, open.getRank() + " : " + open.getOpen() + " - " + open.getClose());
                }
                for(OpeningTimes o : opening_times){
                    TableRow row = new TableRow(getApplicationContext());
                    row.setPadding(10, 10, 10, 10);
                    TextView textView = new TextView(getApplicationContext());
                    textView.setTextSize(24);
                    String text;
                    if(o.getOpen() == -1){
                        text = o.getDay() + ": Closed";
                    } else {
                        text = o.getDay() + ": " + o.getOpen() + "0 - " + o.getClose() + "0";
                    }
                    textView.setText(text);
                    row.addView(textView);
                    days.addView(row);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Error" + databaseError.toException());
            }
        });
        
        title.setText(carParkName);

        status.setText("Open");
    }

    @Override
    protected boolean useDrawerToggle() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
