//=============================================================================
// FILE: SvrNativeActivity.java
//
//                  Copyright (c) 2017 QUALCOMM Technologies Inc.
//                              All Rights Reserved.
//
//=============================================================================
package com.ximmerse.xedgelink;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.content.res.AssetManager;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SvrNativeActivity extends android.app.NativeActivity
{
	static public native int SetVPUState(int state);
	static public native int SetNetworkState(int state);
	static public native int SetCmdLineOptions(String cmdline);
	static public native int NativeShutdown(boolean enable);

	private static final String TAG = "CloudXRJ";

	private final String SP_NAME = "CloudXRConfig";

	public static final String FIRST_TIME_TAG = "first_time";
	public static final  String ASSETS_SUB_FOLDER_NAME = "raw";
	public static final int BUFFER_SIZE = 1024;

	SharedPreferences.Editor editor;;

	private String mPath = "";

	private NetworkUtils mNetwork;
    private NetworkRequest mNetworkReq;
    private ConnectivityManager mConn;

	private boolean allowSleep = true;

	private String cmdlineOptions;

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
		Log.i(TAG, "onRequestPermissionsResult: ");
		if (requestCode == 1 && grantResults != null && grantResults.length > 0) {
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				Log.e(TAG, "Fatal error: read external storage permission has not been granted!");
				finish();
			}

			if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
				Log.e(TAG, "Error: write external storage permission has not been granted!");
				//finish();
				// Make this non-fatal for the moment.  Only affects logging outside of app directory.
			}

			if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
				Log.e(TAG, "Fatal error: internet permission has not been granted!");
				finish();
			}

			doResume();
		}
		else
		{
			Log.e(TAG, "Bad return for RequestPermissions: ["+requestCode+"] {"+permissions+"} {"+grantResults+"}");
		}
	}

	private void GetMemUseage() {
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(info);
		Log.d(TAG, "GetMemUseage: " + info.availMem + " " + info.threshold);
	}

	private void doResume() {
		editor.putBoolean("first_start", false);
		editor.commit();

		// call native set command options
		Log.d(TAG, "Ximmerse CloudXR Client. Version: " + BuildConfig.VERSION_NAME);
        System.loadLibrary( "layer" );

//		mNetwork = new NetworkUtils(this);
//		mConn.registerNetworkCallback(mNetworkReq, mNetwork);

        CloudXRJNIApi.Init(SvrNativeActivity.this, SvrNativeActivity.class);

        CXRBroadcastReceiver cxrBroadcastReceiver = new CXRBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CXRBroadcastReceiver.CXR_ACTION_EXIT);
        intentFilter.addAction(CXRBroadcastReceiver.CXR_ACTION_RESTART);
        intentFilter.addAction(CXRBroadcastReceiver.CXR_ACTION_PAUSE);
        intentFilter.addAction(CXRBroadcastReceiver.CXR_ACTION_RESUME);
        registerReceiver(cxrBroadcastReceiver, intentFilter);
        Log.d(TAG, "Register broadcast");

        cmdlineOptions = getIntent().getStringExtra("args");
        if (cmdlineOptions != null && !cmdlineOptions.isEmpty()) {
            Log.i(TAG, "Pass arg " + cmdlineOptions);
            SetCmdLineOptions(cmdlineOptions);
        }

        mNetworkReq = new NetworkRequest.Builder().build();
        mConn = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        // Push some App infomation
        String mac = tryGetWifiMac(this);
        SaveSettings("mac", mac);
        SaveSettings("version", BuildConfig.VERSION_NAME);
        SaveSettings("flavor", BuildConfig.FLAVOR);
	}

	@Override 
	public void onWindowFocusChanged (boolean hasFocus)
	{
        Log.i(TAG, "onWindowFocusChanged: " + hasFocus);
		if(android.os.Build.VERSION.SDK_INT >= 19) 
		{
			if(hasFocus) 
			{
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate+++");
		super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        editor = getSharedPreferences(SP_NAME, MODE_PRIVATE).edit();
        enableSleep(false);
        Log.i(TAG, "onCreate---");
	}

	/*
	 * copy the Assets from assets/raw to app's external file dir
	 */
    @Deprecated
	public void copyAssetsToExternal() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			InputStream in = null;
			OutputStream out = null;

			files = assetManager.list(ASSETS_SUB_FOLDER_NAME);
			for (int i = 0; i < files.length; i++) {
				in = assetManager.open(ASSETS_SUB_FOLDER_NAME + "/" + files[i]);
//				String outDir = getExternalFilesDir(null).toString() + "/";
				String outDir = "/sdcard/wonderland/calib/default";
				File d = new File(outDir);
				if (!d.exists()) {
					d.mkdirs();
				}
				File outFile = new File(outDir, files[i]);

				out = new FileOutputStream(outFile);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		} catch (IOException e) {
			Log.e(TAG, "Failed to get asset file list.", e);
		}
		File file = getExternalFilesDir(null);
		Log.d(TAG, "file:" + file.toString());
	}

    /*
     * read file from InputStream and write to OutputStream.
     */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private String tryGetWifiMac(Context context) {
		String mac = "";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				byte[] addr = networkInterface.getHardwareAddress();
				if (addr == null || addr.length == 0 || !networkInterface.getName().equals("wlan0"))
					continue;

				StringBuffer sbuf = new StringBuffer();
				for (byte b : addr) {
					sbuf.append(String.format("%02X-", b));
				}

				if (sbuf.length() > 0) {
					sbuf.deleteCharAt(sbuf.length() - 1);
				}

				mac = sbuf.toString();
				Log.e(TAG, "MAC: " + networkInterface.getName() + " " + mac );
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return mac;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause+++");

		boolean first_start = getSharedPreferences(SP_NAME, MODE_PRIVATE).getBoolean("first_start", true);
        NativeShutdown(false);
		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		Log.i(TAG, "isInteractive: " + pm.isInteractive() + " is first_start:" + first_start);
		if (pm.isInteractive() && !first_start) { //pause to background, kill self
            enableSleep(true);
			System.exit(0);
		}

		if (!first_start && mConn != null && mNetwork != null)
			mConn.unregisterNetworkCallback(mNetwork);

		Log.d(TAG, "onPause---");
	}

	@Override
	protected void onResume() {
		super.onResume();
        Log.d(TAG, "onResume");
		if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] {
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.RECORD_AUDIO,
					Manifest.permission.INTERNET
			}, 1);
			Log.w(TAG, "Waiting for permissons from user...");
			editor.putBoolean("first_start", true);
			editor.commit();
		} else {
			doResume();
		}
	}

	/* Functions called from JNI layer start */
	public void SaveSettings(String key, String value) {
		Log.i(TAG, "SaveSettings key " + key + " : " + value);
		editor.putString(key, value);
		editor.commit();
	}

	public void SaveSettings(String key, boolean value) {
		Log.i(TAG, "SaveSettings key " + key + " : " + value);
		editor.putString(key, value == true ? "true" : "false");
		editor.commit();
	}

	public void SaveSettings(String key, String[] values) {
		for(int i = 0; i < values.length; i++)
			Log.i(TAG, "SaveSettings key " + key + " : " + values[i]);

		editor.putStringSet(key, new HashSet<>(Arrays.asList(values)));
		editor.commit();
	}

	public String LoadSetting(String key) {
		String ss = getSharedPreferences(SP_NAME, MODE_PRIVATE).getString(key, "");
		Log.i(TAG, "LoadSetting: " + key + ": " + ss);

		return ss;
	}

	public String[] LoadArraySettings(String key) {
		Set<String> s = getSharedPreferences(SP_NAME, MODE_PRIVATE).getStringSet(key, null);

		if (s == null)
			return null;

		List<String> list = new ArrayList<String>(s);
		if (list != null)
			return list.toArray(new String[list.size()]);
		else
			return null;
	}

	public void restartApp() {

        CloudXRJNIApi.restartApp();
	}

    /**
     * Set the value for the given key.
     *
     * @throws IllegalArgumentException if the key exceeds 32 characters
     * @throws IllegalArgumentException if the value exceeds 92 characters
     */
    public static void setprop(String key, String val) throws IllegalArgumentException {

        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = { String.class, String.class };
            Method set = SystemProperties.getMethod("set", paramTypes);

            //Parameters
            Object[] params = { key, val };
            set.invoke(SystemProperties, params);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            //TODO
        }

    }

    public static void enableSleep(boolean enable) {
        Log.i(TAG, "enableSleep: " + enable);
	    if (enable) {
            setprop("debug.pmsc.stayon", "0");
        } else {
            setprop("debug.pmsc.stayon", "1");
        }
    }
    /* Functions called from JNI layer start */
}
