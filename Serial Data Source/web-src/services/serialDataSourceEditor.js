/**
 * @copyright 2020 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Pier Puccini
 */

maSerialDsEditorFactory.$inject = ['maRestResource'];
function maSerialDsEditorFactory(RestResource) {
    
    const serialDsEditorBaseUrl = '/rest/v2/';

    class serialDsEditorResource extends RestResource {
        
        static get baseUrl() {
            return serialDsEditorBaseUrl;
        }

        static validateString(xid, data) {
            return this.http({
                url: `${this.baseUrl}serial-data-source/validate-ascii/${xid}`,
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