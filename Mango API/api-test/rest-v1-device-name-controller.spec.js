/**
 * Copyright 2020 Infinite Automation Systems Inc.
 * http://infiniteautomation.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;

// Mango REST V1 API - Device Names
describe('device-name-controller', function() {
    before('Login', function() { return login.call(this, client); });
    
    before('Get internal DS', function() {
        return DataSource.get('internal_mango_monitoring_ds').then(ds => {
            this.internalDs = ds;
        });
    })

    // List device names - 
    it('GET /rest/v1/device-names', function() {
        return client.restRequest({
            method: 'GET',
            path: '/rest/v1/device-names'
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            assert.isAbove(response.data.length, 0, 'data');
            response.data.forEach((item, index) => {
                assert.isString(item, 'data[]');
            });
        });
    });

    // List device names by data source ID - 
    it('GET /rest/v1/device-names/by-data-source-id/{id}', function() {
        return client.restRequest({
            method: 'GET',
            path: '/rest/v1/device-names/by-data-source-id/' + encodeURIComponent(this.internalDs.id)
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            assert.isAbove(response.data.length, 0, 'data');
            response.data.forEach((item, index) => {
                assert.isString(item, 'data[]');
            });
        });
    });

    // List device names by data source XID - 
    it('GET /rest/v1/device-names/by-data-source-xid/{xid}', function() {
        return client.restRequest({
            method: 'GET',
            path: '/rest/v1/device-names/by-data-source-xid/' + encodeURIComponent(this.internalDs.xid)
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            assert.isAbove(response.data.length, 0, 'data');
            response.data.forEach((item, index) => {
                assert.isString(item, 'data[]');
            });
        });
    });

});
