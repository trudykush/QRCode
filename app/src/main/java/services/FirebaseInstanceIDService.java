package services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.campionpumps.webservices.qrcode.MainActivity;
import com.campionpumps.webservices.qrcode.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseInstanceIDService extends FirebaseMessagingService {

    private String TAG = FirebaseInstanceIDService.class.getName();
    private static final int NOTIFICATION_MAX_CHARACTERS = 30;

    final String NOTIFICATION_CHANNEL_ID = "kush_notification_channel_001_";
    final String NOTIFICATION_NAME = "kush_";
    final int NOTIFICATION_ID = 10;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
//        String refreshToken = FirebaseInstanceId.getInstance().getId();
        Log.d(TAG, "onNewToken: " + s);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        if(data.size() > 0) {
            Log.d(TAG, "onMessageReceived: " + data);

            sendNotification(data);
        }
    }

    private void sendNotification(Map<String, String> data) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);   //TODO: Even activity should be from server
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create pending intent to launch activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,0);

        String title = data.get("Title");
        String message = data.get("Message");
        String notificationID = data.get("NotificationID");

        if (message != null && message.length() > NOTIFICATION_MAX_CHARACTERS) {
            message = message.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notification Message");
            notificationChannel.enableLights(true);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[] {0, 1000, 500, 1000});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(true)
                        .setTicker("Suspended")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                        .setWhen(0)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent);
        if (notificationManager != null) {
            if (notificationID != null) {
                notificationManager.notify(Integer.parseInt(notificationID), builder.build());
            } else  {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }

        /*Uri defaultoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(FirebaseInstanceIDService.this, "1")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(defaultoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());*/
    }
}
