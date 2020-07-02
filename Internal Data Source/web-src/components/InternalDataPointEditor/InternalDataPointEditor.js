/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './InternalDataPointEditor.html';

class InternalDataPointEditorController {

    static get $inject() { return ['maSystemStatus']; }
    static get $$ngIsClass() { return true; }

    constructor(maSystemStatus) {
        this.maSystemStatus = maSystemStatus;
    }

    queryMetrics(filter, dropDownOpen) {
        if (!this.queryPromise || dropDownOpen) {
            this.queryPromise = this.maSystemStatus.getInternalMetrics().then(response => {
                // store the response so we can access the name later
                return (this.internalMetrics = response.data);
            });
        }

        return this.queryPromise.then(internalMetrics => {
            return internalMetrics.filter(m => !filter || m.name.toLowerCase().includes(filter.toLowerCase()));
        });
    }

    inputChanged() {
        const metric = this.internalMetrics.find(m => m.id === this.dataPoint.pointLocator.monitorId);
        this.dataPoint.pointLocator.configurationDescription = metric.name;
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