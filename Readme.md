## Cordova automatic Installation

_1) execute following Commands :_

		$ sudo cordova plugin add https://github.com/dgrigutsch/phonegap-pushPlugin
		$ sudo cordova build android
		$ cordova build ios


## Manual Installation for Android
_1) copy the contents of src/android/com/ to your project's src/com/ folder. copy the contents of libs/ to your libs/ folder. The final hirearchy will likely look something like this;_


		{project_folder}
		    libs
		        gcm.jar
		        android-support-v13.jar
		        cordova-2.7.0.jar
		    src
		        com
		            plugin
		                gcm
		                    CordovaGCMBroadcastReceiver.java
		                    GCMIntentService.java
		                    PushHandlerActivity.java
		                    PushPlugin.java                     
		            {company_name}
		                {intent_name}
		                    {intent_name}.java                  

_2) Modify your AndroidManifest.xml and add the following lines to your manifest tag:_

	   <uses-permission android:name="android.permission.GET_TASKS" />
	        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	        <uses-permission android:name="android.permission.GET_ACCOUNTS" />
	        <uses-permission android:name="android.permission.WAKE_LOCK" />
	        <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
	        <permission android:name="$PACKAGE_NAME.permission.C2D_MESSAGE" android:protectionLevel="signature" />
	        <uses-permission android:name="$PACKAGE_NAME.permission.C2D_MESSAGE" />
	        
_3) Modify your AndroidManifest.xml and add the following activity, receiver and service tags to your application section._

	   <activity android:name="com.plugin.gcm.PushHandlerActivity"/>
	        <receiver android:name="com.plugin.gcm.CordovaGCMBroadcastReceiver" 
	        android:permission="com.google.android.c2dm.permission.SEND" >
	            <intent-filter>
	                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
	                <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
	                <category android:name="$PACKAGE_NAME" />
	            </intent-filter>
	        </receiver>
	        <service android:name="com.plugin.gcm.GCMIntentService" />
	        
_4) Modify your res/xml/config.xml to include the following line in order to tell Cordova to include this plugin and where it can be found:_

		<plugin name="PushPlugin" value="com.plugin.gcm.PushPlugin" />
		
_5) Add the PushPlugin.js script to your assets/www folder (or javascripts folder, wherever you want really) and reference it in your main index.html file. This file's usage is described in the Plugin API section below._

		<script type="text/javascript" charset="utf-8" src="PushPlugin.js"></script>
		
## Manual Installation for iOS

_1)Copy the following files to your project's Plugins folder:_

		PushPlugin.h
		PushPlugin.m
		
_2)Add a reference for this plugin to the plugins section in config.xml:_

		<plugin name="PushPlugin" value="PushPlugin" />

_3) Add the PushPlugin.js script to your assets/www folder (or javascripts folder, wherever you want really) and reference it in your main index.html file. This file's usage is described in the Plugin API section below._

		<script type="text/javascript" charset="utf-8" src="PushPlugin.js"></script>
		

