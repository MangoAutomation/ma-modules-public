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

describe('Server endpoint tests', function(){
    before('Login', config.login);

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
});
