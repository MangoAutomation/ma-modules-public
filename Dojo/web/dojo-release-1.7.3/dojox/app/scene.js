//>>built
define("dojox/app/scene",["dojo/_base/declare","dojo/_base/connect","dojo/_base/array","dojo/_base/Deferred","dojo/_base/lang","dojo/_base/sniff","dojo/dom-style","dojo/dom-geometry","dojo/dom-class","dojo/dom-construct","dojo/dom-attr","dojo/query","dijit/registry","dijit/_WidgetBase","dijit/_TemplatedMixin","dijit/_WidgetsInTemplateMixin","dojox/css3/transit","./model","./view","./bind","./layout/utils"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f,_10,_11,_12,_13,_14,_15){
return _1("dojox.app.scene",[_e,_f,_10],{isContainer:true,widgetsInTemplate:true,defaultView:"default",selectedChild:null,baseClass:"scene mblView",isFullScreen:false,defaultViewType:_13,constructor:function(_16,_17){
this.children={};
if(_16.parent){
this.parent=_16.parent;
}
if(_16.app){
this.app=_16.app;
}
},buildRendering:function(){
this.inherited(arguments);
_7.set(this.domNode,{width:"100%","height":"100%"});
_9.add(this.domNode,"dijitContainer");
},splitChildRef:function(_18){
var id=_18.split(",");
if(id.length>0){
var to=id.shift();
}else{
console.warn("invalid child id passed to splitChildRef(): ",_18);
}
return {id:to||this.defaultView,next:id.join(",")};
},loadChild:function(_19,_1a){
if(!_19){
var _1b=this.defaultView?this.defaultView.split(","):"default";
_19=_1b.shift();
_1a=_1b.join(",");
}
var cid=this.id+"_"+_19;
if(this.children[cid]){
return this.children[cid];
}
if(this.views&&this.views[_19]){
var _1c=this.views[_19];
if(!_1c.dependencies){
_1c.dependencies=[];
}
var _1d=_1c.template?_1c.dependencies.concat(["dojo/text!app/"+_1c.template]):_1c.dependencies.concat([]);
var def=new _4();
if(_1d.length>0){
require(_1d,function(){
def.resolve.call(def,arguments);
});
}else{
def.resolve(true);
}
var _1e=new _4();
var _1f=this;
_4.when(def,function(){
var _20;
if(_1c.type){
_20=_5.getObject(_1c.type);
}else{
if(_1f.defaultViewType){
_20=_1f.defaultViewType;
}else{
throw Error("Unable to find appropriate ctor for the base child class");
}
}
var _21=_5.mixin({},_1c,{id:_1f.id+"_"+_19,templateString:_1c.template?arguments[0][arguments[0].length-1]:"<div></div>",parent:_1f,app:_1f.app});
if(_1a){
_21.defaultView=_1a;
}
var _22=new _20(_21);
if(!_22.loadedModels){
_22.loadedModels=_12(_1c.models,_1f.loadedModels);
_14([_22],_22.loadedModels);
}
var _23=_1f.addChild(_22);
_2.publish("/app/loadchild",[_22]);
var _24;
_1a=_1a.split(",");
if((_1a[0].length>0)&&(_1a.length>1)){
_24=_22.loadChild(_1a[0],_1a[1]);
}else{
if(_1a[0].length>0){
_24=_22.loadChild(_1a[0],"");
}
}
_4.when(_24,function(){
_1e.resolve(_23);
});
});
return _1e;
}
throw Error("Child '"+_19+"' not found.");
},resize:function(_25,_26){
var _27=this.domNode;
if(_25){
_8.setMarginBox(_27,_25);
if(_25.t){
_27.style.top=_25.t+"px";
}
if(_25.l){
_27.style.left=_25.l+"px";
}
}
var mb=_26||{};
_5.mixin(mb,_25||{});
if(!("h" in mb)||!("w" in mb)){
mb=_5.mixin(_8.getMarginBox(_27),mb);
}
var cs=_7.getComputedStyle(_27);
var me=_8.getMarginExtents(_27,cs);
var be=_8.getBorderExtents(_27,cs);
var bb=(this._borderBox={w:mb.w-(me.w+be.w),h:mb.h-(me.h+be.h)});
var pe=_8.getPadExtents(_27,cs);
this._contentBox={l:_7.toPixelValue(_27,cs.paddingLeft),t:_7.toPixelValue(_27,cs.paddingTop),w:bb.w-pe.w,h:bb.h-pe.h};
this.layout();
},layout:function(){
var _28,_29,_2a;
if(this.selectedChild&&this.selectedChild.isFullScreen){
console.warn("fullscreen sceen layout");
}else{
_29=_c("> [region]",this.domNode).map(function(_2b){
var w=_d.getEnclosingWidget(_2b);
if(w){
return w;
}
return {domNode:_2b,region:_b.get(_2b,"region")};
});
if(this.selectedChild){
_29=_3.filter(_29,function(c){
if(c.region=="center"&&this.selectedChild&&this.selectedChild.domNode!==c.domNode){
_7.set(c.domNode,"zIndex",25);
_7.set(c.domNode,"display","none");
return false;
}else{
if(c.region!="center"){
_7.set(c.domNode,"display","");
_7.set(c.domNode,"zIndex",100);
}
}
return c.domNode&&c.region;
},this);
}else{
_3.forEach(_29,function(c){
if(c&&c.domNode&&c.region=="center"){
_7.set(c.domNode,"zIndex",25);
_7.set(c.domNode,"display","none");
}
});
}
}
if(this._contentBox){
_15.layoutChildren(this.domNode,this._contentBox,_29);
}
_3.forEach(this.getChildren(),function(_2c){
if(!_2c._started&&_2c.startup){
_2c.startup();
}
});
},getChildren:function(){
return this._supportingWidgets;
},startup:function(){
if(this._started){
return;
}
this._started=true;
var _2d=this.defaultView?this.defaultView.split(","):"default";
var _2e,_2f;
_2e=_2d.shift();
_2f=_2d.join(",");
if(this.views[this.defaultView]&&this.views[this.defaultView]["defaultView"]){
_2f=this.views[this.defaultView]["defaultView"];
}
if(this.models&&!this.loadedModels){
this.loadedModels=_12(this.models);
_14(this.getChildren(),this.loadedModels);
}
var cid=this.id+"_"+_2e;
if(this.children[cid]){
var _30=this.children[cid];
this.set("selectedChild",_30);
var _31=this.getParent&&this.getParent();
if(!(_31&&_31.isLayoutContainer)){
this.resize();
this.connect(_6("ie")?this.domNode:dojo.global,"onresize",function(){
this.resize();
});
}
_3.forEach(this.getChildren(),function(_32){
_32.startup();
});
if(this._startView&&(this._startView!=this.defaultView)){
this.transition(this._startView,{});
}
}
},addChild:function(_33){
_9.add(_33.domNode,this.baseClass+"_child");
_33.region="center";
_b.set(_33.domNode,"region","center");
this._supportingWidgets.push(_33);
_a.place(_33.domNode,this.domNode);
this.children[_33.id]=_33;
return _33;
},removeChild:function(_34){
if(_34){
var _35=_34.domNode;
if(_35&&_35.parentNode){
_35.parentNode.removeChild(_35);
}
return _34;
}
},_setSelectedChildAttr:function(_36,_37){
if(_36!==this.selectedChild){
return _4.when(_36,_5.hitch(this,function(_38){
if(this.selectedChild){
if(this.selectedChild.deactivate){
this.selectedChild.deactivate();
}
_7.set(this.selectedChild.domNode,"zIndex",25);
}
this.selectedChild=_38;
_7.set(_38.domNode,"display","");
_7.set(_38.domNode,"zIndex",50);
this.selectedChild=_38;
if(this._started){
if(_38.startup&&!_38._started){
_38.startup();
}else{
if(_38.activate){
_38.activate();
}
}
}
this.layout();
}));
}
},transition:function(_39,_3a){
var _3b,_3c,_3d,_3e=this.selectedChild;
if(_39){
var _3f=_39.split(",");
_3b=_3f.shift();
_3c=_3f.join(",");
}else{
_3b=this.defaultView;
if(this.views[this.defaultView]&&this.views[this.defaultView]["defaultView"]){
_3c=this.views[this.defaultView]["defaultView"];
}
}
_3d=this.loadChild(_3b,_3c);
if(!_3e){
return this.set("selectedChild",_3d);
}
var _40=new _4();
_4.when(_3d,_5.hitch(this,function(_41){
var _42;
if(_41!==_3e){
this.set("selectedChild",_41);
_2.publish("/app/transition",[_41,_3b]);
_11(_3e.domNode,_41.domNode,_5.mixin({},_3a,{transition:this.defaultTransition||"none"})).then(_5.hitch(this,function(){
if(_3c&&_41.transition){
_42=_41.transition(_3c,_3a);
}
_4.when(_42,function(){
_40.resolve();
});
}));
return;
}
if(_3c&&_41.transition){
_42=_41.transition(_3c,_3a);
}
_4.when(_42,function(){
_40.resolve();
});
}));
return _40;
},toString:function(){
return this.id;
},activate:function(){
},deactive:function(){
}});
});
