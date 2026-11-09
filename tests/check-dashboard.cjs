const fs=require('fs'),vm=require('vm'),assert=require('assert');
const html=fs.readFileSync(require('path').join(__dirname,'..','family-calendar.html'),'utf8'),script=html.match(/<script>([\s\S]*)<\/script>/)[1];
const els={};function el(k){return els[k]||(els[k]={innerHTML:'',textContent:'',value:'',focus(){},setAttribute(name,value){this[name]=value;}})}
let data=JSON.stringify({events:[{id:'old1',title:'Existing event',date:'2026-11-06',time:'09:00',person:'Family'}],tasks:[],shopping:[],meals:{},photo:''});
const preferences={};
const ctx={console,Date,Math,JSON,Number,String,Array,localStorage:{getItem:k=>k==='ourFamilyBoardV1'?data:preferences[k],setItem:(k,v)=>{if(k==='ourFamilyBoardV1')data=v;else preferences[k]=v;}},document:{body:{className:''},getElementById:el,querySelector:()=>el('query')},setTimeout:()=>0,setInterval:()=>0,confirm:()=>true,alert:()=>{},Blob,URL};ctx.window=ctx;vm.createContext(ctx);vm.runInContext(script,ctx);
for(const tab of ['Dashboard','Calendar','Tasks','Shopping','Meals','Photos','Family','Rewards','Settings']){ctx.go(tab);assert(el('view').innerHTML.length>100,tab)}
ctx.memberForm();el('mname').value='Test member';el('mcolor').value='#dfeadc';el('editform').onsubmit({preventDefault(){}});let s=JSON.parse(data);assert.equal(s.events[0].title,'Existing event');let mid=s.members[0].id;
ctx.choreForm();el('ctitle').value='Read';el('cmember').value=mid;el('cpoints').value='5';el('editform').onsubmit({preventDefault(){}});s=JSON.parse(data);let tid=s.tasks[0].id;ctx.toggleItem('tasks',tid);s=JSON.parse(data);assert.equal(s.ledger.reduce((n,l)=>n+l.amount,0),5);
ctx.rewardForm();el('rtitle').value='Movie';el('rcost').value='3';el('editform').onsubmit({preventDefault(){}});s=JSON.parse(data);ctx.redeem(mid,s.rewards[0].id);s=JSON.parse(data);assert.equal(s.ledger.reduce((n,l)=>n+l.amount,0),2);ctx.redeem(mid,s.rewards[0].id);assert.equal(JSON.parse(data).ledger.length,2);ctx.toggleItem('tasks',tid);assert.equal(JSON.parse(data).tasks[0].done,true);
ctx.calendarModeSet('Month');ctx.go('Calendar');assert(el('view').innerHTML.includes('calendar'));ctx.calendarModeSet('Week');ctx.go('Calendar');assert(el('view').innerHTML.includes('daycol'));
console.log('Passed: all views render; old data preserved; chore points; redemption; insufficient balance; spent-star undo protection; month/week switch.');

let pending=[];ctx.XMLHttpRequest=function(){this.open=(method,url)=>{this.url=url};this.send=()=>pending.push(this)};ctx.go('Clock & Weather');assert.equal(pending.length,1);assert(el('view').innerHTML.includes('bigtime'));assert(ctx.document.body.className==='clock-mode');let req=pending.shift();req.status=200;req.responseText=JSON.stringify({current:{temperature_2m:60,apparent_temperature:58,weather_code:0},daily:{time:['2026-11-06','2026-11-07','2026-11-08'],weather_code:[0,3,61],temperature_2m_max:[65,64,63],temperature_2m_min:[50,49,48]}});req.onload();assert(el('weatherpanel').innerHTML.includes('Clear skies'));ctx.loadWeather(false);assert.equal(pending.length,0);ctx.loadWeather(true);pending.shift().onerror();assert(el('weatherpanel').innerHTML.includes('Cannot connect'));assert(el('weatherpanel').innerHTML.includes('60°'));ctx.clockSettings();el('wunit').value='celsius';el('whours').value='24';el('wstartup').value='yes';el('wphoto').value='yes';el('editform').onsubmit({preventDefault(){}});assert(JSON.parse(data).clockOptions.startup);assert(pending[0].url.includes('temperature_unit=celsius'));ctx.go('Dashboard');assert.equal(ctx.document.body.className,'');console.log('Passed clock view, live-data rendering with fixture, refresh throttling, failed-refresh status, unit change and startup persistence.');
ctx.go('Clock & Weather');for(const style of ['classic','flip','analog','night']){ctx.setClockStyle(style);assert.equal(JSON.parse(data).clockOptions.style,style);assert(el('view').innerHTML.includes('clockstyle-'+style));if(style==='analog')assert(el('bigtime').innerHTML.includes('<svg'));if(style==='flip')assert(el('bigtime').innerHTML.includes('flipdigit'))}ctx.clockStyles();assert(el('modal').innerHTML.includes('setClockStyle'));ctx.closeModal();ctx.toggleWeather();assert(JSON.parse(data).clockOptions.hideWeather);assert(el('view').innerHTML.includes('weatherhidden'));let count=pending.length;ctx.loadWeather(true);assert.equal(pending.length,count);while(pending.length)pending.shift().onerror();ctx.XMLHttpRequest=function(){this.open=function(){throw {name:'SecurityError'}}};ctx.toggleWeather();assert(el('weatherpanel').innerHTML.includes('SecurityError'));ctx.loadWeather(true);assert(!el('weatherpanel').innerHTML.includes('Updating weather'));assert.equal(JSON.parse(data).events[0].title,'Existing event');console.log('Passed four saved clock styles, analog SVG, flip digits, weather toggle, and recovery from browser security exceptions.');
ctx.go('Clock & Weather');for(const style of ['classic','flip','analog','night']){ctx.setClockStyle(style);for(const size of ['small','medium','large','xlarge']){ctx.setClockLayout('size',size);for(const position of ['top-left','top-center','top-right','middle-left','middle-center','middle-right','bottom-left','bottom-center','bottom-right']){ctx.setClockLayout('position',position);assert.equal(JSON.parse(data).clockOptions.position,position);assert.equal(JSON.parse(data).clockOptions.size,size);assert(el('view').innerHTML.includes('pos-'+position+' size-'+size));assert(el('view').innerHTML.includes('clockgroup'));}}}ctx.setClockLayout('position','invalid');assert.equal(JSON.parse(data).clockOptions.position,'bottom-right');ctx.resetClockLayout();assert.equal(JSON.parse(data).clockOptions.position,'middle-center');assert.equal(JSON.parse(data).clockOptions.size,'large');assert.equal(JSON.parse(data).events[0].title,'Existing event');console.log('Passed all 144 style/size/position combinations, persistence, invalid-input rejection, reset and existing data preservation.');
ctx.go('Clock & Weather');assert(el('view').innerHTML.includes('hubweather'));assert(el('view').innerHTML.includes('hubprayers'));assert(el('view').innerHTML.includes('id="bigtime"'));assert(el('view').innerHTML.includes('id="weatherpanel"'));for(const name of ['Fajr','Dhuhr','Asr','Maghrib','Isha'])assert(el('view').innerHTML.includes(name));assert(el('view').innerHTML.includes('Stop audio'));console.log('Passed combined clock/weather/prayer screen, five prayer cards, countdown container and audio controls.');

function mockAudio(){
  return {
    src:'', paused:true, plays:0, currentTime:0,
    play(){this.paused=false;this.plays++;return {catch:handler=>{this.reject=handler;}};},
    pause(){this.paused=true;},
    removeAttribute(name){assert.equal(name,'src');this.src='';},
    load(){this.loads=(this.loads||0)+1;}
  };
}
const radio=els.radioplayer=mockAudio(),adhan=els.prayerplayer=mockAudio();
const timers=new Map();
let nextTimer=0;
ctx.setTimeout=(fn,ms)=>{const key=++nextTimer;timers.set(key,{fn,ms});return key;};
ctx.clearTimeout=key=>timers.delete(key);
assert(el('view').innerHTML.includes('Play radio'));
assert.equal(radio.plays,0,'opening the dashboard must not start the stream');
ctx.toggleRadio();
assert.equal(new URL(radio.src).protocol,'https:');
radio.onplaying();
assert(el('radiostatus').textContent.includes('Waiting for playback'));
radio.ontimeupdate();
assert.equal(el('radioelapsed').textContent,'','a playing event alone does not establish stream progress');
radio.currentTime=3;
radio.ontimeupdate();
assert(el('radiostatus').textContent.includes('Live'));
assert.equal(el('radioelapsed').textContent,'0:03');
ctx.go('Prayer clock');
assert(el('view').innerHTML.includes('Stop radio'),'audio state survives screen changes');
ctx.testAdhan();
assert.equal(radio.src,'','adhan must disconnect the stream');
assert(radio.paused);
assert.equal(adhan.plays,1);
assert(el('radiotoggle').disabled);
ctx.toggleRadio();
assert.equal(radio.plays,1,'radio cannot start during adhan');
adhan.onplaying();
adhan.onended();
assert(!el('radiotoggle').disabled);
assert.equal(radio.plays,1,'radio requires a new tap after adhan');
ctx.toggleRadio();
const staleReject=radio.reject;
ctx.toggleRadio();
assert.equal(radio.src,'');
ctx.toggleRadio();
staleReject();
assert(radio.src,'a stale rejection must not stop a new stream');
const timeout=Array.from(timers.values()).find(t=>t.ms===15000);
assert(timeout);
radio.onplaying();
radio.onwaiting();
assert(Array.from(timers.values()).includes(timeout),'buffer events must not extend a stalled connection');
timeout.fn();
assert.equal(radio.src,'');
assert(el('radiostatus').textContent.includes('timed out'));
ctx.testAdhan();
adhan.reject();
assert(!el('radiotoggle').disabled,'failed adhan must release radio controls');
console.log('Passed Cairo radio opt-in, stop, timeout, stale callback protection, and adhan priority.');

ctx.toggleRadio();
ctx.setRadioVolume('36',true);
assert.equal(radio.volume,0.36);
assert.equal(el('radiovolumepct').textContent,'36%');
assert.equal(adhan.volume,0.8,'radio volume must be separate from adhan volume');
ctx.toggleRadioMute();
assert(radio.muted);
assert.equal(el('radiomute')['aria-pressed'],'true');
assert(el('radiostatus').textContent.includes('muted'));
ctx.toggleRadio();
const playCount=radio.plays;
ctx.testRadioSound();
assert.equal(radio.plays,playCount,'speaker test must prompt for unmute');
ctx.toggleRadioMute();
ctx.testRadioSound();
assert(radio.src.startsWith('data:audio/mpeg;base64,'),'speaker test must work without a network request');
radio.onplaying();
assert(el('radiostatus').textContent.includes('test tone'));
radio.onended();
assert.equal(radio.src,'');
assert(el('radiostatus').textContent.includes('Android Media volume'));
ctx.toggleRadioMute();
ctx.setRadioFeed('official');
vm.runInContext(script,ctx);
ctx.go('Prayer clock');
assert(el('view').innerHTML.includes('value="36" oninput="setRadioVolume'));
assert(el('view').innerHTML.includes('aria-pressed="true">Unmute'));
assert(el('view').innerHTML.includes('value="official" selected'));
ctx.toggleRadioMute();
ctx.toggleRadio();
assert.equal(new URL(radio.src).hostname,'service.webvideocore.net');
radio.currentTime=2100;
radio.onplaying();
radio.currentTime=2102;
radio.ontimeupdate();
assert.equal(el('radioelapsed').textContent,'0:02','report elapsed playback, not the HLS timeline position');
assert(el('radiostatus').textContent.includes('Official feed'));
ctx.setRadioFeed('mp3');
assert.equal(radio.src,'','switching feed stops the old connection');
ctx.toggleRadio();
assert.equal(new URL(radio.src).hostname,'radio.xecod.com');
ctx.toggleRadio();
console.log('Passed saved radio volume and mute, independent adhan volume, and offline speaker test.');
console.log('Passed feed switching and persistence, elapsed playback, and stalled stream detection.');
