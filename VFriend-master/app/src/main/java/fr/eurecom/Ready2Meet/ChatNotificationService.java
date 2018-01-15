package fr.eurecom.Ready2Meet;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("ChatNotificationService", "Message received from: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if(remoteMessage.getData().size() > 0) {
            Log.d("ChatNotificationService", "Message data payload: " + remoteMessage.getData());
        }

        if(remoteMessage.getNotification() != null) {
            Log.d("ChatNotificationService", "Message Notification Body: " + remoteMessage
                    .getNotification().getBody());
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage
                    .getNotification().getBody());
        }
    }

    private void showNotification(String title, String text) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this
                .getApplicationContext()).setSmallIcon(R.mipmap.ic_launcher).setContentTitle
                (title).setContentText(text);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }
}
