package com.autohdmi.boot;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

public class AutoHdmiService extends IntentService {

    public static final String ACTION_BOOT = "com.autohdmi.boot.action.BOOT";
    public static final String ACTION_MANUAL = "com.autohdmi.boot.action.MANUAL";
    public static final String ACTION_HOME = "com.autohdmi.boot.action.HOME";

    private static final String TAG = "AutoHDMI";
    private static final String PREFS = "autohdmi";

    private static final long[] HOME_DELAYS_MS =
            {0L, 800L, 2000L, 4000L, 7000L};

    private static final long[] BOOT_DELAYS_MS =
            {1000L, 4000L, 7000L, 10000L};

    public AutoHdmiService() {
        super("AutoHdmiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();

        if (ACTION_MANUAL.equals(action)) {
            Log.i(TAG, "Manual HDMI launch");
            launchKonkaTvRootActivity();
            return;
        }

        boolean enabled = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getBoolean("enabled", true);

        if (!enabled) {
            Log.i(TAG, "Auto HDMI disabled");
            return;
        }

        if (isMaintenanceWindowActive()) {
            Log.i(TAG, "Maintenance window active; skip HDMI");
            return;
        }

        if (ACTION_HOME.equals(action)) {
            Log.i(TAG, "HOME HDMI sequence started");
            runSequence(HOME_DELAYS_MS, "HOME");
            return;
        }

        if (ACTION_BOOT.equals(action)) {
            Log.i(TAG, "BOOT fallback HDMI sequence started");
            runSequence(BOOT_DELAYS_MS, "BOOT");
        }
    }

    private void runSequence(long[] schedule, String source) {
        long previous = 0L;

        for (int i = 0; i < schedule.length; i++) {
            long target = schedule[i];
            long sleep = target - previous;

            if (sleep > 0L) {
                SystemClock.sleep(sleep);
            }

            if (isMaintenanceWindowActive()) {
                Log.i(TAG, source + " cancelled by maintenance window");
                return;
            }

            if (isTvSettingsForeground()) {
                Log.i(TAG, source + " already in TVSettings; stop retries");
                return;
            }

            boolean accepted = launchKonkaTvRootActivity();

            Log.i(TAG,
                    source + " HDMI attempt " + (i + 1) + "/" + schedule.length
                            + " at " + target + "ms"
                            + ", startActivity accepted=" + accepted);

            previous = target;
        }
    }

    private boolean isMaintenanceWindowActive() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long until = prefs.getLong(
                HomeProxyActivity.KEY_MAINTENANCE_UNTIL_MS, 0L);
        long now = System.currentTimeMillis();

        if (until <= now) {
            if (until != 0L) {
                prefs.edit()
                        .remove(HomeProxyActivity.KEY_MAINTENANCE_UNTIL_MS)
                        .apply();
            }
            return false;
        }

        return true;
    }

    private boolean isTvSettingsForeground() {
        try {
            ActivityManager am =
                    (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            if (am == null) return false;

            List<ActivityManager.RunningTaskInfo> tasks =
                    am.getRunningTasks(1);

            if (tasks == null || tasks.isEmpty()) return false;

            ComponentName top = tasks.get(0).topActivity;

            return top != null
                    && "com.konka.tvsettings".equals(top.getPackageName());

        } catch (Throwable e) {
            Log.w(TAG, "Unable to inspect foreground activity", e);
            return false;
        }
    }

    private boolean launchKonkaTvRootActivity() {
        try {
            Intent actionIntent =
                    new Intent("com.konka.tvsettings.intent.action.RootActivity");

            actionIntent.addCategory(Intent.CATEGORY_DEFAULT);
            actionIntent.setPackage("com.konka.tvsettings");
            actionIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            );

            startActivity(actionIntent);
            Log.i(TAG, "HDMI launch via public RootActivity action");
            return true;

        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Public RootActivity action not found; use explicit", e);
        } catch (SecurityException e) {
            Log.w(TAG, "Public RootActivity action denied; use explicit", e);
        } catch (Throwable e) {
            Log.w(TAG, "Public RootActivity action failed; use explicit", e);
        }

        try {
            Intent explicitIntent = new Intent();

            explicitIntent.setComponent(new ComponentName(
                    "com.konka.tvsettings",
                    "com.konka.tvsettings.RootActivity"
            ));

            explicitIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            );

            startActivity(explicitIntent);
            Log.i(TAG, "HDMI launch via explicit RootActivity");
            return true;

        } catch (Throwable e) {
            Log.e(TAG, "Unable to start Konka TVSettings RootActivity", e);
            return false;
        }
    }
}
