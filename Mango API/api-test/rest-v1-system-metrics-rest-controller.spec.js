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

const {createClient, login, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

// Mango REST V1 API - Access to the current value for any System Metric
describe('system-metrics-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    // Get the current value for all System Metrics - TBD Add RQL Support to this endpoint
    it('GET /rest/v1/system-metrics', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/system-metrics`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            response.data.forEach((item, index) => {
                // MODEL: ValueMonitor«object»
                assert.isObject(item, 'data[]');
                assert.isString(item.id, 'data[].id');
                assert.isString(item.name, 'data[].name');
                if (item.uploadToStore) {
                    assert.isBoolean(item.uploadToStore, 'data[].uploadToStore');
                }
                assert.exists(item.value, 'data[].value');
                // END MODEL: ValueMonitor«object»
            });
        });
    });

    // Get the current value for one System Metric by its ID - 
    it('GET /rest/v1/system-metrics/{id}', function() {
        const params = {
            id: 'runtime.uptime' // in = path, description = Valid Monitor id, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/system-metrics/${params.id}`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: ValueMonitor«object»
            assert.isObject(response.data, 'data');
            assert.isString(response.data.id, 'data.id');
            assert.isString(response.data.name, 'data.name');
            if (response.data.uploadToStore) {
                assert.isBoolean(response.data.uploadToStore, 'data.uploadToStore');
            }
            assert.isNumber(response.data.value, 'data.value');
        });
    });

});
