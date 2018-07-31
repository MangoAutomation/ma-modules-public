/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './maintenanceEventsList.html';

/**
 * @ngdoc directive
 * @name ngMango.directive:maMaintenanceEventsList
 * @restrict E
 * @description Displays a list of maintenance
 */

const $inject = Object.freeze(['$scope']);
class MaintenanceEventsListController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($scope) {
        this.$scope = $scope;
    }
    
    $onInit() {

    }
    
    $onChanges(changes) {
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
