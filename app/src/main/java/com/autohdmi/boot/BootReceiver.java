package com.autohdmi.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoHDMI";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        boolean enabled = context.getSharedPreferences("autohdmi", Context.MODE_PRIVATE)
                .getBoolean("enabled", true);

        if (!enabled) {
            Log.i(TAG, "BOOT_COMPLETED ignored: auto HDMI disabled");
            return;
        }

        Log.i(TAG, "BOOT_COMPLETED received; start fallback sequence");

        Intent service = new Intent(context, AutoHdmiService.class);
        service.setAction(AutoHdmiService.ACTION_BOOT);
        context.startService(service);
    }
}
