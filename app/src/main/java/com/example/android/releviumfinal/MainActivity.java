package com.example.android.releviumfinal;

import android.Manifest;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private DrawerLayout drawer;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private NotificationManagerCompat mNotificationManager;
    private ChildEventListener pingListener;

    private MapController mapController;

    private DatabaseReference mDatabase;

    FloatingActionButton mFAB1, mFAB2, mFAB3;

    private static final int LOCATION_REQUEST_CODE = 1;

    private static final int PIN_IMAGE_ID = 1;
    private static final int FIRE_IMAGE_ID = 2;
    private static final int WARNING_IMAGE_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        mapController = new MapController(FirebaseAuth.getInstance().getUid());

        mFAB1 = findViewById(R.id.fab_warning);
        mFAB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        "Warning!", R.drawable.ic_menu_sos, WARNING_IMAGE_ID,
                        MainActivity.this, mMap);
            }
        });

        mFAB2 = findViewById(R.id.fab_fire);
        mFAB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        "Fire!", R.drawable.ic_fab_fire, FIRE_IMAGE_ID,
                        MainActivity.this, mMap);
            }
        });

        mFAB3 = findViewById(R.id.fab_pin);
        mFAB3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        "User Pin", R.drawable.ic_fab_pin, PIN_IMAGE_ID,
                        MainActivity.this, mMap);
            }
        });

        mNotificationManager = NotificationManagerCompat.from(this);
        mDatabase = FirebaseDatabase.getInstance().getReference("Ping Details");

        pingListener = mDatabase.addChildEventListener(new ChildEventListener() { //attach listener

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) { //something changed!


                final String description;
                final String imageId;
                final String uuid = dataSnapshot.getKey();

                description = (String) dataSnapshot.child("mDescription").getValue();
                imageId = dataSnapshot.child("mImageId").getValue().toString();

                DatabaseReference pingLocation = FirebaseDatabase
                        .getInstance()
                        .getReference("GeoFireLocations");

                pingLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double lng, lat;
                        lat = (double) dataSnapshot.child(uuid).child("l").child("0").getValue();
                        lng = (double) dataSnapshot.child(uuid).child("l").child("1").getValue();
                        switch (Integer.parseInt(imageId)) {
                            case PIN_IMAGE_ID: {
                                mapController.addMarkerFromDB(lat, lng, description, R.drawable.ic_fab_pin
                                        , MainActivity.this, mMap);
                                sendNotificationDisasterChannel(R.drawable.ic_fab_pin, description);
                                break;
                            }
                            case FIRE_IMAGE_ID: {
                                mapController.addMarkerFromDB(lat, lng, description, R.drawable.ic_fab_fire,
                                        MainActivity.this, mMap);
                                sendNotificationDisasterChannel(R.drawable.ic_fab_fire, description);
                                break;
                            }
                            case WARNING_IMAGE_ID: {
                                mapController.addMarkerFromDB(lat, lng, description, R.drawable.ic_menu_sos,
                                        MainActivity.this, mMap);
                                sendNotificationDisasterChannel(R.drawable.ic_menu_sos, description);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
        mDatabase.addChildEventListener(pingListener);

    }

    public void sendNotificationDisasterChannel(int icon, String message) {
        Notification notification = new NotificationCompat.Builder(this, ApplicationController.CHANNEL_1_ID)
                .setSmallIcon(icon)
                .setContentTitle("New Alert nearby !")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
        mNotificationManager.notify(1, notification);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("TEST123", "OnMapReady");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
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
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mapController.trackUserLocation(latLng);
    }

    //When map is ready to start working
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                    Log.v("TEST123", "Permission Granted");
                } else {
                    Toast.makeText(this, "Please provide location permission", Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_agent) {
            Intent agentActivity = new Intent(this, AgentActivity.class);
            startActivity(agentActivity);
        } else if (id == R.id.nav_account) {
            Intent profileActivity = new Intent(this, ProfileActivity.class);
            startActivity(profileActivity);
        } else if (id == R.id.nav_chat) {
            Intent chatActivity = new Intent(this, ChatActivity.class);
            startActivity(chatActivity);
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, "Location shared!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_sos) {
            Toast.makeText(this, "SOS sent!", Toast.LENGTH_SHORT).show();
        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
