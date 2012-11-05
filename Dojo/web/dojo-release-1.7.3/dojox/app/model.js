//>>built
define("dojox/app/model",["dojo/_base/lang","dojo/_base/Deferred","dojox/mvc/_base"],function(_1,_2,_3){
return function(_4,_5){
var _6={};
if(_5){
_1.mixin(_6,_5);
}
if(_4){
for(var _7 in _4){
if(_7.charAt(0)!=="_"){
var _8=_4[_7].params?_4[_7].params:{};
var _9={"store":_8.store.store,"query":_8.store.query?_8.store.query:{}};
_6[_7]=_2.when(_3.newStatefulModel(_9),function(_a){
return _a;
});
}
}
}
return _6;
};
});
