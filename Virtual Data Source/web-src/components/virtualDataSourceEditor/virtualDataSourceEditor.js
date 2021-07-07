/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import virtualDataSourceEditor from './virtualDataSourceEditor.html';

class VirtualDataSourceEditorController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return []; }
    
    constructor() {
    }
    
    $onChanges(changes) {
    }
}

export default {
    template: virtualDataSourceEditor,
    controller: VirtualDataSourceEditorController,
    bindings: {
        dataSource: '<source'
    }
};