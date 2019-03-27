/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './serialDataSourceEditor.html';

const $inject = Object.freeze(['maSerialDataSource', 'maDialogHelper']);

class serialDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maSerialDataSource, maDialogHelper) {
        this.maSerialDataSource = maSerialDataSource;
        this.maDialogHelper = maDialogHelper;
   }

    $onInit() {
        this.getSerialPorts();
    }
    
    validateString() {
        const data = {
            hex: this.dataSource.hex,
            message: this.string,
            messageRegex: this.dataSource.messageRegex,
            messageTerminator: this.dataSource.messageTerminator,
            pointIdentifierIndex: this.dataSource.pointIdentifierIndex,
            useTerminator: this.dataSource.useTerminator
        };

        this.testValues = null;

        this.maSerialDataSource.validateString(this.dataSource.xid, data).then(response => {
            this.testValues = response;
        }, error => {
            this.maDialogHelper.errorToast(['dsEdit.serial.testStringError', error.data.localizedMessage]);
        });
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