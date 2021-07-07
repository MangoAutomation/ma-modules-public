/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

MangoApiSettingsFactory.$inject = ['maRestResource', '$http'];
function MangoApiSettingsFactory(RestResource, $http) {
    
    const baseUrl = '/rest/latest/server/cors-settings';
    const xidPrefix = 'APISET_';

    class MangoApiSettingsResource extends RestResource {

        static get baseUrl() {
            return baseUrl;
        }

        static get xidPrefix() {
            return xidPrefix;
        }

        static getCorsSettings() {
            let url, method;
            url = baseUrl;
            method = 'GET';

            return $http({
                url,
                method
            }).then(response => {
                return response.data;
            }); 
        }
    
    }
    
    return MangoApiSettingsResource;
}

export default MangoApiSettingsFactory;