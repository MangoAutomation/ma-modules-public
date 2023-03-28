/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

import angular from 'angular';
import internalDataSourceEditor from './components/InternalDataSourceEditor/InternalDataSourceEditor';
import internalDataPointEditor from './components/InternalDataPointEditor/InternalDataPointEditor';
import dsHelpTemplate from './help/dsHelp.html';
import dpHelpTemplate from './help/dpHelp.html';

const internalDataSourceModule = angular.module('maInternalDataSource', ['maUiApp'])
.component('maInternalDataSourceEditor', internalDataSourceEditor)
.component('maInternalDataPointEditor', internalDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider','maUiMenuProvider', 
function(maDataSourceProvider, maPointProvider, maUiMenuProvider) {
    maDataSourceProvider.registerType({
        type: 'INTERNAL',
        description: 'dox.internalDS',
        template: `<ma-internal-data-source-editor data-source="$ctrl.dataSource"></ma-internal-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            createPointsPattern: '.+',
            modelType: 'INTERNAL',
            polling: true,
            pollPeriod: {
                periods: 1,
                type: 'MINUTES'
            },
            eventAlarmLevels: [
                {eventType: 'POLL_ABORTED', level: 'URGENT', duplicateHandling: 'IGNORE', descriptionKey: 'event.ds.pollAborted'}
            ],
        },
        defaultDataPoint: {
            dataSourceTypeName: 'INTERNAL',
            pointLocator: {
                dataType: 'NUMERIC',
                modelType: 'PL.INTERNAL',
                relinquishable: false,
                settable: false
            }
        },
        bulkEditorColumns: [
            {name: 'pointLocator.configurationDescription', label: 'dsEdit.internal.attribute', selectedByDefault: true}
        ]
    });
    
    maPointProvider.registerType({
        type: 'INTERNAL',
        description: 'dsEdit.internalPoint',
        template: `<ma-internal-data-point-editor data-point="$ctrl.dataPoint"></ma-internal-data-point-editor>`
    });

    maUiMenuProvider.registerMenuItems([
        {
            name: 'ui.helps.help.internalDataSource',
            url: '/internal-data-source',
            menuTr: 'dsEdit.internal',
            template: dsHelpTemplate
        },
        {
            name: 'ui.helps.help.internalDataPoint',
            url: '/internal-data-point',
            menuTr: 'dsEdit.internalPoint',
            template: dpHelpTemplate
        }
    ]);
}]);

export default internalDataSourceModule;