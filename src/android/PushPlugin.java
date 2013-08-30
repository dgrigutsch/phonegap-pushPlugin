package com.plugin.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import com.google.android.gcm.*;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PushPlugin extends CordovaPlugin {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG="GCMPlugin";
    private GoogleCloudMessaging gcm;
    private String regid;

    private static CordovaWebView gWebView;

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static String SENDER_ID;

    public static final String ACTIVATE_NOTIFICATIONS="activatePush";
    public static final String DEACTIVATE_NOTIFICATIONS="deactivatePush";
    public static final String BOOTSTRAP = "bootstrap";
    public static final String IS_PUSH_ACTIVATED = "isPushActivated";
    private static ArrayList<String> msgCache;

    private static PushPlugin instance;

    private CallbackContext pushCallback;

      public PushPlugin() {
          instance = this;
      }

      private Context getApplicationContext() {
          return this.cordova.getActivity().getApplicationContext();
      }

      public static PushPlugin getInstance(){
          if(instance == null){
              instance = new PushPlugin();
          }
          return instance;
      }

      @Override
      public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

          boolean result = false;
          pushCallback = callbackContext;
          gWebView = this.webView;

          if(BOOTSTRAP.equals(action)){
              gWebView = this.webView;
              result = true;
              if(msgCache.size() > 0) {
                  Log.e(TAG,"Cache voll");
                    for(String msg : msgCache) {
                        sendJavascript(msg);
                    }
                  msgCache.clear();
              }
          }

          if (ACTIVATE_NOTIFICATIONS.equals(action)) {
              JSONObject jo = args.getJSONObject(0);
              SENDER_ID = (String) jo.get("senderID");
              Log.e(TAG,SENDER_ID);

              if (checkPlayServices()) {
                  gcm = GoogleCloudMessaging.getInstance(this.cordova.getActivity());
                  regid = getRegistrationId(this.cordova.getActivity());
                  if (regid.isEmpty()) {
                      cordova.getThreadPool().execute(new Runnable() {
                          public void run() {
                            registerDevice();
                          }
                      });
                  }
              } else {
                  Log.i(TAG, "No valid Google Play Services APK found.");
              }
              result = true;
              PushPlugin.getInstance().deliverActivationResult(regid, true);
          }
          else if (DEACTIVATE_NOTIFICATIONS.equals(action)) {

            GCMRegistrar.unregister(getApplicationContext());
            result = true;
          }
          else if (IS_PUSH_ACTIVATED.equals(action)) {
              callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
              result = true;
          }
          else {
            result = false;
            Log.e(TAG, "Invalid action : " + action);
          }
        return result;
      }

    /**
     *
     * @param message
     * @param status
     * @param callbackContext
     */
    public void deliverResult(String message, boolean status, CallbackContext callbackContext){
        PluginResult result = new PluginResult(status ? PluginResult.Status.OK : PluginResult.Status.ERROR, message);
        callbackContext.sendPluginResult(result);
    }

      /**
      *
      * @param msg
      * @param status
      */
     public void deliverActivationResult(String msg, boolean status){
         PluginResult result = new PluginResult(status ? PluginResult.Status.OK : PluginResult.Status.ERROR, msg);
         result.setKeepCallback(false);
         if (status) {
             this.pushCallback.sendPluginResult(result);
         }
         else {
             this.pushCallback.sendPluginResult(result);
         }
     }

    public static boolean isActive()
    {
        return gWebView != null;
    }

    public void sendJavascript(final String _json) {
        if(this.webView != null){
            this.webView.sendJavascript(String.format("window.PushPlugin.notificationCallback(%s);", _json));
        }  else {
              if(null == msgCache){
                  msgCache = new ArrayList<String>();
              }
              msgCache.add(_json);
        }
    }

    public static void sendPluginResult(String msg, boolean status){
        PluginResult result = new PluginResult(status ? PluginResult.Status.OK : PluginResult.Status.ERROR, msg);
        result.setKeepCallback(false);
        gWebView.sendPluginResult(result, PushPlugin.getInstance().pushCallback.getCallbackId());
    }

    public void sendExtras(Bundle extras)
    {
        JSONObject jo = new JSONObject();
        String bl = "";
        for(String key:extras.keySet()){
            try {
                jo.put(key,extras.get(key));
            } catch (JSONException e) {
                Log.e(TAG,"cannot "+e.getMessage());
            }
        }
        Log.e(TAG,jo.toString());
        sendJavascript(jo.toString());
    }

    public void onDestroy()
    {
        GCMRegistrar.onDestroy(getApplicationContext());
        gWebView = null;
        super.onDestroy();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.cordova.getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this.cordova.getActivity(),PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return this.cordova.getActivity().getSharedPreferences(this.cordova.getActivity().getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private String registerDevice()  {
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(PushPlugin.this.cordova.getActivity());
            }
            regid = gcm.register(SENDER_ID);
            storeRegistrationId(PushPlugin.this.cordova.getActivity(), regid);
            Log.d(TAG,"RegId: "+regid);
            return regid;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
