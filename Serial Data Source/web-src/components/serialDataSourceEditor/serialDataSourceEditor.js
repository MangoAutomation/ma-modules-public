/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './serialDataSourceEditor.html';

const $inject = Object.freeze(['maDialogHelper']);

class serialDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maDialogHelper) {
        this.maDialogHelper = maDialogHelper;
   }

    $onInit() {}
    
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
}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {},
    controller: serialDataSourceEditorController,
    template: componentTemplate
};