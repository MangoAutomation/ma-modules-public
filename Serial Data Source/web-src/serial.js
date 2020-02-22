/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import serialDataSourceEditor from './components/serialDataSourceEditor/serialDataSourceEditor';
import serialDataPointEditor from './components/serialDataPointEditor/serialDataPointEditor';

import maSerialDsEditorFactory from './services/serialDataSourceEditor';

import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';

const serialSourceModule = angular.module('maSerialDataSource', ['maUiApp'])
.component('maSerialDataSourceEditor', serialDataSourceEditor)
.component('maSerialDataPointEditor', serialDataPointEditor)
.factory('maSerialDsEditor', maSerialDsEditorFactory)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'SERIAL',
        description: 'dsEdit.serial',
        template: `<ma-serial-data-source-editor data-source="$ctrl.dataSource"></ma-serial-data-source-editor>`,
        polling: false,
        defaultDataSource: {
            baudRate: 9600,
            commPortId: '',
            dataBits: 'DATA_BITS_8',
            descriptionKey: 'dsEdit.serial.desc',
            editPermission: [],
            enabled: false,
            eventAlarmLevels: [
                {
                    descriptionKey: 'event.ds.dataSource',
                    duplicateHandling: 'IGNORE',
                    eventType: 'DATA_SOURCE_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.ds.pointRead',
                    duplicateHandling: 'IGNORE',
                    eventType: 'POINT_READ_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.ds.pointWrite',
                    duplicateHandling: 'IGNORE',
                    eventType: 'POINT_WRITE_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    descriptionKey: 'event.serial.patternMismatchException',
                    duplicateHandling: 'IGNORE',
                    eventType: 'POINT_READ_PATTERN_MISMATCH_EVENT',
                    level: 'URGENT'
                }
            ],
            flowControlIn: 'NONE',
            flowControlOut: 'NONE',
            hex: false,
            ioLogFileSizeMBytes: 1,
            logIO: false,
            maxHistoricalIOLogs: 1,
            maxMessageSize: 1024,
            messageRegex: '',
            messageTerminator: '',
            modelType: 'SERIAL',
            name: '',
            parity: 'NONE',
            pointIdentifierIndex: 0,
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}},
            readTimeout: 1000,
            retries: 1,
            stopBits: 'STOP_BITS_1',
            useTerminator: true
        },
        defaultDataPoint: {
            dataSourceTypeName: 'SERIAL',
            enabled: false,
            pointLocator: {
                dataType: 'ALPHANUMERIC',
                modelType: 'PL.SERIAL',
                pointIdentifier: '',
                relinquishable: false,
                settable: true,
                valueIndex: 0,
                valueRegex: ''
            }
        },
        bulkEditorColumns: [
            {name: 'pointLocator.pointIdentifier', label: 'dsEdit.serial.pointIdentifier', selectedByDefault: true},
            {name: 'pointLocator.valueIndex', label: 'dsEdit.serial.valueIndex', selectedByDefault: true},
            {name: 'pointLocator.valueRegex', label: 'dsEdit.serial.valueRegex', selectedByDefault: true}
        ]
    });

    maPointProvider.registerType({
        type: 'SERIAL',
        description: 'dsEdit.serialPoint',
        template: `<ma-serial-data-point-editor data-point="$ctrl.dataPoint"></ma-serial-data-point-editor>`
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