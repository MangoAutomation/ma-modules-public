/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './maintenanceEventsSetup.html';

/**
 * @ngdoc directive
 * @name ngMango.directive:maMaintenanceEventsSetup
 * @restrict E
 * @description Displays a form to create/edit maintenance events
 */

const $inject = Object.freeze(['$scope']);
class MaintenanceEventsSetupController {
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
    controller: MaintenanceEventsSetupController,
    bindings: {},
    // require: {
    //     ngModelCtrl: 'ngModel'
    // },
    designerInfo: {
        translation: 'maintenanceEvents.setup',
        icon: 'settings'
    }
};
