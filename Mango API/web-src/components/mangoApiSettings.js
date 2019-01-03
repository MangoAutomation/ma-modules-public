/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
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
               console.log(this.apiSettings);
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