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

const config = require('@infinite-automation/mango-client/test/setup');

describe('ETag verification', function() {
    before('Login', config.login);
    
    it('Gets ETag header for core resource request', function() {
        return client.restRequest({
            path: '/images/logo.gif',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }    
        }).then(response => {
           assert.exists(response.headers.etag);
        });
    });
    
    it('Gets ETag header for module resource request', function() {
        return client.restRequest({
            path: '/modules/mangoUI/web/img/logo.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }    
        }).then(response => {
           assert.exists(response.headers.etag);
        });
    });
    
    it('Gets ETag header for ui resource request', function() {
        return client.restRequest({
            path: '/ui/img/logo.png',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }    
        }).then(response => {
           assert.exists(response.headers.etag);
        });
    });
    
    it('Does not get ETag header for REST request', function() {
        return client.restRequest({
            path: '/rest/v2/users/current',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }    
        }).then(response => {
           assert.notExists(response.headers.etag);
        });
    });
    
    it('Gets 304 response for request with known ETag', function() {
        return client.restRequest({
            path: '/images/logo.gif',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }     
        }).then(response => {
           assert.exists(response.headers.etag);
           return client.restRequest({
               path: '/images/logo.gif',
               dataType: 'buffer',
               headers: {
                   'Accept': '*/*',
                   'If-None-Match': response.headers.etag
               }     
           }).then(response => {
               assert.strictEqual(response.status, 304);
           });
        });
    });

    it('Gets 200 response for core resource request with changed ETag', function() {
        return client.restRequest({
            path: '/images/logo.gif',
            dataType: 'buffer',
            headers: {
                'Accept': '*/*'
            }     
        }).then(response => {
           assert.exists(response.headers.etag);
           return client.restRequest({
               path: '/images/logo.gif',
               dataType: 'buffer',
               headers: {
                   'Accept': '*/*',
                   'If-None-Match': 'notmyetag'
               }     
           }).then(response => {
               assert.strictEqual(response.status, 200);
           });
        });
    });
    
});
