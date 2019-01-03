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
        
        return actionData.test().then(
            resource => {
                if (resource.status === 'SUCCESS') {
                    this.maDialogHelper.toastOptions({
                        text: resource.result.logOutput,
                        hideDelay: 5000
                    }); 
                } else {
                    actionData.get().then(resource => {
                        this.maDialogHelper.toastOptions({
                            text: resource.result.logOutput,
                            hideDelay: 5000
                        });
                    });
                }
            }
        );
    }

    reset() {
        let actionData = angular.copy(this.log4JReset);

        return actionData.reset().then(
            resource => {
                this.resource = resource;
                
                if (this.resource.status === 'SUCCESS') {
                    this.maDialogHelper.toastOptions({
                        text: this.resource.result.logOutput,
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
                this.maDialogHelper.toastOptions({
                    text: this.resource.result.logOutput,
                    hideDelay: 5000
                }); 
            }).finally(() => {
                delete this.resource;
            }
        );
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: log4JResetController,
    template: componentTemplate
};