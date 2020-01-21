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

// Mango REST V1 API - Configure/Read System Settings
describe('system-settings-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    const testSetup = function(params, requestBody) {
        // common test setup, e.g. create a VO object
        this.test.expectedResult = requestBody || 'value 1';
        
        return client.restRequest({
            method: 'PUT',
            path: `/rest/v1/system-settings/${params.key}`,
            data: this.test.expectedResult
        });
    };
    
    const testTeardown = function(params) {
        // common test teardown, e.g. delete a VO object
        return client.restRequest({
            method: 'DELETE',
            path: `/rest/v1/system-settings/${params.key}`,
        });
    };
    
    // Get All System Settings - Admin Permission Required, All settings returned as string types
    it('GET /rest/v1/system-settings', function() {
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/system-settings`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data, 'data');
        });
    });

    // Update Many System Settings - Admin Privs Required
    it('POST /rest/v1/system-settings', function() {
        const params = {
            key: uuid()
        };
        const requestBody = {
            [params.key]: 'value 2'
        };

        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/v1/system-settings`,
                data: requestBody
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data, 'data');
            assert.strictEqual(response.data[params.key], 'value 2');
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get System Setting By key - Admin Permission Required, if no type supplied assume to be string
    it('GET /rest/v1/system-settings/{key}', function() {
        const params = {
            key: uuid(), // in = path, description = Valid System Setting ID, required = true, type = string, default = , enum = 
            type: 'STRING' // in = query, description = Return Type, required = false, type = string, default = STRING, enum = INTEGER,BOOLEAN,JSON,STRING
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/system-settings/${params.key}`,
                params: {
                    type: params.type
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.strictEqual(response.data, this.test.expectedResult);
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Update an existing System Setting - If no type is provided, String is assumed
    it('PUT /rest/v1/system-settings/{key}', function() {
        const requestBody = 'value 2';
        const params = {
            key: uuid(), // in = path, description = key, required = true, type = string, default = , enum = 
            model: requestBody // in = body, description = Updated model, required = true, type = , default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v1/system-settings/${params.key}`,
                data: requestBody
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.strictEqual(response.data, 'value 2');
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

});
