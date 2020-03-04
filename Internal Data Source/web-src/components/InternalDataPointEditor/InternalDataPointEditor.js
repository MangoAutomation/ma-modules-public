/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './InternalDataPointEditor.html';

const $inject = Object.freeze(['$scope', 'maSystemStatus', 'maTranslate']);

class InternalDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope, maSystemStatus, Translate) {
        this.$scope = $scope;
        this.maSystemStatus = maSystemStatus;
        this.Translate = Translate;
   }

    $onInit() {
        this.label = this.Translate.trSync('dsEdit.internal.attribute');
        this.getInternalMetrics();
        if (this.dataPoint.pointLocator.monitorId) {
            this.selectedMonitor = this.dataPoint.name
        }
    }

    getInternalMetrics() {
        this.maSystemStatus.getInternalMetrics().then(response => {
            this.monitorIds = response.data;
        });
    }

    inputChanged(monitor) {
        if (monitor) {
            this.dataPoint.pointLocator.monitorId = monitor.id
        }
    }
}

export default {
    bindings: {
        dataPoint: '<point'
    },
    require: {},
    controller: InternalDataPointEditorController,
    template: componentTemplate
};