//>>built
define("dojox/charting/Chart",["dojo/_base/lang","dojo/_base/array","dojo/_base/declare","dojo/_base/html","dojo/dom","dojo/dom-geometry","dojo/dom-construct","dojo/_base/Color","dojo/_base/sniff","./Element","./Theme","./Series","./axis2d/common","dojox/gfx/shape","dojox/gfx","dojox/lang/functional","dojox/lang/functional/fold","dojox/lang/functional/reversed"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,g,_f,_10,_11){
var dc=dojox.charting,_12=_f.lambda("item.clear()"),_13=_f.lambda("item.purgeGroup()"),_14=_f.lambda("item.destroy()"),_15=_f.lambda("item.dirty = false"),_16=_f.lambda("item.dirty = true"),_17=_f.lambda("item.name");
_3("dojox.charting.Chart",null,{constructor:function(_18,_19){
if(!_19){
_19={};
}
this.margins=_19.margins?_19.margins:{l:10,t:10,r:10,b:10};
this.stroke=_19.stroke;
this.fill=_19.fill;
this.delayInMs=_19.delayInMs||200;
this.title=_19.title;
this.titleGap=_19.titleGap;
this.titlePos=_19.titlePos;
this.titleFont=_19.titleFont;
this.titleFontColor=_19.titleFontColor;
this.chartTitle=null;
this.theme=null;
this.axes={};
this.stack=[];
this.plots={};
this.series=[];
this.runs={};
this.dirty=true;
this.coords=null;
this._clearRects=[];
this.node=_5.byId(_18);
var box=_6.getMarginBox(_18);
this.surface=g.createSurface(this.node,box.w||400,box.h||300);
},destroy:function(){
_2.forEach(this.series,_14);
_2.forEach(this.stack,_14);
_f.forIn(this.axes,_14);
if(this.chartTitle&&this.chartTitle.tagName){
_7.destroy(this.chartTitle);
}
_2.forEach(this._clearRects,function(_1a){
_e.dispose(_1a);
});
this.surface.destroy();
},getCoords:function(){
return _4.coords(this.node,true);
},setTheme:function(_1b){
this.theme=_1b.clone();
this.dirty=true;
return this;
},addAxis:function(_1c,_1d){
var _1e,_1f=_1d&&_1d.type||"Default";
if(typeof _1f=="string"){
if(!dc.axis2d||!dc.axis2d[_1f]){
throw Error("Can't find axis: "+_1f+" - Check "+"require() dependencies.");
}
_1e=new dc.axis2d[_1f](this,_1d);
}else{
_1e=new _1f(this,_1d);
}
_1e.name=_1c;
_1e.dirty=true;
if(_1c in this.axes){
this.axes[_1c].destroy();
}
this.axes[_1c]=_1e;
this.dirty=true;
return this;
},getAxis:function(_20){
return this.axes[_20];
},removeAxis:function(_21){
if(_21 in this.axes){
this.axes[_21].destroy();
delete this.axes[_21];
this.dirty=true;
}
return this;
},addPlot:function(_22,_23){
var _24,_25=_23&&_23.type||"Default";
if(typeof _25=="string"){
if(!dc.plot2d||!dc.plot2d[_25]){
throw Error("Can't find plot: "+_25+" - didn't you forget to dojo"+".require() it?");
}
_24=new dc.plot2d[_25](this,_23);
}else{
_24=new _25(this,_23);
}
_24.name=_22;
_24.dirty=true;
if(_22 in this.plots){
this.stack[this.plots[_22]].destroy();
this.stack[this.plots[_22]]=_24;
}else{
this.plots[_22]=this.stack.length;
this.stack.push(_24);
}
this.dirty=true;
return this;
},getPlot:function(_26){
return this.stack[this.plots[_26]];
},removePlot:function(_27){
if(_27 in this.plots){
var _28=this.plots[_27];
delete this.plots[_27];
this.stack[_28].destroy();
this.stack.splice(_28,1);
_f.forIn(this.plots,function(idx,_29,_2a){
if(idx>_28){
_2a[_29]=idx-1;
}
});
var ns=_2.filter(this.series,function(run){
return run.plot!=_27;
});
if(ns.length<this.series.length){
_2.forEach(this.series,function(run){
if(run.plot==_27){
run.destroy();
}
});
this.runs={};
_2.forEach(ns,function(run,_2b){
this.runs[run.plot]=_2b;
},this);
this.series=ns;
}
this.dirty=true;
}
return this;
},getPlotOrder:function(){
return _f.map(this.stack,_17);
},setPlotOrder:function(_2c){
var _2d={},_2e=_f.filter(_2c,function(_2f){
if(!(_2f in this.plots)||(_2f in _2d)){
return false;
}
_2d[_2f]=1;
return true;
},this);
if(_2e.length<this.stack.length){
_f.forEach(this.stack,function(_30){
var _31=_30.name;
if(!(_31 in _2d)){
_2e.push(_31);
}
});
}
var _32=_f.map(_2e,function(_33){
return this.stack[this.plots[_33]];
},this);
_f.forEach(_32,function(_34,i){
this.plots[_34.name]=i;
},this);
this.stack=_32;
this.dirty=true;
return this;
},movePlotToFront:function(_35){
if(_35 in this.plots){
var _36=this.plots[_35];
if(_36){
var _37=this.getPlotOrder();
_37.splice(_36,1);
_37.unshift(_35);
return this.setPlotOrder(_37);
}
}
return this;
},movePlotToBack:function(_38){
if(_38 in this.plots){
var _39=this.plots[_38];
if(_39<this.stack.length-1){
var _3a=this.getPlotOrder();
_3a.splice(_39,1);
_3a.push(_38);
return this.setPlotOrder(_3a);
}
}
return this;
},addSeries:function(_3b,_3c,_3d){
var run=new _c(this,_3c,_3d);
run.name=_3b;
if(_3b in this.runs){
this.series[this.runs[_3b]].destroy();
this.series[this.runs[_3b]]=run;
}else{
this.runs[_3b]=this.series.length;
this.series.push(run);
}
this.dirty=true;
if(!("ymin" in run)&&"min" in run){
run.ymin=run.min;
}
if(!("ymax" in run)&&"max" in run){
run.ymax=run.max;
}
return this;
},getSeries:function(_3e){
return this.series[this.runs[_3e]];
},removeSeries:function(_3f){
if(_3f in this.runs){
var _40=this.runs[_3f];
delete this.runs[_3f];
this.series[_40].destroy();
this.series.splice(_40,1);
_f.forIn(this.runs,function(idx,_41,_42){
if(idx>_40){
_42[_41]=idx-1;
}
});
this.dirty=true;
}
return this;
},updateSeries:function(_43,_44){
if(_43 in this.runs){
var run=this.series[this.runs[_43]];
run.update(_44);
this._invalidateDependentPlots(run.plot,false);
this._invalidateDependentPlots(run.plot,true);
}
return this;
},getSeriesOrder:function(_45){
return _f.map(_f.filter(this.series,function(run){
return run.plot==_45;
}),_17);
},setSeriesOrder:function(_46){
var _47,_48={},_49=_f.filter(_46,function(_4a){
if(!(_4a in this.runs)||(_4a in _48)){
return false;
}
var run=this.series[this.runs[_4a]];
if(_47){
if(run.plot!=_47){
return false;
}
}else{
_47=run.plot;
}
_48[_4a]=1;
return true;
},this);
_f.forEach(this.series,function(run){
var _4b=run.name;
if(!(_4b in _48)&&run.plot==_47){
_49.push(_4b);
}
});
var _4c=_f.map(_49,function(_4d){
return this.series[this.runs[_4d]];
},this);
this.series=_4c.concat(_f.filter(this.series,function(run){
return run.plot!=_47;
}));
_f.forEach(this.series,function(run,i){
this.runs[run.name]=i;
},this);
this.dirty=true;
return this;
},moveSeriesToFront:function(_4e){
if(_4e in this.runs){
var _4f=this.runs[_4e],_50=this.getSeriesOrder(this.series[_4f].plot);
if(_4e!=_50[0]){
_50.splice(_4f,1);
_50.unshift(_4e);
return this.setSeriesOrder(_50);
}
}
return this;
},moveSeriesToBack:function(_51){
if(_51 in this.runs){
var _52=this.runs[_51],_53=this.getSeriesOrder(this.series[_52].plot);
if(_51!=_53[_53.length-1]){
_53.splice(_52,1);
_53.push(_51);
return this.setSeriesOrder(_53);
}
}
return this;
},resize:function(_54,_55){
var box;
switch(arguments.length){
case 1:
box=_1.mixin({},_54);
_6.setMarginBox(this.node,box);
break;
case 2:
box={w:_54,h:_55};
_6.setMarginBox(this.node,box);
break;
}
box=_6.getMarginBox(this.node);
var d=this.surface.getDimensions();
if(d.width!=box.w||d.height!=box.h){
this.surface.setDimensions(box.w,box.h);
this.dirty=true;
return this.render();
}else{
return this;
}
},getGeometry:function(){
var ret={};
_f.forIn(this.axes,function(_56){
if(_56.initialized()){
ret[_56.name]={name:_56.name,vertical:_56.vertical,scaler:_56.scaler,ticks:_56.ticks};
}
});
return ret;
},setAxisWindow:function(_57,_58,_59,_5a){
var _5b=this.axes[_57];
if(_5b){
_5b.setWindow(_58,_59);
_2.forEach(this.stack,function(_5c){
if(_5c.hAxis==_57||_5c.vAxis==_57){
_5c.zoom=_5a;
}
});
}
return this;
},setWindow:function(sx,sy,dx,dy,_5d){
if(!("plotArea" in this)){
this.calculateGeometry();
}
_f.forIn(this.axes,function(_5e){
var _5f,_60,_61=_5e.getScaler().bounds,s=_61.span/(_61.upper-_61.lower);
if(_5e.vertical){
_5f=sy;
_60=dy/s/_5f;
}else{
_5f=sx;
_60=dx/s/_5f;
}
_5e.setWindow(_5f,_60);
});
_2.forEach(this.stack,function(_62){
_62.zoom=_5d;
});
return this;
},zoomIn:function(_63,_64){
var _65=this.axes[_63];
if(_65){
var _66,_67,_68=_65.getScaler().bounds;
var _69=Math.min(_64[0],_64[1]);
var _6a=Math.max(_64[0],_64[1]);
_69=_64[0]<_68.lower?_68.lower:_69;
_6a=_64[1]>_68.upper?_68.upper:_6a;
_66=(_68.upper-_68.lower)/(_6a-_69);
_67=_69-_68.lower;
this.setAxisWindow(_63,_66,_67);
this.render();
}
},calculateGeometry:function(){
if(this.dirty){
return this.fullGeometry();
}
var _6b=_2.filter(this.stack,function(_6c){
return _6c.dirty||(_6c.hAxis&&this.axes[_6c.hAxis].dirty)||(_6c.vAxis&&this.axes[_6c.vAxis].dirty);
},this);
_6d(_6b,this.plotArea);
return this;
},fullGeometry:function(){
this._makeDirty();
_2.forEach(this.stack,_12);
if(!this.theme){
this.setTheme(new _b(dojox.charting._def));
}
_2.forEach(this.series,function(run){
if(!(run.plot in this.plots)){
if(!dc.plot2d||!dc.plot2d.Default){
throw Error("Can't find plot: Default - didn't you forget to dojo"+".require() it?");
}
var _6e=new dc.plot2d.Default(this,{});
_6e.name=run.plot;
this.plots[run.plot]=this.stack.length;
this.stack.push(_6e);
}
this.stack[this.plots[run.plot]].addSeries(run);
},this);
_2.forEach(this.stack,function(_6f){
if(_6f.hAxis){
_6f.setAxis(this.axes[_6f.hAxis]);
}
if(_6f.vAxis){
_6f.setAxis(this.axes[_6f.vAxis]);
}
},this);
var dim=this.dim=this.surface.getDimensions();
dim.width=g.normalizedLength(dim.width);
dim.height=g.normalizedLength(dim.height);
_f.forIn(this.axes,_12);
_6d(this.stack,dim);
var _70=this.offsets={l:0,r:0,t:0,b:0};
_f.forIn(this.axes,function(_71){
_f.forIn(_71.getOffsets(),function(o,i){
_70[i]+=o;
});
});
if(this.title){
this.titleGap=(this.titleGap==0)?0:this.titleGap||this.theme.chart.titleGap||20;
this.titlePos=this.titlePos||this.theme.chart.titlePos||"top";
this.titleFont=this.titleFont||this.theme.chart.titleFont;
this.titleFontColor=this.titleFontColor||this.theme.chart.titleFontColor||"black";
var _72=g.normalizedLength(g.splitFontString(this.titleFont).size);
_70[this.titlePos=="top"?"t":"b"]+=(_72+this.titleGap);
}
_f.forIn(this.margins,function(o,i){
_70[i]+=o;
});
this.plotArea={width:dim.width-_70.l-_70.r,height:dim.height-_70.t-_70.b};
_f.forIn(this.axes,_12);
_6d(this.stack,this.plotArea);
return this;
},render:function(){
if(this.theme){
this.theme.clear();
}
if(this.dirty){
return this.fullRender();
}
this.calculateGeometry();
_f.forEachRev(this.stack,function(_73){
_73.render(this.dim,this.offsets);
},this);
_f.forIn(this.axes,function(_74){
_74.render(this.dim,this.offsets);
},this);
this._makeClean();
if(this.surface.render){
this.surface.render();
}
return this;
},fullRender:function(){
this.fullGeometry();
var _75=this.offsets,dim=this.dim,_76;
_2.forEach(this.series,_13);
_f.forIn(this.axes,_13);
_2.forEach(this.stack,_13);
_2.forEach(this._clearRects,function(_77){
_e.dispose(_77);
});
this._clearRects=[];
if(this.chartTitle&&this.chartTitle.tagName){
_7.destroy(this.chartTitle);
}
this.surface.clear();
this.chartTitle=null;
var t=this.theme,_78=t.plotarea&&t.plotarea.fill,_79=t.plotarea&&t.plotarea.stroke,w=Math.max(0,dim.width-_75.l-_75.r),h=Math.max(0,dim.height-_75.t-_75.b),_76={x:_75.l-1,y:_75.t-1,width:w+2,height:h+2};
if(_78){
_78=_a.prototype._shapeFill(_a.prototype._plotFill(_78,dim,_75),_76);
this._clearRects.push(this.surface.createRect(_76).setFill(_78));
}
if(_79){
this._clearRects.push(this.surface.createRect({x:_75.l,y:_75.t,width:w+1,height:h+1}).setStroke(_79));
}
_f.foldr(this.stack,function(z,_7a){
return _7a.render(dim,_75),0;
},0);
_78=this.fill!==undefined?this.fill:(t.chart&&t.chart.fill);
_79=this.stroke!==undefined?this.stroke:(t.chart&&t.chart.stroke);
if(_78=="inherit"){
var _7b=this.node,_78=new _8(_4.style(_7b,"backgroundColor"));
while(_78.a==0&&_7b!=document.documentElement){
_78=new _8(_4.style(_7b,"backgroundColor"));
_7b=_7b.parentNode;
}
}
if(_78){
_78=_a.prototype._plotFill(_78,dim,_75);
if(_75.l){
_76={width:_75.l,height:dim.height+1};
this._clearRects.push(this.surface.createRect(_76).setFill(_a.prototype._shapeFill(_78,_76)));
}
if(_75.r){
_76={x:dim.width-_75.r,width:_75.r+1,height:dim.height+2};
this._clearRects.push(this.surface.createRect(_76).setFill(_a.prototype._shapeFill(_78,_76)));
}
if(_75.t){
_76={width:dim.width+1,height:_75.t};
this._clearRects.push(this.surface.createRect(_76).setFill(_a.prototype._shapeFill(_78,_76)));
}
if(_75.b){
_76={y:dim.height-_75.b,width:dim.width+1,height:_75.b+2};
this._clearRects.push(this.surface.createRect(_76).setFill(_a.prototype._shapeFill(_78,_76)));
}
}
if(_79){
this._clearRects.push(this.surface.createRect({width:dim.width-1,height:dim.height-1}).setStroke(_79));
}
if(this.title){
var _7c=(g.renderer=="canvas"),_7d=_7c||!_9("ie")&&!_9("opera")?"html":"gfx",_7e=g.normalizedLength(g.splitFontString(this.titleFont).size);
this.chartTitle=_d.createText[_7d](this,this.surface,dim.width/2,this.titlePos=="top"?_7e+this.margins.t:dim.height-this.margins.b,"middle",this.title,this.titleFont,this.titleFontColor);
}
_f.forIn(this.axes,function(_7f){
_7f.render(dim,_75);
});
this._makeClean();
if(this.surface.render){
this.surface.render();
}
return this;
},delayedRender:function(){
if(!this._delayedRenderHandle){
this._delayedRenderHandle=setTimeout(_1.hitch(this,function(){
clearTimeout(this._delayedRenderHandle);
this._delayedRenderHandle=null;
this.render();
}),this.delayInMs);
}
return this;
},connectToPlot:function(_80,_81,_82){
return _80 in this.plots?this.stack[this.plots[_80]].connect(_81,_82):null;
},fireEvent:function(_83,_84,_85){
if(_83 in this.runs){
var _86=this.series[this.runs[_83]].plot;
if(_86 in this.plots){
var _87=this.stack[this.plots[_86]];
if(_87){
_87.fireEvent(_83,_84,_85);
}
}
}
return this;
},_makeClean:function(){
_2.forEach(this.axes,_15);
_2.forEach(this.stack,_15);
_2.forEach(this.series,_15);
this.dirty=false;
},_makeDirty:function(){
_2.forEach(this.axes,_16);
_2.forEach(this.stack,_16);
_2.forEach(this.series,_16);
this.dirty=true;
},_invalidateDependentPlots:function(_88,_89){
if(_88 in this.plots){
var _8a=this.stack[this.plots[_88]],_8b,_8c=_89?"vAxis":"hAxis";
if(_8a[_8c]){
_8b=this.axes[_8a[_8c]];
if(_8b&&_8b.dependOnData()){
_8b.dirty=true;
_2.forEach(this.stack,function(p){
if(p[_8c]&&p[_8c]==_8a[_8c]){
p.dirty=true;
}
});
}
}else{
_8a.dirty=true;
}
}
}});
function _8d(_8e){
return {min:_8e.hmin,max:_8e.hmax};
};
function _8f(_90){
return {min:_90.vmin,max:_90.vmax};
};
function _91(_92,h){
_92.hmin=h.min;
_92.hmax=h.max;
};
function _93(_94,v){
_94.vmin=v.min;
_94.vmax=v.max;
};
function _95(_96,_97){
if(_96&&_97){
_96.min=Math.min(_96.min,_97.min);
_96.max=Math.max(_96.max,_97.max);
}
return _96||_97;
};
function _6d(_98,_99){
var _9a={},_9b={};
_2.forEach(_98,function(_9c){
var _9d=_9a[_9c.name]=_9c.getSeriesStats();
if(_9c.hAxis){
_9b[_9c.hAxis]=_95(_9b[_9c.hAxis],_8d(_9d));
}
if(_9c.vAxis){
_9b[_9c.vAxis]=_95(_9b[_9c.vAxis],_8f(_9d));
}
});
_2.forEach(_98,function(_9e){
var _9f=_9a[_9e.name];
if(_9e.hAxis){
_91(_9f,_9b[_9e.hAxis]);
}
if(_9e.vAxis){
_93(_9f,_9b[_9e.vAxis]);
}
_9e.initializeScalers(_99,_9f);
});
};
return dojox.charting.Chart;
});
