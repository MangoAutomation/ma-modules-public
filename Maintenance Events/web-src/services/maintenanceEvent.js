/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';

maintenanceEventsFactory.$inject = ['maRestResource'];
function maintenanceEventsFactory(RestResource) {
    
    const maintenanceEventBaseUrl = '/rest/v2/maintenance-events';
    const maintenanceEventWebSocketUrl = '/v2/websocket/maintenance-events';
    const maintenanceEventXidPrefix = 'ME_';
    
    const defaultProperties = {
        alarmLevel: 'INFORMATION',
        scheduleType: 'MANUAL',
        disabled: false,
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
        timeoutPeriods: 1,
        timeoutPeriodType: 'HOURS',
        togglePermission: null,
        dataPoints: [],
        dataSources: [],
        name: 'New maintenance event'
      };

    class maintenanceEventsResource extends RestResource {
        static get defaultProperties() {
            return defaultProperties;
        }
        
        static get baseUrl() {
            return maintenanceEventBaseUrl;
        }
        
        static get webSocketUrl() {
            return maintenanceEventWebSocketUrl;
        }
        
        static get xidPrefix() {
            return maintenanceEventXidPrefix;
        }

        toggleActive(opts = {}) {
            return this.constructor.http({
                url: `${this.constructor.baseUrl}/toggle/${this.getEncodedId()}`,
                method: 'PUT',
                params: opts.params
            }, opts).then(response => {
                return response.data;
            });
        }
        
        setActive(value, opts = {}) {
            if (!opts.params) {
                opts.params = {};
            }
            
            opts.params.active = value;
            
            return this.constructor.http({
                url: `${this.constructor.baseUrl}/active/${this.getEncodedId()}`,
                method: 'PUT',
                params: opts.params
            }, opts).then(response => {
                return response.data;
            });
        }
        
        static toggleActive(xid) {
            const me = new this({xid});
            return me.toggleActive();
        }

        static setActive(xid, value) {
            const me = new this({xid});
            return me.setActive(value);
        }
    }
    
    return maintenanceEventsResource;
}

export default maintenanceEventsFactory;