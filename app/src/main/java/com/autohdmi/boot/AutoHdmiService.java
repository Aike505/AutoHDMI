package com.autohdmi.boot;

import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

public class AutoHdmiService extends IntentService {
    public static final String ACTION_BOOT = "com.autohdmi.boot.action.BOOT";
    public static final String ACTION_MANUAL = "com.autohdmi.boot.action.MANUAL";

    private static final String TAG = "AutoHDMI";

    // Robust boot sequence:
    // attempt #1 at ~8s, #2 at ~13s, #3 at ~18s after the service starts.
    private static final long INITIAL_DELAY_MS = 8000L;
    private static final long RETRY_DELAY_MS = 5000L;
    private static final int ATTEMPTS = 3;

    public AutoHdmiService() {
        super("AutoHdmiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean boot = intent != null && ACTION_BOOT.equals(intent.getAction());

        if (boot) {
            sleepQuietly(INITIAL_DELAY_MS);
        }

        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            boolean launched = launchKonkaTvRootActivity();
            Log.i(TAG, "HDMI launch attempt " + attempt + "/" + ATTEMPTS
                    + ", startActivity returned=" + launched);

            if (attempt < ATTEMPTS) {
                sleepQuietly(RETRY_DELAY_MS);
            }
        }
    }

    private boolean launchKonkaTvRootActivity() {
        // Preferred route: use the public action that the Konka TVSettings package
        // advertises in its manifest.
        Intent actionIntent =
                new Intent("com.konka.tvsettings.intent.action.RootActivity");
        actionIntent.addCategory(Intent.CATEGORY_DEFAULT);
        actionIntent.setPackage("com.konka.tvsettings");
        actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        try {
            startActivity(actionIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Action intent was not resolved; trying explicit component", e);
        } catch (SecurityException e) {
            Log.w(TAG, "Action intent was denied; trying explicit component", e);
        }

        // Fallback: this is the exact component already verified over ADB
        // on the target Konka TV.
        Intent explicitIntent = new Intent();
        explicitIntent.setComponent(new ComponentName(
                "com.konka.tvsettings",
                "com.konka.tvsettings.RootActivity"));
        explicitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        try {
            startActivity(explicitIntent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Unable to start Konka RootActivity", e);
            return false;
        }
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
