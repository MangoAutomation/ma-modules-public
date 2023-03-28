/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import angular from 'angular';
import asciiFileDataSourceEditor from './components/asciiFileDataSourceEditor/asciiFileDataSourceEditor';
import asciiFileDataPointEditor from './components/asciiFileDataPointEditor/asciiFileDataPointEditor';

import asciiFile from './services/asciiFile';

import settingsTemplate from './settings.html';
import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';
import systemSettingsTemplate from './help/settingsHelp.html';

const asciiFileDataSourceModule = angular.module('maAsciiFileDataSource', ['maUiApp'])
.component('maAsciiFileDataSourceEditor', asciiFileDataSourceEditor)
.component('maAsciiFileDataPointEditor', asciiFileDataPointEditor)
.factory('maAsciiFile', asciiFile)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'ASCII FILE',
        description: 'dsEdit.asciiFile',
        template: `<ma-ascii-file-data-source-editor data-source="$ctrl.dataSource"></ma-ascii-file-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            descriptionKey: 'dsEdit.file.desc',
            editPermission: [],
            enabled: false,
            eventAlarmLevels:[
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
                    duplicateHandling: 'IGNORE',
                    eventType: 'POINT_READ_EXCEPTION',
                    level: 'URGENT'
                }
            ],
            filePath: '',
            modelType: 'ASCII FILE',
            pollPeriod: {periods: 5, type: 'MINUTES'},
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}},
            quantize: false,
            useCron: false
        },
        defaultDataPoint: {
            dataSourceTypeName: 'ASCII FILE',
            enabled: false,
            pointLocator: {
                dataType: 'BINARY',
                hasTimestamp: false,
                modelType: 'PL.ASCII_FILE',
                pointIdentifier: '',
                pointIdentifierIndex: 0,
                relinquishable: false,
                settable: false,
                timestampFormat: '',
                timestampIndex: 0,
                valueIndex: 0,
                valueRegex: '',
            }
        },
        bulkEditorColumns: []
    });

    maPointProvider.registerType({
        type: 'ASCII FILE',
        description: 'dsEdit.asciiFilePoint',
        template: `<ma-ascii-file-data-point-editor data-point="$ctrl.dataPoint"></ma-ascii-file-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.helps.help.asciiDataSource',
            url: '/ascii-file-data-source',
            menuTr: 'dsEdit.asciiFile',
            template: dsHelpTemplate
        },
        {
            name: 'ui.helps.help.asciiDataPoint',
            url: '/ascii-file-data-point',
            menuTr: 'dsEdit.asciiFilePoint',
            template: dpHelpTemplate
        },
        {
            name: 'ui.helps.help.systemSettings',
            url: '/ascii-file-system-settings',
            menuTr: 'dsEdit.file.systemSettingsDescription',
            template: systemSettingsTemplate
        },
        {
            name: 'ui.system.system.asciiFile',
            url: '/ascii-file',
            template: settingsTemplate,
            menuTr: 'dsEdit.file.systemSettingsDescription',
            menuHidden: true,
            params: {
                helpPage: 'ui.helps.help.systemSettings'
            },
        }
    ]);
}]);

export default asciiFileDataSourceModule;