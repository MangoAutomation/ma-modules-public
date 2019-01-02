/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import mangoApi from './components/mangoApi';

const mangoApiModule = angular.module('maApi', [])
.component(mangoApi, 'maApi')
.config(['maUiMenuProvider', 'maSystemSettingsProvider', function(maUiMenuProvider, SystemSettingsProvider) {
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.settings.mangoApi',
            url: '/mango-api',
            template: '<ma-api></ma-api>',
            menuTr: 'mangoNoSql.settings.header',
            menuIcon: 'storage',
            permission: 'superadmin',
            weight: 2000,
            params: {
                noPadding: false,
                hideFooter: false,
                helpPage: 'ui.help.mangoApi'
            },
        },
        {
            name: 'ui.help.mangoApi',
            url: '/mango-api/help',
            resolve: {
                viewTemplate: function() {
                    return import(/* webpackMode: "eager" */ './help/helpPage.html');
                }
            },
            menuTr: 'mangoNoSql.settings.header'
        },
    ]);

}]);
 


export default mangoApiModule;
