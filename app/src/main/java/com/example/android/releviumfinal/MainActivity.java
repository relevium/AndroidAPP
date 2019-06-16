package com.example.android.releviumfinal;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private DrawerLayout drawer;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private NotificationManagerCompat mNotificationManagerPing, mNotificationManagerLocation;
    private static ChildEventListener mPingListener, mUserLocationListener, mUserStatuesListener, mMessageListener, test;
    private ArrayList<Marker> mForeignUserLocation = new ArrayList<>();
    private String mUserFirstName;
    private String mUserLastName;
    private String mUserUID, mUserEmail, mImageURL;
    private FirebaseUser mUser;
    private int count = 0;
    private int limit = 0;
    private FirebaseAuth mUserAuth;
    private Marker mUserMarker, mUserAnnonmoysMarker;
    private Boolean mAnonymityPreference;
    private Target mTarget;


    private CircleImageView userProfileImage;

    TextView mNDUserName, mNDEmail;

    private static DatabaseReference mRootRef, rootMessageRef, mDatabase, mUserLocationDataBase, userInfoRef, messageRef;

    private MapController mapController;


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
        mRootRef = FirebaseDatabase.getInstance().getReference();

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

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

        mNDUserName = headerView.findViewById(R.id.textView_nd_user_name);
        mNDEmail = headerView.findViewById(R.id.textView_nd_email);
        userProfileImage = headerView.findViewById(R.id.imageView_nd_user_image);

        mapController = new MapController(FirebaseAuth.getInstance().getUid());

        mUserAuth = FirebaseAuth.getInstance();
        mUser = mUserAuth.getCurrentUser();
        mUserUID = mUser.getUid();

        mRootRef.child("Users").child(mUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserFirstName = dataSnapshot.child("mFirstName").getValue(String.class);
                mUserLastName = dataSnapshot.child("mLastName").getValue(String.class);
                mUserEmail = dataSnapshot.child("mEmail").getValue(String.class);
                mImageURL = dataSnapshot.child("image").getValue(String.class);
                mAnonymityPreference = dataSnapshot.child("AnonymityPreference").getValue(Boolean.class);
                if (mImageURL != null) {
                    Picasso.get().load(mImageURL).placeholder(R.drawable.ic_progress_image).into(userProfileImage);
                }
                String concatName = (mUserFirstName + " " + mUserLastName);
                mNDUserName.setText(concatName);
                mNDEmail.setText(mUserEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mFAB1 = findViewById(R.id.fab_warning);
        mFAB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarkerOnMap("Warning!", R.drawable.ic_menu_sos, WARNING_IMAGE_ID);
            }
        });

        mFAB2 = findViewById(R.id.fab_fire);
        mFAB2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarkerOnMap("Fire", R.drawable.ic_fab_fire, FIRE_IMAGE_ID);
            }
        });

        mFAB3 = findViewById(R.id.fab_pin);
        mFAB3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarkerOnMap("User Ping", R.drawable.ic_fab_pin, PIN_IMAGE_ID);

            }
        });

        mNotificationManagerPing = NotificationManagerCompat.from(this);
        mDatabase = FirebaseDatabase.getInstance().getReference("Ping-Details");

        mPingListener = new ChildEventListener() { //attach listener

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) { //something changed!

                Log.v("TEST1", "mPingListener Working");

                final String description;
                final String imageId;
                final String uuid = dataSnapshot.getKey();

                description = (String) dataSnapshot.child("mDescription").getValue();
                imageId = dataSnapshot.child("mImageId").getValue().toString();

                DatabaseReference pingLocation = FirebaseDatabase
                        .getInstance()
                        .getReference("GeoFirePingLocations");

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
                                //sendNotificationDisasterChannel(R.drawable.ic_fab_pin, description);
                                break;
                            }
                            case FIRE_IMAGE_ID: {
                                mapController.addMarkerFromDB(lat, lng, description, R.drawable.ic_fab_fire,
                                        MainActivity.this, mMap);
                                //sendNotificationDisasterChannel(R.drawable.ic_fab_fire, description);
                                break;
                            }
                            case WARNING_IMAGE_ID: {
                                mapController.addMarkerFromDB(lat, lng, description, R.drawable.ic_menu_sos,
                                        MainActivity.this, mMap);
                                //sendNotificationDisasterChannel(R.drawable.ic_menu_sos, description);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w("FCM token failed", "getInstanceId failed", task.getException());
                                    return;
                                }
                                String token = task.getResult().getToken();
                                DatabaseReference fcmDatabase = FirebaseDatabase.getInstance()
                                        .getReference("FCM-ID").child(FirebaseAuth.getInstance().getUid());
                                fcmDatabase.setValue(token);
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

        };

        mUserLocationDataBase = FirebaseDatabase.getInstance().getReference("User-Location");

        mUserLocationListener = new ChildEventListener() { //attach listener

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) { //something changed!

                Log.v("TEST1", "mUserLocationListener Working");

                final String uuid = dataSnapshot.getKey();
                final double lat, lng;

                lat = (double) dataSnapshot.child("l").child("0").getValue();
                lng = (double) dataSnapshot.child("l").child("1").getValue();

                DatabaseReference userInfo = FirebaseDatabase
                        .getInstance()
                        .getReference("Users");

                userInfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Boolean foreignerAnonymityPreference;
                        boolean notFound = true;
                        final String userFirstName, userLastName, image;
                        String userState;
                        userFirstName = (String) dataSnapshot.child(uuid).child("mFirstName").getValue();
                        userLastName = (String) dataSnapshot.child(uuid).child("mLastName").getValue();
                        userState = (String) dataSnapshot.child(uuid).child("userState").child("state").getValue();
                        foreignerAnonymityPreference = dataSnapshot.child(uuid).child("AnonymityPreference").getValue(Boolean.class);
                        image = dataSnapshot.child(uuid).child("image").getValue(String.class);

                        final LatLng latLng = new LatLng(lat, lng);
                        if (foreignerAnonymityPreference == null) {
                            foreignerAnonymityPreference = false;
                        }
                        if (userState == null) {
                            userState = "neutral";
                        }
                        if (!foreignerAnonymityPreference || uuid.equals(mUserUID)) {
                            if (userState.equals("online")) {
                                if (!uuid.equals(mUserUID)) {
                                    for (int i = 0; i < mForeignUserLocation.size(); i++) {
                                        if (mForeignUserLocation.get(i).getSnippet().equals(uuid)) {
                                            Log.v("TEST234", "found cached");
                                            Marker temp = mForeignUserLocation.get(i);
                                            temp.setPosition(latLng);
                                            temp.setVisible(true);
                                            mForeignUserLocation.add(i, temp);
                                            notFound = false;
                                            break;
                                        } else {
                                            notFound = true;
                                        }

                                    }
                                    if (image != null && notFound) {
                                        Log.v("TEST234", "found with image");
                                        picassoLoader(latLng, userFirstName, userLastName, uuid, image);
                                    } else if (notFound) {
                                        Log.v("TEST234", "assign default");
                                        picassoLoader(latLng, userFirstName, userLastName, uuid, "default");
                                    }
                                }

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
                final String uuid = dataSnapshot.getKey();

                final double lat, lng;

                lat = (double) dataSnapshot.child("l").child("0").getValue();
                lng = (double) dataSnapshot.child("l").child("1").getValue();

                LatLng latLng = new LatLng(lat, lng);

                for (int i = 0; i < mForeignUserLocation.size(); i++) {
                    if (!uuid.equals(mUserUID)) {
                        if (mForeignUserLocation.get(i).getSnippet().equals(uuid)) {
                            Log.v("TEST234", "found cached");
                            Marker temp = mForeignUserLocation.get(i);
                            temp.setPosition(latLng);
                            mForeignUserLocation.add(i, temp);
                            break;
                        }
                    }
                }
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

        };

        userInfoRef = FirebaseDatabase
                .getInstance()
                .getReference("Users");

        mUserStatuesListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.v("TEST1", "mUserStatuesListener Working");
                String userState, uuid;
                uuid = dataSnapshot.getKey();
                userState = (String) dataSnapshot.child("userState").child("state").getValue();

                if (userState == null) {
                    userState = "neutral";
                }
                if (userState.equals("offline")) {
                    for (int i = 0; i < mForeignUserLocation.size(); i++) {
                        if (!uuid.equals(mUserUID)) {
                            if (mForeignUserLocation.get(i).getSnippet().equals(uuid)) {
                                Marker temp = mForeignUserLocation.get(i);
                                temp.setVisible(false);
                                mForeignUserLocation.add(i, temp);
                                break;
                            }
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
        };

        rootMessageRef = FirebaseDatabase.getInstance().getReference("Messages/" + mUserUID);

        mMessageListener = new ChildEventListener() {


            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.v("MN", "Parent Call");
                messageRef = dataSnapshot.getRef();
                limit++;
                test = messageRef.limitToLast(1).addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.v("MN", "Child Call");


                        if (count >= limit) {
                            String fromUserUID, message, fromUserName;

                            Messages messages = dataSnapshot.getValue(Messages.class);
                            fromUserUID = messages.getFrom();
                            message = messages.getMessage();
                            fromUserName = messages.getFromName();

                            if (mUserUID != null && fromUserUID != null) {
                                if (!fromUserUID.equals(mUserUID)) {
                                    sendNotificationChatChannel(R.drawable.ic_menu_message,
                                            message, "New message from " + fromUserName,
                                            fromUserName, fromUserUID);
                                }
                            }

                        } else {
                            count++;
                        }
                        Log.v("MN", limit + " limit count !");
                        Log.v("MN", count + " count count !");


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
        };
    }

    public void picassoLoader(final LatLng latLng, final String userFirstName,
                              final String userLastName, final String uuid, final String image) {
        mTarget = new Target() {
            Marker tempMarker;

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                final Marker foreignUserLocation = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title(userFirstName + " " + userLastName)
                        .snippet(uuid)
                );
                foreignUserLocation.setTag("UserLocationMarker");
                if (uuid.equals(mUserUID)) {
                    if (image.equals("anonymous")) {
                        mUserAnnonmoysMarker = foreignUserLocation;
                    } else {
                        mUserMarker = foreignUserLocation;
                    }
                }
                foreignUserLocation.setTag("UserLocationMarker");
                mForeignUserLocation.add(foreignUserLocation);

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.d("picasso", "onBitmapFailed");
                e.printStackTrace();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Target preLoadTarget = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        final Marker loading = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title(userFirstName + " " + userLastName)
                                .snippet(uuid)
                        );
                        tempMarker = loading;
                        tempMarker.setTag("UserLocationMarker");
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };
                Picasso.get()
                        .load(R.drawable.ic_person)
                        .resize(200, 200)
                        .centerCrop()
                        .transform(new BubbleTransformation(20))
                        .into(preLoadTarget);
                if (uuid.equals(mUserUID)) {
                    if (image.equals("anonymous")) {
                        mUserAnnonmoysMarker = tempMarker;
                    } else {
                        mUserMarker = tempMarker;
                    }
                }
            }
        };
        if (image.equals("default")) {
            Log.v("PicassoTest", "Inside the function as default");
            Picasso.get()
                    .load(R.drawable.ic_person)
                    .resize(200, 200)
                    .centerCrop()
                    .transform(new BubbleTransformation(20))
                    .into(mTarget);
        } else if (image.equals("anonymous")) {
            Log.v("PicassoTest", "Inside the function as anonymous");
            Picasso.get()
                    .load(R.drawable.ic_action_name)
                    .resize(200, 200)
                    .centerCrop()
                    .transform(new BubbleTransformation(20, "#505050"))
                    .into(mTarget);
        } else {
            Log.v("PicassoTest", "Inside the function as url");
            Picasso.get()
                    .load(image)
                    .resize(200, 200)
                    .centerCrop()
                    .transform(new BubbleTransformation(20))
                    .into(mTarget);
        }

    }


    public void addMarkerOnMap(String msg, int icon, int imageID) {
        if (mLastLocation != null) {
            mapController.addMarker(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                    msg, icon, imageID,
                    MainActivity.this, mMap);
        } else {
            Toast.makeText(MainActivity.this, "Please Turn on GPS", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendNotificationChatChannel(int icon, String message, String tittle,
                                            String targetUserName, String targetUserUUID) {
        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
        chatIntent.putExtra("tittle", targetUserName);
        chatIntent.putExtra("visit_user_id", targetUserUUID);
        chatIntent.putExtra("name", mUserFirstName + " " + mUserLastName);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(chatIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, ApplicationController.CHANNEL_1_ID)
                .setSmallIcon(icon)
                .setContentTitle(tittle)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build();
        mNotificationManagerPing.notify(1, notification);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag().equals("UserLocationMarker")) {
                    if (marker.getTitle().equals(mUserMarker.getTitle())) {
                        marker.showInfoWindow();
                        return false;
                    } else {
                        Intent chatActivity = new Intent(MainActivity.this, ChatActivity.class);
                        chatActivity.putExtra("tittle", marker.getTitle());
                        chatActivity.putExtra("visit_user_id", marker.getSnippet());
                        chatActivity.putExtra("name", mUserFirstName + " " + mUserLastName);
                        startActivity(chatActivity);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    boolean flag = true;

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (flag) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            flag = false;
        }

        mapController.trackUserLocation(latLng);

        if (mImageURL == null) {
            mImageURL = "default";
        }

        if (mAnonymityPreference == null) {
            mAnonymityPreference = false;
        }
        if (mAnonymityPreference) {
            if (mUserMarker != null) {
                mUserMarker.setPosition(latLng);
            } else {
                picassoLoader(latLng, mUserFirstName, mUserLastName, mUserUID, "anonymous");
            }
        } else {
            if (mUserMarker != null) {
                mUserMarker.setPosition(latLng);
            } else {
                picassoLoader(latLng, mUserFirstName, mUserLastName, mUserUID, mImageURL);
            }
        }


    }

    //When map is ready to start working
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(MainActivity.this).checkLocationSettings(builder.build());


        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                startIntentSenderForResult(
                                        resolvable.getResolution().getIntentSender(),
                                        LocationRequest.PRIORITY_HIGH_ACCURACY, null,
                                        0, 0, 0, null);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.v("GPS", "onActivityResult: GPS Enabled by user");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.v("GPS", "onActivityResult: User rejected GPS request");
                        break;
                    default:
                        break;
                }
                break;
        }
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
            updateUserStatus("offline");
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
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
//            Intent chatActivity = new Intent(this, ChatActivity.class);
//            startActivity(chatActivity);
            Toast.makeText(MainActivity.this, "Coming Soon !", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_share) {
            String shareSubject = "User Location using Relevium APP";
            String Uri = "http://maps.google.com/maps?daddr=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "My Current Location." +
                    "\nShared using Relevium application <3.\n\n\n" + Uri);

            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(sharingIntent, 0);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe) {
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        } else if (id == R.id.nav_sos) {
            Toast.makeText(this, "SOS sent!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_log_out) {
            updateUserStatus("offline");
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        if (mMessageListener != null) {
            rootMessageRef.removeEventListener(mMessageListener);
        }
        if (mPingListener != null) {
            mDatabase.removeEventListener(mPingListener);
        }
        if (mUserLocationListener != null) {
            mUserLocationDataBase.removeEventListener(mUserLocationListener);
        }
        if (mUserStatuesListener != null) {
            userInfoRef.removeEventListener(mUserStatuesListener);
        }
        count = 0;
        limit = 0;
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onStop() {
        if (mMessageListener != null) {
            rootMessageRef.removeEventListener(mMessageListener);
        }
        if (mPingListener != null) {
            mDatabase.removeEventListener(mPingListener);
        }
        if (mUserLocationListener != null) {
            mUserLocationDataBase.removeEventListener(mUserLocationListener);
        }
        if (mUserStatuesListener != null) {
            userInfoRef.removeEventListener(mUserStatuesListener);
        }
        count = 0;
        limit = 0;
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    public void onResume() {
        count = 0;
        limit = 0;
        if (mMessageListener != null) {
            rootMessageRef.removeEventListener(mMessageListener);
        }
        if (mPingListener != null) {
            mDatabase.removeEventListener(mPingListener);
        }
        if (mUserLocationListener != null) {
            mUserLocationDataBase.removeEventListener(mUserLocationListener);
        }
        if (mUserStatuesListener != null) {
            userInfoRef.removeEventListener(mUserStatuesListener);
        }
        rootMessageRef.addChildEventListener(mMessageListener);
        mDatabase.addChildEventListener(mPingListener);
        mUserLocationDataBase.addChildEventListener(mUserLocationListener);
        userInfoRef.addChildEventListener(mUserStatuesListener);
        super.onResume();
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        DateFormat currentDate = java.text.SimpleDateFormat.getDateInstance();
        saveCurrentDate = currentDate.format(calendar.getTime());

        DateFormat currentTime = SimpleDateFormat.getTimeInstance();
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        mRootRef.child("Users").child(mUserUID).child("userState")
                .updateChildren(onlineStateMap);

    }

    @Override
    protected void onStart() {
        if (mMessageListener != null) {
            rootMessageRef.removeEventListener(mMessageListener);
        }
        if (mPingListener != null) {
            mDatabase.removeEventListener(mPingListener);
        }
        if (mUserLocationListener != null) {
            mUserLocationDataBase.removeEventListener(mUserLocationListener);
        }
        if (mUserStatuesListener != null) {
            userInfoRef.removeEventListener(mUserStatuesListener);
        }
        count = 0;
        limit = 0;
        super.onStart();

        if (mUserUID == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        } else {
            updateUserStatus("online");

            VerifyUserExistence();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMessageListener != null) {
            rootMessageRef.removeEventListener(mMessageListener);
        }
        if (test != null) {
            messageRef.removeEventListener(test);
        }
        if (mPingListener != null) {
            mDatabase.removeEventListener(mPingListener);
        }
        if (mUserLocationListener != null) {
            mUserLocationDataBase.removeEventListener(mUserLocationListener);
        }
        if (mUserStatuesListener != null) {
            userInfoRef.removeEventListener(mUserStatuesListener);
        }
        count = 0;
        limit = 0;
        super.onDestroy();

        if (mUserUID != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        String currentUserID = mUserAuth.getCurrentUser().getUid();

        mRootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("mFirstName").exists())) {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(settingsIntent);
    }
}
