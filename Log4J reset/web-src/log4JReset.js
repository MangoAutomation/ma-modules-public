import angular from 'angular';

const log4JReset = angular.module('maLog4JReset', [])
    .config(['maUiMenuProvider', function(maUiMenuProvider) {
        maUiMenuProvider.registerMenuItems([
        ]);
    }]);
 
export default log4JReset;
