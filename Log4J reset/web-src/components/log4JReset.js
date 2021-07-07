/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './log4JReset.html';

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

        this.wsConnection = this.maLog4JReset.subscribe((event, item) => {
            if (item.status === 'SUCCESS') {
                this.maDialogHelper.toastOptions({
                    text: item.result.logOutput,
                    hideDelay: 5000
                }); 
            }
        });
    }

    test() {
        const actionData = this.log4JReset.copy();
        actionData.test();
    }

    reset() {
        const actionData = this.log4JReset.copy();
        return actionData.reset();
    }

}

export default {
    bindings: {},
    require: {

    },
    controller: log4JResetController,
    template: componentTemplate
};