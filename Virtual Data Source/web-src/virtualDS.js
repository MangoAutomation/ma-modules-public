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
        polling: true
    });
    
    maPointProvider.registerType({
        type: 'VIRTUAL',
        description: 'dsEdit.virtualPoint',
        template: `<ma-virtual-data-point-editor data-point="$ctrl.dataPoint"></ma-virtual-data-point-editor>`
    });
}]);

export default virtualDataSourceModule;