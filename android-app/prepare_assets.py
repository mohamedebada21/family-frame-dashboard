#!/usr/bin/env python3
"""Bundle the shared dashboard with the standalone Android app's integration."""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
source = (root / "family-calendar.html").read_text()


def replace_once(old, new):
    global source
    if source.count(old) != 1:
        raise SystemExit("Dashboard integration point changed: " + old[:75])
    source = source.replace(old, new, 1)


replace_once("radioFeed='mp3'", "radioFeed='local'")
feeds = re.search(r"var radioFeeds=\{.*?\n\};", source, re.S)
if feeds is None:
    raise SystemExit("Radio feed configuration was not found")
source = source[:feeds.start()] + """var radioFeeds={
  local:{label:'Built-in Cairo relay',url:'http://127.0.0.1:8768/cairo.mp3'}
};""" + source[feeds.end():]
replace_once("audio:'file:///sdcard/Download/adhan.mp3'", "audio:'http://127.0.0.1:8768/adhan.mp3'")
replace_once("function validAdhanSource(v){return ",
             "function validAdhanSource(v){return v==='http://127.0.0.1:8768/adhan.mp3'||")
start = source.index("window.backup=function()")
end = source.index("window.restore=function", start)
source = source[:start] + "window.backup=function(){FrameNative.exportBackup(JSON.stringify(s,null,2));};" + source[end:]
source = source.replace("http://127.0.0.1:8767/cairo.mp3", "http://127.0.0.1:8768/cairo.mp3")
source = source.replace("Start Frame Radio Relay, then tap Play radio.", "Tap Play radio to use the built-in relay.")
source = source.replace("Check Wi-Fi and that Frame Radio Relay is running.", "Check Wi-Fi, then tap Play radio. Restart the dashboard if needed.")
source = source.replace("Fully storage permission", "the selected adhan recording")
source = source.replace("Enable Autoplay Audio in Fully, then test again.", "Choose your MP3 using the Frame button, then test again.")
source = source.replace("Fully Autoplay Audio setting", "selected recording")
source = source.replace("file:///sdcard/Download/adhan.mp3", "http://127.0.0.1:8768/adhan.mp3")
source = source.replace("The downloadable HTML can run in Fully Kiosk without a server; local storage must be enabled.",
                        "Your dashboard is bundled in Family Frame. Use the Frame button for Wi-Fi, audio and Home settings.")
source = source.replace("All features here work locally; opening this page on another device does not share your data.",
                        "Calendar data stays on this frame. Weather and radio use Wi-Fi.")
source = source.replace("Events and lists currently stay in this browser.", "Events and lists currently stay on this frame.")
source = source.replace("clearing browser data", "clearing app data")
source = source.replace("Family Hub · Version 7.7", "Family Frame · Android 1.1")
replace_once("</style>", """
.frame-native-back{left:12px;right:auto!important}.frame-native-button{position:fixed;right:12px;bottom:10px;z-index:2000;
background:#f3eedf;color:#123d35;border:1px solid #a4b9b0;padding:8px 16px;
min-height:44px;border-radius:22px;box-shadow:0 2px 10px #0002}
</style>""")
# The native bridge is present only on this bundled, trusted document.
replace_once("<script>", """<button class="frame-native-button frame-native-back" onclick="go('Dashboard')" aria-label="Back to dashboard">← Dashboard</button>
<button class="frame-native-button" onclick="FrameNative.showSettings()" aria-label="Frame settings">⚙ Frame</button>
<script>""")
replace_once("window.prayerSettings=function(){var body=", """window.prayerSettings=function(){var body='<button type="button" onclick="FrameNative.chooseAdhan()">Choose adhan MP3</button>'+""")
# Keep all dashboard validation and the existing import confirmation.
replace_once("function tick(){", """window.importFrameBackup=function(text){
  window.restore({files:[new Blob([text],{type:'application/json'})]});
};
window.frameAdhanReady=function(){
  ps.audio='http://127.0.0.1:8768/adhan.mp3';
  prayerStore();
  audioStatus('Recording saved. Tap Test adhan now.');
};
function tick(){""")
csp = """<meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; img-src data:; connect-src https://api.open-meteo.com https://geocoding-api.open-meteo.com https://api.aladhan.com; media-src data: http://127.0.0.1:8768; frame-src 'none'; object-src 'none'">"""
replace_once("<style>", csp + "<style>")
target = Path(sys.argv[1])
target.mkdir(parents=True, exist_ok=True)
(target / "family-calendar.html").write_text(source)
