package com.autohdmi.boot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class HomeProxyActivity extends Activity {

    private static final String TAG = "AutoHDMI";

    // 开机后 120 秒以内，把 HOME 当成“开机 HOME”
    // 120 秒以后按 HOME，则正常进入康佳桌面
    private static final long BOOT_WINDOW_MS = 120_000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long uptime = SystemClock.elapsedRealtime();

        boolean enabled = getSharedPreferences("autohdmi", MODE_PRIVATE)
                .getBoolean("enabled", true);

        Log.i(TAG, "HomeProxy started, uptime=" + uptime
                + ", enabled=" + enabled);

        if (enabled && uptime < BOOT_WINDOW_MS) {
            // 开机阶段：立即启动快速 HDMI 切换服务
            Intent service = new Intent(this, AutoHdmiService.class);
            service.setAction(AutoHdmiService.ACTION_HOME);
            startService(service);

            // Activity 本身完全不显示界面
            finish();
            return;
        }

        // 非开机阶段：HOME 键仍然进入康佳原桌面
        openKonkaLauncher();
        finish();
    }

    private void openKonkaLauncher() {
        try {
            Intent intent = new Intent();
            intent.setClassName(
                    "com.konka.ios7launcher",
                    "com.cyanogenmod.trebuchet.Launcher"
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable e) {
            Log.e(TAG, "Cannot open Konka launcher", e);
        }
    }
}