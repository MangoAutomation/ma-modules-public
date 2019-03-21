/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './asciiFileDataSourceEditor.html';

const $inject = Object.freeze(['$scope', 'maAsciiFile']);

class asciiFileDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor($scope, maAsciiFile) {
        this.$scope = $scope;
        this.maAsciiFile = maAsciiFile;
    }

    $onInit() {
        this.asciiFile = new this.maAsciiFile();
    }

    validateFile(){
        this.asciiFile.validate(this.dataSource.xid, this.file);
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