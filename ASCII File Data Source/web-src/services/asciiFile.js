/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

asciiFilerFactory.$inject = ['$http'];
function asciiFilerFactory($http) {
    
    class AsciiFileResource {

        validate(xid, data) {
            let url, method;
            url = '/rest/latest/ascii-file-data-source/validate-ascii/' + xid;
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
            url = '/rest/latest/ascii-file-data-source/validate-ascii-file-exists';
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