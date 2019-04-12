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
        template: `<ma-envcands-data-source-editor data-source="$ctrl.dataSource"></ma-envcands-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            dataStartTime: '',
            descriptionKey: 'envcands.desc',
            editPermission: [],
            enabled: false,
            eventAlarmLevels: [
                {
                    descriptionKey: 'event.ds.pollAborted',
                    duplicateHandling: 'IGNORE',
                    eventType: 'POLL_ABORTED',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.ds.dataSource',
                    duplicateHandling: 'IGNORE_SAME_MESSAGE',
                    eventType: 'DATA_RETRIEVAL_FAILURE_EVENT',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.ds.dataParse',
                    duplicateHandling: 'IGNORE',
                    eventType: 'PARSE_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'envcands.event.noTemperatureData',
                    duplicateHandling: 'IGNORE_SAME_MESSAGE',
                    eventType: 'NO_DATA_RETRIEVED_EVENT',
                    level: 'INFORMATION'
                }
            ],
            modelType: 'EnvCan',
            name: '',
            pollPeriod: {periods: 1, type: 'HOURS'},
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}},
            quantize: false,
            stationId: 1,
            useCron: false,
        },
        defaultDataPoint: {
            dataSourceTypeName: 'EnvCan',
            enabled: false,
            pointLocator: {
                attribute: 'TEMP',
                dataType: 'NUMERIC',
                modelType: 'PL.ENV_CAN',
                relinquishable: false,
                settable: false
            }
        },
        bulkEditorColumns: []
    });

    maPointProvider.registerType({
        type: 'EnvCan',
        description: 'dsEdit.envcandsPoint',
        template: `<ma-envcands-data-point-editor data-point="$ctrl.dataPoint"></ma-envcands-data-point-editor>`
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