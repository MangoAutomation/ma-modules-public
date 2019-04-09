/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import envcandsDataSourceEditor from './components/envcandsDataSourceEditor/envcandsDataSourceEditor';
import envcandsDataPointEditor from './components/envcandsDataPointEditor/envcandsDataPointEditor';
import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';

const envcandsSourceModule = angular.module('maEnvcandsDataSource', ['maUiApp'])
.component('maEnvcandsDataSourceEditor', envcandsDataSourceEditor)
.component('maEnvcandsDataPointEditor', envcandsDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'EnvCan',
        description: 'dsEdit.envcands',
        template: `<ma-env-can-data-source-editor data-source="$ctrl.dataSource"></ma-env-can-data-source-editor>`,
        polling: false,
        defaultDataSource: {},
        defaultDataPoint: {
            dataSourceTypeName: 'EnvCan',
            enabled: false,
            pointLocator: {}
        },
        bulkEditorColumns: []
    });

    maPointProvider.registerType({
        type: 'EnvCan',
        description: 'dsEdit.envcandsPoint',
        template: `<ma-env-can-data-point-editor data-point="$ctrl.dataPoint"></ma-env-can-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.help.envcandsDataSource',
            url: '/env-can-data-source',
            menuTr: 'dsEdit.envcands',
            template: dsHelpTemplate
        },
        {
            name: 'ui.help.envcandsDataPoint',
            url: '/env-can-data-point',
            menuTr: 'dsEdit.envcandsPoint',
            template: dpHelpTemplate
        }
    ]);
}]);

export default envcandsSourceModule;