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

describe('Audit endpoint tests', function(){
    before('Login', function() { return login.call(this, client); });

    it('Gets entire audit table', () => {
      return client.restRequest({
          path: '/rest/latest/audit',
          method: 'GET'
      }).then(response => {
        assert.isAbove(response.data.items.length, 0);
      });
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
