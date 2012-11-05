//>>built
define("dojox/mobile/ViewController",["dojo/_base/kernel","dojo/_base/array","dojo/_base/connect","dojo/_base/declare","dojo/_base/lang","dojo/_base/window","dojo/dom","dojo/dom-class","dojo/dom-construct","dojo/on","dojo/ready","dijit/registry","./ProgressIndicator","./TransitionEvent"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,on,_a,_b,_c,_d){
var dm=_5.getObject("dojox.mobile",true);
var _e=_4("dojox.mobile.ViewController",null,{constructor:function(){
this.viewMap={};
this.currentView=null;
this.defaultView=null;
_a(_5.hitch(this,function(){
on(_6.body(),"startTransition",_5.hitch(this,"onStartTransition"));
}));
},findCurrentView:function(_f,src){
if(_f){
var w=_b.byId(_f);
if(w&&w.getShowingView){
return w.getShowingView();
}
}
if(dm.currentView){
return dm.currentView;
}
w=src;
while(true){
w=w.getParent();
if(!w){
return null;
}
if(_8.contains(w.domNode,"mblView")){
break;
}
}
return w;
},onStartTransition:function(evt){
evt.preventDefault();
if(!evt.detail||(evt.detail&&!evt.detail.moveTo&&!evt.detail.href&&!evt.detail.url&&!evt.detail.scene)){
return;
}
var w=this.findCurrentView(evt.detail.moveTo,(evt.target&&evt.target.id)?_b.byId(evt.target.id):_b.byId(evt.target));
if(!w||(evt.detail&&evt.detail.moveTo&&w===_b.byId(evt.detail.moveTo))){
return;
}
if(evt.detail.href){
var t=_b.byId(evt.target.id).hrefTarget;
if(t){
dm.openWindow(evt.detail.href,t);
}else{
w.performTransition(null,evt.detail.transitionDir,evt.detail.transition,evt.target,function(){
location.href=evt.detail.href;
});
}
return;
}else{
if(evt.detail.scene){
_3.publish("/dojox/mobile/app/pushScene",[evt.detail.scene]);
return;
}
}
var _10=evt.detail.moveTo;
if(evt.detail.url){
var id;
if(dm._viewMap&&dm._viewMap[evt.detail.url]){
id=dm._viewMap[evt.detail.url];
}else{
var _11=this._text;
if(!_11){
if(_b.byId(evt.target.id).sync){
_1.xhrGet({url:evt.detail.url,sync:true,load:function(_12){
_11=_5.trim(_12);
}});
}else{
var s="dojo/_base/xhr";
require([s],_5.hitch(this,function(xhr){
var _13=_c.getInstance();
_6.body().appendChild(_13.domNode);
_13.start();
var obj=xhr.get({url:evt.detail.url,handleAs:"text"});
obj.addCallback(_5.hitch(this,function(_14,_15){
_13.stop();
if(_14){
this._text=_14;
new _d(evt.target,{transition:evt.detail.transition,transitionDir:evt.detail.transitionDir,moveTo:_10,href:evt.detail.href,url:evt.detail.url,scene:evt.detail.scene},evt.detail).dispatch();
}
}));
obj.addErrback(function(_16){
_13.stop();
});
}));
return;
}
}
this._text=null;
id=this._parse(_11,_b.byId(evt.target.id).urlTarget);
if(!dm._viewMap){
dm._viewMap=[];
}
dm._viewMap[evt.detail.url]=id;
}
_10=id;
w=this.findCurrentView(_10,_b.byId(evt.target.id))||w;
}
var src=_b.getEnclosingWidget(evt.target);
var _17,_18;
if(src&&src.callback){
_17=src;
_18=src.callback;
}
w.performTransition(_10,evt.detail.transitionDir,evt.detail.transition,_17,_18);
},_parse:function(_19,id){
var _1a,_1b,i,j,len;
var _1c=this.findCurrentView();
var _1d=_b.byId(id)&&_b.byId(id).containerNode||_7.byId(id)||_1c&&_1c.domNode.parentNode||_6.body();
var _1e=null;
for(j=_1d.childNodes.length-1;j>=0;j--){
var c=_1d.childNodes[j];
if(c.nodeType===1){
if(c.getAttribute("fixed")==="bottom"){
_1e=c;
break;
}
}
}
if(_19.charAt(0)==="<"){
_1a=_9.create("DIV",{innerHTML:_19});
for(i=0;i<_1a.childNodes.length;i++){
var n=_1a.childNodes[i];
if(n.nodeType===1){
_1b=n;
break;
}
}
if(!_1b){
return;
}
_1b.style.visibility="hidden";
_1d.insertBefore(_1a,_1e);
var ws=_1.parser.parse(_1a);
_2.forEach(ws,function(w){
if(w&&!w._started&&w.startup){
w.startup();
}
});
for(i=0,len=_1a.childNodes.length;i<len;i++){
_1d.insertBefore(_1a.firstChild,_1e);
}
_1d.removeChild(_1a);
_b.byNode(_1b)._visible=true;
}else{
if(_19.charAt(0)==="{"){
_1a=_9.create("DIV");
_1d.insertBefore(_1a,_1e);
this._ws=[];
_1b=this._instantiate(eval("("+_19+")"),_1a);
for(i=0;i<this._ws.length;i++){
var w=this._ws[i];
w.startup&&!w._started&&(!w.getParent||!w.getParent())&&w.startup();
}
this._ws=null;
}
}
_1b.style.display="none";
_1b.style.visibility="visible";
return _1.hash?"#"+_1b.id:_1b.id;
},_instantiate:function(obj,_1f,_20){
var _21;
for(var key in obj){
if(key.charAt(0)=="@"){
continue;
}
var cls=_5.getObject(key);
if(!cls){
continue;
}
var _22={};
var _23=cls.prototype;
var _24=_5.isArray(obj[key])?obj[key]:[obj[key]];
for(var i=0;i<_24.length;i++){
for(var _25 in _24[i]){
if(_25.charAt(0)=="@"){
var val=_24[i][_25];
_25=_25.substring(1);
if(typeof _23[_25]=="string"){
_22[_25]=val;
}else{
if(typeof _23[_25]=="number"){
_22[_25]=val-0;
}else{
if(typeof _23[_25]=="boolean"){
_22[_25]=(val!="false");
}else{
if(typeof _23[_25]=="object"){
_22[_25]=eval("("+val+")");
}
}
}
}
}
}
_21=new cls(_22,_1f);
if(_1f){
_21._visible=true;
this._ws.push(_21);
}
if(_20&&_20.addChild){
_20.addChild(_21);
}
this._instantiate(_24[i],null,_21);
}
}
return _21&&_21.domNode;
}});
new _e();
return _e;
});
