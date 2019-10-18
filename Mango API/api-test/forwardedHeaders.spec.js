/**
 * Copyright 2017 Infinite Automation Systems Inc.
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

const {createClient, login} = require('@infinite-automation/mango-client/test/testHelper');

describe('Forwarded and X-Forwarded-* headers', function() {
    const client = createClient();
    
    before('Login', function() { return login.call(this, client); });

    it('Honors the X-Forwarded-* headers for a request from localhost', function() {
        const protocol = 'https';
        const host = 'forwarded-host.example.com';
        const port = '8443';
        
        return client.restRequest({
            path: '/rest/v2/testing/location',
            method: 'GET',
            headers: {
                'X-Forwarded-Proto': protocol,
                'X-Forwarded-Host': host,
                'X-Forwarded-Port': port
            }
        }).then(response => {
            assert.isString(response.headers.location);
            
            /* global URL:true */
            const url = new URL(response.headers.location);
            assert.strictEqual(url.protocol, protocol + ':');
            assert.strictEqual(url.hostname, host);
            assert.strictEqual(url.port, port);
        });
    });

    it('Honors the X-Forwarded-* headers for a request from localhost (port in host field)', function() {
        const protocol = 'https';
        const host = 'forwarded-host.example.com';
        const port = '8443';
        
        return client.restRequest({
            path: '/rest/v2/testing/location',
            method: 'GET',
            headers: {
                'X-Forwarded-Proto': protocol,
                'X-Forwarded-Host': `${host}:${port}`
            }
        }).then(response => {
            assert.isString(response.headers.location);
            
            /* global URL:true */
            const url = new URL(response.headers.location);
            assert.strictEqual(url.protocol, protocol + ':');
            assert.strictEqual(url.hostname, host);
            assert.strictEqual(url.port, port);
        });
    });
    
    it('Honors the Forwarded header for a request from localhost', function() {
        const protocol = 'https';
        const host = 'forwarded-host.example.com';
        const port = '8443';
        
        return client.restRequest({
            path: '/rest/v2/testing/location',
            method: 'GET',
            headers: {
                Forwarded: `host="${host}:${port}";proto=${protocol}`
            }
        }).then(response => {
            assert.isString(response.headers.location);
            
            /* global URL:true */
            const url = new URL(response.headers.location);
            assert.strictEqual(url.protocol, protocol + ':');
            assert.strictEqual(url.hostname, host);
            assert.strictEqual(url.port, port);
        });
    });
});