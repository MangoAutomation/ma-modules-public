/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

reportServiceFactory.$inject = ['maRestResource'];
function reportServiceFactory(RestResource) {

    const reportBaseUrl = '/rest/v2/reports';
    const reportWebSocketUrl = '/v1/websocket/reports';
    const reportXidPrefix = 'REPORT_';

    const defaultProperties = {
        name: ''
    };
    
    class Report extends RestResource {
        static get defaultProperties() {
            return defaultProperties;
        }
        
        static get baseUrl() {
            return reportBaseUrl;
        }
        
        static get webSocketUrl() {
            return reportWebSocketUrl;
        }
        
        static get xidPrefix() {
            return reportXidPrefix;
        }
    }

    return Report;
}

export default reportServiceFactory;