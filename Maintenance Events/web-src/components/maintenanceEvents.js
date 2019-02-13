/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './maintenanceEvents.html';

const $inject = Object.freeze(['$scope', '$mdMedia']);

class maintenanceEventsController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope, $mdMedia) {
        this.$scope = $scope;
        this.$mdMedia = $mdMedia;
   }

    $onInit() {
        
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: maintenanceEventsController,
    template: componentTemplate
};