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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('Cache control verification', function() {
    before('Login', function() { return login.call(this, client); });

    it('Gets default cache-control header for core resource request', function() {
        return client.restRequest({
            path: '/images/logo.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
            assert.strictEqual(response.headers['cache-control'], 'max-age=86400');
            assert.exists(response.headers['last-modified']);
        });
    });

    it('Gets default cache-control header for module resource request', function() {
        return client.restRequest({
            path: '/modules/mangoUI/web/img/icon16.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
            assert.strictEqual(response.headers['cache-control'], 'max-age=86400');
            assert.exists(response.headers['last-modified']);
        });
    });

    it('Gets default cache-control header for ui resource request', function() {
        return client.restRequest({
            path: '/ui/img/icon16.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
            assert.strictEqual(response.headers['cache-control'], 'max-age=86400');
            assert.exists(response.headers['last-modified']);
        });
    });

    it('Gets default resource cache-control header for ui/login request', function() {
        return client.restRequest({
            path: '/ui/login',
            dataType: 'buffer',
            headers: {
                'Accept': 'text/html'
            }
        }).then(response => {
            assert.strictEqual(response.headers['cache-control'], 'max-age=0');
            assert.exists(response.headers['last-modified']);
        });
    });

    it('Gets no cache for REST request', function() {
        return client.restRequest({
            path: '/rest/latest/users/current',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
            assert.strictEqual(response.headers['cache-control'], 'no-store');
            assert.notExists(response.headers['last-modified']);
        });
    });

    it('Gets 304 response for resource request with same modified time', function() {
        return client.restRequest({
            path: '/images/logo.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
            assert.exists(response.headers['cache-control']);
            assert.exists(response.headers['last-modified']);
            const lastModified = response.headers['last-modified'];
            return client.restRequest({
                path: '/images/logo.png',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*',
                    'if-modified-since': lastModified
                }
            }).then(response => {
                assert.strictEqual(response.status, 304);
            });
        });
    });

    it('Gets 200 response for core resource request modified since', function() {
        return client.restRequest({
            path: '/images/logo.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
            assert.exists(response.headers['cache-control']);
            assert.exists(response.headers['last-modified']);
            const lastModified = response.headers['last-modified'];
            const beforeLastModified = new Date(new Date(lastModified).getTime() - 2000);
            return client.restRequest({
                path: '/images/logo.png',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*',
                    'if-modified-since': beforeLastModified.toUTCString()
                }
            }).then(response => {
               assert.strictEqual(response.status, 200);
            });
        });
    });

    it('Handles not found ok', function() {
        return client.restRequest({
            path: '/images/nada',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }
        }).then(response => {
           throw new Error('Should not have gotten ok response');
        }, error => {
            assert.strictEqual(error.status, 404);
        });
    });

});
