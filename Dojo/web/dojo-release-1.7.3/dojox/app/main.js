//>>built
define("dojox/app/main",["dojo/_base/lang","dojo/_base/declare","dojo/_base/Deferred","dojo/_base/connect","dojo/ready","dojo/_base/window","dojo/dom-construct","./scene"],function(_1,_2,_3,_4,_5,_6,_7,_8){
dojo.experimental("dojox.app");
var _9=_2([_8],{constructor:function(_a){
this.scenes={};
if(_a.stores){
for(var _b in _a.stores){
if(_b.charAt(0)!=="_"){
var _c=_a.stores[_b].type?_a.stores[_b].type:"dojo.store.Memory";
var _d={};
if(_a.stores[_b].params){
_1.mixin(_d,_a.stores[_b].params);
}
var _e=dojo.getObject(_c);
if(_d.data&&_1.isString(_d.data)){
_d.data=_1.getObject(_d.data);
}
_a.stores[_b].store=new _e(_d);
}
}
}
},start:function(_f){
var _10=this.loadChild();
_3.when(_10,_1.hitch(this,function(){
this.startup();
this.setStatus(this.lifecycle.STARTED);
}));
},templateString:"<div></div>",selectedChild:null,baseClass:"application mblView",defaultViewType:_8,buildRendering:function(){
if(this.srcNodeRef===_6.body()){
this.srcNodeRef=_7.create("DIV",{},_6.body());
}
this.inherited(arguments);
}});
function _11(_12,_13,_14,_15){
var _16=_12.modules.concat(_12.dependencies);
if(_12.template){
_16.push("dojo/text!"+"app/"+_12.template);
}
require(_16,function(){
var _17=[_9];
for(var i=0;i<_12.modules.length;i++){
_17.push(arguments[i]);
}
if(_12.template){
var ext={templateString:arguments[arguments.length-1]};
}
App=_2(_17,ext);
_5(function(){
app=App(_12,_13||_6.body());
app.setStatus(app.lifecycle.STARTING);
app.start();
});
});
};
return function(_18,_19){
if(!_18){
throw Error("App Config Missing");
}
if(_18.validate){
require(["dojox/json/schema","dojox/json/ref","dojo/text!dojox/application/schema/application.json"],function(_1a,_1b){
_1a=dojox.json.ref.resolveJson(_1a);
if(_1a.validate(_18,_1b)){
_11(_18,_19);
}
});
}else{
_11(_18,_19);
}
};
});
