/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import reportSelectTemplate from './reportSelect.html';

class ReportSelectController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return ['maReport']; }
    
    constructor(maReport) {
        this.maReport = maReport;
    }
    
    $onInit() {
        this.ngModelCtrl.$render = () => {
            this.selected = this.ngModelCtrl.$viewValue;
        };
        
        this.queryPromise = this.maReport.query().then(items => {
            this.items = items;
        });
    }
    
    $onChanges(changes) {
    }

    inputChanged() {
        this.ngModelCtrl.$setViewValue(this.selected);
    }
}

export default {
    bindings: {
        selectedText: '<?',
        noFloat: '<?'
    },
    require: {
        ngModelCtrl: 'ngModel'
    },
    transclude: {
        label: '?maLabel'
    },
    template: reportSelectTemplate,
    controller: ReportSelectController
};
