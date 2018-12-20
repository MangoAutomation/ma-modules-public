/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './log4JReset.html';
import angular from 'angular';

const $inject = Object.freeze(['$scope', 'maLog4JReset', 'maDialogHelper']);

class log4JResetController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope, maLog4JReset, maDialogHelper) {
        this.$scope = $scope;
        this.maLog4JReset = maLog4JReset;
        this.maDialogHelper = maDialogHelper;
   }

    $onInit() {
       this.log4JReset = new this.maLog4JReset();
    }

    test() {
        let actionData = angular.copy(this.log4JReset);
        
        return actionData.start().then(
            resource => {
                this.resource = resource;
                console.log('resource: ', resource);
                
                if (this.resource.finished) {
                    this.maDialogHelper.toastOptions({
                        textTr: ['log4JReset.settings.testFinished'],
                        hideDelay: 5000
                    }); 
                }
            }, error => {
                if (error.status === 422) {
                    this.validationMessages = error.data.result.messages;
                }

                this.maDialogHelper.toastOptions({
                    textTr: ['log4JReset.settings.testError', error.mangoStatusText],
                    classes: 'md-warn',
                    hideDelay: 5000
                });

            }, progress => {
                
                this.resource = progress;
                this.warnings = this.resource.result.warnings;
                console.log('Progress: ', progress);

            }).finally(() => {
                delete this.resource;
            }
        );
    }

    reset() {
        let actionData = angular.copy(this.log4JReset);

        return actionData.reset().then(response => {
            console.log(response);
        });
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: log4JResetController,
    template: componentTemplate
};