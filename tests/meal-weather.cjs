const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const assert = require('node:assert/strict');
const html = fs.readFileSync(process.argv[2] || path.join(__dirname, '..', 'family-calendar.html'), 'utf8');
const script = html.match(/<script>([\s\S]*)<\/script>/)[1];
const previous = {events:[],tasks:[],shopping:[],meals:{'2026-12-30-Breakfast':'Oatmeal'},photo:''};
const store = {ourFamilyBoardV1: JSON.stringify(previous)};
const elements = {};
const element = key => elements[key] ||= {innerHTML:'',textContent:'',value:'',focus(){},setAttribute(){}};
const pending = [];
const context = {
  console, Date, Math, JSON, Number, String, Array, Blob, URL,
  setTimeout:()=>0, clearTimeout(){}, setInterval:()=>0,
  alert(){}, confirm:()=>true,
  localStorage:{getItem:key=>store[key]||null,setItem:(key,value)=>{store[key]=value;}},
  document:{body:{className:''},getElementById:element,querySelector:()=>element('query')},
  XMLHttpRequest:function(){this.open=(method,url)=>{this.url=url;};this.send=()=>pending.push(this);}
};
context.window = context;
vm.createContext(context);
vm.runInContext(script, context);
context.go('Dashboard');
assert(element('view').innerHTML.includes('Weekly meal plan'));
context.weekMealForm('2026-12-31', 'Dinner');
element('meal').value = 'Rice & chicken';
element('editform').onsubmit({preventDefault(){}});
assert.equal(JSON.parse(store.ourFamilyBoardV1).meals['2026-12-31-Dinner'], 'Rice & chicken');
assert.equal(JSON.parse(store.ourFamilyBoardV1).meals['2026-12-30-Breakfast'], 'Oatmeal');
assert(element('view').innerHTML.includes('Rice &amp; chicken'));
context.moveWeek(1);
assert(!element('view').innerHTML.includes('Rice &amp; chicken'));
context.moveWeek(-1);
assert(element('view').innerHTML.includes('Rice &amp; chicken'));
assert.equal((element('view').innerHTML.match(/class="daycol /g)||[]).length, 7);
// Reopen the persisted app data in a new JavaScript context.
const reloaded = {...context};
reloaded.window = reloaded;
vm.createContext(reloaded);
vm.runInContext(script, reloaded);
reloaded.weekMealForm('2026-12-31', 'Dinner');
assert(element('modal').innerHTML.includes('Rice &amp; chicken'));
context.go('Clock & Weather');
let request = pending.shift();
function respond(temperature) {
  request.status=200;
  request.responseText=JSON.stringify({current:{temperature_2m:temperature,apparent_temperature:temperature,weather_code:0},daily:{time:['2026-11-19'],weather_code:[0],temperature_2m_max:[temperature],temperature_2m_min:[temperature]}});
  request.onload();
}
respond(32);
let panel=element('weatherpanel').innerHTML;
assert(panel.includes('32°F'));
assert(panel.includes('0°C'));
context.clockSettings();
element('wunit').value='celsius';
element('whours').value='12';
element('wstartup').value='no';
element('wphoto').value='no';
element('editform').onsubmit({preventDefault(){}});
request=pending.shift();
assert(request.url.includes('temperature_unit=celsius'));
respond(-40);
panel=element('weatherpanel').innerHTML;
assert(panel.indexOf('-40°C') < panel.indexOf('-40°F'));
assert(panel.includes('Feels like -40°C / -40°F'));
context.loadWeather(true);
request=pending.shift();
respond(null);
assert(!element('weatherpanel').innerHTML.includes('NaN'));
console.log('Passed saved weekly meals, year-boundary navigation, escaped text, reload persistence and both temperature units');
