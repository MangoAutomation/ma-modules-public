/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './serialDataSourceEditor.html';

const $inject = Object.freeze(['maSerialDataSource']);

class serialDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maSerialDataSource) {
        this.maSerialDataSource = maSerialDataSource;
   }

    $onInit() {
        this.getSerialPorts();
    }

    getSerialPorts() {
        return this.maSerialDataSource.getSerialPorts().then(serialPorts => {
            this.serialPorts = serialPorts;
        });
    }

}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {},
    controller: serialDataSourceEditorController,
    template: componentTemplate
};