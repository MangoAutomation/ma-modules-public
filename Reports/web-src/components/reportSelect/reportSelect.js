/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import reportSelectTemplate from './reportSelect.html';
import './reportSelect.css';

class ReportSelectController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return ['maReport', '$attrs']; }
    
    constructor(maReport, attrs) {
        this.maReport = maReport;
        this.attrs = attrs;
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
        noFloat: '<?',
        showClearOption: '<?',
        required: '@?', // there's some funky handling going on in Angular where this comes back as a boolean
        name: '@?'
    },
    require: {
        ngModelCtrl: 'ngModel',
        ngFormCtrl: '^?form'
    },
    transclude: {
        label: '?maLabel'
    },
    template: reportSelectTemplate,
    controller: ReportSelectController
};
