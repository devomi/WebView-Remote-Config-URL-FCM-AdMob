package xposed73.com.simplifywebview.FCMService;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.ContentValues.TAG;



public class MyFirebaseIdService extends FirebaseMessagingService {


        @Override
        public void onNewToken(String s) {
            super.onNewToken(s);
            //Log.e("NEW_TOKEN",token);
            // Get updated InstanceID token.
            // If you want to send messages to this application instance or
            // manage this apps subscriptions on the server side, send the
            // Instance ID token to your app server.

            //TODO: Send Token to Server
        }

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            // Check if message contains a data payload.
        }


}