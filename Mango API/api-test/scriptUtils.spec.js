/**
 * Copyright 2019 Infinite Automation Systems Inc.
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

describe('Test Script Utility Endpoints', function() {
    before('Login', config.login);

    it('Validate compiled PointValueTime result script', () => {
      return client.restRequest({
          path: '/rest/v2/script/validate',
          method: 'POST',
          data: {
              compile: true,
              script: 'LOG.debug("test");\nreturn 1.0;',
              context: null,
              permissions: null,
              logLevel: 'DEBUG',
              resultDataType: 'NUMERIC'
            }
      }).then(response => {
          assert.strictEqual(response.data.result.dataType, 'NUMERIC');
          assert.strictEqual(response.data.result.value, 1);
          assert.strictEqual(response.data.result.annotation, null);
      });
    });
    
    it('Validate non compiled PointValueTime result script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                compile: false,
                script: 'LOG.debug("test");\nreturn 1.0;',
                context: null,
                permissions: null,
                logLevel: 'DEBUG',
                resultDataType: 'NUMERIC'
              }
        }).then(response => {
            assert.strictEqual(response.data.result.dataType, 'NUMERIC');
            assert.strictEqual(response.data.result.value, 1);
            assert.strictEqual(response.data.result.annotation, null);
        });
      });
    
    it('Validate compiled String result script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                compile: true,
                script: 'LOG.debug("test");\nreturn "hello";',
                context: null,
                permissions: null,
                logLevel: 'DEBUG'
              }
        }).then(response => {
            assert.strictEqual(response.data.result, 'hello');
        });
      });
      
      it('Validate non compiled String result script', () => {
          return client.restRequest({
              path: '/rest/v2/script/validate',
              method: 'POST',
              data: {
                  compile: false,
                  script: 'LOG.debug("test");\nreturn "hello";',
                  context: null,
                  permissions: null,
                  logLevel: 'DEBUG'
                }
          }).then(response => {
              assert.strictEqual(response.data.result, 'hello');
          });
        });
    
    it.only('Validate buggy script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                compile: false,
                script: 'do stuff that is broken',
                context: null,
                permissions: null,
                logLevel: 'DEBUG'
              }
        }).then(response => {
            throw new Error('Should not have returned a valid response.');
        }, error => {
            assert.strictEqual(error.status, 422);
        });
      });
});
