/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import angular from 'angular';

const sqlConsoleModule = angular.module('maSqlConsole', []);

//.config(['MenuProvider', function(MenuProvider) {
//    var menuItem = {
//        url: '/sql-console',
//        name: 'ui.settings.sqlConsole',
//        template: '<iframe-view src="/sqlConsole.shtm"></iframe-view>',
//        menuTr: 'header.sql',
//        menuIcon: 'storage',
//        permission: 'superadmin',
//        weight: 2000
//    };
//    
//    MenuProvider.registerMenuItems([menuItem]);
//}]);

export default sqlConsoleModule;
