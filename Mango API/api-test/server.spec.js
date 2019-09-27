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
const client = createClient();

describe('Server endpoint tests', function(){
    before('Login', function() { return login.call(this, client); });

    //TODO test query timezones
    //TODO test send email
    //TODO test restart Mango
    //TODO test list http sessions

    it('Gets list of system information', () => {
      return client.restRequest({
          path: '/rest/v2/server/system-info',
          method: 'GET'
      }).then(response => {
        assert.notEqual(response.data.timezone, null);
      });
    });

    it('Gets count for all data points', () => {
      return client.restRequest({
          path: '/rest/v2/server/point-history-counts',
          method: 'GET'
      }).then(response => {
        assert.isAbove(response.data.length, 0);
      });
    });
    
    it('Gets all serial ports', () => {
        return client.restRequest({
            path: '/rest/v2/server/serial-ports?refresh=true',
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.length);
        });
    });

    it('Gets cors settings', () => {
        return client.restRequest({
            path: '/rest/v2/server/cors-settings',
            method: 'GET'
        }).then(response => {
            assert.isBoolean(response.data.enabled, true);
        });
    });
    
    it('List directory', () => {
        return client.restRequest({
            path: '/rest/v2/server/execute-command',
            method: 'POST',
            data:{
                timeout: 5000,
                command: 'ls'
            }
        }).then(response => {
            assert.isString(response.data);
        });
    });
});
