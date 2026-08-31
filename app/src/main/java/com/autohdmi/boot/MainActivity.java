package com.autohdmi.boot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String PREFS = "autohdmi";
    private static final String KEY_ENABLED = "enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int padding = dp(24);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("AutoHDMI");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "\n开机快速路径：HOME 出现时立即尝试，0 / 0.8 / 2 / 4 / 7 秒重试。\n"
                        + "BOOT_COMPLETED 保留 1 / 4 / 7 / 10 秒兜底。\n"
                        + "平时按 HOME 仍进入康佳桌面。\n\n"
                        + "目标：com.konka.tvsettings/.RootActivity"
        );
        description.setTextSize(16);
        root.addView(description);

        final CheckBox enabled = new CheckBox(this);
        enabled.setText("开机自动进入 HDMI");
        enabled.setTextSize(18);
        enabled.setChecked(
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .getBoolean(KEY_ENABLED, true)
        );
        enabled.setOnCheckedChangeListener((buttonView, isChecked) ->
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_ENABLED, isChecked)
                        .apply()
        );
        root.addView(enabled);

        Button hdmiButton = makeButton("立即进入 HDMI");
        hdmiButton.setOnClickListener(v -> {
            Intent service = new Intent(this, AutoHdmiService.class);
            service.setAction(AutoHdmiService.ACTION_MANUAL);
            startService(service);
        });
        root.addView(hdmiButton);

        Button launcherButton = makeButton("打开康佳桌面");
        launcherButton.setOnClickListener(v -> openComponent(
                "com.konka.ios7launcher",
                "com.cyanogenmod.trebuchet.Launcher",
                "无法打开康佳桌面"
        ));
        root.addView(launcherButton);

        Button factoryButton = makeButton("打开工厂菜单");
        factoryButton.setOnClickListener(v -> openComponent(
                "com.konka.kkfactory",
                "com.konka.kkfactory.FactoryHome",
                "无法打开工厂菜单"
        ));
        root.addView(factoryButton);

        TextView note = new TextView(this);
        note.setText(
                "\n首次安装 HOME 代理版后：\n"
                        + "按一次 HOME，在系统询问默认主屏幕时选择 AutoHDMI，并选择“始终”。\n\n"
                        + "若不把 AutoHDMI 设为默认 HOME，仍可使用 BOOT_COMPLETED 兜底，"
                        + "但会受到康佳开机广播队列延迟影响。"
        );
        note.setTextSize(14);
        root.addView(note);

        setContentView(scroll);
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = dp(12);
        button.setLayoutParams(lp);
        return button;
    }

    private void openComponent(String packageName, String className, String errorMessage) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, className));
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } catch (Throwable e) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}
