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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');

describe('Forwarded and X-Forwarded-* headers', function() {
    const client = createClient();
    
    before('Login', function() { return login.call(this, client); });
    
    it('Returns the correct location when no forwarded headers are sent', function() {
        const {protocol, host, port} = client.options;

        return client.restRequest({
            path: '/rest/latest/testing/location',
            method: 'GET'
        }).then(response => {
            assert.isString(response.headers.location);

            /* global URL:true */
            const url = new URL(response.headers.location);
            assert.strictEqual(url.protocol, protocol + ':');
            assert.strictEqual(url.hostname, host);
            
            if (protocol === 'http' && port === 80 || protocol === 'https' && port === 443) {
                assert.strictEqual(url.port, '');
            } else {
                assert.strictEqual(url.port, '' + port);
            }
        });
    });
    
    // not supported by Jetty ForwardedRequestCustomizer, but is supported by Spring ForwardedHeaderFilter
    it.skip('Trusts the X-Forwarded-* headers (port in X-Forwarded-Port)', function() {
        const protocol = 'https';
        const host = 'forwarded-host.example.com';
        const port = '8443';
        
        return client.restRequest({
            path: '/rest/latest/testing/location',
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

    it('Trusts the X-Forwarded-* headers (port in host field)', function() {
        const protocol = 'https';
        const host = 'forwarded-host.example.com';
        const port = '8443';
        
        return client.restRequest({
            path: '/rest/latest/testing/location',
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
    
    it('Trusts the Forwarded header', function() {
        const protocol = 'https';
        const host = 'forwarded-host.example.com';
        const port = '8443';
        
        return client.restRequest({
            path: '/rest/latest/testing/location',
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
    
    it('Trusts the X-Forwarded-For header', function() {
        const testIp = '10.123.231.213';
        return client.restRequest({
            path: '/rest/latest/testing/remote-addr',
            method: 'GET',
            headers: {
                'X-Forwarded-For': testIp
            }
        }).then(response => {
            assert.strictEqual(response.data, testIp);
        });
    });
    
    it('Trusts the X-Forwarded-For header (IPv6)', function() {
        const testIp = '9556:caee:3b13:39b4:9dc2:ef7:2807:895';
        return client.restRequest({
            path: '/rest/latest/testing/remote-addr',
            method: 'GET',
            headers: {
                'X-Forwarded-For': testIp
            }
        }).then(response => {
            assert.strictEqual(response.data, `[${testIp}]`);
        });
    });
    
    it('Trusts the Forwarded header "for" component', function() {
        const testIp = '10.123.231.213';
        return client.restRequest({
            path: '/rest/latest/testing/remote-addr',
            method: 'GET',
            headers: {
                Forwarded: `for=${testIp}`
            }
        }).then(response => {
            assert.strictEqual(response.data, testIp);
        });
    });
    
    // note re. bracket notation - https://github.com/eclipse/jetty.project/issues/1503
    it('Trusts the Forwarded header "for" component (IPv6)', function() {
        const testIp = '9556:caee:3b13:39b4:9dc2:ef7:2807:895';
        return client.restRequest({
            path: '/rest/latest/testing/remote-addr',
            method: 'GET',
            headers: {
                Forwarded: `for="[${testIp}]"`
            }
        }).then(response => {
            assert.strictEqual(response.data, `[${testIp}]`);
        });
    });
});