package xposed73.com.simplifywebview.FCMService;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import xposed73.com.simplifywebview.Config.Config;
import static xposed73.com.simplifywebview.Config.Config.STR_KEY;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleMessage(remoteMessage.getData().get(STR_KEY));
    }

    private void handleMessage(String message) {
        Intent pushNotification = new Intent(Config.STR_PUSH);
        pushNotification.putExtra(Config.STR_MESSAGE,message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
    }
}