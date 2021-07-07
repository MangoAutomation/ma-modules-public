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

describe('JSON emport endpoints', function() {
    
    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};
    
    before('Login', function() { return login.call(this, client); });
    this.timeout(20000);

    it('Tests expiration of temp resource', () => {
      var timeout = 500;
      return client.restRequest({
          path: '/rest/latest/json-emport?timeout='+timeout,
          method: 'POST',
          data: {
            "users": [
              {
                "timezone": "",
                "locale": "",
                "homeUrl": null,
                "phone": "",
                "permissions": "superadmin",
                "name": "Administrator",
                "receiveAlarmEmails": "IGNORE",
                "receiveOwnAuditEvents": false,
                "disabled": false,
                "muted": true,
                "email": "admin@myMangoDomain.com",
                "username": "admin"
              }
            ]
          }
      }).then(response => {
          return delay(800).then(() => {
            //Check results @ response.headers.location
            return delay(1000).then(() => {
                //Expect 404
                return client.restRequest({
                  path: response.headers.location,
                  method: 'GET'
                }).then(response => {
                  throw new Error('Expected 404');
                }, error =>{
                  //Make sure the resource has expired
                  assert.strictEqual(error.response.statusCode, 404);
                });
            });
          });
      });
    });

    //TODO need to build a large configuration so that we have time to cancel
    // it
    it.skip('Tests cancel of import', () => {
      var timeout = 500;
      //Build a large configuration to import
      const configuration = {};

      return client.restRequest({
          path: '/rest/latest/json-emport?timeout='+timeout,
          method: 'POST',
          data: configuration
      }).then(response => {
        testContext.tempImportResourceLocation = response.headers.location;
        return client.restRequest({
          path: testContext.tempImportResourceLocation,
          data: {cancel: true},
          method: 'PUT'
        }).then(response => {
          //TODO Check that it was accepted
          console.log(response.data);
          return client.restRequest({
            path: testContext.tempImportResourceLocation,
            method: 'GET'
          }).then(response => {
            console.log(response.data);
          });
        });
      });
    });

    //TODO test the user actually was updated

    function delay(time) {
        return new Promise((resolve) => {
            setTimeout(resolve, time);
        });
    }

});
