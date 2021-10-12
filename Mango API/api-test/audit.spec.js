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

const {createClient, login, uuid, config} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const User = client.User;
const jwtUrl = '/rest/latest/auth-tokens';

describe('Audit endpoint tests', function(){
    before('Login', function() { return login.call(this, client); });

    before('Create a no roles test user to test audit', function() {
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            roles: [],
            password: this.testUserPassword
        });
        return this.testUser.save();
    });

    after('Delete the test user', function() {
        return this.testUser.delete();
    });

    before('Helper functions', function() {
        this.createToken = function(body = {}, clt = client) {
            return clt.restRequest({
                path: `${jwtUrl}/create`,
                method: 'POST',
                data: body
            }).then(response => {
                return response.data.token;
            });
        };

        this.noCookieConfig = {
            enableCookies: false
        };
    });

    it('Gets entire audit table', () => {

      return client.restRequest({
          path: '/rest/latest/audit',
          method: 'GET'
      }).then(response => {
        assert.isAbove(response.data.items.length, 0);
      });
    });

    it('No admin role user attempts to get entire audit table', function()  {
        return this.createToken({username: this.testUser.username}).then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient;
        }).then(jwtClient => jwtClient.restRequest({
            path: '/rest/latest/audit',
            method: 'GET'
        }).then(response => {
            throw new Error('We should not get a result');
        }, error => {
            assert.strictEqual(error.status, 403);
        }))
    });

    it('Performs simple audit query', () => {
      return client.restRequest({
          path: '/rest/latest/audit?limit(1)',
          method: 'GET'
      }).then(response => {
        assert.equal(response.data.items.length, 1);
      });
    });

    it('Performs audit query with alarmLevel filtering', () => {
      return client.restRequest({
          path: '/rest/latest/audit?alarmLevel=INFORMATION&limit(4)',
          method: 'GET'
      }).then(response => {
        assert.equal(response.data.items.length, 4);
        for(var i=0; i<response.data.items.length; i++){
          assert.equal(response.data.items[i].alarmLevel, 'INFORMATION');
        }
      });
    });

    it('Performs audit query with changeType filtering', () => {
      return client.restRequest({
          path: '/rest/latest/audit?changeType=CREATE&limit(10)',
          method: 'GET'
      }).then(response => {
        assert.equal(response.data.items.length, 10);
        for(var i=0; i<response.data.items.length; i++){
          assert.equal(response.data.items[i].changeType, 'CREATE');
        }
      });
    });

});
