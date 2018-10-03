/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import angular from 'angular';
import maSqlConsole from './components/sqlConsole';
import maSqlConsoleFactory from './services/sqlConsole';

const sqlConsoleModule = angular.module('maSqlConsole', [])
.component('maSqlConsole', maSqlConsole)
.factory('maSqlConsole', maSqlConsoleFactory)
.config(['maUiMenuProvider', function(maUiMenuProvider) {
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.settings.sqlConsole',
            url: '/sql-console',
            template: '<ma-sql-console></ma-sql-console>',
            menuTr: 'header.sql',
            menuIcon: 'storage',
            permission: 'superadmin',
            weight: 2000
        },
    ]);
}]);

export default sqlConsoleModule;
