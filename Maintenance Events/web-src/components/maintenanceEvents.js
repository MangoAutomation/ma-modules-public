/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './maintenanceEvents.html';

const $inject = Object.freeze(['$scope', '$mdMedia', '$state', 'maMaintenanceEvent']);

class maintenanceEventsController {
    static get $inject() {
        return $inject;
    }
    static get $$ngIsClass() {
        return true;
    }

    constructor($scope, $mdMedia, $state, maMaintenanceEvent) {
        this.$scope = $scope;
        this.$mdMedia = $mdMedia;
        this.$state = $state;
        this.maMaintenanceEvent = maMaintenanceEvent;
    }

    $onInit() {
        this.selectedEvent = null;
        if (this.$state.params.xid) {
            this.maMaintenanceEvent.get(this.$state.params.xid).then(
                (event) => {
                    delete event.$promise;
                    this.selectedEvent = event;
                    this.eventUpdated(this.selectedEvent);
                },
                () => {
                    this.newEvent();
                    this.updateUrl();
                }
            );
        } else {
            this.newEvent();
            this.updateUrl();
        }
    }

    getMaintenanceList() {
        this.maMaintenanceEvent
            .list()
            .then((list) => (this.maintenanceList = list));
    }

    newEvent() {
        this.selectedEvent = new this.maMaintenanceEvent();
    }

    eventSelected(event) {
        this.updateUrl();
    }

    updateUrl() {
        this.$state.params.xid =
            (this.selectedEvent &&
                !this.selectedEvent.isNew() &&
                this.selectedEvent.xid) ||
            null;
        this.$state.go('.', this.$state.params, {
            location: 'replace',
            notify: false,
        });
        this.getMaintenanceList();
    }

    eventUpdated(event) {
        this.updatedEvent = event;
        this.updateUrl();
    }

    eventDeleted(event) {
        this.deletedEvent = event;
        this.updateUrl();
    }
}

export default {
    bindings: {},
    require: {

    },
    controller: maintenanceEventsController,
    template: componentTemplate
};