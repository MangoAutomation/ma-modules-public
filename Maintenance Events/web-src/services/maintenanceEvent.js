/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';

maintenanceEventsFactory.$inject = ['maRestResource', '$http'];
function maintenanceEventsFactory(RestResource, $http) {
    
    const maintenanceEventBaseUrl = '/rest/v2/maintenance-events';

    const maintenanceEventXidPrefix = 'ME_';
    
    const defaultProperties = {
        alarmLevel: 'INFORMATION',
        scheduleType: 'MANUAL',
        disabled: true,
        activeYear: 2018,
        activeMonth: 7,
        activeDay: 0,
        activeHour: 0,
        activeMinute: 0,
        activeSecond: 0,
        activeCron: '',
        inactiveYear: 2018,
        inactiveMonth: 7,
        inactiveDay: 0,
        inactiveHour: 0,
        inactiveMinute: 0,
        inactiveSecond: 0,
        inactiveCron: '',
        timeoutPeriods: 0,
        timeoutPeriodType: 'HOURS',
        togglePermission: null,
        dataPoints: [],
        dataSources: [],
        id: 1,
        xid: '',
        name: 'New maintenance event'
      };

    class maintenanceEventsResource extends RestResource {
        static get defaultProperties() {
            return defaultProperties;
        }
        
        static get baseUrl() {
            return maintenanceEventBaseUrl;
        }
        
        static get xidPrefix() {
            return maintenanceEventXidPrefix;
        }

        initialize() {}
    }
    
    return maintenanceEventsResource;
}

export default maintenanceEventsFactory;