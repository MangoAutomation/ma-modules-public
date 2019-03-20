/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import asciiFileDataSourceEditor from './components/asciiFileDataSourceEditor/asciiFileDataSourceEditor';
import asciiFileDataPointEditor from './components/asciiFileDataPointEditor/asciiFileDataPointEditor';

import settingsTemplate from './settings.html';
import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';
import systemSettingsTemplate from './help/settingsHelp.html';

const asciiFileDataSourceModule = angular.module('maAsciiFileDataSource', ['maUiApp'])
.component('maAsciiFileDataSourceEditor', asciiFileDataSourceEditor)
.component('maAsciiFileDataPointEditor', asciiFileDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', 'maUiMenuProvider', function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'ASCII FILE',
        description: 'dsEdit.asciiFile',
        template: `<ma-ascii-file-data-source-editor data-source="$ctrl.dataSource"></ma-ascii-file-data-source-editor>`,
        polling: false,
        defaultDataSource: {},
        defaultDataPoint: {
            dataSourceTypeName: 'ASCII FILE',
            enabled: false,
            pointLocator: {}
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
            name: 'ui.help.asciiDataSource',
            url: '/ascii-file-data-source',
            menuTr: 'dsEdit.asciiFile',
            template: dsHelpTemplate
        },
        {
            name: 'ui.help.asciiDataPoint',
            url: '/ascii-file-data-point',
            menuTr: 'dsEdit.asciiFilePoint',
            template: dpHelpTemplate
        },
        {
            name: 'ui.help.systemSettings',
            url: '/ascii-file-system-settings',
            menuTr: 'dsEdit.file.systemSettingsDescription',
            template: systemSettingsTemplate
        },
        {
            name: 'ui.settings.system.asciiFile',
            url: '/ascii-file',
            template: settingsTemplate,
            menuTr: 'dsEdit.file.systemSettingsDescription',
            menuHidden: true,
            params: {
               
                helpPage: 'ui.help.systemSettings'
            },
        }
    ]);
}]);

export default asciiFileDataSourceModule;