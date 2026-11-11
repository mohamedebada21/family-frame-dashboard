# Install Family Frame Dashboard on an Android tablet

Use this guide to install version 7.6 on a compatible Android tablet. The original setup used Android 6.0.1 and Fully Kiosk Browser 1.61.4. Check that your tablet allows app installation and USB debugging and that Fully offers a compatible build.

The examples use Mac Terminal or Bash on Linux. The `adb` commands also work on Windows with Platform Tools on PATH and the tablet paths substituted for `$TABLET_DIR`.

## 1. Download the dashboard and tools

1. Save [`family-calendar.html`](family-calendar.html) from this branch into your computer's Downloads folder. On GitHub, use **Download raw file**. Keep the filename and `.html` extension. If you cloned this branch, you can use the file from your clone instead.
2. Download and extract [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools).
3. On the tablet, join Wi-Fi and enable automatic date and time. Install [Fully Kiosk Browser](https://www.fully-kiosk.com/docs/en/getting-started/downloads) from Google Play or Fully's official site. Open it once so Android creates its storage folder.

## 2. Connect the tablet and authorize ADB

On the tablet, open **Settings → About tablet** and tap **Build number** seven times. Some tablets put it under **Software information**. Enable **USB debugging** under **Developer options**, connect with a USB data cable, unlock the tablet and accept its authorization prompt. Custom photo frames may have different settings. [Android USB setup](https://developer.android.com/studio/run/device)

If you extracted Platform Tools into Downloads, run:

```bash
export PATH="$HOME/Downloads/platform-tools:$PATH"
adb devices -l
```

Continue when your tablet shows `device`. If it shows `unauthorized`, approve the prompt on the tablet. For an empty list, check the cable, USB port and debugging setting. Disconnect other devices or add `-s SERIAL` after `adb` in each command.

## 3. Copy the dashboard

Use Fully's app-specific storage on Android 11 and newer. On Android 6–10, set `TABLET_DIR=/sdcard/Download` in place of the assignment below. For an existing install, first [export a backup](docs/BACKUPS.md) and use the directory from the existing Start URL.

```bash
cd ~/Downloads
TABLET_DIR=/sdcard/Android/data/de.ozerov.fully/files
adb shell mkdir -p "$TABLET_DIR"
adb push family-calendar.html "$TABLET_DIR/family-calendar.html"
printf 'file://%s/family-calendar.html\n' "$TABLET_DIR"
```

Keep the same terminal open if you plan to copy an adhan recording. The optional [installer script](scripts/install.sh) uses the shared `/sdcard/Download` folder. Use the commands above for an app-specific installation.

## 4. Set Fully's Start URL

1. Swipe right from Fully's left edge to open its menu.
2. Open **Settings → Web Content Settings → Start URL** and enter the URL printed in step 3, including the three slashes. For the shared folder, use `file:///sdcard/Download/family-calendar.html`.
3. Exit Settings and select the menu action to load the Start URL. Check that the dashboard opens and its buttons respond to touch.

If Fully cannot read the file, check the path and its local-file access. Use the file picker next to the Start URL field if offered. [Fully local files](https://www.fully-kiosk.com/docs/en/faq/localfiles) · [Android 11+ storage](https://www.fully-kiosk.com/docs/en/faq/scoped)

## 5. Set up the clock, weather and prayer times

1. Open **Clock & Weather**. Under **Clock settings**, select the weather city, temperature units and time format. Choose a clock **Style** and adjust **Size & position**. Tap **Show weather** if needed, then refresh.
2. Open **Prayer settings**. Prayer times in this build are for **Queens, New York**. Changing the weather city affects the weather only. Set the tablet's time zone to New York for this setup, then refresh prayer times online.
3. Choose your calculation method and Asr convention. The defaults are ISNA and standard Asr. Compare today's calculated start times with your mosque's schedule before adjusting offsets.

## 6. Add automatic adhan (optional)

Save a recording you have permission to use as `adhan.mp3` in your computer's Downloads. In the same terminal as step 3, run:

```bash
adb push ~/Downloads/adhan.mp3 "$TABLET_DIR/adhan.mp3"
printf 'file://%s/adhan.mp3\n' "$TABLET_DIR"
```

1. Paste the printed URL into the audio source in the dashboard's **Prayer settings**.
2. In Fully, open **Web Content Settings** and enable **Autoplay Audio**. [Fully web content settings](https://www.fully-kiosk.com/docs/en/configuration/web-content)
3. In the dashboard's **Adhan controls**, tap **Test adhan now**, then **Test automatic audio in 10 seconds** and wait without touching the screen. Check Android media volume and the dashboard's adhan volume.
4. After both tests pass, enable **Automatic adhan** and select the prayers. Keep Fully in the foreground and the tablet awake at prayer times. Repeat the delayed test after reboot.

## 7. Play Cairo Quran Radio (optional)

1. In **Clock & Weather**, choose **Feed → MP3 relay** or **Official feed**, then tap **Play radio**. Use **Test sound** to check the speaker and adjust the dashboard's radio volume and Android media volume.
2. If both feeds fail, follow the [Android relay instructions](android-relay/README.md) to obtain a signed `frame-radio-relay.apk` from a trusted source or build and sign it yourself. The relay is intended for Android 6 or newer. With the APK in Downloads, run:

   ```bash
   adb install -r ~/Downloads/frame-radio-relay.apk
   adb shell am start -n com.familyframe.radio/.MainActivity
   ```

3. On the tablet, tap **Start relay → Open dashboard**. Select **Feed → On-frame relay**, disconnect USB and tap **Play radio** to test over the tablet's Wi-Fi. Starting the relay enables its boot startup. To listen after reboot, tap **Play radio** again.

## 8. Enable phone control over home Wi-Fi (optional)

1. In Fully, open **Settings → Remote Administration**. Turn on **Enable Remote Administration** and **Remote Admin from Local Network**. Set a strong password. [Fully Remote Administration settings](https://www.fully-kiosk.com/docs/en/configuration/remote-administration)
2. Find the tablet's IP address in Android's Wi-Fi details or your router's device list. Connect your phone to the same main Wi-Fi and visit the tablet's address on port 2323, for example `http://192.168.1.50:2323`. Keep this on your home network with router port forwarding disabled.
3. Sign in and check that you can reload the dashboard. Remote Admin controls Fully. Dashboard events and lists stay in the tablet's browser. Fully may display a PLUS trial watermark while Remote Admin is enabled. [Remote Admin controls](https://www.fully-kiosk.com/docs/en/features/remote-admin) · [Fully PLUS licensing](https://www.fully-kiosk.com/docs/en/legal/fully-plus-license)

## 9. Reboot and verify

For automatic startup, enable **Device Management → Launch on Boot** if your Fully build offers it. Enable **Keep Screen On** for scheduled adhan. Test that you can open Fully's menu and exit before enabling a kiosk lock. [Fully device management settings](https://www.fully-kiosk.com/docs/en/configuration/device-management)

- [ ] Reboot and disconnect USB. Confirm that Fully opens the dashboard and touch controls respond.
- [ ] Check date, time, weather refresh, the five prayer times and the next-prayer countdown.
- [ ] Add a test calendar event and confirm it survives restarting Fully.
- [ ] If configured, test delayed adhan, radio playback and phone access.
- [ ] If migrating, import your [dashboard backup](docs/BACKUPS.md) and set prayer preferences and the MP3 separately.
- [ ] Turn off USB debugging when setup is complete.

## Troubleshooting

- **Stuck in Settings or Remote Admin on the tablet.** Use Android Back or open the Fully menu and load the Start URL. If navigation is hidden and USB debugging is available, connect to the computer and run `adb shell input keyevent 4`.
- **Weather and radio both fail.** Check internet access on the tablet. If needed, forget and rejoin Wi-Fi with the password. Verify the tablet's date and time if HTTPS fails. Keep Fully's **Ignore SSL Errors** setting off.
- **Phone access times out.** Check the current tablet IP and both Remote Admin switches. Put both devices on the main Wi-Fi so they can reach each other.
- For other issues, see [Troubleshooting](docs/TROUBLESHOOTING.md).
