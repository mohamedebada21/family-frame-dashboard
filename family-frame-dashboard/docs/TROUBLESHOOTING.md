# Troubleshooting

## ADB shows no device

```bash
adb devices -l
```

Check device power, a known data-capable cable, another USB port and any adapter/hub. On a Mac, inspect USB enumeration:

```bash
ioreg -p IOUSB -w 0
```

An `Android` USB entry indicates USB enumeration, not necessarily authorized ADB. If ADB reports `unauthorized`, unlock the screen and accept the debugging prompt. If necessary, reconnect and restart the computer's ADB server:

```bash
adb kill-server
adb start-server
adb devices -l
```

Restarting that server affects other ADB sessions. A custom frame may not expose debugging settings; there is no universal seven-tap unlock or universal firmware conversion procedure.

## File does not open / need to return to Fully

```bash
adb shell ls -l /sdcard/Download/family-calendar.html
adb shell input keyevent 4
adb shell monkey -p de.ozerov.fully -c android.intent.category.LAUNCHER 1
```

Check the exact Start URL and filename. Downloaded filenames containing `(1)` or `(2)` will not match `family-calendar.html`. If Back is blocked by kiosk configuration, use the configured Fully exit gesture/PIN. Do not reset app data to resolve navigation.

## Wi-Fi connected but weather unavailable

Wi-Fi association does not prove internet or certificate trust is working. First check the city, Show weather toggle, internet access and device date/time, then tap refresh.

```bash
adb shell date
adb logcat -d -t 500 | grep -iE 'chromium|ssl|certificate|cors|open-meteo|aladhan'
```

`Trust anchor for certification path not found` indicates certificate validation failed. The following old-device workaround fixed the reference Android 6 frame in November 2026. It is conditional, not a required step on every new device. Server certificate chains can change.

### Android 6: install the official ISRG Root X1 if the chain requires it

Download the certificate from Let's Encrypt, not a mirror:

```bash
curl -fL https://letsencrypt.org/certs/isrgrootx1.pem -o ~/Downloads/ISRG-Root-X1.crt
openssl x509 -in ~/Downloads/ISRG-Root-X1.crt -noout -subject -issuer -dates
adb push ~/Downloads/ISRG-Root-X1.crt /sdcard/Download/ISRG-Root-X1.crt
```

Open Android security settings and use its certificate installation option if available:

```bash
adb shell am start -a android.settings.SECURITY_SETTINGS
```

Verify that the certificate being installed is ISRG Root X1 from the official source. Installing it changes trust on the device. Newer Android apps may not trust user-installed CAs; this workaround is not universal.

### If the reference installer gives EACCES on a file URL

The original frame's certificate installer could not read a direct `file:///sdcard/...` URI. Granting READ_EXTERNAL_STORAGE failed because its package did not request that permission. Do not keep retrying that grant.

Instead, register the file and query its media ID:

```bash
adb shell content insert --uri content://media/external/file --bind _data:s:/storage/emulated/0/Download/ISRG-Root-X1.crt --bind mime_type:s:application/x-x509-ca-cert
adb shell "content query --uri content://media/external/file --projection _id:_data --where \"_data='/storage/emulated/0/Download/ISRG-Root-X1.crt'\""
```

Use the returned `_id` in the next command. **Replace MEDIA_ID; do not copy the old device's numeric ID.**

```bash
adb shell am force-stop com.android.certinstaller
adb shell am start -a android.intent.action.VIEW -d content://media/external/file/MEDIA_ID -t application/x-x509-ca-cert --grant-read-uri-permission -n com.android.certinstaller/.CertInstallerMain
```

Complete the installation on the touchscreen. A message saying the activity was brought to the front is not proof that the certificate installed. Restart Fully and refresh weather; inspect logs again if it still fails. Keep Ignore SSL Errors disabled.

References: [Let's Encrypt certificate hierarchy](https://letsencrypt.org/certificates/), [Fully SSL troubleshooting](https://www.fully-kiosk.com/docs/en/faq/ssl), [Android URI read grants](https://developer.android.com/reference/android/content/Intent#FLAG_GRANT_READ_URI_PERMISSION).

## Prayer times missing or incorrect

- Tap refresh while online; weather and prayer times use different API hosts and one can fail independently.
- Check the device date/time and Queens location assumption.
- Check calculation method, Asr convention and offsets against your local mosque.
- This app shows prayer start times, not iqamah times.
- The cache covers downloaded months only. Reconnect before cached dates run out.
- Changing weather city does not change the fixed prayer location.

## Adhan does not play

```bash
adb shell ls -l /sdcard/Download/adhan.mp3
```

Check the audio path, Android media volume, app volume, Automatic adhan master switch and individual prayer checkboxes. Enable Fully's Autoplay Audio. Test immediate and 10-second delayed playback, then repeat after reboot. Use a local recording to avoid streaming/network failures.

The frame must stay awake with the dashboard running. A page reload does not replay an already attempted prayer; late/missed prayers are deliberately skipped. The app has no native Android background alarm service.

## Clock layout looks different on another screen

Use Style and Size & position, then check Fully zoom/scaling. The original working layout was confirmed on a 1280×800 landscape screen. Other resolutions and WebView versions need visual checks; automated tests do not measure screen geometry.
