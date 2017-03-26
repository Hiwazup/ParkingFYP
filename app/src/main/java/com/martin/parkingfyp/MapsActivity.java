package com.martin.parkingfyp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Duration;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.OpeningHours;
import com.google.maps.model.TravelMode;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    DatabaseReference mRef_Location = mRef.child("carparks");
    DatabaseReference mRef_Hours = mRef.child("opening_hours");
    ArrayList <LatLng> locations;
    ArrayList<MarkerOptions> markers = new ArrayList<>();
    ArrayList<Integer> free_spaces = new ArrayList<>();
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    LocationSettingsRequest mLocationSettingsRequest;
    Marker mCurrentLocationMarker;
    Marker mDestinationMarker;
    Polyline mCurrentPolyline;
    GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyBZVaVWoVWI7UbGEevT78j1jzYoeUXJAS0");
    private String TAG = "LocationTAG";
    private int DEFAULT_ZOOM = 15;
    int counter = 0;
    boolean initial = true;
    long estimatedDuration;
    boolean listen = false;
    final Handler handler = new Handler();
    Duration duration;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getFree_spaces();
        buildGoogleApiClient();
        /// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocations();
        setupMap();
        setUpAutocompleteFragment();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==
                    PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            mMap.setMyLocationEnabled(true);
        }
    }

    public void setupMap(){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(51.899175, -8.4748883)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getTitle().equalsIgnoreCase("destination") ||
                        marker.getTitle().equalsIgnoreCase("current location")){
                    routeDirections(false);
                } else {
                    Intent intent = new Intent(MapsActivity.this, CarParkDetails.class);
                    intent.putExtra("Name", marker.getTitle());
                    startActivity(intent);
                }
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                destinationLatLng = marker.getPosition();
                if(marker.getSnippet().equalsIgnoreCase("closed")){
                    displayCentredToast("This Car Park Is Closed");
                } else if(marker.getTitle().equalsIgnoreCase("destination") ||
                        marker.getTitle().equalsIgnoreCase("current location")){
                    routeDirections(false);
                } else {
                    routeDirections(true);
                }
            }
        });
        if(getIntent().getExtras() != null){
            LatLng location = (LatLng)getIntent().getExtras().get("ZoomToPosition");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM + 1));
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null){
            if(mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        buildLocationSettingsRequest();
        //checkLocationPermission();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings();
        }
    }

    public void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onConnectionSuspended(int i) {
        switch (i){
            case (CAUSE_NETWORK_LOST):
                Toast.makeText(this, "Network Lost\nPlease Reconnect", Toast.LENGTH_LONG).show();
                break;
            case(CAUSE_SERVICE_DISCONNECTED):
                Toast.makeText(this, "Disconnected\nPlease Reconnect", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        try{
            sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "Latitude " + location.getLatitude() + ", Longitude " + location.getLongitude());
            if(initial) {
                if(getIntent().getExtras() != null){
                    LatLng locationExtra = (LatLng)getIntent().getExtras().get("ZoomToPosition");
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(locationExtra));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                    getIntent().removeExtra("ZoomToPosition");
                    initial = false;
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sourceLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                    initial = false;
                }
            }
        }catch (NullPointerException e){
            Log.e(TAG, "onLocationChanged: ", e);
            Toast.makeText(this, "Could Not Get Location", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast toast = Toast.makeText(this, "Could Not Make Connection\n" +
                "Please Check Your Settings", Toast.LENGTH_LONG);
        TextView text = (TextView)findViewById(android.R.id.message);
        text.setGravity(Gravity.CENTER);
        toast.show();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates
                            (mGoogleApiClient, mLocationRequest, this);
                }
                Log.i(TAG, "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            LocationServices.FusedLocationApi.requestLocationUpdates
                                    (mGoogleApiClient, mLocationRequest, this);
                        }
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        displayCentredToast("Location Is Required For Some Features");
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    @Override
    public Toolbar getToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setVisibility(View.GONE);
        toolbar.setTitle("");
        return (Toolbar) findViewById(R.id.map_toolbar);
    }

    public ArrayList<LatLng> getLocations() {
        final String dayOfWeek = new SimpleDateFormat("EEEE").format(Calendar.getInstance().getTime());
        final double time = Double.parseDouble(new SimpleDateFormat("HH.mm").format(Calendar.getInstance().getTime()));
        if(locations == null){
            locations = new ArrayList<>();
            mRef_Location.orderByChild("rank").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                        final CorkCarParkDetails c = d.getValue(CorkCarParkDetails.class);
                        final LatLng location = new LatLng(c.getLatitude(), c.getLongitude());
                        locations.add(location);
                        mRef_Hours.child(c.getName()).child(dayOfWeek).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                OpeningTimes opening = dataSnapshot.getValue(OpeningTimes.class);
                                MarkerOptions marker = new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.parking)).title(c.getName());
                                Log.d(TAG, c.getName() + "Time " + time +  " Opening " + opening.getOpen());
                                Log.d(TAG, c.getName() + "Time " + time + " Closing: " + opening.getClose());
                                Log.d(TAG, "Opening - Time" + (opening.getOpen() - time));
                                if(opening.getOpen() - time <= 1 && opening.getOpen() - time > 0){
                                    marker.snippet("Opening at " + opening.getOpen() + "0");
                                } else if (opening.getClose() - time <= 1 && opening.getClose() - time > 0){
                                    marker.snippet("Closing at " + opening.getClose() + "0");
                                } else if(time < opening.getOpen() || time > opening.getClose()){
                                //if (time.compareTo(opening.getOpen()) < 0 || time.compareTo(opening.getClose()) > 0) {
                                    marker.snippet("Closed");
                                } else {
                                    //marker.snippet("Hold For Directions");
                                    addInfoWindow();
                                }
                                markers.add(marker);
                                mMap.addMarker(marker);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
        return locations;
    }

    public void addInfoWindow(){
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout layout = new LinearLayout(MapsActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(MapsActivity.this);
                title.setText(marker.getTitle());
                title.setTypeface(null, Typeface.BOLD);
                TextView textForTap = new TextView(MapsActivity.this);
                textForTap.setText("Click For Map Route");
                textForTap.setGravity(Gravity.CENTER_VERTICAL);
                TextView textForHold = new TextView(MapsActivity.this);
                textForHold.setText("Hold For Car Park Details");
                textForHold.setGravity(Gravity.CENTER_VERTICAL);
                layout.addView(title);
                layout.addView(textForTap);
                layout.addView(textForHold);
                return layout;
            }
        });
    }

    public LatLng chooseBestCarPark(){
        ArrayList<String>sourceAddressesArrayList = new ArrayList<>();
        Log.d(TAG, "Free Spaces Size " + free_spaces.size());
        for(int i = 0; i < markers.size(); i++){
            if(free_spaces.size() == markers.size()) {
                if (free_spaces.get(i) > 0) {
                    if (!markers.get(i).getSnippet().equalsIgnoreCase("closed")) {
                        sourceAddressesArrayList.add(locations.get(i).latitude + "," + locations.get(i).longitude);
                    }
                }
            } else {
                Log.d(TAG, "Markers" + markers.size() + "   Free Spaces " + free_spaces.size());
                return null;
            }
        }
        String [] sourceAddresses = sourceAddressesArrayList.toArray(new String[sourceAddressesArrayList.size()]);
        String [] destinationAddress = new String[]{destinationLatLng.latitude + "," + destinationLatLng.longitude};
        DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, sourceAddresses, destinationAddress);
        request.mode(TravelMode.WALKING);
        request.departureTime(new ReadableInstant() {
            @Override
            public long getMillis() {
                return Calendar.getInstance().getTimeInMillis();
            }

            @Override
            public Chronology getChronology() {
                return null;
            }

            @Override
            public DateTimeZone getZone() {
                return null;
            }

            @Override
            public int get(DateTimeFieldType type) {
                return 0;
            }

            @Override
            public boolean isSupported(DateTimeFieldType field) {
                return false;
            }

            @Override
            public Instant toInstant() {
                return null;
            }

            @Override
            public boolean isEqual(ReadableInstant instant) {
                return false;
            }

            @Override
            public boolean isAfter(ReadableInstant instant) {
                return false;
            }

            @Override
            public boolean isBefore(ReadableInstant instant) {
                return false;
            }

            @Override
            public int compareTo(ReadableInstant readableInstant) {
                return 0;
            }
        });
        try {
            DistanceMatrix matrix = request.await();
            DistanceMatrixRow [] rows = matrix.rows;
            Duration shortestDuration = rows[0].elements[0].duration;
            for(int i = 0; i < rows.length; i++){
                for(DistanceMatrixElement element : rows[i].elements){
                    if(shortestDuration.inSeconds > element.duration.inSeconds){
                        shortestDuration = element.duration;
                        position = i;
                    }
                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(locations.get(position)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            /*DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch(i){
                        case DialogInterface.BUTTON_POSITIVE:
                            listen = true;
                            break;
                    }
                }
            };*/
            if(!listen) {
                final Duration duration = shortestDuration;
                TextView alertMessage = new TextView(this);
                alertMessage.setText("\nEstimated Duration Walking Is " + shortestDuration.humanReadable +
                        "\n\nWould You Like To Be Notified If This Car Park Becomes Unavailable?");
                alertMessage.setGravity(Gravity.CENTER);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setTitle("Best Car Park")
                        .setMessage("\nEstimated Duration Walking To Destination is " + shortestDuration.humanReadable +
                                ".\n\nWould You Like To Be Notified If This Car Park Becomes Unavailable?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listen = true;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "run: In Here");
                                        if(free_spaces.get(position) == 0) {
                                            chooseBestCarPark();
                                            while (duration.inSeconds > 60000) {
                                                handler.postDelayed(this, duration.inSeconds / 2);
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                //displayCentredToast("This Is The Recommended Car Park Based On Your Destination, Current Traffic and Availability.\nEstimated Duration Walking is " + shortestDuration.humanReadable);
                //displayCentredToast("If This Recommendation Becomes Unavailable You Will Be Notified");
                destinationLatLng = locations.get(position);
                //Log.d(TAG, "chooseBestCarPark: " + listen);
                //if(listen){

                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayCentredToast(String message){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        TextView toastText = (TextView)toast.getView().findViewById(android.R.id.message);
        toastText.setGravity(Gravity.CENTER);
        toast.show();
    }
    
 /*   public void listenToCarPark(final int position){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int spaces = free_spaces.get(position);
                Log.d(TAG, "Spaces " + free_spaces.get(position));
                if(spaces == 0){

                }
                handler.postDelayed(this, 10000);
            }
        });
    }
*/
    public void routeDirections(boolean carParkSelected) {
        //handler.postDelayed(new Runnable() {
          //  @Override
            //public void run() {
                try {
                    if(!carParkSelected) {
                        chooseBestCarPark();
                    }
                    String source = sourceLatLng.latitude + "," + sourceLatLng.longitude;
                    String destination = destinationLatLng.latitude + "," + destinationLatLng.longitude;
                    DirectionsApiRequest request = DirectionsApi.getDirections(context, source, destination);
                    request.departureTime(new ReadableInstant() {
                        @Override
                        public long getMillis() {
                            return Calendar.getInstance().getTimeInMillis();
                        }

                        @Override
                        public Chronology getChronology() {
                            return null;
                        }

                        @Override
                        public DateTimeZone getZone() {
                            return null;
                        }

                        @Override
                        public int get(DateTimeFieldType type) {
                            return 0;
                        }

                        @Override
                        public boolean isSupported(DateTimeFieldType field) {
                            return false;
                        }

                        @Override
                        public Instant toInstant() {
                            return null;
                        }

                        @Override
                        public boolean isEqual(ReadableInstant instant) {
                            return false;
                        }

                        @Override
                        public boolean isAfter(ReadableInstant instant) {
                            return false;
                        }

                        @Override
                        public boolean isBefore(ReadableInstant instant) {
                            return false;
                        }

                        @Override
                        public int compareTo(ReadableInstant readableInstant) {
                            return 0;
                        }
                    });
                    PolylineOptions polylineOptions = new PolylineOptions();
                    try {
                        DirectionsResult result = request.await();
                        DirectionsRoute[] routes = result.routes;
                        for (DirectionsRoute route : routes) {
                            DirectionsLeg[] legs = route.legs;
                            for (DirectionsLeg leg : legs) {
                                estimatedDuration = leg.durationInTraffic.inSeconds;
                                DirectionsStep[] steps = leg.steps;
                                for (DirectionsStep step : steps) {
                                    for (com.google.maps.model.LatLng a : step.polyline.decodePath()) {
                                        polylineOptions.add(new LatLng(a.lat, a.lng));
                                    }
                                }
                            }
                        }
                        polylineOptions.width(20);
                        polylineOptions.color(Color.RED);
                        if(mCurrentPolyline != null) {
                            mCurrentPolyline.remove();
                        }
                        mCurrentPolyline = mMap.addPolyline(polylineOptions);
                        //Log.d(TAG, "routeDirections: " + result.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }catch (NullPointerException nullPointer){
                    Log.e(TAG, "routeDirections: ", nullPointer);
                }
                Log.d(TAG, "Time " + estimatedDuration);
                //handler.postDelayed(this, estimatedDuration/2);
            }
        //}, 100000);
    //}

    public void setUpAutocompleteFragment(){
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(51.878971, -8.648000),
                new LatLng(51.904486, -8.417389)));
        AutocompleteFilter countryFilter = new AutocompleteFilter.Builder().setCountry("IE").build();
        autocompleteFragment.setFilter(countryFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d(TAG, "onPlaceSelected: "+ place.getAddress());
                String address = (String) place.getAddress();
                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<android.location.Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocationName(address, 1);
                    Log.d(TAG, addresses.size() +"");
                    destinationLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    Log.d(TAG, "" + destinationLatLng.latitude + ", "+ destinationLatLng.longitude);
                    MarkerOptions marker = new MarkerOptions().position(destinationLatLng).title("Destination").snippet("Click For Recommendation");
                    if(mDestinationMarker != null){
                        mDestinationMarker.remove();
                    }
                    mDestinationMarker = mMap.addMarker(marker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), "An Error Occured Retrieving That Location", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    public void getFree_spaces() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                for (int i = 0; i < markers.size(); i++) {
                    mRef_Parks.child(markers.get(i).getTitle()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int numberOfSpaces = dataSnapshot.child("free_spaces").getValue(Integer.class);
                            int position = dataSnapshot.child("rank").getValue(Integer.class) - 1;
                            if(free_spaces.size() != markers.size() || free_spaces.isEmpty()){
                                free_spaces.add(position, numberOfSpaces);
                            } else {
                                free_spaces.set(position, numberOfSpaces);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                handler.postDelayed(this, 120000);
            }
        }, 1000);
    }
}