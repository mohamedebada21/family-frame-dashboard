const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const source = fs.readFileSync(process.argv[2], 'utf8');
const script = source.match(/<script>([\s\S]*)<\/script>/)[1];
const storage = {};
const elements = {};
const element = name => elements[name] ||= {
  innerHTML: '', textContent: '', value: '', focus() {},
  setAttribute(name, value) { this[name] = value; }
};
let exported;
const context = {
  console, Date, Math, JSON, Number, String, Array, Blob, URL,
  setTimeout: () => 0, clearTimeout() {}, setInterval: () => 0,
  confirm: () => true, alert: () => {},
  FrameNative: { exportBackup: text => { exported = JSON.parse(text); } },
  localStorage: { getItem: key => storage[key] || null, setItem: (key, value) => { storage[key] = value; } },
  document: { body: { className: '' }, getElementById: element, querySelector: () => element('query') }
};
context.window = context;
vm.createContext(context);
vm.runInContext(script, context);
for (const view of ['Dashboard', 'Calendar', 'Tasks', 'Shopping', 'Meals', 'Photos', 'Family', 'Rewards', 'Settings', 'Clock & Weather', 'Prayer clock']) {
  context.go(view);
  assert(element('view').innerHTML.length > 100, view);
}
context.backup();
assert(Array.isArray(exported.events));
assert(source.includes('FrameNative.showSettings()'));
assert(!source.includes("radioFeed='mp3'"));
const audio = {
  currentTime: 0, plays: 0,
  play() { this.plays++; }, pause() {}, load() {},
  removeAttribute() { this.src = ''; }
};
elements.radioplayer = audio;
context.toggleRadio();
assert.equal(audio.src, 'http://127.0.0.1:8768/cairo.mp3');
context.toggleRadio();
context.frameAdhanReady();
assert.equal(JSON.parse(storage.familyPrayerQueensV1).audio, 'http://127.0.0.1:8768/adhan.mp3');
console.log('Passed bundled views, native export, radio routing and imported adhan preferences');
