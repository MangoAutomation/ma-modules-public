/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './asciiFileDataPointEditor.html';

const $inject = Object.freeze(['maPoint']);

class asciiFileDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maPoint) {
        this.dataTypes = maPoint.dataTypes.filter(t => t.key !== 'IMAGE');
    }

    $onInit() {
        
    }

}

export default {
    bindings: {
        dataPoint: '<point'
    },
    require: {
        pointEditor: '^maDataPointEditor'
    },
    controller: asciiFileDataPointEditorController,
    template: componentTemplate
};