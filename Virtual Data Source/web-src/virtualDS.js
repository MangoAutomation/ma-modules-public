/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import angular from 'angular';
import virtualDataSourceEditor from './components/virtualDataSourceEditor/virtualDataSourceEditor';
import virtualDataPointEditor from './components/virtualDataPointEditor/virtualDataPointEditor';

const virtualDataSourceModule = angular.module('maVirtualDataSource', ['maUiApp'])
.component('maVirtualDataSourceEditor', virtualDataSourceEditor)
.component('maVirtualDataPointEditor', virtualDataPointEditor)
.config(['maDataSourceProvider', 'maPointProvider', function(maDataSourceProvider, maPointProvider) {
    maDataSourceProvider.registerType({
        type: 'VIRTUAL',
        description: 'dsEdit.virtual',
        template: `<ma-virtual-data-source-editor data-source="$ctrl.dataSource"></ma-virtual-data-source-editor>`,
        polling: true,
        defaultDataSource: {
            modelType: 'VIRTUAL',
            polling: true,
            pollPeriod: {
                periods: 1,
                type: 'MINUTES'
            },
            alarmLevels: {
                POLL_ABORTED: 'INFORMATION'
            }
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
                changeType: 'BROWNIAN'
            }
        }
    });
    
    maPointProvider.registerType({
        type: 'VIRTUAL',
        description: 'dsEdit.virtualPoint',
        template: `<ma-virtual-data-point-editor data-point="$ctrl.dataPoint"></ma-virtual-data-point-editor>`
    });
}]);

export default virtualDataSourceModule;