package com.ximmerse.xedgelink;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CXRBroadcastReceiver extends android.content.BroadcastReceiver {
    private final String TAG = "CXRBroadcastReceiver";

    public static final String CXR_ACTION_RESTART = "com.ximmerse.cxr.restart";
    public static final String CXR_ACTION_EXIT = "com.ximmerse.cxr.exit";
    public static final String CXR_ACTION_PAUSE = "com.ximmerse.cxr.pause";
    public static final String CXR_ACTION_RESUME = "com.ximmerse.cxr.resume";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + context + ", " + intent);
        switch (intent.getAction()) {
            case CXR_ACTION_RESTART:
                CloudXRJNIApi.restartApp();
//                SvrNativeActivity.NativeShutdown(true);
                break;
            case CXR_ACTION_EXIT:
                SvrNativeActivity.enableSleep(true);
                SvrNativeActivity.NativeShutdown(false);
                System.exit(0);
                break;
            case CXR_ACTION_PAUSE:
                SvrNativeActivity.NativeShutdown(false);
                break;
            case CXR_ACTION_RESUME:
                SvrNativeActivity.NativeShutdown(true);
                break;
            default:
                break;
        }
    }
}
