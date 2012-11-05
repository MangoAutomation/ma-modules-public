//>>built
define("dojox/app/module/history",["dojo/_base/kernel","dojo/_base/lang","dojo/_base/declare","dojo/on"],function(_1,_2,_3,_4){
return _3(null,{postCreate:function(_5,_6){
this.inherited(arguments);
var _7=window.location.hash;
this._startView=((_7&&_7.charAt(0)=="#")?_7.substr(1):_7)||this.defaultView;
_4(this.domNode,"startTransition",_1.hitch(this,"onStartTransition"));
_4(window,"popstate",_1.hitch(this,"onPopState"));
},startup:function(){
this.inherited(arguments);
},proceeding:false,waitingQueue:[],onStartTransition:function(_8){
if(_8.preventDefault){
_8.preventDefault();
}
_8.cancelBubble=true;
if(_8.stopPropagation){
_8.stopPropagation();
}
var _9=_8.detail.target;
var _a=/#(.+)/;
if(!_9&&_a.test(_8.detail.href)){
_9=_8.detail.href.match(_a)[1];
}
history.pushState(_8.detail,_8.detail.href,_8.detail.url);
this.proceedTransition({target:_9,opts:_1.mixin({reverse:false},_8.detail)});
},proceedTransition:function(_b){
if(this.proceeding){
this.waitingQueue.push(_b);
return;
}
this.proceeding=true;
_1.when(this.transition(_b.target,_b.opts),_1.hitch(this,function(){
this.proceeding=false;
var _c=this.waitingQueue.shift();
if(_c){
this.proceedTransition(_c);
}
}));
},onPopState:function(_d){
if(this.getStatus()!==this.lifecycle.STARTED){
return;
}
var _e=_d.state;
if(!_e){
if(!this._startView&&window.location.hash){
_e={target:(location.hash&&location.hash.charAt(0)=="#")?location.hash.substr(1):location.hash,url:location.hash};
}else{
_e={};
}
}
var _f=_e.target||this._startView||this.defaultView;
if(this._startView){
this._startView=null;
}
var _10=_e.title||null;
var _11=_e.url||null;
if(_d._sim){
history.replaceState(_e,_10,_11);
}
var _12=history.state;
this.proceedTransition({target:_f,opts:_1.mixin({reverse:true},_e)});
}});
});
