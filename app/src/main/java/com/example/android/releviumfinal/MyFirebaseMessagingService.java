package com.example.android.releviumfinal;

import android.app.Notification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    NotificationManagerCompat mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mNotificationManager = NotificationManagerCompat.from(this);
        sendNotification(remoteMessage.getNotification().getBody());
    }

    private void sendNotification(String message) {
        Notification notification = new NotificationCompat.Builder(this, ApplicationController.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_menu_sos)
                .setContentTitle("New Alert nearby !")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
        mNotificationManager.notify(1, notification);
    }
    @Override
    public void onNewToken(String token) {
        Log.d("onNewTokenCreated", "Refreshed token: " + token);
        DatabaseReference fcmDatabase = FirebaseDatabase.getInstance()
                .getReference("FCM-ID").child(FirebaseAuth.getInstance().getUid());
        fcmDatabase.setValue(token);
    }
}

