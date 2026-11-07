# Backups, migration and updates

## Three different things to save

| Item | How to save it | Included in app JSON export? |
| --- | --- | --- |
| Events, tasks, shopping, meals, members, rewards, photo and clock/weather preferences | Dashboard Settings → export backup | Yes |
| Prayer method, Asr convention, volume, selected prayers, offsets and audio source | Record values or photograph the Prayer settings screen privately | No |
| Adhan recording | Keep the original `adhan.mp3` on your computer | No |
| Fully Start URL, autoplay, boot and kiosk settings | Record settings separately; use Fully's own export if your version offers it | No |

The HTML file is the app, not its saved content. Copying the HTML to another device does not copy your calendar or family photo. The app uses `ourFamilyBoardV1` for dashboard data and `familyPrayerQueensV1` for prayer state in browser localStorage.

## Export before updating

1. Open dashboard **Settings** and export your backup.
2. Locate `our-family-backup.json` in the browser's download folder. The folder can vary by browser settings.
3. Copy that file to your computer. If it is in the frame's Download folder:

```bash
adb pull /sdcard/Download/our-family-backup.json ~/Downloads/our-family-backup.json
```

Use the actual filename if the browser added a suffix. Check that the file is nonempty. Keep private backups outside your repository. An export may contain personal schedules, names and a photo.

## Migrate to a second device

1. Complete SETUP.md on the new device.
2. Copy the exported JSON:

```bash
adb push ~/Downloads/our-family-backup.json /sdcard/Download/our-family-backup.json
```

3. On the new dashboard, use Settings → restore and select that JSON. Restoration replaces that device's dashboard data; export any existing data there first.
4. Re-enter prayer settings, copy the MP3 and repeat the delayed audio test.
5. Confirm clock, weather city, photo and sample events. Each device remains independent; changes do not synchronize.

## Update the app on the same device

Replace the project HTML with the new version, then run:

```bash
bash scripts/install.sh
```

Keep the exact same Start URL and Fully installation. Do not clear browser/app data. The installer saves a copy of the previous HTML under `backups/` before overwriting it, but it does not back up localStorage.

## Roll back an HTML update

Choose the intended timestamped HTML under `backups/` and push it to the same target. Example (replace TIMESTAMP with the actual folder name):

```bash
adb push backups/TIMESTAMP/family-calendar.html /sdcard/Download/family-calendar.html
adb shell am force-stop de.ozerov.fully
adb shell monkey -p de.ozerov.fully -c android.intent.category.LAUNCHER 1
```

Use a compatible JSON backup if a later app version changed its data format. Reverting HTML alone does not revert data.
