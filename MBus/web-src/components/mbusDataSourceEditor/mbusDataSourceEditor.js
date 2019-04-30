/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './mbusDataSourceEditor.html';

const $inject = Object.freeze([]);

class mbusDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor() {
        
    }

    $onInit() {
        this.searchTool = {
            addressingType: 'primaryAddressing',
            primaryAddressing: {
                from: '00',
                to: 'FA'
            }
        };
    }

}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {
        dsEditor: '^maDataSourceEditor'
    },
    controller: mbusDataSourceEditorController,
    template: componentTemplate
};