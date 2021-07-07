/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import componentTemplate from './serialDataSourceEditor.html';

const $inject = Object.freeze(['maDialogHelper', 'maSerialDsEditor']);

class serialDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maDialogHelper, maSerialDsEditor) {
        this.maDialogHelper = maDialogHelper;
        this.maSerialDsEditor = maSerialDsEditor;
        this.testValues = null;
    }

    $onInit() {
        this.logFilePath = `/serial-data-source/log-file-path/${encodeURIComponent(this.dataSource.xid)}`
    }

    $onChanges() {
        this.logFilePath = `/serial-data-source/log-file-path/${encodeURIComponent(this.dataSource.xid)}`
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

        this.maSerialDsEditor.validateString(this.dataSource.xid, data).then(response => {
            this.testResponse = {
                success: [],
                errors: []
            }
            response.forEach(res => {
                res.pointName == null 
                    ? this.testResponse.errors.push(res) 
                    : this.testResponse.success.push(res)
            });

            if (this.testResponse.errors.length > 0) {
            this.maDialogHelper.errorToast(['serial.test.noTerminator']);
                
            }
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