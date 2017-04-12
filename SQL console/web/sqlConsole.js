/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

define(['angular', 'require'], function(angular, require) {
'use strict';

return angular.module('maSqlConsole', [])
.config(['MENU_ITEMS', 'mangoStateProvider', function(MENU_ITEMS, mangoStateProvider) {
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
//    mangoStateProvider.addStates([menuItem]);
//    for (var i = 0; i < MENU_ITEMS.length; i++) {
//        if (MENU_ITEMS[i].name === 'ui.settings') {
//            MENU_ITEMS[i].children.splice(0, 0, menuItem);
//            break;
//        }
//    }
}]);

}); // require
