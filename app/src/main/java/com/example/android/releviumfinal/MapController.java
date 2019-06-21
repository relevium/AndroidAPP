package com.example.android.releviumfinal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class MapController extends MainActivity {

    String mUserId;

    public MapController(String mUserId) {
        this.mUserId = mUserId;
    }

    public Marker addMarker(double latitude, double longitude, String tittle, String message, int image, int imageId,
                          Context context, GoogleMap mMap) {
        BitmapDescriptor bmp = generateBitmapDescriptorFromRes(context, image);

        LatLng userLocation = new LatLng(latitude, longitude);
        Marker userMarker = mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title(tittle)
                .snippet(message)
                .icon(bmp));
        userMarker.setTag("UserPing");

        addMarkerToDatabase(imageId, message, userLocation);
        return userMarker;
    }

    public void addMarkerToDatabase(int imageId, String description, LatLng mLastLocation) {
        DatabaseReference pingDetailsRef = FirebaseDatabase.getInstance().getReference("Ping-Details").push();

        Pings ping = new Pings();
        ping.setmDescription(description);
        ping.setmImageId(imageId);
        ping.setmUserID(mUserId);


        pingDetailsRef.setValue(ping).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

        DatabaseReference geoFireLocations = FirebaseDatabase.getInstance().getReference("GeoFirePingLocations");
        GeoFire geoFire = new GeoFire(geoFireLocations);
        geoFire.setLocation(pingDetailsRef.getKey(), new GeoLocation(mLastLocation.latitude, mLastLocation.longitude), new
                GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Do some stuff if you want to
                    }
                });
    }

    public void trackUserLocation(LatLng mLastLocation) {

        DatabaseReference geoFireLocations = FirebaseDatabase.getInstance().getReference("User-Location");
        GeoFire geoFire = new GeoFire(geoFireLocations);
        geoFire.setLocation(mUserId, new GeoLocation(mLastLocation.latitude, mLastLocation.longitude), new
                GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Do some stuff if you want to
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

    public Marker addMarkerFromDB(double latitude, double longitude, String tittle, String message, int image, Context context
            , GoogleMap mMap) {
        BitmapDescriptor bmp = generateBitmapDescriptorFromRes(context, image);
        LatLng userLocation = new LatLng(latitude, longitude);
        Marker mapMarker = mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title(tittle)
                .snippet(message)
                .icon(bmp));
        mapMarker.setTag("UserPing");
        return mapMarker;
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        TextView txt_name = (TextView)marker.findViewById(R.id.name);
        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

}
