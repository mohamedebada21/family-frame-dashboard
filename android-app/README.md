# Family Frame for Android

Family Frame is a standalone Android application for the 102KZ Android 6.0.1 frame. It bundles the existing dashboard in Android's system WebView, with its own Home activity, an integrated Cairo radio relay and an on-screen Frame menu. Android 6 / API 23 is the minimum. Installation, audio and startup must be tested on the actual device.

## Install on the frame

1. In your existing dashboard, open **Settings → Export backup**. Copy the JSON backup to your computer. Keep your adhan recording and prayer preferences separately. The Android app uses its own data storage.
2. Download the signed `family-frame.apk` and place it in your computer's Downloads folder. Install and open it while connected to the frame over USB:

   ```bash
   adb install -r ~/Downloads/family-frame.apk
   adb shell am start -n com.familyframe.dashboard/.MainActivity
   ```

3. On the frame, tap **Frame → Set Family Frame as Home** and select **Family Frame** as the Home app. If Android displays a chooser, select **Always**. The Android 6 boot receiver also requests dashboard startup after boot. The Home setting is important on firmware that launches its photo interface during startup.
4. Use **Settings → Import backup** inside the dashboard to choose your previous JSON backup. Confirm the import. Use **Frame → Choose adhan MP3** to copy your recording into the app. Re-enter your prayer preferences and test adhan before enabling automatic playback.
5. Test **Clock & Weather → Play radio**. The built-in relay starts automatically inside Family Frame. Its private port is 8768.
6. Disconnect USB, power the frame off and on, and confirm that Family Frame opens by itself. Check saved events, weather, prayer times, delayed adhan, radio and Wi-Fi after restart. The computer can remain disconnected during daily use.

**Frame** stays available in the bottom-right corner. It opens Wi-Fi, date/time, volume and Home settings. Android Back also opens this menu. Version 1.1 adds an on-screen **Dashboard** button and a floating **Back to Family Frame** button while Android settings or a file picker is open. The floating button requires the one-time **Display over other apps** permission. On the reference frame, you can grant it after installation with:

```bash
adb shell appops set com.familyframe.dashboard SYSTEM_ALERT_WINDOW allow
```

Alternatively, opening an external settings screen from the Frame menu offers an **Enable return button** dialog. Turn on the Android permission switch. That permission screen returns to Family Frame automatically after 20 seconds on the Android 6 frame. Once permission is granted, the intended settings screen opens with the return button. Tap it to return. The floating button is removed when the dashboard resumes.

## Frames without a working file picker

Open Family Frame once, then copy your files to its import folder:

```bash
adb push ~/Downloads/our-family-backup.json /sdcard/Android/data/com.familyframe.dashboard/files/import/our-family-backup.json
adb push ~/Downloads/adhan.mp3 /sdcard/Android/data/com.familyframe.dashboard/files/import/adhan.mp3
```

Use **Frame → Import copied backup** and **Frame → Import copied adhan**. The backup import retains the dashboard's validation and asks before replacing saved data. Recordings must be MP3 and at most 32 MB. You can remove the import copies after confirming they work.

**Export backup** saves JSON under the app's external `files/backups` directory. Copy that directory to your computer when needed:

```bash
adb pull /sdcard/Android/data/com.familyframe.dashboard/files/backups ~/Downloads/family-frame-backups
```

Prayer preferences and the separate adhan recording still need to be saved separately. App data, imported audio and these external folders are removed if you uninstall Family Frame. Use `adb install -r` with an APK signed with the same signing key for updates.

## Runtime behavior

Version 1.1 also includes an editable weekly meal plan on the dashboard and simultaneous Fahrenheit/Celsius weather. Use Meals or the dashboard weekly card to plan breakfast, lunch and dinner. Clock settings chooses which temperature unit appears first.

- Dashboard layout, events, chores, photos and clock preferences use the shared HTML source bundled into the APK at build time. An APK update is required to update that embedded HTML.
- The Android app provides the Home screen, boot receiver, file picker, MP3 import, backup export, device settings, floating return button and foreground relay service. Daily use opens Family Frame directly.
- The relay reuses `android-relay`'s server, with a separate port so the older companion can coexist. It binds to `127.0.0.1` and serves a fixed radio endpoint and the imported adhan file.
- Prayer calculations remain fixed to Queens / America/New_York. Automatic adhan runs in the dashboard while the app is open and the screen is kept awake. This version retains the foreground prayer scheduling behavior.
- Weather and radio require internet. Radio starts when you tap Play, including after reboot. Saved dashboard data and the clock work offline.
- Web content is bundled, file URL access is disabled, and top-level links are blocked. JSON networking uses a fixed endpoint allowlist through Android's HTTPS stack. TLS certificates are validated, including installed user CAs on supported Android versions. API failures remain visible in the dashboard.
- The app targets the reference Android 6 frame. Newer Android versions can restrict boot launches and older target-SDK apps. Selecting Family Frame as Home and verifying startup on each device is required for unattended use.

## Build and sign

Install Python 3, Java JDK 17 or newer, Android SDK Platform 35 and Build Tools 35.0.0. Set `ANDROID_SDK_ROOT`, then run from the repository root:

```bash
bash android-app/test.sh
bash android-app/build.sh
```

The output is `android-app/family-frame-unsigned.apk`. Sign it using your own keystore. Keep the same key for updates, outside the repository. The following example reads the password from a private file:

```bash
"$ANDROID_SDK_ROOT/build-tools/35.0.0/apksigner" sign \
  --ks /private/path/family-frame.p12 --ks-key-alias family-frame \
  --ks-pass file:/private/path/store-pass \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out family-frame.apk android-app/family-frame-unsigned.apk
"$ANDROID_SDK_ROOT/build-tools/35.0.0/apksigner" verify --verbose family-frame.apk
```

An existing JRE with Eclipse ECJ can compile sources by setting `ECJ_JAR` to that compiler's JAR. `ANDROID_BUILD_TOOLS_VERSION` can select a different installed tools folder. The build compiles Java 8 bytecode and creates DEX for API 23.

The automated checks exercise the bundled dashboard integration, backup bridge, radio routing, API allowlist and loopback MP3 server. They complement the real-device checks above.

[Android WebView content](https://developer.android.com/develop/ui/views/layout/webapps/load-local-content) · [Android Home intent](https://developer.android.com/reference/android/content/Intent#CATEGORY_HOME) · [APK signing](https://developer.android.com/tools/apksigner)
