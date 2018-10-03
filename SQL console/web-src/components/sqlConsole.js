/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './sqlConsole.html';

/**
 * @ngdoc directive
 * @name ngMango.directive:maMaintenanceEventsList
 * @restrict E
 * @description Displays a list of maintenance events
 */

const $inject = Object.freeze(['$rootScope', '$scope']);
class SqlConsoleController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($rootScope, $scope) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;

    }
    
    $onInit() {
        
    }

    
}

export default {
    template: componentTemplate,
    controller: SqlConsoleController,
    bindings: {},
    require: {},
    designerInfo: {
        translation: 'maintenanceEvents.list',
        icon: 'list'
    }
};