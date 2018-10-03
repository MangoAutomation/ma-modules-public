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

const $inject = Object.freeze(['$rootScope', '$scope', '$http', 'maSqlConsole']);
class SqlConsoleController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($rootScope, $scope, $http, maSqlConsole) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;
        this.$http = $http;
        this.maSqlConsole = maSqlConsole;
    }
    
    $onInit() {}

    getTables() {
        this.maSqlConsole.getTables().then(response => {
            this.tableHeaders = response.headers
            this.tables = response.data;
        });
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