package com.dev.macx.visningsappen;

/**
 * This class is used for..
 *
 * @author Midhun.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.dev.macx.visningsappen.Utils.SimpleGeofence;
import com.dev.macx.visningsappen.Utils.SimpleGeofenceStore;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "IntentService";

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int i=0;
        i++;
        Log.e("Service","Started");
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
       // if(!Container.getInstance().alerton.equals("1")) return;
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
            String errorMsg = getErrorString(geoFenceEvent.getErrorCode() );
            Toast.makeText(this,errorMsg, Toast.LENGTH_LONG).show();
            Log.e( TAG, errorMsg );
        } else {

            // Get the geofence id triggered. Note that only one geofence can be triggered at a
            // time in this example, but in some cases you might want to consider the full list
            // of geofences triggered.
            String triggeredGeoFenceId = geoFenceEvent.getTriggeringGeofences().get(0)
                    .getRequestId();
            SimpleGeofenceStore simpleGeofenceStore = new SimpleGeofenceStore(this);
            SimpleGeofence simpleGeofence = simpleGeofenceStore.getGeofence(triggeredGeoFenceId);

            int transitionType = geoFenceEvent.getGeofenceTransition();
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                {
                    // Get the geofence that were triggered
                    List<Geofence> triggeringGeofences = geoFenceEvent.getTriggeringGeofences();
                    sendSimpleNotification("Visnigsappen", "You have reached the place you are looking for.", this);
                    if(simpleGeofence.getId()== null){
                        simpleGeofence.setId(String.valueOf(System.currentTimeMillis()));
                    }
                    simpleGeofenceStore.setGeofence(simpleGeofence.getId(),simpleGeofence);
                    Intent i = new Intent("android.intent.action.MAIN");
                    this.sendBroadcast(i);

                    //String geofenceTransitionDetails = getGeofenceTrasitionDetailsandSendNotification(triggeringGeofences );

                }
            }
            if(simpleGeofence.getId()== null){
                simpleGeofence.setId(String.valueOf(System.currentTimeMillis()));
            }
            simpleGeofenceStore.setGeofence(simpleGeofence.getId(),simpleGeofence);
        }
    }

    public static void sendSimpleNotification(String title, String msg, Context context) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MapActivity.class);
        notificationIntent.putExtra("directmap", "true");
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        //        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
        //                new Intent(ctx, Splash.class), 0);
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.mark1);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.mark1)
                        .setLargeIcon(image)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final int resourceId) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

}
