/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import angular from 'angular';
import virtualDataSourceEditor from './components/virtualDataSourceEditor/virtualDataSourceEditor';
import virtualDataPointEditor from './components/virtualDataPointEditor/virtualDataPointEditor';
import virtualDsHelpTemplate from './help/dsHelp.html';
import virtualDpHelpTemplate from './help/dpHelp.html';

const virtualDataSourceModule = angular.module('maVirtualDataSource', ['maUiApp'])
.component('maVirtualDataSourceEditor', virtualDataSourceEditor)
.component('maVirtualDataPointEditor', virtualDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider','maUiMenuProvider', function(maDataSourceProvider, maPointProvider,maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'VIRTUAL',
        description: 'dsEdit.virtual',
        template: `<ma-virtual-data-source-editor data-source="$ctrl.dataSource"></ma-virtual-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            name: '',
            enabled: false,
            polling: true,
            modelType: 'VIRTUAL',
            descriptionKey: 'VIRTUAL.dataSource',
            pollPeriod: {
                periods: 1,
                type: 'MINUTES'
            },
            editPermission: ['superadmin'],
            purgeSettings: {
                override: false,
                frequency: {
                    periods: 1,
                    type: 'YEARS'
                }
            },
            eventAlarmLevels: [
                {eventType: 'POLL_ABORTED', level: 'URGENT', duplicateHandling: 'IGNORE', descriptionKey: 'event.ds.pollAborted'}
            ],
            quantize: false,
            useCron: false
        },
        defaultDataPoint: {
            dataSourceTypeName: 'VIRTUAL',
            pointLocator: {
                max: 100.0,
                min: 0.0,
                maxChange: 0.1,
                startValue: '50',
                modelType: 'PL.VIRTUAL',
                dataType: 'NUMERIC',
                settable: true,
                changeType: 'BROWNIAN',
                values: []
            }
        },
        bulkEditorColumns: [
            {name: 'pointLocator.changeType', label: 'dsEdit.virtual.changeType', selectedByDefault: true}
        ]
    });

    maPointProvider.registerType({
        type: 'VIRTUAL',
        description: 'dsEdit.virtualPoint',
        template: `<ma-virtual-data-point-editor data-point="$ctrl.dataPoint"></ma-virtual-data-point-editor>`
    });
    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.help.virtualDataSource',
            url: '/virtual-data-source',
            menuTr: 'dsEdit.virtual',
            template: virtualDsHelpTemplate
        },
        {
            name: 'ui.help.virtualDataPoint',
            url: '/virtual-data-point',
            menuTr: 'dsEdit.virtualPoint',
            template: virtualDpHelpTemplate
        }
    ]);
}]);

export default virtualDataSourceModule;