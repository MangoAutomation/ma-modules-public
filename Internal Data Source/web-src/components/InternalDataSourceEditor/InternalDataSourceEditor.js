/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './InternalDataSourceEditor.html';

const $inject = Object.freeze(['$scope']);

class InternalDataSourceEditorController {

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
        dataSource: '<source'
    },
    require: {},
    controller: InternalDataSourceEditorController,
    template: componentTemplate
};