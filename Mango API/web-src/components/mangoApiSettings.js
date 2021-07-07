/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './mangoApiSettings.html';
import angular from 'angular';

const $inject = Object.freeze(['$scope', 'maApiSettings']);

class mangoApiController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope, maApiSettings) {
        this.$scope = $scope;
        this.maApiSettings = maApiSettings;
   }

    $onInit() {
        this.getCorsSettings();
    }

    getCorsSettings() {
        this.maApiSettings.getCorsSettings()
            .then(corsSettings => {
               this.apiSettings = corsSettings; 
            }
        );
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: mangoApiController,
    template: componentTemplate
};