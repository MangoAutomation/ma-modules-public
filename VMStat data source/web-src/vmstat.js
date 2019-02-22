/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import vmStatDataSourceEditor from './components/vmStatDataSourceEditor/vmStatDataSourceEditor';
import vmStatDataPointEditor from './components/vmStatDataPointEditor/vmStatDataPointEditor';
import dsHelpTemplate from './dsHelp.html';
import dpHelpTemplate from './dpHelp.html';

const vmStatDataSourceModule = angular.module('maVmStatDataSource', ['maUiApp'])
.component('maVmStatDataSourceEditor', vmStatDataSourceEditor)
.component('maVmStatDataPointEditor', vmStatDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'SCRIPTING',
        description: 'dsEdit.vmStat',
        template: `<ma-scripting-data-source-editor data-source="$ctrl.dataSource"></ma-scripting-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            connectionDescription: '* * * * * ?',
            context: [],
            cronPattern: '* * * * * ?',
            description: 'Scripting',
            descriptionKey: 'dsEdit.vmStat',
            editPermission: [],
            enabled: true,
            eventAlarmLevels: [],
            executionDelaySeconds: 0,
            historicalSetting: false,
            logCount: 5,
            logLevel: 'NONE',
            logSize: 1,
            modelType: 'SCRIPTING',
            purgeSettings: {override: true, frequency: {periods: 1, type: 'YEARS'}},
            script: '',
            scriptPermissions: ['superadmin'],
            updateEvent: 'UPDATE',
            quantize: false,
            useCron: false
        },
        defaultDataPoint: {
            dataSourceTypeName: 'SCRIPTING',
            pointLocator: {
                dataType: 'BINARY',
                modelType: 'PL.SCRIPTING',
                relinquishable: false,
                settable: true,
                varName: '',
            }
        },
        bulkEditorColumns: []
    });

    maPointProvider.registerType({
        type: 'SCRIPTING',
        description: 'dsEdit.vmStatPoint',
        template: `<ma-scripting-data-point-editor data-point="$ctrl.dataPoint"></ma-scripting-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.help.vmStatDataSource',
            url: '/vm-stat-data-source',
            menuTr: 'dsEdit.vmStat',
            template: dsHelpTemplate
        },
        {
            name: 'ui.help.vmStatDataPoint',
            url: '/vm-stat-data-point',
            menuTr: 'dsEdit.vmStatPoint',
            template: dpHelpTemplate
        }
    ]);
}]);

export default vmStatDataSourceModule;