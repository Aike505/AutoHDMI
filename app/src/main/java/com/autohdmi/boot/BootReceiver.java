package com.autohdmi.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoHDMI";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        boolean enabled =
                context.getSharedPreferences("autohdmi", Context.MODE_PRIVATE)
                        .getBoolean("enabled", true);

        if (!enabled) {
            return;
        }

        Log.i(TAG, "BOOT_COMPLETED received");

        Intent service =
                new Intent(context, AutoHdmiService.class);

        service.setAction(AutoHdmiService.ACTION_BOOT);

        context.startService(service);
    }
}