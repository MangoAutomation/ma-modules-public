/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import mbusDataSourceEditor from './components/mbusDataSourceEditor/mbusDataSourceEditor';
import mbusDataPointEditor from './components/mbusDataPointEditor/mbusDataPointEditor';
import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';

const mbusSourceModule = angular.module('maMbusDataSource', ['maUiApp'])
.component('maMbusDataSourceEditor', mbusDataSourceEditor)
.component('maMbusDataPointEditor', mbusDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'MBUS',
        description: 'dsEdit.mbus',
        template: `<ma-mbus-data-source-editor data-source="$ctrl.dataSource"></ma-mbus-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            connection: {},
            descriptionKey: 'dsEdit.mbus',
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
                    duplicateHandling: 'IGNORE',
                    eventType: 'DATA_SOURCE_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.ds.pointRead',
                    duplicateHandling: 'IGNORE_SAME_MESSAGE',
                    eventType: 'POINT_READ_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.ds.pointWrite',
                    duplicateHandling: 'IGNORE',
                    eventType: 'POINT_WRITE_EXCEPTION',
                    level: 'URGENT'
                }
            ],
            id: 321,
            modelType: 'MBUS',
            pollPeriod: {periods: 5, type: 'MINUTES'},
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}},
            quantize: false,
            useCron: false,
        },
        defaultDataPoint: {
            dataSourceTypeName: 'MBUS',
            enabled: false,
            pointLocator: {
                dbIndex: 0,
                modelType: 'PL.MBUS',
                effectiveSiPrefix: '',
                address: 0,
                addressing: 'primary',
                identNumber: 0,
                medium: 'Other',
                manufacturer: '',
                version: 0,
                responseFrame: '',
                difCode: '12 digit BCD',
                functionField: 'Instantaneous value',
                subUnit: 0,
                tariff: 0,
                storageNumber: 0,
                vifType: '',
                vifLabel: '',
                unitOfMeasurement: '',
                siPrefix: '',
                exponent: null,
                vifeTypes: [],
                vifeLabels: [],
                dataType: 'NUMERIC',
                relinquishable: false,
                settable: false
            }
        },
        bulkEditorColumns: [
            {name: 'pointLocator.dbIndex', label: 'dsEdit.mbus.dbIndex', selectedByDefault: true},
            {name: 'pointLocator.effectiveSiPrefix', label: 'dsEdit.mbus.effectiveSiPrefix', selectedByDefault: true},
            {name: 'pointLocator.manufacturer', label: 'dsEdit.mbus.manufacturer', selectedByDefault: true},
            {name: 'pointLocator.medium', label: 'dsEdit.mbus.medium', selectedByDefault: true},
            {name: 'pointLocator.address', label: 'dsEdit.mbus.addressHex', selectedByDefault: true},
            {name: 'pointLocator.responseFrame', label: 'dsEdit.mbus.responseFrame', selectedByDefault: true},
        ]
    });

    maPointProvider.registerType({
        type: 'MBUS',
        description: 'dsEdit.mbusPoint',
        template: `<ma-mbus-data-point-editor data-point="$ctrl.dataPoint"></ma-mbus-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.help.mbusDataSource',
            url: '/mbus-data-source',
            menuTr: 'dsEdit.mbus',
            template: dsHelpTemplate
        },
        {
            name: 'ui.help.mbusDataPoint',
            url: '/mbus-data-point',
            menuTr: 'dsEdit.mbusPoint',
            template: dpHelpTemplate
        }
    ]);
}]);

export default mbusSourceModule;