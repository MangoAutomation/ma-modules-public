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

const $inject = Object.freeze(['$rootScope', '$scope', 'maMaintenanceEvent']);
class MaintenanceEventsListController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($rootScope, $scope, maMaintenanceEvent) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;
        this.maMaintenanceEvent = maMaintenanceEvent;
    }
    
    $onInit() {
        this.ngModelCtrl.$render = () => this.render();
        this.getEvents();
    }

    $onChanges(changes) {
        if(this.events) {
            if (changes.updatedItem && this.updatedItem) {
                const foundIndex = this.events.findIndex(item => item.xid === this.updatedItem.xid);
    
                if (foundIndex >= 0) {
                    // if we found it then replace it in the list
                    this.events[foundIndex] = this.updatedItem;
                    this.selectedEvent = this.updatedItem;
                    
                } else {
                    // otherwise add it to the list
                    this.events.push(this.updatedItem);
                    this.selectedEvent = this.events[this.events.length - 1];
                }
    
                this.new = false;
            }
    
            if (changes.deletedItem && this.deletedItem) {
                const foundIndex = this.events.findIndex(item => item.xid === this.deletedItem.xid);
    
                if (foundIndex >= 0) {
                    this.selectedEvent = this.updatedItem;
                    this.events.splice(foundIndex, 1);
                    this.newMaintenanceEvent();
                } 
            }
        }
    }

    setViewValue() {
        this.ngModelCtrl.$setViewValue(this.selectedEvent);
    }

    render() {
        this.selectedEvent = this.ngModelCtrl.$viewValue;
    }

    getEvents() {
        return this.maMaintenanceEvent.list().then(events => {
            this.events = events;
        });
    }
    
    newMaintenanceEvent() {
        this.new = true;
        this.selectedEvent = new this.maMaintenanceEvent();
        this.setViewValue();
        this.itemSelected();
    }

    selectMaintenanceEvent(event) {
        this.new = false;
        this.selectedEvent = event;
        this.setViewValue();
        this.itemSelected();
    }

    itemSelected() {
        if (typeof this.onSelect === 'function') {
            const copyOfItem = this.selectedEvent.copy(); 
            this.onSelect({$item: copyOfItem});
        }
    }

}

export default {
    template: componentTemplate,
    controller: MaintenanceEventsListController,
    bindings: {
        updatedItem: '<?',
        deletedItem: '<?',
        onSelect: '&?',
    },
    require: {
        ngModelCtrl: 'ngModel'
    },
    designerInfo: {
        translation: 'maintenanceEvents.list',
        icon: 'list'
    }
};
