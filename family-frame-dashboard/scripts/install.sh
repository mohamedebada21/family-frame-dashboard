#!/usr/bin/env bash
# Mac/Linux; Bash 3.2 or newer. No root or app-data clearing.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
usage() { printf 'Usage: bash scripts/install.sh [-s DEVICE_SERIAL]\n'; }
serial=''
if [ "$#" -gt 0 ]; then
  if [ "$#" -eq 2 ] && [ "$1" = '-s' ]; then serial="$2"; else usage; exit 2; fi
fi
command -v adb >/dev/null 2>&1 || { printf 'ADB missing. Follow SETUP.md first.\n' >&2; exit 1; }
[ -f "$ROOT/family-calendar.html" ] || { printf 'Dashboard HTML missing.\n' >&2; exit 1; }
if [ -z "$serial" ]; then
  devices="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
  count="$(printf '%s\n' "$devices" | awk 'NF {n++} END {print n+0}')"
  [ "$count" = 1 ] || { printf 'Expected one authorized device; found %s. Check adb devices -l or use -s SERIAL.\n' "$count" >&2; exit 1; }
  serial="$devices"
fi
ADB=(adb -s "$serial")
state="$("${ADB[@]}" get-state | tr -d '\r')"
[ "$state" = device ] || { printf 'Selected device is not ready.\n' >&2; exit 1; }
package="$("${ADB[@]}" shell pm path de.ozerov.fully | tr -d '\r')"
case "$package" in package:*) ;; *) printf 'Install Fully Kiosk Browser before deploying.\n' >&2; exit 1;; esac
target='/sdcard/Download/family-calendar.html'
"${ADB[@]}" shell mkdir -p /sdcard/Download
existing="$("${ADB[@]}" shell "if [ -f '$target' ]; then echo present; else echo absent; fi" | tr -d '\r')"
if [ "$existing" = present ]; then
  mkdir -p "$ROOT/backups"
  backup_dir="$(mktemp -d "$ROOT/backups/install-$(date +%Y%m%d-%H%M%S)-XXXXXX")"
  "${ADB[@]}" pull "$target" "$backup_dir/family-calendar.html"
  printf 'Previous HTML saved to %s (browser data is NOT in this file).\n' "$backup_dir"
elif [ "$existing" != absent ]; then
  printf 'Could not establish whether a previous HTML exists; stopped.\n' >&2
  exit 1
fi
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
"${ADB[@]}" push "$ROOT/family-calendar.html" "$target"
"${ADB[@]}" pull "$target" "$tmp/family-calendar.html"
cmp "$ROOT/family-calendar.html" "$tmp/family-calendar.html" || { printf 'Transfer verification failed; Fully was not restarted.\n' >&2; exit 1; }
"${ADB[@]}" shell am force-stop de.ozerov.fully
"${ADB[@]}" shell monkey -p de.ozerov.fully -c android.intent.category.LAUNCHER 1
printf '\nVerified transfer and requested Fully launch.\nSet Start URL to file:///sdcard/Download/family-calendar.html\nComplete the visual, weather and audio checks in SETUP.md on the device.\n'
