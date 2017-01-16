/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

define(['angular', 'require'], function(angular, require) {
'use strict';

return angular.module('maApi', [])
.config(['SystemSettingsProvider', function(SystemSettingsProvider) {
    SystemSettingsProvider.addSection({
        titleTr: 'rest.settings.title',
        template: require.toUrl('./settings.html')
    });
}]);

}); // require
