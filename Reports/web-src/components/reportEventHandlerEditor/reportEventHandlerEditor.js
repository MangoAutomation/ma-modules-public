/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import reportEventHandlerEditor from './reportEventHandlerEditor.html';

class ReportEventHandlerEditorController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return ['maDataPointTags']; }
    
    $onChanges(changes) {
        if (changes.eventHandler) {
            
        }
    }
    
    updateIds() {
        this.eventHandler.activeReportId = this.activeReport ? this.activeReport.id : null;
        this.eventHandler.inactiveReportId = this.inactiveReport ? this.inactiveReport.id : null;
    }
}

export default {
    template: reportEventHandlerEditor,
    controller: ReportEventHandlerEditorController,
    bindings: {
        eventHandler: '<'
    }
};