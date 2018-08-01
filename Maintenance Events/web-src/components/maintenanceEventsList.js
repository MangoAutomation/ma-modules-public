/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './maintenanceEventsList.html';

/**
 * @ngdoc directive
 * @name ngMango.directive:maMaintenanceEventsList
 * @restrict E
 * @description Displays a list of maintenance events
 */

const $inject = Object.freeze(['$scope', 'maMaintenanceEvent']);
class MaintenanceEventsListController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($scope, maMaintenanceEvent) {
        this.$scope = $scope;
        this.maMaintenanceEvent = maMaintenanceEvent;

    }
    
    $onInit() {
        this.getEvents();
    }

    getEvents() {
        this.maMaintenanceEvent.list().then(events => {
            this.events = events;
        });
    }
    
    newMaintenanceEvent() {
        this.new = true;
        console.log('new Event');
    }

    selectMaintenanceEvent(event) {
        this.new = false;
        this.selectEvent = event;
        console.log('event selected', event);
    }

}

export default {
    template: componentTemplate,
    controller: MaintenanceEventsListController,
    bindings: {},
    // require: {
    //     ngModelCtrl: 'ngModel'
    // },
    designerInfo: {
        translation: 'maintenanceEvents.list',
        icon: 'list'
    }
};
