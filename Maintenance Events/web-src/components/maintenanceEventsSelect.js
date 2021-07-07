/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './maintenanceEventsSelect.html';

const $inject = Object.freeze(['maMaintenanceEvent']);

class maintenanceEventsSelectController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maMaintenanceEvent) {
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

    setViewValue(selectedEvent) {
        this.ngModelCtrl.$setViewValue(selectedEvent);
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
        this.selectedEvent = new this.maMaintenanceEvent();
        this.setViewValue();
        this.itemSelected();
    }

    selectMaintenanceEvent() {
        const selectedEvent = this.selectedEvent.copy();
        this.setViewValue(selectedEvent);
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
    bindings: {
        updatedItem: '<?',
        deletedItem: '<?',
        onSelect: '&?',
        selectMultiple: '<?',
    },
    require: {
        ngModelCtrl: 'ngModel'
    },
    transclude: {
        labelSlot: '?maLabel'
    },
    controller: maintenanceEventsSelectController,
    template: componentTemplate
};