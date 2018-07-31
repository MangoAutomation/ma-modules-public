(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory(require("angular"));
	else if(typeof define === 'function' && define.amd)
		define(["angular"], factory);
	else if(typeof exports === 'object')
		exports["maintenanceEvents"] = factory(require("angular"));
	else
		root["maintenanceEvents"] = factory(root["angular"]);
})(window, function(__WEBPACK_EXTERNAL_MODULE_angular__) {
return /******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "/modules/maintenanceEvents/web/angular/";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "./web-src/maintenanceEvents.js");
/******/ })
/************************************************************************/
/******/ ({

/***/ "./web-src/components/maintenanceEvents.html":
/*!***************************************************!*\
  !*** ./web-src/components/maintenanceEvents.html ***!
  \***************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

eval("module.exports = \"<div layout=\\\"column\\\" layout-gt-md=\\\"row\\\" layout-gt-lg=\\\"row\\\" flex=\\\"noshrink\\\">\\n    <div flex=\\\"100\\\" flex-gt-md=\\\"25\\\" flex-gt-lg=\\\"20\\\">\\n        <ma-maintenance-events-list></ma-maintenance-events-list>\\n    </div>\\n\\n    <div flex>\\n        <ma-maintenance-events-setup></ma-maintenance-events-setup>\\n    </div>\\n    \\n</div>\";\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEvents.html?");

/***/ }),

/***/ "./web-src/components/maintenanceEvents.js":
/*!*************************************************!*\
  !*** ./web-src/components/maintenanceEvents.js ***!
  \*************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var _maintenanceEvents_html__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./maintenanceEvents.html */ \"./web-src/components/maintenanceEvents.html\");\n/* harmony import */ var _maintenanceEvents_html__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_maintenanceEvents_html__WEBPACK_IMPORTED_MODULE_0__);\n/**\n * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.\n * @author Luis Güette\n */\n\n\n\n/* harmony default export */ __webpack_exports__[\"default\"] = ({\n    bindings: {},\n    template: _maintenanceEvents_html__WEBPACK_IMPORTED_MODULE_0___default.a\n});\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEvents.js?");

/***/ }),

/***/ "./web-src/components/maintenanceEventsList.html":
/*!*******************************************************!*\
  !*** ./web-src/components/maintenanceEventsList.html ***!
  \*******************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

eval("module.exports = \"<md-card flex>\\n    <md-card-title flex=\\\"nogrow\\\">\\n        <md-card-title-text>\\n            <span class=\\\"md-headline\\\">\\n                <span ma-tr=\\\"maintenanceEvents.list\\\"></span>\\n            </span>\\n        </md-card-title-text>\\n    </md-card-title>\\n    <md-card-content>\\n        <h1>Hola</h1>\\n    </md-card-content>\\n</md-card>\";\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEventsList.html?");

/***/ }),

/***/ "./web-src/components/maintenanceEventsList.js":
/*!*****************************************************!*\
  !*** ./web-src/components/maintenanceEventsList.js ***!
  \*****************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var _maintenanceEventsList_html__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./maintenanceEventsList.html */ \"./web-src/components/maintenanceEventsList.html\");\n/* harmony import */ var _maintenanceEventsList_html__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_maintenanceEventsList_html__WEBPACK_IMPORTED_MODULE_0__);\n/**\n * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.\n * @author Luis Güette\n */\n\n\n\n/**\n * @ngdoc directive\n * @name ngMango.directive:maMaintenanceEventsList\n * @restrict E\n * @description Displays a list of maintenance\n */\n\nconst $inject = Object.freeze(['$scope']);\nclass MaintenanceEventsListController {\n    static get $inject() { return $inject; }\n    static get $$ngIsClass() { return true; }\n    \n    constructor($scope) {\n        this.$scope = $scope;\n    }\n    \n    $onInit() {\n\n    }\n    \n    $onChanges(changes) {\n    }\n    \n    \n\n}\n\n/* harmony default export */ __webpack_exports__[\"default\"] = ({\n    template: _maintenanceEventsList_html__WEBPACK_IMPORTED_MODULE_0___default.a,\n    controller: MaintenanceEventsListController,\n    bindings: {},\n    // require: {\n    //     ngModelCtrl: 'ngModel'\n    // },\n    designerInfo: {\n        translation: 'maintenanceEvents.list',\n        icon: 'list'\n    }\n});\n\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEventsList.js?");

/***/ }),

/***/ "./web-src/components/maintenanceEventsSetup.html":
/*!********************************************************!*\
  !*** ./web-src/components/maintenanceEventsSetup.html ***!
  \********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

eval("module.exports = \"<md-card flex>\\n    <md-card-title flex=\\\"nogrow\\\">\\n        <md-card-title-text>\\n            <span class=\\\"md-headline\\\">\\n                <span ma-tr=\\\"maintenanceEvents.setup\\\"></span>\\n            </span>\\n        </md-card-title-text>\\n    </md-card-title>\\n    <md-card-content>\\n        <h1>Hola</h1>\\n    </md-card-content>\\n</md-card>\";\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEventsSetup.html?");

/***/ }),

/***/ "./web-src/components/maintenanceEventsSetup.js":
/*!******************************************************!*\
  !*** ./web-src/components/maintenanceEventsSetup.js ***!
  \******************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var _maintenanceEventsSetup_html__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./maintenanceEventsSetup.html */ \"./web-src/components/maintenanceEventsSetup.html\");\n/* harmony import */ var _maintenanceEventsSetup_html__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_maintenanceEventsSetup_html__WEBPACK_IMPORTED_MODULE_0__);\n/**\n * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.\n * @author Luis Güette\n */\n\n\n\n/**\n * @ngdoc directive\n * @name ngMango.directive:maMaintenanceEventsSetup\n * @restrict E\n * @description Displays a form to create/edit maintenance events\n */\n\nconst $inject = Object.freeze(['$scope']);\nclass MaintenanceEventsSetupController {\n    static get $inject() { return $inject; }\n    static get $$ngIsClass() { return true; }\n    \n    constructor($scope) {\n        this.$scope = $scope;\n    }\n    \n    $onInit() {\n\n    }\n    \n    $onChanges(changes) {\n    }\n    \n    \n\n}\n\n/* harmony default export */ __webpack_exports__[\"default\"] = ({\n    template: _maintenanceEventsSetup_html__WEBPACK_IMPORTED_MODULE_0___default.a,\n    controller: MaintenanceEventsSetupController,\n    bindings: {},\n    // require: {\n    //     ngModelCtrl: 'ngModel'\n    // },\n    designerInfo: {\n        translation: 'maintenanceEvents.setup',\n        icon: 'settings'\n    }\n});\n\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEventsSetup.js?");

/***/ }),

/***/ "./web-src/maintenanceEvents.js":
/*!**************************************!*\
  !*** ./web-src/maintenanceEvents.js ***!
  \**************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var angular__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! angular */ \"angular\");\n/* harmony import */ var angular__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(angular__WEBPACK_IMPORTED_MODULE_0__);\n/* harmony import */ var _components_maintenanceEvents__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./components/maintenanceEvents */ \"./web-src/components/maintenanceEvents.js\");\n/* harmony import */ var _components_maintenanceEventsList__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./components/maintenanceEventsList */ \"./web-src/components/maintenanceEventsList.js\");\n/* harmony import */ var _components_maintenanceEventsSetup__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./components/maintenanceEventsSetup */ \"./web-src/components/maintenanceEventsSetup.js\");\n/**\n * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.\n * @author Luis Güette\n */\n\n\n\n\n\n\n/* harmony default export */ __webpack_exports__[\"default\"] = (angular__WEBPACK_IMPORTED_MODULE_0___default.a.module('maMaintenanceEvents', ['maUiApp'])\n.component('maMaintenanceEvents', _components_maintenanceEvents__WEBPACK_IMPORTED_MODULE_1__[\"default\"])\n.component('maMaintenanceEventsList', _components_maintenanceEventsList__WEBPACK_IMPORTED_MODULE_2__[\"default\"])\n.component('maMaintenanceEventsSetup', _components_maintenanceEventsSetup__WEBPACK_IMPORTED_MODULE_3__[\"default\"])\n.config(['maUiMenuProvider', function(maUiMenuProvider) {\n    maUiMenuProvider.registerMenuItems([\n        {\n            name: 'ui.settings.maintenanceEvents',\n            url: '/maintenance-events',\n            template: '<ma-maintenance-events></ma-maintenance-events>',\n            menuTr: 'header.maintenanceEvents',\n            menuIcon: 'event_busy',\n            menuHidden: false,\n            params: {\n                noPadding: false,\n                hideFooter: false\n            },\n            permission: 'superadmin'\n        },\n    ]);\n}]));\n\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/maintenanceEvents.js?");

/***/ }),

/***/ "angular":
/*!**************************!*\
  !*** external "angular" ***!
  \**************************/
/*! no static exports found */
/***/ (function(module, exports) {

eval("module.exports = __WEBPACK_EXTERNAL_MODULE_angular__;\n\n//# sourceURL=webpack://maintenanceEvents/external_%22angular%22?");

/***/ })

/******/ })["default"];
});