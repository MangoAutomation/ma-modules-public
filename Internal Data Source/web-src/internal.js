/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';
import internalDataSourceEditor from './components/InternalDataSourceEditor/InternalDataSourceEditor';
import internalDataPointEditor from './components/InternalDataPointEditor/InternalDataPointEditor';

const internalDataSourceModule = angular.module('maInternalDataSource', ['maUiApp'])
.component('maInternalDataSourceEditor', internalDataSourceEditor)
.component('maInternalDataPointEditor', internalDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', function(maDataSourceProvider, maPointProvider) {
    maDataSourceProvider.registerType({
        type: 'INTERNAL',
        description: 'dox.internalDS',
        template: `<ma-internal-data-source-editor data-source="$ctrl.dataSource"></ma-internal-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            modelType: 'INTERNAL',
            polling: true,
            pollPeriod: {
                periods: 1,
                type: 'MINUTES'
            },
            eventAlarmLevels: [
                {eventType: 'POLL_ABORTED', level: 'INFORMATION', duplicateHandling: 'IGNORE', descriptionKey: 'event.ds.pollAborted'}
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
        bulkEditorColumns: []
    });
    
    maPointProvider.registerType({
        type: 'INTERNAL',
        description: 'dsEdit.internalPoint',
        template: `<ma-internal-data-point-editor data-point="$ctrl.dataPoint"></ma-internal-data-point-editor>`
    });
}]);

export default internalDataSourceModule;