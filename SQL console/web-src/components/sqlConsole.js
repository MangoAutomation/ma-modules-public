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
            limit: 15,
            page: 1
        };
    }
    
    $onInit() {}

    getTables() {
        this.csvUrl = null;
        
        this.disableButtons = true;
        this.gettingTables = true;
        
        this.maSqlConsole.getTables().then(response => {
            this.tableHeaders = response.headers;
            this.rows = response.data;
        }).finally(() => {
            delete this.disableButtons;
            delete this.gettingTables;
        });
    }

    query(queryString, isSelection) {
        this.csvUrl = null;

        this.disableButtons = true;
        if (isSelection) {
            this.queryingSelection = true;
        } else {
            this.querying = true;
        }
        
        this.maSqlConsole.query(queryString).then(response => {
            this.tableHeaders = response.headers;
            this.rows = response.data;
            this.csvUrl = this.maSqlConsole.queryCsvUrl(queryString);
        }, error => {
            this.maDialogHelper.toastOptions({
                text: error.data.cause,
                classes: 'md-warn',
                hideDelay: 10000
            });
        }).finally(() => {
            delete this.disableButtons;
            delete this.querying;
            delete this.queryingSelection;
        });
    }

    update(queryString, isSelection) {
        this.disableButtons = true;
        if (isSelection) {
            this.updatingSelection = true;
        } else {
            this.updating = true;
        }
        
        this.maSqlConsole.update(queryString).then(response => {
            this.maDialogHelper.toastOptions({
                textTr: ['sql.rowsUpdated', response],
                hideDelay: 5000
            });
            
            if (this.queryString && this.queryAfterUpdate) {
                this.query(this.queryString);
            }
        }, error => {
            this.maDialogHelper.toastOptions({
                text: error.data.cause,
                classes: 'md-warn',
                hideDelay: 10000
            });
        }).finally(() => {
            delete this.disableButtons;
            delete this.updating;
            delete this.updatingSelection;
        });
    }

    runSelectedQuery(queryString) {
        if (queryString.trim() === '') {
            this.maDialogHelper.toastOptions({
                textTr: 'sql.emptySelection',
                classes: 'md-warn',
                hideDelay: 5000
            });
        }

        this.query(queryString, true);
    }

    runSelectedUpdate(queryString) {
        if (queryString.trim() === '') {
            this.maDialogHelper.toastOptions({
                textTr: 'sql.emptySelection',
                classes: 'md-warn',
                hideDelay: 5000
            });
        }

        this.update(queryString, true);
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