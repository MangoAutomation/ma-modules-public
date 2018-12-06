/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import angular from 'angular';
import virtualDataSourceEditor from './components/virtualDataSourceEditor/virtualDataSourceEditor';

const virtualDataSourceModule = angular.module('maVirtualDataSource', ['maUiApp'])
.component('maVirtualDataSourceEditor', virtualDataSourceEditor)
.config(['$injector', function($injector) {
    if ($injector.has('maDataSourceProvider')) {
        const maDataSourceProvider = $injector.get('maDataSourceProvider');
        if (typeof maDataSourceProvider.registerType === 'function') {
            maDataSourceProvider.registerType({
                type: 'VIRTUAL',
                description: 'dsEdit.virtual',
                template: `<ma-virtual-data-source-editor data-source="$ctrl.dataSource"></ma-virtual-data-source-editor>`,
                polling: true
            });
        }
    }
}]);

export default virtualDataSourceModule;