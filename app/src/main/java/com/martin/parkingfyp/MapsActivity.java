package com.martin.parkingfyp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;
import com.martin.parkingfyp.model.CorkCarParkDetails;
import com.martin.parkingfyp.model.OpeningTimes;

import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mLastLocation;

    private ArrayList<LatLng> locations;
    private ArrayList<MarkerOptions> markers;
    private ArrayList<Integer> free_spaces;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final int DEFAULT_ZOOM = 15;
    private final int FULL_REFRESH_TIME = 3600000;
    private final int REFRESH_TIME = 120000;
    private final int LOCATION_INTERVAL = 1000;
    private final GeoApiContext context = new GeoApiContext()
            .setApiKey("AIzaSyBZVaVWoVWI7UbGEevT78j1jzYoeUXJAS0");

    private Marker mDestinationMarker;
    private Polyline mCurrentPolyline;

    private PolylineOptions polylineOptions;
    private MarkerOptions destMarkerOptions;

    private boolean initial = true;
    private boolean listen = false;

    private final Handler handler = new Handler();

    private Duration drivingDuration;
    private Duration walkingDuration;

    private int position = 0;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getFree_spaces();
        buildGoogleApiClient();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markers = new ArrayList<>();
        free_spaces = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocations();
        setupMap();
        setUpAutocompleteFragment();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    public void setupMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(51.899175, -8.4748883)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTitle().equalsIgnoreCase(getString(R.string.dest_marker_title)) ||
                        marker.getTitle().equalsIgnoreCase(getString(R.string.curr_marker_title))) {
                    listen = false;
                    routeDirections(false);
                } else {
                    Intent intent = new Intent(MapsActivity.this, CarParkDetails.class);
                    intent.putExtra(getString(R.string.maps_extra), marker.getTitle());
                    startActivity(intent);
                }
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                recommendedCarParkLatLng = marker.getPosition();
                if (marker.getSnippet().equalsIgnoreCase(getString(R.string.closed_snippet))) {
                    displayCentredToast(getString(R.string.closed_message), Toast.LENGTH_SHORT);
                } else if (marker.getTitle().equalsIgnoreCase(getString(R.string.dest_marker_title)) ||
                        marker.getTitle().equalsIgnoreCase(getString(R.string.curr_marker_title))) {
                    routeDirections(false);
                } else {
                    routeDirections(true);
                }
            }
        });
        if (getIntent().getExtras() != null) {
            LatLng location = (LatLng) getIntent().getExtras().get(getString(R.string.carpark_details_extra));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM + 1));
        }
        refreshMap();
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
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if (mMap != null) {
            refreshMap();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        buildLocationSettingsRequest();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings();
        }
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onConnectionSuspended(int i) {
        switch (i) {
            case (CAUSE_NETWORK_LOST):
                displayCentredToast(getString(R.string.lost_network_message), Toast.LENGTH_LONG);
                break;
            case (CAUSE_SERVICE_DISCONNECTED):
                displayCentredToast(getString(R.string.disconnected_message), Toast.LENGTH_LONG);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        try {
            sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (initial) {
                if (getIntent().getExtras() != null) {
                    LatLng locationExtra = (LatLng) getIntent().getExtras()
                            .get(getString(R.string.carpark_details_extra));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(locationExtra));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                    getIntent().removeExtra(getString(R.string.carpark_details_extra));
                    initial = false;
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sourceLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                    initial = false;
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            displayCentredToast(getString(R.string.no_location_message), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        displayCentredToast(getString(R.string.connection_failed_message), Toast.LENGTH_LONG);
    }

    @Override
    public Toolbar getToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setVisibility(View.GONE);
        toolbar.setTitle("");
        return (Toolbar) findViewById(R.id.map_toolbar);
    }

    public ArrayList<LatLng> getLocations() {
        final String dayOfWeek = new SimpleDateFormat(getString(R.string.day_of_week_format))
                .format(Calendar.getInstance().getTime());
        final double time = Double.parseDouble(new SimpleDateFormat(getString(R.string.time_format)).
                format(Calendar.getInstance().getTime()));
        markers = new ArrayList<>();
        locations = new ArrayList<>();
        mRef_Parks.orderByChild(getString(R.string.alpha_order))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        counter = 0;
                        for (final DataSnapshot d : dataSnapshot.getChildren()) {
                            final CorkCarParkDetails c = d.getValue(CorkCarParkDetails.class);
                            final LatLng location = new LatLng(c.getLatitude(), c.getLongitude());
                            locations.add(location);
                            mRef_Hours.child(c.getName()).child(dayOfWeek)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            OpeningTimes opening = dataSnapshot.getValue(OpeningTimes.class);
                                            MarkerOptions marker = new MarkerOptions().position(location)
                                                    .icon(BitmapDescriptorFactory
                                                            .fromResource(R.drawable.parking))
                                                    .title(c.getName());
                                            if (opening.getOpen() == 0 && opening.getClose() == 24) {
                                                if (free_spaces.size() == 0 || free_spaces == null) {
                                                    marker.snippet(getString(R.string.default_snippet_spaces));
                                                } else {
                                                    if (free_spaces.get(counter) > 20) {
                                                        marker.snippet(getString(R.string.default_snippet_spaces));
                                                    } else if (free_spaces.get(counter) == 0) {
                                                        marker.snippet(getString(R.string.default_snippet_no_spaces));
                                                    } else {
                                                        marker.snippet("Low Spaces");
                                                    }
                                                }
                                            } else if (opening.getOpen() - time <= 1 &&
                                                    opening.getOpen() - time > 0) {
                                                marker.snippet(getString(R.string.opening_soon_snippet));
                                            } else if (opening.getClose() - time <= 1 &&
                                                    opening.getClose() - time > 0) {
                                                marker.snippet(getString(R.string.closing_soon_snippet));
                                            } else if (time < opening.getOpen() ||
                                                    time > opening.getClose()) {
                                                marker.snippet(getString(R.string.closed_snippet));
                                            } else {
                                                if (free_spaces.size() == 0 || free_spaces == null) {
                                                    marker.snippet(getString(R.string.default_snippet_spaces));
                                                } else {
                                                    if (free_spaces.get(counter) > 20) {
                                                        marker.snippet(getString(R.string.default_snippet_spaces));
                                                    } else if (free_spaces.get(counter) == 0) {
                                                        marker.snippet(getString(R.string.default_snippet_no_spaces));
                                                    } else {
                                                        marker.snippet(getString(R.string.default_snippet_low_spaces));
                                                    }
                                                }
                                            }
                                            addInfoWindow();
                                            markers.add(marker);
                                            mMap.addMarker(marker);
                                            counter++;
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        return locations;
    }

    public void addInfoWindow() {
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
                title.setGravity(Gravity.CENTER);
                TextView textForTap = new TextView(MapsActivity.this);
                textForTap.setText(marker.getSnippet());
                textForTap.setGravity(Gravity.CENTER);
                textForTap.setSingleLine(false);
                layout.addView(title);
                layout.addView(textForTap);
                return layout;
            }
        });
    }

    public LatLng chooseBestCarPark() {
        ArrayList<String> sourceAddressesArrayList = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            if (free_spaces.size() == markers.size()) {
                if (free_spaces.get(i) > 0) {
                    if (markers.get(i).getSnippet().equalsIgnoreCase(getString(R.string.default_snippet_spaces))) {
                        sourceAddressesArrayList.add(locations.get(i).latitude + "," + locations.get(i).longitude);
                    }
                }
            } else {
                return null;
            }
        }
        String[] sourceAddresses = sourceAddressesArrayList.toArray(new String[sourceAddressesArrayList.size()]);
        String[] destinationAddress = new String[]{destinationLatLng.latitude + "," + destinationLatLng.longitude};
        DistanceMatrixApiRequest request = DistanceMatrixApi.getDistanceMatrix(context, sourceAddresses, destinationAddress);
        request.mode(TravelMode.WALKING);
        try {
            DistanceMatrix matrix = request.await();
            DistanceMatrixRow[] rows = matrix.rows;
            walkingDuration = rows[0].elements[0].duration;
            for (int i = 0; i < rows.length; i++) {
                for (DistanceMatrixElement element : rows[i].elements) {
                    if (walkingDuration.inSeconds > element.duration.inSeconds) {
                        walkingDuration = element.duration;
                        position = i;
                    }
                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(locations.get(position)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            if (!listen) {
                recommendedCarParkLatLng = locations.get(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayCentredToast(String message, int length) {
        Toast toast = Toast.makeText(this, message, length);
        TextView toastText = (TextView) toast.getView().findViewById(android.R.id.message);
        toastText.setGravity(Gravity.CENTER);
        toast.show();
    }


    public void routeDirections(boolean carParkSelected) {
        try {
            if (sourceLatLng == null) {
                displayCentredToast(getString(R.string.no_location_message), Toast.LENGTH_LONG);
            } else {
                if (!carParkSelected) {
                    chooseBestCarPark();
                }
                String source = sourceLatLng.latitude + "," + sourceLatLng.longitude;
                String destination = recommendedCarParkLatLng.latitude + "," + recommendedCarParkLatLng.longitude;
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
                    public int compareTo(@NonNull ReadableInstant readableInstant) {
                        return 0;
                    }
                });
                polylineOptions = new PolylineOptions();
                try {
                    DirectionsResult result = request.await();
                    DirectionsRoute[] routes = result.routes;
                    for (DirectionsRoute route : routes) {
                        DirectionsLeg[] legs = route.legs;
                        for (DirectionsLeg leg : legs) {
                            drivingDuration = leg.durationInTraffic;
                            DirectionsStep[] steps = leg.steps;
                            for (DirectionsStep step : steps) {
                                for (com.google.maps.model.LatLng a : step.polyline.decodePath()) {
                                    polylineOptions.add(new LatLng(a.lat, a.lng));
                                }
                            }
                        }
                    }
                    Log.d(TAG, "Time to location : " + drivingDuration);
                    polylineOptions.width(20);
                    polylineOptions.color(Color.RED);
                    if (mCurrentPolyline != null) {
                        mCurrentPolyline.remove();
                    }
                    if (!carParkSelected) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder
                                .setTitle(R.string.alert_title)
                                .setMessage(String.format(getString(R.string.alert_message), walkingDuration.humanReadable))
                                .setPositiveButton(R.string.alert_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        listen = true;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (free_spaces.get(position) == 0) {
                                                    listen = false;
                                                    routeDirections(false);
                                                } else if (drivingDuration.inSeconds > FULL_REFRESH_TIME) {
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (!markers.get(position).getSnippet()
                                                                    .equalsIgnoreCase(getString(
                                                                            R.string.default_snippet_spaces))) {
                                                                listen = false;
                                                                routeDirections(false);
                                                            }
                                                        }
                                                    }, FULL_REFRESH_TIME);
                                                }
                                                while (drivingDuration.inSeconds > REFRESH_TIME) {
                                                    handler.postDelayed(this, drivingDuration.inSeconds);
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(R.string.alert_negative, null)
                                .show();
                    }
                    mCurrentPolyline = mMap.addPolyline(polylineOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "routeDirections: ", e);
        }
    }

    public void setUpAutocompleteFragment() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(51.871841, -8.572309),
                new LatLng(51.932493, -8.398836)));
        //new LatLng(51.871841, -8.572309)));
        AutocompleteFilter countryFilter = new AutocompleteFilter.Builder()
                .setCountry(getString(R.string.country_filter)).build();
        autocompleteFragment.setFilter(countryFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String address = (String) place.getAddress();
                Geocoder geocoder = new Geocoder(getApplicationContext());
                List<android.location.Address> addresses;
                try {
                    addresses = geocoder.getFromLocationName(address, 1);
                    destinationLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    MarkerOptions marker = new MarkerOptions().position(destinationLatLng)
                            .title(getString(R.string.dest_marker_title))
                            .snippet(getString(R.string.dest_marker_snippet));
                    if (mDestinationMarker != null) {
                        mDestinationMarker.remove();
                    }
                    mDestinationMarker = mMap.addMarker(marker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), getString(R.string.location_retrieving_error),
                            Toast.LENGTH_SHORT).show();
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < markers.size(); i++) {
                    mRef_Parks.child(markers.get(i).getTitle()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int numberOfSpaces = dataSnapshot.child(getString(
                                    R.string.free_spaces_child)).getValue(Integer.class);
                            int position = dataSnapshot.child(getString(R.string.alpha_order))
                                    .getValue(Integer.class) - 1;
                            if (free_spaces.size() != markers.size() || free_spaces.isEmpty()) {
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
                handler.postDelayed(this, REFRESH_TIME);
            }
        }, 1000);
    }

    public void refreshMap() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.clear();
                if (mDestinationMarker != null) {
                    mMap.addMarker(new MarkerOptions().position(destinationLatLng)
                            .title(getString(R.string.dest_marker_title))
                            .snippet(getString(R.string.dest_marker_snippet)));
                }
                if (mCurrentPolyline != null) {
                    mMap.addPolyline(polylineOptions);
                }
                getLocations();
                handler.postDelayed(this, REFRESH_TIME);
            }
        }, REFRESH_TIME);
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates
                            (mGoogleApiClient, mLocationRequest, this);
                }
                displayCentredToast(getString(R.string.getting_location_message), Toast.LENGTH_SHORT);
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
                        displayCentredToast(getString(R.string.location_permission_rejected),
                                Toast.LENGTH_LONG);
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }
}