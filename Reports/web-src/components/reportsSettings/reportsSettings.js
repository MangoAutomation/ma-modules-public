/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import settingsTemplate from './reportsSettings.html';

export default {
    template: settingsTemplate,
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
};