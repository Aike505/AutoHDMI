# AutoHDMI for Konka Android TV

A deliberately tiny Android app for an older Konka Android TV (Android 4.4.2 / API 19).

Verified target behavior before this project was created:

```bash
adb shell am start -n com.konka.tvsettings/.RootActivity
```

brings the existing Konka TV input task to the foreground and, on the target TV, restores HDMI2.

## Runtime behavior

At `BOOT_COMPLETED`:

1. Wait ~8 seconds.
2. Ask Konka TVSettings to enter its RootActivity.
3. Wait 5 seconds and try again.
4. Wait 5 seconds and try once more.

The repeated calls are intentional. On the target TV, bringing the already-running RootActivity to the foreground was verified to be idempotent.

The app first uses the public action advertised by the Konka package:

`com.konka.tvsettings.intent.action.RootActivity`

and falls back to the explicit component:

`com.konka.tvsettings/.RootActivity`

## Build

Open this folder in Android Studio.

The project uses:
- minSdk 19
- targetSdk 19
- compileSdk 35
- Java source
- no AndroidX and no third-party dependencies

Build a debug APK with Android Studio (`Build > Build APK(s)`) or, if you add/use a Gradle wrapper:

```bash
./gradlew assembleDebug
```

Expected APK path:

`app/build/outputs/apk/debug/app-debug.apk`

## Install over ADB

From the Mac platform-tools directory:

```bash
./adb install -r /path/to/app-debug.apk
```

After install, launch it once. This is important on Android 3.1+ because a newly installed package can remain in the "stopped" state until first launch:

```bash
./adb shell am start -n com.autohdmi.boot/.MainActivity
```

The app defaults to "开机自动进入 HDMI" enabled.

You can press the on-screen "立即测试进入 HDMI" button, or simply perform the real cold-boot test after the first launch.

## Logs

While ADB is available:

```bash
./adb logcat -s AutoHDMI:I '*:S'
```

Useful messages include:
- `BOOT_COMPLETED received`
- `HDMI launch attempt 1/3`
- any ActivityNotFoundException / SecurityException

## Disable / remove

Open AutoHDMI and uncheck "开机自动进入 HDMI", or uninstall it:

```bash
./adb uninstall com.autohdmi.boot
```

## Important limitation

This app only automates the Konka TV's transition to its TVSettings RootActivity. It does not add HDMI-CEC, infrared learning, or Konka key control to the Dangbei H5 remote.
