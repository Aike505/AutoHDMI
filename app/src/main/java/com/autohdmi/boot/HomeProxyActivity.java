package com.autohdmi.boot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class HomeProxyActivity extends Activity {

    private static final String TAG = "AutoHDMI";
    private static final long BOOT_WINDOW_MS = 120000L;
    private static final long REENTRY_GUARD_MS = 30000L;
    private static final long FINISH_DELAY_MS = 11000L;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long uptime = SystemClock.elapsedRealtime();

        boolean enabled = getSharedPreferences("autohdmi", MODE_PRIVATE)
                .getBoolean("enabled", true);

        long lastUptime = getSharedPreferences("autohdmi", MODE_PRIVATE)
                .getLong("last_proxy_uptime", -1L);

        boolean sameBootRecentEntry =
                lastUptime >= 0L
                        && uptime >= lastUptime
                        && (uptime - lastUptime) < REENTRY_GUARD_MS;

        Log.i(TAG,
                "HomeProxy started"
                        + ", uptime=" + uptime
                        + ", enabled=" + enabled
                        + ", lastUptime=" + lastUptime
                        + ", guarded=" + sameBootRecentEntry);

        if (!enabled) {
            openKonkaLauncher();
            finish();
            return;
        }

        if (uptime >= BOOT_WINDOW_MS) {
            openKonkaLauncher();
            finish();
            return;
        }

        if (sameBootRecentEntry) {
            Log.w(TAG, "HomeProxy re-entry guarded; falling back to Konka launcher");
            openKonkaLauncher();
            finish();
            return;
        }

        getSharedPreferences("autohdmi", MODE_PRIVATE)
                .edit()
                .putLong("last_proxy_uptime", uptime)
                .apply();

        Intent service = new Intent(this, AutoHdmiService.class);
        service.setAction(AutoHdmiService.ACTION_HOME);
        startService(service);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Log.i(TAG, "HomeProxy finish after safety delay");
                    finish();
                }
            }
        }, FINISH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void openKonkaLauncher() {
        try {
            Intent intent = new Intent();
            intent.setClassName(
                    "com.konka.ios7launcher",
                    "com.cyanogenmod.trebuchet.Launcher"
            );
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            );
            startActivity(intent);

        } catch (Throwable e) {
            Log.e(TAG, "Cannot open Konka launcher", e);
        }
    }
}
