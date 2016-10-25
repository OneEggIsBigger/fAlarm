package de.openfiresource.falarm;


import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import de.openfiresource.falarm.models.OperationMessage;
import de.openfiresource.falarm.models.OperationUser;
import de.openfiresource.falarm.service.AlarmService;
import de.openfiresource.falarm.ui.MainActivity;
import de.openfiresource.falarm.ui.OperationActivity;
import de.openfiresource.falarm.ui.OperationUserFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean activate = preferences.getBoolean("activate", true);

        Map<String, String> data = remoteMessage.getData();

        // Check if message contains a data payload.
        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + data);

            //@todo: A little dirty now
            if (data.containsKey("come")) {
                OperationUser operationUser = OperationUser.fromFCM(this, data);
                long opId = operationUser.save();

                //Send Broadcast
                Intent brIntent = new Intent();
                brIntent.setAction(OperationUserFragment.INTENT_USER_CHANGED);
                sendBroadcast(brIntent);

            } else if (activate) {
                OperationMessage operationMessage = OperationMessage.fromFCM(this, data);
                if (operationMessage != null) {
                    long notificationId = operationMessage.save();

                    //Send Broadcast
                    Intent brIntent = new Intent();
                    brIntent.setAction(MainActivity.INTENT_RECEIVED_MESSAGE);
                    sendBroadcast(brIntent);

                    //Start alarm Service
                    Intent intentData = new Intent(getBaseContext(),
                            AlarmService.class);
                    intentData.putExtra(OperationActivity.EXTRA_ID, operationMessage.getId());

                    //Firt stop old service when exist, then start new
                    stopService(intentData);
                    startService(intentData);
                }
            }
        }
    }
    // [END receive_message]
}