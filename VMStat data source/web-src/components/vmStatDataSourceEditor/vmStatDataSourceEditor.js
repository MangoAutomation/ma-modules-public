/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './vmStatDataSourceEditor.html';

const $inject = Object.freeze(['$scope']);

class vmStatDataSourceEditorController {

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
    controller: vmStatDataSourceEditorController,
    template: componentTemplate
};