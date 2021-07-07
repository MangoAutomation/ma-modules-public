/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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
            this.dataStartTime = new Date(this.dataSource.dataStartTime);
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