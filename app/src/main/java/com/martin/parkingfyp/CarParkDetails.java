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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
    protected DatabaseReference mRef_Parks = mRef.child("carparks");
    protected DatabaseReference mRef_Hours = mRef.child("opening_hours");

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


        mRef_Parks.child(carParkName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                details = dataSnapshot.getValue(CorkCarParkDetails.class);
                free_spaces.setText(details.getFree_spaces() + " Spaces Available");
                spaces.setText(details.getSpaces() + " Total Spaces");
                location = new LatLng(details.getLatitude(), details.getLongitude());
                //Log.d("Database", "Spaces " + details.getFree_spaces());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Error" + databaseError.toException());
            }
        });
        //String dayOfWeek = new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime());
        mRef_Hours.child(carParkName).child("opening_times").orderByChild("rank").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<OpeningTimes> opening_times = new ArrayList<OpeningTimes>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    OpeningTimes open = snapshot.getValue(OpeningTimes.class);
                    opening_times.add(open);
                    //Log.d(TAG, "" + open.getCurrentTime());
                    //Log.d(TAG, open.getDayOfWeek());
                    //Log.d(TAG, open.getTime());
                    //Log.d(TAG, open.getRank() + " : " + open.getOpen() + " - " + open.getClose());
                }
                for(OpeningTimes o : opening_times){
                    if(o.getDayOfWeek().equalsIgnoreCase(o.getDay())){
                        if(o.getTime().compareTo(o.getOpen()) < 0 || o.getTime().compareTo(o.getClose())>0){
                            status.setText("Closed");
                        } else {
                            status.setText("Open");
                        }
                        //Log.d(TAG, "" + o.getTime().compareTo(o.getOpen()));
                    }
                    TableRow row = new TableRow(getApplicationContext());
                    //row.setPadding(10, 10, 10, 10);
                    TextView textView = new TextView(getApplicationContext());
                    textView.setTextSize(24);
                    String text;
                    if(o.getOpen().equalsIgnoreCase("-1")){
                        text = o.getDay() + ": Closed";
                    } else { text = o.getDay() + ": " + o.getOpen() + " - " + o.getClose();
                    }
                    textView.setText(text);
                    row.addView(textView);
                    days.addView(row);
                }
                //Log.d(TAG, "" + dataSnapshot.getValue(OpeningTimes.class).getOpen());
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
