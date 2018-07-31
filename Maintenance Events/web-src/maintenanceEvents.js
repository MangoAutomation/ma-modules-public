/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import maintenanceEvents from './components/maintenanceEvents';

export default angular.module('maExcelReports', ['maUiApp'])
.component('maMaintenanceEvents', maintenanceEvents)
.config(['maUiMenuProvider', function(maUiMenuProvider) {
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.settings.system.maintenanceEvents',
            url: '/maintenance-events',
            template: '<ma-maintenance-events></ma-maintenance-events>',
            menuTr: 'header.maintenanceEvents',
            menuIcon: 'grid_on',
            menuHidden: true
        }
    ]);
}]);
