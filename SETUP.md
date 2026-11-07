# Set up another Android frame

This procedure starts with a fresh device. For an existing installation, export a backup first and follow [Backups and updates](docs/BACKUPS.md).

## 1. Prepare the equipment

- Android touchscreen device that permits app installation and USB debugging.
- Its normal power adapter, reliable Wi-Fi and a USB **data** cable.
- Mac or Linux computer, this extracted project folder and ADB.
- Fully Kiosk Browser APK compatible with the device, from the [official download page](https://www.fully-kiosk.com/en/#download-box).
- Optional: your chosen, permitted-to-use adhan recording named `adhan.mp3`.

The reference frame runs Android 6.0.1 / armeabi-v7a. Do not assume a current APK supports every old device; select a compatible build from the official source. Do not flash firmware just to follow this procedure.

## 2. Install ADB on the computer

Download [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools), extract it, and add that directory to your PATH. For example, if extracted into Downloads, run this in the current Mac terminal:

```bash
export PATH="$HOME/Downloads/platform-tools:$PATH"
adb version
```

This PATH change lasts only for this terminal session. Alternatively use your existing ADB installation.

## 3. Connect and authorize the frame

1. Power the frame normally. Connect it to the computer with the data cable.
2. Enable USB debugging in Android developer settings if the device exposes them. Tapping Build number is not supported by every custom frame firmware; it did not expose settings on the reference frame. If debugging is unavailable, consult that model's manufacturer instructions.
3. Accept the USB debugging authorization prompt on the frame, if shown.
4. Run:

```bash
adb devices -l
```

Proceed only when the target appears with status `device`. `unauthorized` requires approval on the frame; an empty list usually requires checking the cable, hub, USB port or debugging availability. If multiple devices are connected, use `adb -s SERIAL ...` for each manual command below, replacing SERIAL with your chosen device's identifier.

Record the device details:

```bash
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.product.cpu.abi
adb shell wm size
```

## 4. Configure Android

- Connect Wi-Fi and confirm internet access.
- Set the correct date/time and timezone. For the Queens prayer build, use New York / Eastern time, with automatic time where available.
- Set media volume to an audible level.

Useful commands to open the corresponding settings pages:

```bash
adb shell am start -a android.settings.WIFI_SETTINGS
adb shell am start -a android.settings.DATE_SETTINGS
adb shell date
```

## 5. Install Fully Kiosk Browser

Replace the example APK path below with the file you downloaded:

```bash
adb install -r ~/Downloads/fully-kiosk.apk
adb shell monkey -p de.ozerov.fully -c android.intent.category.LAUNCHER 1
```

Grant the permissions Fully requests for local files. If it is already installed and working, keep that installation. The `-r` option updates an existing package; do not uninstall it as an update step.

## 6. Copy the dashboard

In Terminal, change into this extracted project folder. Example:

```bash
cd ~/Downloads/family-frame-dashboard
bash scripts/install.sh
```

For multiple attached devices:

```bash
bash scripts/install.sh -s YOUR_DEVICE_SERIAL
```

The script checks connectivity and Fully, backs up an existing HTML file locally, transfers the app, verifies the transferred bytes and restarts Fully. It does not configure Fully settings or copy browser data. See the manual equivalent if you prefer:

```bash
adb push family-calendar.html /sdcard/Download/family-calendar.html
adb shell am force-stop de.ozerov.fully
adb shell monkey -p de.ozerov.fully -c android.intent.category.LAUNCHER 1
```

## 7. Set Fully's Start URL

Open Fully Settings and set **Start URL** to exactly:

```text
file:///sdcard/Download/family-calendar.html
```

- Enable JavaScript and local-file access if the browser exposes these options.
- Enable **Web Content Settings → Autoplay Audio** for adhan.
- Keep the screen/device awake while the app is in use; disable sleep/screensaver behavior that suspends the page if automatic adhan is required.
- Configure startup on boot if offered by your Fully version and device. Some options may require a Fully license.
- Keep **Ignore SSL Errors off**. Use the certificate troubleshooting procedure only if you actually encounter trust errors.
- Before enabling any kiosk lock, learn how to reopen Fully Settings and record your exit PIN. Test leaving the app first.

The exact labels and available options vary by Fully version. [Official Fully settings documentation](https://www.fully-kiosk.com/docs/en/configuration/web-content).

## 8. Configure the combined display

Open **Clock & Weather**:

1. **Clock settings:** choose weather city, Fahrenheit/Celsius, 12/24-hour time, and whether to open the clock on startup.
2. **Style:** choose classic, flip, analog or night.
3. **Size & position:** choose your preferred clock layout.
4. If the weather is hidden, tap **Show weather**, then refresh.
5. **Prayer settings:** choose calculation method and Asr convention. Default is ISNA with standard Asr. Compare today's start times with your local mosque; apply minute adjustments only as appropriate.

Prayer times are for Queens regardless of the weather city. They update by date and cache monthly schedules; refresh while online during initial setup.

## 9. Add and test adhan

Copy your recording from the Mac:

```bash
adb push ~/Downloads/adhan.mp3 /sdcard/Download/adhan.mp3
```

In **Prayer settings**, set the audio source to:

```text
file:///sdcard/Download/adhan.mp3
```

1. Open **Prayer clock** (or **Adhan controls** from the combined display).
2. Test audio immediately, then test the delayed 10-second playback without touching the screen while waiting.
3. Check media volume and the app's adhan volume. Stop audio when finished.
4. Enable **Automatic adhan** and select the prayers you want to hear.
5. Reboot and repeat the delayed test. Confirm Fully returns to the dashboard and remains awake.

Automatic adhan is off initially. An HTML page cannot guarantee an alarm while Android is asleep, the browser is closed or another app has suspended it. Use a native alarm/prayer app if background reliability is required.

## 10. Acceptance checklist

- [ ] Correct date, time and timezone after reboot.
- [ ] Dashboard opens and buttons respond to touch.
- [ ] Clock, weather and prayer cards fit the screen.
- [ ] Weather refresh completes and shows the chosen city/units.
- [ ] Five prayer times and next-prayer countdown are correct for Queens.
- [ ] Adhan immediate and delayed tests work, including after reboot.
- [ ] A test calendar event survives restarting Fully.
- [ ] A dashboard backup can be exported and found on the device.
- [ ] You can reopen Fully Settings and exit the kiosk.
- [ ] You have recorded prayer settings and saved the MP3 separately.

Keep this completed checklist with the device's model and Fully version. You do not need to repeat the old frame's certificate repair on a new device unless the same trust error occurs.
