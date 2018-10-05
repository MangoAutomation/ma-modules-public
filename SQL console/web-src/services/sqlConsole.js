/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import angular from 'angular';

sqlConsoleFactory.$inject = ['maRestResource'];
function sqlConsoleFactory(RestResource) {
    
    const sqlConsoleBaseUrl = '/rest/v2/sql-console';

    class sqlConsoleResource extends RestResource {
        
        static get baseUrl() {
            return sqlConsoleBaseUrl;
        }
        
        getTables() {
            return this.constructor.http({
                url: `${this.constructor.baseUrl}/list-tables`,
                method: 'GET'
            }).then(response => {
                return response.data
            });
        }

        query(queryString) {
            return this.constructor.http({
                url: `${this.constructor.baseUrl}`,
                method: 'GET',
                params: {query: queryString}
            }).then(response => {
                return response.data
            });
        }

        update(queryString) {
            return this.constructor.http({
                url: `${this.constructor.baseUrl}`,
                method: 'POST',
                data: queryString,
                headers: {
                    'Content-Type': 'application/sql'
                }
            }).then(response => {
                return response.data
            });
        }

        static getTables() {
            const sql = new this({});
            return sql.getTables();
        }

        static query(queryString) {
            const sql = new this({});
            return sql.query(queryString);
        }

        static update(queryString) {
            const sql = new this({});
            return sql.update(queryString);
        }
    }
    
    return sqlConsoleResource;
}

export default sqlConsoleFactory;