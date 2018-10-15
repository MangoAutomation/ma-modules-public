/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './sqlConsole.html';
import './sqlConsole.css';

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

        this.queryOpts = {
            limit: 10,
            page: 1
        };
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

    query(queryString) {
        this.maSqlConsole.query(queryString).then(
            response => {
                this.tableHeaders = response.headers
                this.rows = response.data;
            }, error => {
                this.maDialogHelper.toastOptions({
                    text: error.data.cause,
                    classes: 'md-warn',
                    hideDelay: 5000
                });
            }
        );
    }

    update(queryString) {
        this.maSqlConsole.update(queryString).then(
            response => {
                this.maDialogHelper.toastOptions({
                    textTr: ['sql.rowsUpdated', response],
                    hideDelay: 3000
                });
            }, error => {
                this.maDialogHelper.toastOptions({
                    text: error.data.cause,
                    classes: 'md-warn',
                    hideDelay: 5000
                });
            }
        );
    }

    runSelectedQuery(queryString) {
        if (this.selection.trim() === "") {
            this.maDialogHelper.toastOptions({
                textTr: 'sql.emptySelection',
                classes: 'md-warn',
                hideDelay: 3000
            });
        }

        if (this.selection.includes("SELECT")) {
            this.query(queryString);
        } else {
            this.update(queryString);
        }
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