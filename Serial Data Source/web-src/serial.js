/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import serialDataSourceEditor from './components/serialDataSourceEditor/serialDataSourceEditor';
import serialDataPointEditor from './components/serialDataPointEditor/serialDataPointEditor';
import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';

const serialSourceModule = angular.module('maSerialDataSource', ['maUiApp'])
.component('maSerialDataSourceEditor', serialDataSourceEditor)
.component('maSerialDataPointEditor', serialDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'SERIAL',
        description: 'dsEdit.serial',
        template: `<ma-serial-data-source-editor data-source="ctrl.dataSource"></ma-serial-data-source-editor>`,
        polling: false,
        defaultDataSource: {},
        defaultDataPoint: {
            dataSourceTypeName: 'SERIAL',
            enabled: false,
            pointLocator: {}
        },
        bulkEditorColumns: []
    });

    maPointProvider.registerType({
        type: 'SERIAL',
        description: 'dsEdit.serialPoint',
        template: `<ma-serial-data-point-editor data-point="ctrl.dataPoint"></ma-serial-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.help.serialDataSource',
            url: '/serial-data-source',
            menuTr: 'dsEdit.serial',
            template: dsHelpTemplate
        },
        {
            name: 'ui.help.serialDataPoint',
            url: '/serial-data-point',
            menuTr: 'dsEdit.serialPoint',
            template: dpHelpTemplate
        }
    ]);
}]);

export default serialSourceModule;