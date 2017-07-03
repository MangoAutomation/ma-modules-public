/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

define(['angular', 'require'], function(angular, require) {
'use strict';

return angular.module('maAsciiFile', ['maUiApp'])
.config(['maUiMenuProvider', function(maUiMenuProvider) {
    maUiMenuProvider.registerMenuItems([{
        name: 'ui.settings.system.asciiFile',
        url: '/ascii-file',
        templateUrl: require.toUrl('./settings.html'),
        menuTr: 'dsEdit.file.systemSettingsDescription',
        menuHidden: true
    }]);
}]);

}); // require
