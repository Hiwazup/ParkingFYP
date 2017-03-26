package com.martin.parkingfyp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;
import com.martin.parkingfyp.model.Prices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Martin on 01/02/2017.
 */

public class CarParkDetails  extends BaseActivity {
    private CorkCarParkDetails details;
    private TextView free_spaces;
    private TextView status;
    private TextView spaces;
    private TextView address;
    private LatLng location;
    private String TAG = "Database";
    private TableLayout days;
    MapsActivity maps = new MapsActivity();
    String carParkName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_park_details);
        setToolbarText("Details");
        
        carParkName = getIntent().getExtras().getString("Name");
        
        final TextView title = (TextView)findViewById(R.id.details_title);
        free_spaces = (TextView)findViewById(R.id.details_free_spaces);
        spaces = (TextView)findViewById(R.id.details_spaces);
        status = (Button)findViewById(R.id.details_status);
        days = (TableLayout)findViewById(R.id.Day_Table);
        address = (TextView)findViewById(R.id.address);

        title.setText(carParkName);
        getDataFromCarparkTable();
        getDataFromOpeningHoursTable();
        getDataFromPricesTable();

    }

    private void getDataFromCarparkTable() {
        mRef_Parks.child(carParkName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                details = dataSnapshot.getValue(CorkCarParkDetails.class);
                free_spaces.setText(details.getFree_spaces() + " Spaces Available");
                spaces.setText(details.getSpaces() + " Total Spaces");
                location = new LatLng(details.getLatitude(), details.getLongitude());
                address.setText(details.getAddress());
                /*Geocoder geocoder = new Geocoder(getApplicationContext());
                List<Address> addresses = null;
                try {
                    String text = "";
                    addresses = geocoder.getFromLocation(details.getLatitude(), details.getLongitude(), 1);
                    if(addresses.size() == 1){
                        for(int i = 0; i < addresses.get(0).getMaxAddressLineIndex()-1; i = i+2){
                            text += addresses.get(0).getAddressLine(i) + ",  " + addresses.get(0).getAddressLine(i+1) + "\n";
                            Log.d(TAG, "onDataChange: " + addresses.get(0).getAddressLine(i));
                        }
                        text += addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex()-1);
                        address.setText(text);
                        mRef_Parks.child(carParkName).child("address").setValue(text);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Error" + databaseError.toException());
            }
        });
    }

    private void getDataFromOpeningHoursTable() {
        final String dayOfWeek = new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime());
        mRef_Hours.child(carParkName).orderByChild("rank").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<OpeningTimes> opening_times = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                      OpeningTimes openingTime = snapshot.getValue(OpeningTimes.class);
                      if(openingTime.getDay().equalsIgnoreCase(dayOfWeek)) {
                      isOpen(openingTime);
                    }
                    opening_times.add(openingTime);
                }
                addRowHeader("Opening Times");
                for(OpeningTimes o : opening_times){
                    TableRow row = new TableRow(getApplicationContext());
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    row.setWeightSum(1);
                    //TextView textView = new TextView(getApplicationContext());
                    //TextView textView2 = new TextView(getApplicationContext());
                    //textView.setTextSize(18);
                    //textView2.setTextSize(18);
                    String text1, text2;
                    if(o.getOpen() == -1){
                        text1 = o.getDay() + ":";
                        text2 = "Closed";
                    } else {
                        text1 = o.getDay() + ": ";
                        if(o.getOpen() == 0.00){
                            text2 = "24 Hours";
                        }else if(o.getClose() == 24.00){
                            text2 = o.getOpen() + " - 00:00";
                        } else {
                            text2 = o.getOpen() + "0 - " + o.getClose() + "0";
                        }
                    }
                TextView textView = setupTextView(text1, 0.54f, 18);
                TextView textView2 = setupTextView(text2, 0.54f, 18);
                    //.setText(text1);
                    //.setText(text2);
                    //textView.setTextColor(Color.BLACK);
                    //textView.setAlpha(.87f);
                    //textView2.setTextColor(Color.BLACK);
                    //textView2.setAlpha(.87f);
                    row.addView(textView);
                    row.addView(textView2);
                    days.addView(row);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Error" + databaseError.toException());
            }
        });
    }

    public TextView setupTextView(String text, Float opacity, int size){
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setAlpha(opacity);
        textView.setTextSize(size);
        return textView;
    }

    private void getDataFromPricesTable() {
        mRef_Prices.child(carParkName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Prices> prices = new ArrayList<>();
                Log.d(TAG, "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    prices.add(snapshot.getValue(Prices.class));
                }
                addRowHeader("");
                addRowHeader("Price List");
                for (Prices price : prices) {
                    TableRow row = new TableRow(getApplicationContext());
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    row.setWeightSum(1);
                    //TextView textView = new TextView(getApplicationContext());
                    //TextView textView2 = new TextView(getApplicationContext());
                    //textView.setTextSize(18);
                    //textView2.setTextSize(18);
                    String text1 = price.getDescription() + ":";
                    String text2 = "€" + price.getPrice() + "0";
                    //textView.setText(text1);
                    //textView2.setText(text2);
                    //textView.setTextColor(Color.BLACK);
                    //textView2.setTextColor(Color.BLACK);
                    TextView textView = setupTextView(text1, 0.54f, 18);
                    TextView textView2 = setupTextView(text2, 0.54f, 18);
                    row.addView(textView);
                    row.addView(textView2);
                    days.addView(row);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Error" + databaseError.toException());
            }
        });
    }

    public void isOpen(OpeningTimes opening){
        final float time = Float.parseFloat(new SimpleDateFormat("HH.mm").format(Calendar.getInstance().getTime()));
        if(time < opening.getOpen() || time > opening.getClose()){//if (time.compareTo(opening.getOpen()) < 0 || time.compareTo(opening.getClose()) > 0) {
            status.setText("Closed");
        /*} else if(o.getTime().compareTo(o.getClose()) == -1 && !o.getOpen().equalsIgnoreCase("00:00")) {
                    Log.d(TAG, ""+o.getTime().compareTo(o.getClose()));
                    status.setText("Closing Soon");*/
        } else {
            status.setText("View On Map");
            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getApplicationContext(), location.latitude + ", " + location.longitude, Toast.LENGTH_SHORT).show();
                    destinationLatLng = location;
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("ZoomToPosition", location);
                    startActivity(intent);
                }
            });
        }
    }

    public void addRowHeader(String s){
        TableRow row = new TableRow(getApplicationContext());
        //TextView text = new TextView(getApplicationContext());
        TextView text = setupTextView(s, 0.87f, 20);
        //text.setText(s);
        //text.setTextSize(20);
        //text.setTypeface(null, Typeface.BOLD);
        //text.setTextColor(Color.BLACK);
        row.addView(text);
        days.addView(row);
    }

    @Override
    protected boolean useDrawerToggle() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
