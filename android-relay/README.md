# Frame Radio Relay for Android

This optional Android companion runs on the frame. It fetches Cairo Quran Radio over HTTPS with Android's network stack and passes audio to the dashboard at `127.0.0.1:8767`. The original frame's vendor media player failed the radio servers' TLS handshakes, even though local MP3 playback worked. The relay uses the frame's Wi-Fi and a separate loopback port from the USB relay.

## One-time setup

1. Install the signed `frame-radio-relay.apk` with `adb install -r frame-radio-relay.apk`.
2. Copy dashboard version 7.6 or newer to `/sdcard/Download/family-calendar.html` and restart Fully.
3. Open **Frame Radio Relay** or run `adb shell am start -n com.familyframe.radio/.MainActivity`. Tap **Start relay** and then **Open dashboard**.
4. Disconnect USB. In **Clock & Weather**, select **Feed → On-frame relay**, then tap **Play radio**.

Starting the relay enables startup after boot. The app runs a foreground service. **Stop relay** stops the service and turns off its boot startup. Use the dashboard's Play and Stop controls for daily listening. For a playback error, confirm the frame has Wi-Fi and collect the relay log with `adb logcat -d -s FrameRadioRelay`.

The APK targets Android 6 or later and has no native libraries, so it does not need a separate ARMv7 build. The relay was tested on a desktop JVM with TLS 1.2 and the Android APK was built and signature-verified. Test audio and a reboot on your frame before relying on unattended operation.

## Build from source

Install a JDK with `javac`, `zip`, Android SDK Platform 35 and Build Tools 35.0.0. Set `ANDROID_SDK_ROOT` to the SDK directory. Run from the repository root:

```bash
bash android-relay/build.sh
```

Sign the resulting `android-relay/frame-radio-relay-unsigned.apk` with your own keystore and `apksigner`. Enable v1 signing for Android 6. Android requires the same signing key for in-place updates. Keep your keystore and password out of the repository.

[Android APK signing documentation](https://developer.android.com/tools/apksigner)
