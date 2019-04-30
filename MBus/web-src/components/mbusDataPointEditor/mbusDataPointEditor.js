/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './mbusDataPointEditor.html';

const $inject = Object.freeze(['maPoint']);

class mbusDataPointEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maPoint) {
        this.dataTypes = maPoint.dataTypes.filter(t => t.key !== 'IMAGE');
    }

    $onInit() {
        
    }

    difCodeChange() {
        const numericDataTypes = [
            '12 digit BCD',
            '16 bit integer',
            '24 bit integer',
            '2 digit BCD',
            '32 bit integer',
            '32 bit real',
            '48 bit integer',
            '4 digit BCD',
            '64 bit integer',
            '6 digit BCD',
            '8 bit integer',
            '8 digit BCD',
        ];

        if (numericDataTypes.includes(this.dataPoint.pointLocator.difCode)) {
            this.dataPoint.dataType = 'NUMERIC';
        } else if (this.dataPoint.pointLocator.difCode === 'variable length') {
            this.dataPoint.dataType = 'ALPHANUMERIC';
        } else {
            this.dataPoint.dataType = 'UNKNOWN';
        }
    }

}

export default {
    bindings: {
        dataPoint: '<point'
    },
    require: {
        pointEditor: '^maDataPointEditor'
    },
    controller: mbusDataPointEditorController,
    template: componentTemplate
};