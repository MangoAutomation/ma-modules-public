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

const $inject = Object.freeze(['$rootScope', '$scope', 'maDialogHelper']);
class MaintenanceEventsSetupController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($rootScope, $scope, maDialogHelper) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;
        this.maDialogHelper = maDialogHelper;

        this.dataSources = [];
        this.dataPoints = [];
    }
    
    $onInit() {
        this.ngModelCtrl.$render = () => this.render();
    }
    
    setViewValue() {
        this.ngModelCtrl.$setViewValue(this.selectedEvent);
    }

    render() {
        this.selectedEvent = this.ngModelCtrl.$viewValue;
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
        this.selectedEvent.dataSources = this.getIds(this.dataSources);
        this.selectedEvent.dataPoints = this.getIds(this.dataPoints);

        if (!this.form.$valid) {
            this.maDialogHelper.toastOptions({
                textTr: 'maintenanceEvents.invalidForm.invalidForm',
                classes: 'md-warn',
                hideDelay: 3000
            });
            return;
        }

        this.selectedEvent.save().then(() => {
            
            this.selectedEvent = null;
            this.maDialogHelper.toastOptions({textTr: ['maintenanceEvents.meSaved']});
            this.$rootScope.$broadcast('meUpdated', true);

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
                
                this.selectedEvent = null;
                this.maDialogHelper.toastOptions({textTr: ['maintenanceEvents.meDeleted']});
                this.$rootScope.$broadcast('meUpdated', true);

            }, (error) => {

                this.maDialogHelper.toastOptions({
                    textTr: ['maintenanceEvents.meNotDeleted'],
                    classes: 'md-warn',
                    hideDelay: 5000
                });

            });
        }, angular.noop);  
    }

    getIds (dataArray) {
        let ids = [];

        dataArray.map(item => {
            ids.push(item.id);
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
