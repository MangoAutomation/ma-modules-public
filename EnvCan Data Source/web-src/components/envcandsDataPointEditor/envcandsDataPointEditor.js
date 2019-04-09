/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './envcandsDataPointEditor.html';

const $inject = Object.freeze([]);

class envcandsDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor() {
        
    }

    $onInit() {
        
    }

}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {},
    controller: envcandsDataPointEditorController,
    template: componentTemplate
};