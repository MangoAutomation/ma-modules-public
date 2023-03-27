/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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
            name: 'ui.system.system.mangoApiSettings',
            url: '/mango-api-settings',
            template: '<ma-api-settings></ma-api-settings>',
            menuTr: 'rest.settings.title',
            menuIcon: 'storage',
            permission: ['superadmin'],
            params: {
                noPadding: false,
                hideFooter: false,
                helpPage: 'ui.helps.help.mangoApiSettings'
            },
        },
        {
            name: 'ui.helps.help.mangoApiSettings',
            url: '/mango-api-settings/help',
            templatePromise() {
                return import(/* webpackMode: "eager" */ './help/helpPage.html');
            },
            menuTr: 'rest.settings.title'
        },
    ]);

}]);
 
export default mangoApiModule;