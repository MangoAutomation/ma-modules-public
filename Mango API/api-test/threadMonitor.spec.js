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
const client = createClient();

describe('Thread access tests', function(){
    before('Login', function() { return login.call(this, client); });

    it('View all threads', () => {
      return client.restRequest({
          path: '/rest/v1/threads',
          method: 'GET'
      }).then(response => {
        for(var i=0; i<response.data.length; i++){
          assert.isNumber(response.data[i].cpuTime);
          assert.isNumber(response.data[i].userTime);
          assert.isString(response.data[i].name);
          assert.notEqual(response.data[i].location, null);
          assert.isNumber(response.data[i].priority);
          assert.isNumber(response.data[i].id);
          assert.isString(response.data[i].state);
          if(response.data[i].state === 'RUNNABLE'){
            assert.equal(response.data[i].lockInfo, null);
            assert.equal(response.data[i].lockOwnerId, -1);
            assert.equal(response.data[i].lockOwnerName, null);
          }else if(response.data[i].state === 'BLOCKED'){
            assert.notEqual(response.data[i].lockInfo, null);
            assert.isNumber(response.data[i].lockOwnerId);
            assert.notEqual(response.data[i].lockOwnerName, null);
          }
        }
      });
    });
});
