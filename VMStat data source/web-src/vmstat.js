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
        type: 'VMSTAT',
        description: 'dsEdit.vmstat',
        template: `<ma-vm-stat-data-source-editor data-source="$ctrl.dataSource"></ma-vm-stat-data-source-editor>`,
        polling: false,
        defaultDataSource: {
            connectionDescription: '',
            descriptionKey: 'dsEdit.vmstat',
            editPermission: [],
            enabled: false,
            eventAlarmLevels: [
                {
                    description: 'Data source exception',
                    descriptionKey: 'event.ds.dataSource',
                    duplicateHandling: 'IGNORE_SAME_MESSAGE',
                    eventType: 'DATA_SOURCE_EXCEPTION',
                    level: 'URGENT'
                },
                {
                    description: 'Point data parse exception',
                    descriptionKey: 'event.ds.dataParse',
                    duplicateHandling: 'IGNORE',
                    eventType: 'PARSE_EXCEPTION',
                    level: 'URGENT'
                }
            ],
            modelType: 'VMSTAT',
            name: '',
            outputScale: 'NONE',
            pollSeconds: 60,
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}}
        },
        defaultDataPoint: {
            dataSourceTypeName: 'VMSTAT',
            pointLocator: {
                attribute: 'CPU_ID',
                dataType: 'NUMERIC',
                modelType: 'PL.VMSTAT',
                relinquishable: false,
                settable: false
            }
        },
        bulkEditorColumns: [
            {name: 'pointLocator.attribute', label: 'dsEdit.vmstat.attribute', selectedByDefault: true}
        ]
    });

    maPointProvider.registerType({
        type: 'VMSTAT',
        description: 'dsEdit.vmstatPoint',
        template: `<ma-vm-stat-data-point-editor data-point="$ctrl.dataPoint"></ma-vm-stat-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.help.vmstatDataSource',
            url: '/vm-stat-data-source',
            menuTr: 'dsEdit.vmstat',
            template: dsHelpTemplate
        },
        {
            name: 'ui.help.vmstatDataPoint',
            url: '/vm-stat-data-point',
            menuTr: 'dsEdit.vmstatPoint',
            template: dpHelpTemplate
        }
    ]);
}]);

export default vmStatDataSourceModule;