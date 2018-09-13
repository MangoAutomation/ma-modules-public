/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './maintenanceEventsSetup.html';
import angular from 'angular';

/**
 * @ngdoc directive
 * @name ngMango.directive:maMaintenanceEventsSetup
 * @restrict E
 * @description Displays a form to create/edit maintenance events
 */


 const $inject = Object.freeze(['$rootScope', '$scope', 'maDialogHelper', 'maDataSource', 'maPoint', '$http']);
class MaintenanceEventsSetupController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($rootScope, $scope, maDialogHelper, maDataSource, maPoint, $http) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;
        this.maDialogHelper = maDialogHelper;
        this.maDataSource = maDataSource;
        this.maPoint = maPoint;
        this.$http = $http;

        this.dataSources = [];
        this.dataPoints = [];
    }
    
    $onInit() {
        this.ngModelCtrl.$render = () => this.render();

        this.$scope.$watch('$ctrl.selectedEvent', (newValues) => {
            if (this.selectedEvent) {
                this.getDataSourcesByIds(this.selectedEvent.dataSources);
                this.getDataPointsByIds(this.selectedEvent.dataPoints);

                this.getMaintenanceEventsByXid(this.selectedEvent.xid).then(response => {
                    const items = response.data.items;
                    if (items.length) {
                        return this.activeEvent = items[items.length - 1].active;
                    }
                });
            }
        });

        this.$scope.$on('meNew', (event) => {
            this.dataSources = [];
            this.dataPoints = [];
        });
    }

    getMaintenanceEventsByXid(xid) {
        return this.$http.post('/rest/v1/events/module-defined-query', {
            queryType: "MAINTENANCE_EVENTS_BY_MAINTENANCE_EVENT_RQL",
            parameters: {
                rql: "xid=" + xid
            }
        });
    }

    getDataSourcesByIds(ids) {
        if (!ids || ids.length == 0) return;

        let rqlQuery = 'in(xid,' + ids.join(',') +')';

        this.maDataSource.rql({rqlQuery}).$promise.then(dataSources => {
            this.dataSources = dataSources;
        })
    }

    getDataPointsByIds(ids) {
        if (!ids || ids.length == 0) return;

        let rqlQuery = 'in(xid,' + ids.join(',') +')';

        this.maPoint.rql({rqlQuery}).$promise.then(points => {
            this.dataPoints = points;
        })
    }
    
    setViewValue() {
        this.ngModelCtrl.$setViewValue(this.selectedEvent);
    }

    render() {
        this.selectedEvent = this.ngModelCtrl.$viewValue;
    }

    toggleEvent() {
        this.selectedEvent.toggleActive().then(active => {
            this.activeEvent = active;
        });
    }

    addDataSource() {
        if (this.dataSources.filter(t => t.xid === this.selectedDataSource.xid).length == 0) {
            this.dataSources.push(this.selectedDataSource); 
        }
        this.selectedDataSource = null;
    }

    addDataPoint() {
        if (this.dataPoints.filter(t => t.xid === this.selectedDataPoint.xid).length == 0) {
            this.dataPoints.push(this.selectedDataPoint); 
        }
        this.selectedDataPoint = null;
    }

    save() {
        this.selectedEvent.dataSources = this.getXids(this.dataSources);
        this.selectedEvent.dataPoints = this.getXids(this.dataPoints);

        if (!this.form.$valid) {
            this.maDialogHelper.toastOptions({
                textTr: 'maintenanceEvents.invalidForm',
                classes: 'md-warn',
                hideDelay: 3000
            });
            return;
        }

        this.selectedEvent.save().then(() => {
            
            this.dataSources = [];
            this.dataPoints = [];
            this.selectedEvent = null;
            this.maDialogHelper.toastOptions({textTr: ['maintenanceEvents.meSaved']});
            this.$rootScope.$broadcast('meUpdated', true);
            this.dataSources = [];
            this.dataPoints = [];
            this.validationMessages = null;
            this.form.$setPristine();
            this.form.$setUntouched();

        }, (error) => {
            this.validationMessages = error.data.result.messages;

            this.maDialogHelper.toastOptions({
                textTr: ['maintenanceEvents.notSaved', error.mangoStatusText],
                classes: 'md-warn',
                hideDelay: 5000
            });
        });
    }

    delete() {
        this.maDialogHelper.confirm(event, ['maintenanceEvents.confirmDelete']).then(() => {
            this.selectedEvent.delete().then(() => {
                
                this.dataSources = [];
                this.dataPoints = [];
                this.selectedEvent = null;
                this.maDialogHelper.toastOptions({textTr: ['maintenanceEvents.meDeleted']});
                this.$rootScope.$broadcast('meUpdated', true);
                this.dataSources = [];
                this.dataPoints = [];
                this.validationMessages = null;

            }, (error) => {

                this.maDialogHelper.toastOptions({
                    textTr: ['maintenanceEvents.meNotDeleted'],
                    classes: 'md-warn',
                    hideDelay: 5000
                });

            });
        }, angular.noop);  
    }

    getXids (dataArray) {
        let ids = [];

        dataArray.map(item => {
            ids.push(item.xid);
        });

        return ids
    }

    checkError(property) {
        if (!this.validationMessages) {
            return null;
        }

        return this.validationMessages.filter((item) => {
            return item.property === property;
        }, property)[0];
    }
}

export default {
    template: componentTemplate,
    controller: MaintenanceEventsSetupController,
    bindings: {},
    require: {
        ngModelCtrl: 'ngModel'
    },
    designerInfo: {
        translation: 'maintenanceEvents.setup',
        icon: 'settings'
    }
};
