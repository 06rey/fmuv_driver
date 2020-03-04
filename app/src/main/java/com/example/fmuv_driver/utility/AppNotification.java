package com.example.fmuv_driver.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.fmuv_driver.R;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AppNotification {

    private Notification notification;
    private Context context;
    private PendingIntent pendingIntent;
    private String channel_id, title, text;
    private Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    public static final String OVER_SPEED_NOTIFICATION = "OVER SPEED";

    public static class Builder {

        private Context context;
        private PendingIntent pendingIntent;
        private String channel_id, title, text;

        public Builder (Context context) {
            this.context = context;
        }

        public Builder setPendingIntent(PendingIntent pendingIntent) {
            this.pendingIntent = pendingIntent;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setChannel_id(String channel_id) {
            this.channel_id = channel_id;
            return this;
        }

        public AppNotification build() {

            AppNotification appNotification = new AppNotification();

            appNotification.pendingIntent = this.pendingIntent;
            appNotification.context = this.context;
            appNotification.channel_id = this.channel_id;
            appNotification.title = title;
            appNotification.text = this.text;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        channel_id,
                        "Foreground Service Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }

            return appNotification;
        }
    }

    public AppNotification() {
        // Constructor
    }

    public Notification getNotification() {
        this.notification = new NotificationCompat.Builder(this.context, this.channel_id)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(alarmSound)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentIntent(pendingIntent)
                .build();
        return this.notification;
    }

    public void setMsgNotification(String speed) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService( NOTIFICATION_SERVICE ) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext() , "default" ) ;
        mBuilder.setContentTitle( "Over Speed Detected!" ) ;
        mBuilder.setContentText( "An over speeding of " + speed + " is detected!" ) ;
        mBuilder.setSmallIcon(R.drawable.notif_icon) ;
        mBuilder.setAutoCancel( true) ;
        mBuilder.setSound(alarmSound);
        mBuilder.setOnlyAlertOnce(true);
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel(OVER_SPEED_NOTIFICATION, "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId(OVER_SPEED_NOTIFICATION) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
    }
}
