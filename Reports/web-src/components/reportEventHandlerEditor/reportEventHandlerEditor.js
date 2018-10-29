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
        if (!this.eventHandler) {
            this.activeReport = null;
            this.inactiveReport = null;
            return;
        }
        
        if (this.eventHandler.activeReportId >= 0) {
            this.maReport.getById(this.eventHandler.activeReportId).then(report => {
                this.activeReport = report;
            });
        } else {
            this.activeReport = null;
        }
        
        if (this.eventHandler.inactiveReportId >= 0) {
            this.maReport.getById(this.eventHandler.inactiveReportId).then(report => {
                this.inactiveReport = report;
            });
        } else {
            this.inactiveReport = null;
        }
    }
    
    updateIds() {
        this.eventHandler.activeReportId = this.activeReport ? this.activeReport.id : -1;
        this.eventHandler.inactiveReportId = this.inactiveReport ? this.inactiveReport.id : -1;
    }
}

export default {
    template: reportEventHandlerEditor,
    controller: ReportEventHandlerEditorController,
    bindings: {
        eventHandler: '<'
    }
};