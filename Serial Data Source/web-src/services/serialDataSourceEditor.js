/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

maSerialDsEditorFactory.$inject = ['$http'];
function maSerialDsEditorFactory($http) {
    
    const serialDsEditorBaseUrl = '/rest/latest';

    class serialDsEditorResource {
        
        static get baseUrl() {
            return serialDsEditorBaseUrl;
        }

        static validateString(xid, data) {
            return $http({
                url: `${this.baseUrl}/serial-data-source/validate-ascii/${encodeURIComponent(xid)}`,
                method: 'POST',
                data: data,
                headers: {
                    'Content-Type': 'application/json;charset=UTF-8'
                }
            }).then(response => {
                return response.data;
            });
        }
    }
    
    return serialDsEditorResource;
}

export default maSerialDsEditorFactory;