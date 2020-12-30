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

describe('Log file query tests', function(){
    
    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};
    
    before('Login', function() { return login.call(this, client); });
    this.timeout(20000);

    it('Downloads ma.log', function(){
      return client.restRequest({
          path: '/rest/latest/logging/view/ma.log',
          method: 'GET',
          dataType: 'buffer',
          headers: {
              'Accept': 'text/plain'
          },
          params: {
            download: true
          }
      }).then(response => {
          assert.match(response.headers['content-type'], /text\/plain.*/);
          assert.strictEqual(response.headers['cache-control'], 'no-store');
          assert.strictEqual(response.headers['content-disposition'], 'attachment');
          assert.isAbove(response.data.length, 0);
      });
    });

    it('View ma.log', function(){
      return client.restRequest({
          path: '/rest/latest/logging/view/ma.log',
          method: 'GET',
          dataType: 'buffer',
          headers: {
              'Accept': 'text/plain'
          },
          params: {
            download: false
          }
      }).then(response => {
          assert.match(response.headers['content-type'], /text\/plain.*/);
          assert.strictEqual(response.headers['cache-control'], 'no-store');
          assert.strictEqual(response.headers['content-disposition'], 'inline');
          assert.isAbove(response.data.length, 0);
      });
    });

});
