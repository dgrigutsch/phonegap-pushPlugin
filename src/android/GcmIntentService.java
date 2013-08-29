package com.plugin.gcm;

import android.app.*;
import android.os.PowerManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private static PowerManager.WakeLock sWakeLock;
    private static final Object LOCK = GcmIntentService.class;
    private static final String WAKELOCK_KEY = "GCM_LIB";

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " +extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                if (extras != null)
                {
                    boolean	foreground = this.isInForeground();
                    extras.putBoolean("foreground", foreground);
                    if (foreground)
                        PushPlugin.sendExtras(extras);
                    else
                        createNotification(this.getApplicationContext(), extras);
                }

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        synchronized (LOCK) {
            // sanity check for null as this is a public method
            if (sWakeLock != null) {
                sWakeLock.release();
            } else {
                // should never happen during normal workflow
                Log.e(TAG, "Wakelock reference is null");
            }
        }
    }

    public void createNotification(Context context, Bundle extras)
    {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.icon_small;

        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle(context.getText(R.string.notification_title))
                        .setSmallIcon(icon)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(appName)
                        .setContentIntent(contentIntent);

        String message = extras.getString("message");
        if (message != null) {
            mBuilder.setContentText(message);
        } else {
            mBuilder.setContentText("<missing message content>");
        }

        String msgcnt = extras.getString("msgcnt");
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }

        Notification notification = mBuilder.build();

        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify((String) appName, NOTIFICATION_ID, notification);

    }

    public static void cancelNotification(Context context)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((String)getAppName(context), NOTIFICATION_ID);
    }

    private static String getAppName(Context context)
    {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String)appName;
    }

    public boolean isInForeground()
    {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);

        if (services.get(0).topActivity.getPackageName().toString().equalsIgnoreCase(getApplicationContext().getPackageName().toString()))
            return true;

        return false;
    }



    static void runIntentInService(Context context, Intent intent,
                                   String className) {
        synchronized (LOCK) {
            if (sWakeLock == null) {
                // This is called from BroadcastReceiver, there is no init.
                PowerManager pm = (PowerManager)
                        context.getSystemService(Context.POWER_SERVICE);
                sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        WAKELOCK_KEY);
            }
        }
        sWakeLock.acquire();
        intent.setClassName(context, className);
        context.startService(intent);
    }

}

