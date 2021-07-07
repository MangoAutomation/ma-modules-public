/**
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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

const {createClient, login, config, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('Cross Origin Resource Sharing (CORS)', function() {
    const allowedOrigin = config.corsTestAllowedOrigin;
    const notAllowedOrigin = `http://${uuid()}`;

    before('Login', function() {
        if (!allowedOrigin) {
            // skip all the cors tests
            this.skip();
            return;
        }
        
        return login.call(this, client).then(() => {
            this.allowedCorsClient = createClient({
                defaultHeaders: {
                    origin: allowedOrigin
                }
            });
            
            this.notAllowedCorsClient = createClient({
                defaultHeaders: {
                    origin: notAllowedOrigin
                }
            });
            
            // copy the session cookie to the CORS client
            Object.assign(this.allowedCorsClient.cookies, client.cookies);
        });
    });

    it('Can make a CORS OPTION request', function() {
        return this.allowedCorsClient.restRequest({
            path: '/rest/latest/users/current',
            method: 'OPTIONS'
        }).then(response => {
            assert.strictEqual(response.headers['access-control-allow-origin'], allowedOrigin);
            assert.strictEqual(response.headers['access-control-allow-credentials'], 'true');
        });
    });

    it('Can make a CORS GET request', function() {
        return this.allowedCorsClient.restRequest({
            path: '/rest/latest/users/current'
        }).then(response => {
            assert.strictEqual(response.data.username, config.username);
            assert.strictEqual(response.headers['access-control-allow-origin'], allowedOrigin);
            assert.strictEqual(response.headers['access-control-allow-credentials'], 'true');
        });
    });

    it('Disallows origins that aren\'t whitelisted', function() {
        return this.notAllowedCorsClient.restRequest({
            path: '/rest/latest/users/current',
            dataType: 'string'
        }).then(response => {
            throw new Error(`Received a succcessful reponse: ${response.status}`);
        }, error => {
            assert.strictEqual(error.status, 403);
        });
    });
});
