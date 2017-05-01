/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

define(['angular', 'require'], function(angular, require) {
'use strict';

return angular.module('maReports', [])
.config(['maSystemSettingsProvider', function(SystemSettingsProvider) {
//    SystemSettingsProvider.addSection({
//        titleTr: 'header.excelreports',
//        template: require.toUrl('./settings.html')
//    });

    SystemSettingsProvider.addAuditAlarmLevelSettings({
        key: 'auditEventAlarmLevel.REPORT',
        translation: 'event.audit.report'
    });
}]);

}); // require
