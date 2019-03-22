/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './asciiFileDataSourceEditor.html';

const $inject = Object.freeze(['$scope', 'maAsciiFile', 'maDialogHelper']);

class asciiFileDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope, maAsciiFile, maDialogHelper) {
        this.$scope = $scope;
        this.maAsciiFile = maAsciiFile;
        this.maDialogHelper = maDialogHelper;
    }

    $onInit() {
        this.asciiFile = new this.maAsciiFile();
    }

    validateFile() {
        this.asciiFile.validate(this.dataSource.xid, this.file).then(response => {
            console.log(response);
        });
    }

    validateFileExists() {
        this.asciiFile.validateFileExists(this.dataSource.filePath).then(response => {
            if (response.status === 200) {
                this.maDialogHelper.toast(['dsEdit.file.canRead']);
            }
        }, error => {
            if (error.status === 400) {
                this.maDialogHelper.errorToast(['dsEdit.file.pathIsNotCanonical']);
            }
            
            if (error.status === 403) {
                this.maDialogHelper.errorToast(['dsEdit.file.cannotRead']);
            }
        });
    }

}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {},
    controller: asciiFileDataSourceEditorController,
    template: componentTemplate
};