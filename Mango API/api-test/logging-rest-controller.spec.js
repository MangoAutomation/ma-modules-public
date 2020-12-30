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

// Mango REST V1 API - Logging Rest Controller
describe('logging-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    const testSetup = function(params, requestBody) {
        return Promise.resolve();
    };
    
    const testTeardown = function(params) {
        return Promise.resolve();
    };

    // View log - Optionally download file as attachment
    it('GET /rest/latest/logging/view/{filename}', function() {
        const params = {
            download: true, // in = query, description = Set content disposition to attachment, required = false, type = boolean, default = true, enum = 
            filename: 'ma.log' // in = path, description = filename, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/logging/view/${params.filename}`,
                params: {
                    download: params.download
                },
                headers: {
                    accept: 'text/plain'
                },
                dataType: 'string'
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isString(response.data, 'data');
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

});
