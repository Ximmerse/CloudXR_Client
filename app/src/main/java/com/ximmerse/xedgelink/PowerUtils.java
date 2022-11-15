package com.ximmerse.xedgelink;

import android.content.Context;
import android.content.Intent;

public class PowerUtils {
    final static String ACTION_ALLOW_SLEEPING = "com.android.ops.exitapp";
    final static String ACTION_FORBID_SLEEPING = "com.android.ops.startapp";

    static void setAllowSleep(Context context, boolean allowSleep) {
        Intent intent = new Intent();
        if (allowSleep) {
            intent.setAction(ACTION_ALLOW_SLEEPING);
        } else {
            intent.setAction(ACTION_FORBID_SLEEPING);
        }

        context.sendBroadcast(intent);
    }


}
