/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import virtualDataPointEditor from './virtualDataPointEditor.html';

class VirtualDataPointEditorController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return ['maPoint']; }
    
    constructor(maPoint) {
        this.dataTypes = maPoint.dataTypes.filter(t => t.key !== 'IMAGE');
    }
    
    $onChanges(changes) {
    }
}

export default {
    template: virtualDataPointEditor,
    controller: VirtualDataPointEditorController,
    bindings: {
        dataPoint: '<point'
    }
};