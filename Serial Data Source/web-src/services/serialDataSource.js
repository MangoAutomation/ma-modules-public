/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

serialDataSourceFactory.$inject = ['$http'];
function serialDataSourceFactory($http) {
    
    class SerialDataSource {
        
        static getSerialPorts() {
            return $http({
                method: 'GET',
                url: '/rest/v2/server/serial-ports',
            }).then(function(response) {
                return response.data;
            });
        }

    }
    
    return SerialDataSource;
}

export default serialDataSourceFactory;
