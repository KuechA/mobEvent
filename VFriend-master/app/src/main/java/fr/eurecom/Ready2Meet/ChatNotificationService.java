package fr.eurecom.Ready2Meet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("ChatNotificationService", "Message received from: " + remoteMessage.getFrom());

        String eventId = null;
        // Check if message contains a data payload.
        if(remoteMessage.getData().size() > 0) {
            Log.d("ChatNotificationService", "Message data payload: " + remoteMessage.getData());
            eventId = remoteMessage.getData().containsKey("EventId") ? remoteMessage.getData()
                    .get("EventId") : null;
        }

        if(remoteMessage.getNotification() != null) {
            Log.d("ChatNotificationService", "Message Notification Body: " + remoteMessage
                    .getNotification().getBody());
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage
                    .getNotification().getBody(), eventId);
        }
    }

    private void showNotification(String title, String text, String eventId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this
                .getApplicationContext()).setSmallIcon(R.mipmap.ic_launcher).setContentTitle
                (title).setContentText(text);

        if(eventId != null) {
            Intent resultIntent = new Intent(getApplicationContext(), ChatActivity.class);
            resultIntent.putExtra("EventId", eventId);
            resultIntent.putExtra("EventTitle", title);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext()
                    , 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }
}
