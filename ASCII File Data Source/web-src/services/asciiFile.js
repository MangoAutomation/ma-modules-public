/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

asciiFilerFactory.$inject = ['$http'];
function asciiFilerFactory($http) {
    
    class AsciiFileResource {

        validate(xid, data) {
            let url, method;
            url = '/rest/v2/ascii-file-data-source/validate-ascii/' + xid;
            method = 'POST';
            
            return $http({
                url,
                method,
                data: data,
                headers: {
                    'Content-Type': 'text/plain'
                },
            }).then(response => {
                return response.data;
            });
        }

        validateFileExists(data) {
            let url, method;
            url = '/rest/v2/ascii-file-data-source/validate-ascii-file-exists';
            method = 'POST';
            
            return $http({
                url,
                method,
                data: data,
                headers: {
                    'Content-Type': 'text/plain'
                },
            }).then(response => {
                return response;
            });
        }
    }
    
    return AsciiFileResource;
}

export default asciiFilerFactory;