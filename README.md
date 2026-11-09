# Family Frame Dashboard

A local touchscreen family dashboard with a combined clock, weather and Queens prayer screen. Version 7.5 adds a USB radio relay for older frames whose media player cannot connect to the HTTPS feeds.

## Start here

1. Follow [SETUP.md](SETUP.md) for a new Android device.
2. Use [Troubleshooting](docs/TROUBLESHOOTING.md) if USB, weather or audio fails.
3. Follow [Backups and updates](docs/BACKUPS.md) before changing an existing device.
4. Use [GitHub setup](docs/GITHUB.md) to create your own copy of the repository.

## Download and install

```bash
git clone https://github.com/mohamedebada21/family-frame-dashboard.git
cd family-frame-dashboard
```

Then follow [SETUP.md](SETUP.md). Do not run the installer until ADB and Fully are ready. Personal data stays on each device; this repository contains only the application and documentation.

## Features

- Family calendar, chores, shopping, meals, members and reward points.
- A family photo and four clock styles: classic, flip, analog and night.
- Clock size/position controls and a combined clock/weather/prayer page.
- Weather from Open-Meteo with selectable city and temperature units.
- Five daily prayer start times for Queens, next-prayer countdown and optional adhan playback.
- Cairo Quran Radio on **Clock & Weather** and **Prayer clock**. Tap **Play radio** to listen over Wi-Fi. The radio stops for the adhan. Tap Play again when the adhan ends.
- If a feed is silent or stalls, select the other feed and tap **Play radio**. The broadcaster's official feed uses HLS, and support depends on the frame's browser.
- If Android logs show an HTTPS handshake failure, try the [USB relay](docs/TROUBLESHOOTING.md#usb-relay-for-older-frames).
- The radio volume and mute settings are saved on the frame. Use **Test sound** to play a short tone without Wi-Fi. For the adhan volume, use **Prayer settings**.
- Local browser storage; dashboard JSON export/import.

## What has been tested

The owner confirmed the combined page works on a 102KZ photo frame: Android 6.0.1, 32-bit ARM, 1280×800, Fully Kiosk Browser 1.61.4. Other devices need their own installation and audio test. A touchscreen photo frame is not automatically an installable Android tablet; some manufacturers lock application installation or USB debugging.

## Limits to understand

- Calendar entries and tasks are local. Google Calendar sync and phone sync are **not implemented**.
- Prayer location is fixed to **Queens, NY**, using America/New_York time. Changing the weather city does **not** change prayer location. Use this build for a second device in Queens; another location requires a code change.
- Prayer calculation method and Asr convention are selectable. Calculated start times are not mosque iqamah times.
- Adhan is a foreground browser feature. The device must remain awake with this app running. Missed prayers are not replayed later; an attempted playback is recorded before audio starts to avoid duplicates, even if that attempt fails.
- Weather needs internet. Downloaded prayer months work offline only while those cached dates cover the current day.
- No adhan recording, Fully APK, certificates, account credentials or personal backup is bundled.
- Dashboard export does not include prayer settings/cache or the separate MP3. See [Backups](docs/BACKUPS.md).

## Repository layout

```text
family-calendar.html          Working app; no build step
SETUP.md                      New-device procedure
docs/                         Troubleshooting, backups and GitHub setup
scripts/install.sh            Checked ADB file deployment (Mac/Linux)
tests/check-dashboard.cjs     Node-based regression checks
```

For local code checks, install Node.js and run:

```bash
node tests/check-dashboard.cjs
node tests/security.cjs
```

These checks use a mocked browser and weather response. They do not establish real-device layout, network connectivity or background/audio reliability.

## Sources and dependencies

- [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) and [ADB guide](https://developer.android.com/tools/adb)
- [Fully Kiosk official downloads](https://www.fully-kiosk.com/en/#download-box) and [web content settings](https://www.fully-kiosk.com/docs/en/configuration/web-content)
- [Open-Meteo documentation](https://open-meteo.com/en/docs)
- [AlAdhan prayer API](https://aladhan.com/prayer-times-api)
- [Let's Encrypt certificates](https://letsencrypt.org/certificates/)

No open-source license has been selected for this package. Choose a license deliberately before offering it for public reuse. Third-party apps, services and recordings retain their own terms.

## Security review

Read [SECURITY.md](SECURITY.md) before installation or public distribution. Local input-validation tests passed; repository protections still require configuration on GitHub. No security audit or secure-device guarantee is implied.
