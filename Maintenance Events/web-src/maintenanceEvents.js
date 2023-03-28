/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import angular from 'angular';
import maintenanceEvents from './components/maintenanceEvents';
import maintenanceEventsList from './components/maintenanceEventsList';
import maintenanceEventsSelect from './components/maintenanceEventsSelect';
import maintenanceEventsSetup from './components/maintenanceEventsSetup';
import filteringMaintenanceEventsList from './components/filteringMaintenanceEventsList';
import maintenanceEventFactory from './services/maintenanceEvent';

import './maintenanceEvents.css';

export default angular.module('maMaintenanceEvents', ['maUiApp'])
.component('maMaintenanceEvents', maintenanceEvents)
.component('maMaintenanceEventsList', maintenanceEventsList)
.component('maMaintenanceEventsSelect', maintenanceEventsSelect)
.component('maMaintenanceEventsSetup', maintenanceEventsSetup)
.component('maFilteringMaintenanceEventsList', filteringMaintenanceEventsList)
.factory('maMaintenanceEvent', maintenanceEventFactory)
.config(['maUiMenuProvider', 'maEventTypeInfoProvider', function(maUiMenuProvider, eventTypeInfoProvider) {
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.automation.maintenanceEvents',
            url: '/maintenance-events/{xid}',
            template: '<ma-maintenance-events></ma-maintenance-events>',
            menuTr: 'header.maintenanceEvents',
            menuIcon: 'event_busy',
            menuHidden: false,
            params: {
                noPadding: false,
                hideFooter: false,
                helpPage: 'ui.helps.help.maintenanceEvents'
            },
            permission: ['superadmin']
        },
        {
            name: 'ui.helps.help.maintenanceEvents',
            url: '/maintenance-events/help',
            templatePromise() {
                return import(/* webpackMode: "eager" */ './help/helpPage.html');
            },
            menuTr: 'header.maintenanceEvents'
        }
    ]);

    eventTypeInfoProvider.registerEventTypeOptions({
        typeName: 'MAINTENANCE',
        typeId(type, subType, ref1, ref2) {
            return `${type}_${ref1}`;
        }
    });

}]);
