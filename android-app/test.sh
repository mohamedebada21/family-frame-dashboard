#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")" && pwd)
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
python3 "$root/prepare_assets.py" "$work/assets"
node "$root/tests/dashboard.cjs" "$work/assets/family-calendar.html"
node "$root/../tests/meal-weather.cjs" "$work/assets/family-calendar.html"
sources=("$root/src/com/familyframe/dashboard/NetworkPolicy.java"
  "$root/../android-relay/src/com/familyframe/radio/RelayServer.java"
  "$root"/tests/*.java)
if [ -n "${ECJ_JAR:-}" ]; then
  java -jar "$ECJ_JAR" -17 -nowarn -d "$work/classes" "${sources[@]}"
else
  javac -d "$work/classes" "${sources[@]}"
fi
java -cp "$work/classes" NetworkPolicyTest
java -cp "$work/classes" RelayServerTest
