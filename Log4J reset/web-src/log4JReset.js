import angular from 'angular';
import './log4JReset.css';
import log4JResetComponent from './components/log4JReset';
import log4JResetFactory from './services/log4JReset';

const log4JReset = angular.module('maLog4JReset', [])
    .component('maLog4jReset', log4JResetComponent)
    .factory('maLog4JReset', log4JResetFactory)
    .config(['maUiMenuProvider', function(maUiMenuProvider) {
        maUiMenuProvider.registerMenuItems([
            {
                name: 'ui.settings.log4JReset',
                url: '/log4j-reset',
                template: '<ma-log4j-reset></ma-log4j-reset>',
                menuTr: 'log4JReset.settings.header',
                menuIcon: 'format_align_justify',
                permission: 'superadmin',
                params: {
                    noPadding: false,
                    hideFooter: false,
                    helpPage: 'ui.help.log4JReset'
                },
            },
            {
                name: 'ui.help.log4JReset',
                url: '/log4j-reset/help',
                resolve: {
                    viewTemplate: function() {
                        return import(/* webpackMode: "eager" */ './help/helpPage.html');
                    }
                },
                menuTr: 'log4JReset.settings.header'
            }
        ]);
    }]);
 
export default log4JReset;
