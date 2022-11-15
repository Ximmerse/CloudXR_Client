package com.ximmerse.xedgelink;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CloudXRJNIApi {
    public static Context appContext;
    public static Class appClass;

    public static void Init(Context context, Class clz) {
        appContext = context;
        appClass = clz;
    }

    public static void restartApp() {
        Intent intent = new Intent(appContext, appClass);
        Intent restartIntent = Intent.makeRestartActivityTask(intent.getComponent());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("is_restart", true);
        appContext.startActivity(restartIntent);
        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());

    }
}
