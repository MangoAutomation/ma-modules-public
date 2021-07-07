/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './vmStatDataPointEditor.html';

const $inject = Object.freeze(['$scope']);

class vmStatDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope) {
        this.$scope = $scope;
   }

    $onInit() {
        
    }

}

export default {
    bindings: {
        dataPoint: '<point'
    },
    require: {},
    controller: vmStatDataPointEditorController,
    template: componentTemplate
};