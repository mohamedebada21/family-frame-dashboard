#!/usr/bin/env bash
set -euo pipefail

root=$(cd "$(dirname "$0")" && pwd)
sdk=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
: "${sdk:?Set ANDROID_SDK_ROOT to your Android SDK directory}"
tools="$sdk/build-tools/35.0.0"
platform="$sdk/platforms/android-35/android.jar"
output=${1:-"$root/frame-radio-relay-unsigned.apk"}
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir -p "$work/classes" "$work/dex"

"${JAVAC:-javac}" -source 8 -target 8 -classpath "$platform" \
  -d "$work/classes" "$root"/src/com/familyframe/radio/*.java
"$tools/d8" --min-api 23 --lib "$platform" --output "$work/dex" \
  "$work"/classes/com/familyframe/radio/*.class
"$tools/aapt2" link -o "$work/unaligned.apk" -I "$platform" \
  --manifest "$root/AndroidManifest.xml"
(cd "$work/dex" && zip -q "$work/unaligned.apk" classes.dex)
"$tools/zipalign" -f 4 "$work/unaligned.apk" "$output"
printf 'Built %s. Sign it with apksigner before installing.\n' "$output"
