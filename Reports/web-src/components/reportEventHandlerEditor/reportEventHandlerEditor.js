/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import reportEventHandlerEditor from './reportEventHandlerEditor.html';

class ReportEventHandlerEditorController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return ['maReport']; }
    
    constructor(maReport) {
        this.maReport = maReport;
    }
    
    $onChanges(changes) {
        if (changes.eventHandler) {
            this.getReports();
        }
    }
    
    getReports() {
        if (!this.eventHandler || this.eventHandler.handlerType !== 'REPORT') {
            this.activeReport = null;
            this.inactiveReport = null;
            return;
        }
        
        if (this.eventHandler.activeReportXid) {
            this.maReport.get(this.eventHandler.activeReportXid).then(report => {
                this.activeReport = report;
            });
        } else {
            this.activeReport = null;
        }
        
        if (this.eventHandler.inactiveReportXid) {
            this.maReport.get(this.eventHandler.inactiveReportXid).then(report => {
                this.inactiveReport = report;
            });
        } else {
            this.inactiveReport = null;
        }
    }
    
    updateIds() {
        this.eventHandler.activeReportXid = this.activeReport ? this.activeReport.xid : -1;
        this.eventHandler.inactiveReportXid = this.inactiveReport ? this.inactiveReport.xid : -1;
    }
}

export default {
    template: reportEventHandlerEditor,
    controller: ReportEventHandlerEditorController,
    bindings: {
        eventHandler: '<'
    }
};