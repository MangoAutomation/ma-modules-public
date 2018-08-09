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

        this.$scope.$on('meUpdated', (event) => {
            this.new = false;
            this.getEvents();
        });

        this.$scope.$on('meDeleted', (event) => {
            this.getEvents();
        });
    }
    
    $onInit() {
        this.ngModelCtrl.$render = () => this.render();
        this.getEvents();
    }

    setViewValue() {
        this.ngModelCtrl.$setViewValue(this.selectedEvent);
    }

    render() {
        this.selectedEvent = this.ngModelCtrl.$viewValue;
    }

    getEvents() {
        this.maMaintenanceEvent.list().then(events => {
            this.events = events;
        });
    }
    
    newMaintenanceEvent() {
        this.new = true;
        this.selectedEvent = new this.maMaintenanceEvent();
        this.setViewValue();
    }

    selectMaintenanceEvent(event) {
        this.new = false;
        this.selectedEvent = event;
        this.setViewValue();
    }

}

export default {
    template: componentTemplate,
    controller: MaintenanceEventsListController,
    bindings: {},
    require: {
        ngModelCtrl: 'ngModel'
    },
    designerInfo: {
        translation: 'maintenanceEvents.list',
        icon: 'list'
    }
};
