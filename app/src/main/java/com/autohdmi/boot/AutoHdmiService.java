package com.autohdmi.boot;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class AutoHdmiService extends IntentService {

    private static final String TAG = "AutoHDMI";

    public static final String ACTION_BOOT =
            "com.autohdmi.boot.ACTION_BOOT";

    public static final String ACTION_HOME =
            "com.autohdmi.boot.ACTION_HOME";

    // HOME代理路径：非常积极
    private static final long[] HOME_DELAYS = {
            0,
            800,
            2000,
            4000,
            7000
    };

    // BOOT_COMPLETED只是兜底
    private static final long[] BOOT_DELAYS = {
            1000,
            4000,
            7000,
            10000
    };

    public AutoHdmiService() {
        super("AutoHdmiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        boolean enabled = getSharedPreferences("autohdmi", MODE_PRIVATE)
                .getBoolean("enabled", true);

        if (!enabled) {
            Log.i(TAG, "Auto HDMI disabled");
            return;
        }

        String action = intent.getAction();

        if (ACTION_HOME.equals(action)) {
            Log.i(TAG, "Fast HOME HDMI sequence started");
            runSequence(HOME_DELAYS, "HOME");
        } else {
            Log.i(TAG, "BOOT fallback HDMI sequence started");
            runSequence(BOOT_DELAYS, "BOOT");
        }
    }

    private void runSequence(long[] schedule, String source) {

        long lastDelay = 0;

        for (int i = 0; i < schedule.length; i++) {

            long targetDelay = schedule[i];
            long sleep = targetDelay - lastDelay;

            if (sleep > 0) {
                SystemClock.sleep(sleep);
            }

            Log.i(TAG,
                    source + " HDMI attempt "
                            + (i + 1) + "/"
                            + schedule.length
                            + " at " + targetDelay + "ms");

            launchHdmi();

            lastDelay = targetDelay;
        }
    }

    private void launchHdmi() {

        // 第一选择：康佳公开 action
        try {
            Intent intent =
                    new Intent("com.konka.tvsettings.intent.action.RootActivity");

            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage("com.konka.tvsettings");

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            );

            startActivity(intent);

            Log.i(TAG, "HDMI launch via public action");

            return;

        } catch (Throwable e) {
            Log.w(TAG,
                    "Public HDMI action failed, trying explicit RootActivity",
                    e);
        }

        // 第二选择：显式 Activity
        try {
            Intent fallback = new Intent();

            fallback.setClassName(
                    "com.konka.tvsettings",
                    "com.konka.tvsettings.RootActivity"
            );

            fallback.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            );

            startActivity(fallback);

            Log.i(TAG, "HDMI launch via explicit RootActivity");

        } catch (Throwable e) {
            Log.e(TAG, "HDMI launch completely failed", e);
        }
    }
}