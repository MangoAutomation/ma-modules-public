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

eval("module.exports = \"<h1>Hola</h1>\";\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/components/maintenanceEvents.html?");

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

/***/ "./web-src/maintenanceEvents.js":
/*!**************************************!*\
  !*** ./web-src/maintenanceEvents.js ***!
  \**************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
eval("__webpack_require__.r(__webpack_exports__);\n/* harmony import */ var angular__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! angular */ \"angular\");\n/* harmony import */ var angular__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(angular__WEBPACK_IMPORTED_MODULE_0__);\n/* harmony import */ var _components_maintenanceEvents__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./components/maintenanceEvents */ \"./web-src/components/maintenanceEvents.js\");\n/**\n * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.\n * @author Luis Güette\n */\n\n\n\n\n/* harmony default export */ __webpack_exports__[\"default\"] = (angular__WEBPACK_IMPORTED_MODULE_0___default.a.module('maExcelReports', ['maUiApp'])\n.component('maMaintenanceEvents', _components_maintenanceEvents__WEBPACK_IMPORTED_MODULE_1__[\"default\"])\n.config(['maUiMenuProvider', function(maUiMenuProvider) {\n    maUiMenuProvider.registerMenuItems([\n        {\n            name: 'ui.settings.system.maintenanceEvents',\n            url: '/maintenance-events',\n            template: '<ma-maintenance-events></ma-maintenance-events>',\n            menuTr: 'header.maintenanceEvents',\n            menuIcon: 'grid_on',\n            menuHidden: true\n        }\n    ]);\n}]));\n\n\n//# sourceURL=webpack://maintenanceEvents/./web-src/maintenanceEvents.js?");

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