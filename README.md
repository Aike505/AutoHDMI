# AutoHDMI complete replacement

这是可直接覆盖 GitHub 仓库源码的完整版本。

## 行为

- 默认 HOME 快速路径：0 / 0.8 / 2 / 4 / 7 秒尝试进入 `com.konka.tvsettings/.RootActivity`
- BOOT_COMPLETED 兜底：1 / 4 / 7 / 10 秒
- 开机超过 120 秒后再按 HOME，会进入康佳原桌面
- HOME 代理有 30 秒重入保护，避免 Android 4.4 上重复解析 HOME 形成循环
- 设置页保留：
  - 开机自动进入 HDMI
  - 立即进入 HDMI
  - 打开康佳桌面
  - 打开工厂菜单

## 安装后

```bash
adb shell am start -n com.autohdmi.boot/.MainActivity
```

然后按一次 HOME，把 AutoHDMI 设为“始终”使用的主屏幕。

## 日志

```bash
adb logcat -d -v time -s AutoHDMI:I '*:S'
```

## 注意

这里进入的是康佳 `TVSettings RootActivity`，不是直接写 HDMI1/HDMI2 的底层 source id。
