/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './filteringMaintenanceEventsList.html';

/**
 * @ngdoc directive
 * @name ngMango.directive:maFilteringMaintenanceEventsList
 * @restrict E
 * @description Displays a search input for maintenance events
 */

const $inject = Object.freeze(['$scope', 'maMaintenanceEvent', 'maTranslate']);
class FilteringMaintenanceEventsListController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($scope, maMaintenanceEvent, maTranslate) {
        this.$scope = $scope;
        this.maMaintenanceEvent = maMaintenanceEvent;
        this.maTranslate = maTranslate;

        this.maTranslate.tr(['maintenanceEvents.searchForEventsByName']).then(value => {
            this.label = value;
        });
    }
    
    $onInit() {
        this.ngModelCtrl.$render = () => this.render();

        this.getEvents().then(events => {
            this.events = events;
        });
    }

    setViewValue() {
        if (this.selectedEvent) {
            this.ngModelCtrl.$setViewValue(this.selectedEvent);
        }
    }

    render() {
        this.selectedEvent = this.ngModelCtrl.$viewValue;
    }
    
    searchTextChange(searchText) {
        return this.getEvents().then(events => {
            
            return this.events = events.filter(event => {
                return event.name.includes(searchText);
            });

        })
    }

    getEvents() {
        return this.maMaintenanceEvent.list();
    }

    displayText(event) {
        return event.name
    }

}

export default {
    template: componentTemplate,
    controller: FilteringMaintenanceEventsListController,
    bindings: {},
    require: {
        ngModelCtrl: 'ngModel'
    },
    designerInfo: {
        translation: 'maintenanceEvents.searchForEventsByName',
        icon: 'search'
    }
};
