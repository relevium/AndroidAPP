package com.example.android.releviumfinal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    protected DrawerLayout drawer;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;

    private String mUserId;
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

        Log.v("TEST123", "Inside");
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

        mFAB1 = findViewById(R.id.fab_warning);
        mFAB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        "Warning!", R.drawable.ic_menu_sos, 3);
            }
        });

        mFAB2 = findViewById(R.id.fab_fire);
        mFAB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        "Fire!", R.drawable.ic_fab_fire, 2);
            }
        });

        mFAB3 = findViewById(R.id.fab_pin);
        mFAB3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                        "User Pin", R.drawable.ic_fab_pin, 1);
            }
        });

        mUserId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Pings");

        mDatabase.addValueEventListener(new ValueEventListener() { //attach listener

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //something changed!
                for (DataSnapshot pingsSnapshot : dataSnapshot.getChildren()) {
                    double lng, lat;
                    String description;
                    String imageId;

                    lat = (double) pingsSnapshot.child("l").child("0").getValue();
                    lng = (double) pingsSnapshot.child("l").child("1").getValue();
                    description = (String) pingsSnapshot.child("Description").getValue();
                    imageId = (String) pingsSnapshot.child("Image").getValue().toString();
                    switch (Integer.parseInt(imageId)) {
                        case PIN_IMAGE_ID: {
                            addMarkerFromDB(lat, lng, description, R.drawable.ic_fab_pin);
                            break;
                        }
                        case FIRE_IMAGE_ID: {
                            addMarkerFromDB(lat, lng, description, R.drawable.ic_fab_fire);
                            break;
                        }
                        case WARNING_IMAGE_ID: {
                            addMarkerFromDB(lat, lng, description, R.drawable.ic_menu_sos);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { //update UI here if error occurred.

            }
        });

    }

    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void addMarkerFromDB(double latitude, double longitude, String message, int image) {
        BitmapDescriptor bmp = generateBitmapDescriptorFromRes(this, image);
        LatLng userLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title(message)
                .icon(bmp));
    }

    public void addMarker(double latitude, double longitude, String message, int image, int imageId) {
        BitmapDescriptor bmp = generateBitmapDescriptorFromRes(this, image);
        LatLng userLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title(message)
                .icon(bmp));
        addMarkerToDatabase(imageId, message);
    }

    public void addMarkerToDatabase(int imageId, String description) {
        GeoFire geoFire = new GeoFire(mDatabase);
        geoFire.setLocation(mUserId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new
                GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Do some stuff if you want to
                    }
                });
        mDatabase.child(mUserId).child("Description").setValue(description);
        mDatabase.child(mUserId).child("Image").setValue(imageId);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("TEST123", "OnMapReady");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
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

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
