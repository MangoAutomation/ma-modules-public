/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './serialDataPointEditor.html';

const $inject = Object.freeze(['maPoint']);

class serialDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maPoint) {
        this.dataTypes = maPoint.dataTypes;
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
    controller: serialDataPointEditorController,
    template: componentTemplate
};