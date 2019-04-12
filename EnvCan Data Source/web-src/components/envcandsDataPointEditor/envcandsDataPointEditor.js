/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author 
 */

import componentTemplate from './envcandsDataPointEditor.html';

const $inject = Object.freeze([]);

class envcandsDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor() {}

    $onInit() {
        
    }

    updateDataType() {
        if (this.dataPoint.pointLocator.attribute === 'WEATHER') {
            this.dataPoint.dataType = 'ALPHANUMERIC';
        } else {
            this.dataPoint.dataType = 'NUMERIC';
        }
    }

}

export default {
    bindings: {
        dataPoint: '<point'
    },
    require: {
        pointEditor: '^maDataPointEditor'
    },
    controller: envcandsDataPointEditorController,
    template: componentTemplate
};