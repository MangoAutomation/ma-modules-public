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

const $inject = Object.freeze(['$rootScope', '$scope', '$http', 'maSqlConsole', 'maDialogHelper']);
class SqlConsoleController {
    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }
    
    constructor($rootScope, $scope, $http, maSqlConsole, maDialogHelper) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;
        this.$http = $http;
        this.maSqlConsole = maSqlConsole;
        this.maDialogHelper = maDialogHelper;
    }
    
    $onInit() {}

    getTables() {
        this.maSqlConsole.getTables().then(
            response => {
                this.tableHeaders = response.headers
                this.rows = response.data;
            }
        );
    }

    query() {
        this.maSqlConsole.query(this.queryString).then(
            response => {
                this.tableHeaders = response.headers
                this.rows = response.data;
            }
        );
    }

    update() {
        this.maSqlConsole.update(this.queryString).then(
            response => {
                this.maDialogHelper.toastOptions({
                    textTr: ['sql.rowsUpdated', response],
                    hideDelay: 3000
                });
            }
        );
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