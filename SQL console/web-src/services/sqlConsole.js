/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';

sqlConsoleFactory.$inject = ['maRestResource'];
function sqlConsoleFactory(RestResource) {
    
    const sqlConsoleBaseUrl = '/rest/latest/sql-console';

    class sqlConsoleResource extends RestResource {
        
        static get baseUrl() {
            return sqlConsoleBaseUrl;
        }

        static getTables() {
            return this.http({
                url: `${this.baseUrl}/list-tables`,
                method: 'GET'
            }).then(response => {
                return response.data;
            });
        }

        static query(query) {
            return this.http({
                url: `${this.baseUrl}`,
                method: 'GET',
                params: {query}
            }).then(response => {
                return response.data;
            });
        }

        static update(query) {
            return this.http({
                url: `${this.baseUrl}`,
                method: 'POST',
                data: query,
                headers: {
                    'Content-Type': 'application/sql'
                }
            }).then(response => {
                return response.data;
            });
        }
        
        static queryCsvUrl(query) {
            const encodedQuery = angular.$$encodeUriSegment(query);
            return `${this.baseUrl}?format=csv2&query=${encodedQuery}`;
        }
    }
    
    return sqlConsoleResource;
}

export default sqlConsoleFactory;