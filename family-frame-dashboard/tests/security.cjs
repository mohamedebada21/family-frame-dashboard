const fs=require('fs'),vm=require('vm'),assert=require('assert'),path=require('path');
const html=fs.readFileSync(path.join(__dirname,'..','family-calendar.html'),'utf8');
const script=html.match(/<script>([\s\S]*)<\/script>/)[1];
function base(){return {events:[],tasks:[],shopping:[],meals:{},photo:'',members:[],rewards:[],ledger:[]};}
function context(initial){
 const store={ourFamilyBoardV1:JSON.stringify(initial)},els={},alerts=[];
 const el=k=>els[k]||(els[k]={innerHTML:'',textContent:'',value:'',focus(){}});
 const c={Date,Math,JSON,Number,String,Array,console,Blob,URL,setTimeout:()=>0,setInterval:()=>0,
 localStorage:{getItem:k=>store[k]||null,setItem:(k,v)=>store[k]=v},
 document:{body:{className:''},getElementById:el,querySelector:()=>el('query')},
 alert:s=>alerts.push(s),confirm:()=>true,
 FileReader:function(){this.readAsText=f=>{this.result=f.text;this.onload();};}};
 c.window=c;vm.createContext(c);vm.runInContext(script,c);return {c,store,el,alerts};
}
const cases=[
 v=>v.members.push({id:"x');alert(1);//",name:'X',color:'#abcdef'}),
 v=>v.members.push({id:'x',name:'X',color:'red" onmouseover="alert(1)'}),
 v=>v.rewards.push({id:'x',title:'X',cost:'<img src=x onerror=alert(1)>'}),
 v=>v.ledger.push({id:'x',member:'y',amount:'<script>',date:'2026-11-07',label:'X'}),
 v=>v.photo='data:image/svg+xml;base64,PHN2Zz4=',
 v=>v.events.push({id:"x'",title:'X',date:'2026-11-07',time:'09:00'}),
 v=>v.clockOptions={lat:'0&injected=1'},
 v=>v.members={},
 v=>v.meals=JSON.parse('{"__proto__":{"polluted":true}}')
];
for(const corrupt of cases){const x=context(base()),before=x.store.ourFamilyBoardV1,v=base();corrupt(v);
 x.c.restore({files:[{size:100,text:JSON.stringify(v)}]});
 assert.equal(x.store.ourFamilyBoardV1,before,'invalid restore must not overwrite saved data');
 assert(x.alerts.some(s=>/not a valid/.test(s)));
 const startup=context(v);startup.c.go('Family');
 assert(!startup.el('view').innerHTML.includes('alert(1)'),'unsafe stored data reached UI');
}
const good=base();good.members=[{id:'safe1',name:'<img src=x onerror=alert(1)>',color:'#abcdef'}];
const x=context(base());x.c.restore({files:[{size:100,text:JSON.stringify(good)}]});x.c.go('Family');
assert(x.el('view').innerHTML.includes('&lt;img'));
assert(!x.el('view').innerHTML.includes('<img src=x'));
const before=x.store.ourFamilyBoardV1;x.c.restore({files:[{size:6500001,text:'{}'}]});assert.equal(x.store.ourFamilyBoardV1,before);
console.log('Passed: malicious IDs, colors, numbers, photos, objects and coordinates rejected; failed imports preserve data; text escaped; oversized imports blocked.');
