/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import mangoApiSettings from './components/mangoApiSettings';
import mangoApiSettingsFactory from './services/mangoApiSettings';
import './mangoApi.css';

const mangoApiModule = angular.module('maApi', [])
.component('maApiSettings', mangoApiSettings)
.factory('maApiSettings', mangoApiSettingsFactory)
.config(['maUiMenuProvider', function(maUiMenuProvider) {
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.settings.system.mangoApiSettings',
            url: '/mango-api-settings',
            template: '<ma-api-settings></ma-api-settings>',
            menuTr: 'rest.settings.title',
            menuIcon: 'storage',
            permission: 'superadmin',
            weight: 2000,
            params: {
                noPadding: false,
                hideFooter: false,
                helpPage: 'ui.help.mangoApiSettings'
            },
        },
        {
            name: 'ui.help.mangoApiSettings',
            url: '/mango-api-settings/help',
            resolve: {
                viewTemplate: function() {
                    return import(/* webpackMode: "eager" */ './help/helpPage.html');
                }
            },
            menuTr: 'rest.settings.title'
        },
    ]);

}]);
 
export default mangoApiModule;