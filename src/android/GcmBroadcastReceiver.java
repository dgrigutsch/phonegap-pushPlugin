package com.plugin.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    private static boolean mReceiverSet = false;
    private final static String TAG = GcmBroadcastReceiver.class.getSimpleName();

    @Override
    public final void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: "+ intent.getAction());
        // do a one-time check if app is using a custom GCMBroadcastReceiver
        if (!mReceiverSet) {
            mReceiverSet = true;

        }
        String className = getGCMIntentServiceClassName(context);
        Log.e(TAG, "GCM IntentService class: %s" + className);
        // Delegates to the application-specific intent service.
        GcmIntentService.runIntentInService(context, intent, className);
        setResult(Activity.RESULT_OK, null /* data */, null /* extra */);
    }

    /**
     * Gets the class name of the intent service that will handle GCM messages.
     */
    protected String getGCMIntentServiceClassName(Context context) {
        return getDefaultIntentServiceClassName(context);
    }

    /**
     * Gets the default class name of the intent service that will handle GCM
     * messages.
     */
    static final String getDefaultIntentServiceClassName(Context context) {
        String className = GcmIntentService.class.getPackage().getName()+"."+GcmIntentService.class.getSimpleName();
        return className;
    }
}