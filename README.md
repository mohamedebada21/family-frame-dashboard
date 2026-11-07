Family Frame Dashboard
A local touchscreen family dashboard with a combined clock, weather and Queens prayer screen. This is the Version 7.1 HTML with backup-input validation, packaged with repeatable installation instructions.

Start here
Follow SETUP.md for a new Android device.
Use Troubleshooting if USB, weather or audio fails.
Follow Backups and updates before changing an existing device.
Use GitHub setup to create your own private repository.
Features
Family calendar, chores, shopping, meals, members and reward points.
A family photo and four clock styles: classic, flip, analog and night.
Clock size/position controls and a combined clock/weather/prayer page.
Weather from Open-Meteo with selectable city and temperature units.
Five daily prayer start times for Queens, next-prayer countdown and optional adhan playback.
Local browser storage; dashboard JSON export/import.
What has been tested
The owner confirmed the combined page works on a 102KZ photo frame: Android 6.0.1, 32-bit ARM, 1280×800, Fully Kiosk Browser 1.61.4. Other devices need their own installation and audio test. A touchscreen photo frame is not automatically an installable Android tablet; some manufacturers lock application installation or USB debugging.

Limits to understand
Calendar entries and tasks are local. Google Calendar sync and phone sync are not implemented.
Prayer location is fixed to Queens, NY, using America/New_York time. Changing the weather city does not change prayer location. Use this build for a second device in Queens; another location requires a code change.
Prayer calculation method and Asr convention are selectable. Calculated start times are not mosque iqamah times.
Adhan is a foreground browser feature. The device must remain awake with this app running. Missed prayers are not replayed later; an attempted playback is recorded before audio starts to avoid duplicates, even if that attempt fails.
Weather needs internet. Downloaded prayer months work offline only while those cached dates cover the current day.
No adhan recording, Fully APK, certificates, account credentials or personal backup is bundled.
Dashboard export does not include prayer settings/cache or the separate MP3. See Backups.
Repository layout
family-calendar.html          Working app; no build step
SETUP.md                      New-device procedure
docs/                         Troubleshooting, backups and GitHub setup
scripts/install.sh            Checked ADB file deployment (Mac/Linux)
tests/check-dashboard.cjs     Node-based regression checks
For local code checks, install Node.js and run:

node tests/check-dashboard.cjs
node tests/security.cjs
These checks use a mocked browser and weather response. They do not establish real-device layout, network connectivity or background/audio reliability.

Sources and dependencies
Android Platform Tools and ADB guide
Fully Kiosk official downloads and web content settings
Open-Meteo documentation
AlAdhan prayer API
Let's Encrypt certificates
No open-source license has been selected for this package. Choose a license deliberately before offering it for public reuse. Third-party apps, services and recordings retain their own terms.

Security review
Read SECURITY.md before installation or public distribution. Local input-validation tests passed; repository protections still require configuration on GitHub. No security audit or secure-device guarantee is implied.
