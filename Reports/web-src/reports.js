/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import angular from 'angular';
import reportsSettings from './components/reportsSettings/reportsSettings';
import reportEventHandlerEditor from './components/reportEventHandlerEditor/reportEventHandlerEditor';
import reportSelect from './components/reportSelect/reportSelect';
import reportService from './services/reportService';

const reportsModule = angular.module('maReports', ['maUiApp'])
.component('maReportsSettings', reportsSettings)
.component('maReportSelect', reportSelect)
.component('maReportEventHandlerEditor', reportEventHandlerEditor)
.factory('maReport', reportService)
.config(['maSystemSettingsProvider', 'maUiMenuProvider', '$injector', function(SystemSettingsProvider, maUiMenuProvider, $injector) {
    maUiMenuProvider.registerMenuItems([{
        name: 'ui.settings.system.reports',
        url: '/reports',
        template: '<ma-reports-settings></ma-reports-settings>',
        menuTr: 'header.reports',
        menuIcon: 'book',
        menuHidden: true
    }]);

    SystemSettingsProvider.addAuditAlarmLevelSettings([
        {
            key: 'auditEventAlarmLevel.REPORT',
            translation: 'event.audit.report'
        }
    ]);

    if ($injector.has('maEventHandlerProvider')) {
        const maEventHandlerProvider = $injector.get('maEventHandlerProvider');
        if (typeof maEventHandlerProvider.registerEventHandlerType === 'function') {
            maEventHandlerProvider.registerEventHandlerType({
                type: 'REPORT',
                description: 'reports.handler',
                template: `<ma-report-event-handler-editor event-handler="$ctrl.eventHandler"></ma-report-event-handler-editor>`
            });
        }
    }
}]);

export default reportsModule;