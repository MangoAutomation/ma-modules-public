/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

define(['angular', 'require'], function(angular, require) {
'use strict';

return angular.module('maReports', ['maUiApp'])
.component('maReportsSettings', {
    templateUrl: require.toUrl('./settings.html'),
	controller: ['maDialogHelper', 'maSystemActions', function(maDialogHelper, maSystemActions) {
		this.purgeReports = function(event, purgeAll) {
			return maDialogHelper.confirmSystemAction({
				event: event,
				confirmTr: purgeAll ? 'systemSettings.reports.purgeAll' : 'systemSettings.reports.purgeUsingSettings',
				actionName: 'reportPurge',
				actionData: {purgeAll: purgeAll},
				descriptionTr: 'systemSettings.reports.purge',
				resultsTr: 'systemSettings.reports.purgeSuccess'
			});
		};
	}]
})
.config(['maSystemSettingsProvider', 'maUiMenuProvider', function(SystemSettingsProvider, maUiMenuProvider) {
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
}]);

}); // require
