/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './envcandsDataSourceEditor.html';

const $inject = Object.freeze([]);

class envcandsDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor() {
        
    }

    $onInit() {
        if (!this.dataSource.isNew()) {
            this.dataSource.dataStartTime = new Date(this.dataSource.dataStartTime);
        }
    }

}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {},
    controller: envcandsDataSourceEditorController,
    template: componentTemplate
};