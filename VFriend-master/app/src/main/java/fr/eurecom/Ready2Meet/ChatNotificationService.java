package fr.eurecom.Ready2Meet;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("ChatNotificationService", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if(remoteMessage.getData().size() > 0) {
            Log.d("ChatNotificationService", "Message data payload: " + remoteMessage.getData());
        }

        if(remoteMessage.getNotification() != null) {
            Log.d("ChatNotificationService", "Message Notification Body: " + remoteMessage
                    .getNotification().getBody());
        }
    }
}
