package com.autohdmi.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoHDMI";
    private static final String PREFS = "autohdmi";
    private static final String KEY_ENABLED = "enabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, true);

        Log.i(TAG, "BOOT_COMPLETED received; enabled=" + enabled);

        if (enabled) {
            Intent service = new Intent(context, AutoHdmiService.class);
            service.setAction(AutoHdmiService.ACTION_BOOT);
            context.startService(service);
        }
    }
}
