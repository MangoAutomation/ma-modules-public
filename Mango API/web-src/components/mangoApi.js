/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './mangoApi.html';
import angular from 'angular';

const $inject = Object.freeze(['$scope']);

class mangoApiController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope) {
        this.$scope = $scope;
   }

    $onInit() {
        
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: mangoApiController,
    template: componentTemplate
};