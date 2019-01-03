/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './log4JReset.html';
import angular from 'angular';

const $inject = Object.freeze(['$scope']);

class log4JResetController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope) {
        this.$scope = $scope;
   }

    onInit() {
        
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: log4JResetController,
    template: componentTemplate
};