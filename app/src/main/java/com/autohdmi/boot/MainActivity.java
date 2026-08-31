package com.autohdmi.boot;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String PREFS = "autohdmi";
    private static final String KEY_ENABLED = "enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        // First launch defaults to enabled.
        if (!prefs.contains(KEY_ENABLED)) {
            prefs.edit().putBoolean(KEY_ENABLED, true).apply();
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = 32;
        layout.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("AutoHDMI\n\n"
                + "开机后自动进入康佳 TVSettings RootActivity。\n"
                + "启动策略：约 1 / 4 / 7 / 10 秒尝试，共 4 次。\n\n"
                + "目标：com.konka.tvsettings/.RootActivity");
        title.setTextSize(20f);
        layout.addView(title);

        final CheckBox enabled = new CheckBox(this);
        enabled.setText("开机自动进入 HDMI");
        enabled.setChecked(prefs.getBoolean(KEY_ENABLED, true));
        enabled.setTextSize(18f);
        layout.addView(enabled);

        enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean(KEY_ENABLED, enabled.isChecked()).apply();
            }
        });

        Button test = new Button(this);
        test.setText("立即测试进入 HDMI");
        test.setTextSize(18f);
        layout.addView(test);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(MainActivity.this, AutoHdmiService.class);
                service.setAction(AutoHdmiService.ACTION_MANUAL);
                startService(service);
            }
        });
        
        Button factory = new Button(this);
        factory.setText("打开工厂菜单");
        factory.setTextSize(18f);
        layout.addView(factory);

        factory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(
                        "com.konka.kkfactory",
                        "com.konka.kkfactory.FactoryHome"
                );
                startActivity(intent);
            }
        });

        setContentView(layout);
    }
}
