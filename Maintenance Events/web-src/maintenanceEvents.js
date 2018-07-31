/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import maintenanceEvents from './components/maintenanceEvents';
import maintenanceEventsList from './components/maintenanceEventsList';
import maintenanceEventsSetup from './components/maintenanceEventsSetup';

export default angular.module('maMaintenanceEvents', ['maUiApp'])
.component('maMaintenanceEvents', maintenanceEvents)
.component('maMaintenanceEventsList', maintenanceEventsList)
.component('maMaintenanceEventsSetup', maintenanceEventsSetup)
.config(['maUiMenuProvider', function(maUiMenuProvider) {
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.settings.maintenanceEvents',
            url: '/maintenance-events',
            template: '<ma-maintenance-events></ma-maintenance-events>',
            menuTr: 'header.maintenanceEvents',
            menuIcon: 'event_busy',
            menuHidden: false,
            params: {
                noPadding: false,
                hideFooter: false
            },
            permission: 'superadmin'
        },
    ]);
}]);
