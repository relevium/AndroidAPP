package com.example.android.releviumfinal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CleanService extends Service {
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e("CleanService", "Removing");
        updateUserStatus("offline");
    }
    private void updateUserStatus(String state) {
        String mUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
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
}
