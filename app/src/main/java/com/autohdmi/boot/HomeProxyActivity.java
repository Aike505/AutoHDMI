package com.autohdmi.boot;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class HomeProxyActivity extends Activity {

    private static final String TAG = "AutoHDMI";
    private static final String PREFS = "autohdmi";
    private static final String KEY_ENABLED = "enabled";

    private static final String KEY_SECRET_FIRST_MS = "secret_first_ms";
    private static final String KEY_SECRET_COUNT = "secret_count";
    public static final String KEY_MAINTENANCE_UNTIL_MS = "maintenance_until_ms";

    private static final int SECRET_PRESS_COUNT = 5;
    private static final long SECRET_WINDOW_MS = 4000L;
    private static final long MAINTENANCE_WINDOW_MS = 60000L;
    private static final long FINISH_DELAY_MS = 11000L;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleHome();
    }

    private void handleHome() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(KEY_ENABLED, true);
        if (!enabled) {
            openKonkaLauncher();
            finish();
            return;
        }

        long now = System.currentTimeMillis();

        long maintenanceUntil = prefs.getLong(KEY_MAINTENANCE_UNTIL_MS, 0L);
        if (maintenanceUntil > now) {
            Log.i(TAG, "Maintenance window active; open Konka launcher");
            openKonkaLauncher();
            finish();
            return;
        }

        long first = prefs.getLong(KEY_SECRET_FIRST_MS, 0L);
        int count = prefs.getInt(KEY_SECRET_COUNT, 0);

        if (first <= 0L || now < first || now - first > SECRET_WINDOW_MS) {
            first = now;
            count = 1;
        } else {
            count++;
        }

        Log.i(TAG, "HOME secret counter=" + count + "/" + SECRET_PRESS_COUNT);

        if (count >= SECRET_PRESS_COUNT) {
            prefs.edit()
                    .putLong(KEY_SECRET_FIRST_MS, 0L)
                    .putInt(KEY_SECRET_COUNT, 0)
                    .putLong(KEY_MAINTENANCE_UNTIL_MS, now + MAINTENANCE_WINDOW_MS)
                    .apply();

            try {
                stopService(new Intent(this, AutoHdmiService.class));
            } catch (Throwable e) {
                Log.w(TAG, "Unable to stop AutoHdmiService", e);
            }

            openKonkaLauncher();
            finish();
            return;
        }

        prefs.edit()
                .putLong(KEY_SECRET_FIRST_MS, first)
                .putInt(KEY_SECRET_COUNT, count)
                .apply();

        Intent service = new Intent(this, AutoHdmiService.class);
        service.setAction(AutoHdmiService.ACTION_HOME);
        startService(service);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    finish();
                }
            }
        }, FINISH_DELAY_MS);
    }

    private void openKonkaLauncher() {
        try {
            Intent intent = new Intent();
            intent.setClassName(
                    "com.konka.ios7launcher",
                    "com.cyanogenmod.trebuchet.Launcher"
            );
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            );
            startActivity(intent);
        } catch (Throwable e) {
            Log.e(TAG, "Cannot open Konka launcher", e);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
