package com.example.android.releviumfinal;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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
}

