package com.ximmerse.xedgelink;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.os.Bundle;
import android.os.health.TimerStat;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkUtils extends NetworkCallback {
    final private int MAX_RETRY_COUNT = 5;
    static String TAG = "CloudXRJ";

    private Context mAppContext;
    private int retry;

    Timer timer = new Timer();
    TimerTask task;

    public NetworkUtils(Context context) {
        this.mAppContext = context;
    }

    @Override
    public void onAvailable(Network network) {
        Log.i(TAG, "onAvailable: ");
        retry = 0;
        task = new TimerTask() {
            @Override
            public void run() {
                SvrNativeActivity.SetNetworkState(1); // checking

                String ip = "";
                try {
                    ip = mAppContext.getApplicationContext().getSharedPreferences("CloudXRConfig", MODE_PRIVATE).getString("default_server_addr", "");
//                    ip = SvrNativeActivity.GetServerAddress();
                } catch (Exception e) {

                }

                if (ip != null && !ip.isEmpty()) {
                    if (NetworkUtils.Ping(ip)) {
                        boolean is_restart = false;
                        if (mAppContext != null) {
                            Bundle bundle = ((Activity) mAppContext).getIntent().getExtras();
                            if (bundle != null)
                                is_restart = bundle.getBoolean("is_restart");

                            Log.d(TAG, "onAvailable: " + is_restart);
                        }

                        if (SvrNativeActivity.SetNetworkState(is_restart ? 3 : 2) == 0) {
                            task.cancel();
                        }
                    } else if (retry <= MAX_RETRY_COUNT) {
                        retry++;
                    } else if (retry > MAX_RETRY_COUNT) {
                        retry = 0;
                        if (SvrNativeActivity.SetNetworkState(4) == 0)
                            task.cancel();
                    }
                } else {
                    if (SvrNativeActivity.SetNetworkState(2) == 0)
                        task.cancel();
                }
            }
        };

        timer.schedule(task, 0, 500);

        super.onAvailable(network);
    }

    @Override
    public void onLost(Network network) {
        Log.i(TAG, "onLost: ");
        task.cancel();
        SvrNativeActivity.SetNetworkState(0);
        super.onLost(network);
    }

    @Override
    public void onUnavailable() {
        Log.i(TAG, "onUnavailable: ");
        SvrNativeActivity.SetNetworkState(0);
        super.onUnavailable();
    }

    static private boolean Ping(String ip) {
        Log.i("Ping", "startPing...");
        boolean success = false;
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("ping -c 1 -i 0.2 -W 1 " + ip);
            int status = p.waitFor();
            if (status == 0) {
                success = true;
            } else {
                success = false;
            }
        } catch (IOException | InterruptedException e) {
            success = false;
            Log.e(TAG, "Ping " + ip + " : " + e.getMessage());
        } finally {
            p.destroy();
        }

        Log.i(TAG, "Ping " + ip + " : " + success);
        return success;
    }

    static private boolean checkTcpConnection(String ip, int port) {
        return true;
    }
}
