#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")" && pwd)
sdk=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
: "${sdk:?Set ANDROID_SDK_ROOT to your Android SDK directory}"
version=${ANDROID_BUILD_TOOLS_VERSION:-35.0.0}
tools="$sdk/build-tools/$version"
platform="$sdk/platforms/android-35/android.jar"
output=${1:-"$root/family-frame-unsigned.apk"}
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir -p "$work/classes" "$work/dex" "$work/assets" "$work/res"
python3 "$root/prepare_assets.py" "$work/assets"
"$tools/aapt2" compile --dir "$root/res" -o "$work/res/compiled.zip"
"$tools/aapt2" link -o "$work/unaligned.apk" -I "$platform" \
  --manifest "$root/AndroidManifest.xml" -A "$work/assets" "$work/res/compiled.zip"
sources=("$root"/src/com/familyframe/dashboard/*.java \
  "$root/../android-relay/src/com/familyframe/radio/RelayServer.java")
if [ -n "${ECJ_JAR:-}" ]; then
  java -jar "$ECJ_JAR" -8 -nowarn -classpath "$platform" -d "$work/classes" "${sources[@]}"
else
  "${JAVAC:-javac}" -source 8 -target 8 -classpath "$platform" \
    -d "$work/classes" "${sources[@]}"
fi
(cd "$work/classes" && zip -qr "$work/classes.jar" .)
"$tools/d8" --min-api 23 --lib "$platform" --output "$work/dex" "$work/classes.jar"
(cd "$work/dex" && zip -q "$work/unaligned.apk" classes.dex)
"$tools/zipalign" -f 4 "$work/unaligned.apk" "$output"
printf 'Built %s. Sign it with apksigner before installing.\n' "$output"
