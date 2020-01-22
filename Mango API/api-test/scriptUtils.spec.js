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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('Test Script Utility Endpoints', function() {
    before('Login', function() { return login.call(this, client); });

    it('Validate wrapped PointValueTime result script', () => {
      return client.restRequest({
          path: '/rest/v2/script/validate',
          method: 'POST',
          data: {
              wrapInFunction: true,
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
    
    it('Validate wrapped PointValueTime result script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: true,
                script: 'LOG.debug("test"); \nreturn 1.0;',
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
    
    it('Validate wrapped String result script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: true,
                script: 'LOG.debug("test");\nreturn "hello";',
                context: null,
                permissions: null,
                logLevel: 'DEBUG'
              }
        }).then(response => {
            assert.strictEqual(response.data.result, 'hello');
        });
      });
      
      it('Validate wrapped String result script', () => {
          return client.restRequest({
              path: '/rest/v2/script/validate',
              method: 'POST',
              data: {
                  wrapInFunction: true,
                  script: 'LOG.debug("test");\nreturn "hello";',
                  context: null,
                  permissions: null,
                  logLevel: 'DEBUG'
                }
          }).then(response => {
              assert.strictEqual(response.data.result, 'hello');
          });
        });
    
    it('Validate wrapped buggy script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: true,
                script: 'for(var i=0; i<10; j++){ LOG.debug("test");}',
                context: null,
                permissions: null,
                logLevel: 'DEBUG'
              }
        }).then(response => {
            assert.strictEqual(response.data.errors.length, 1);
            assert.strictEqual(response.data.errors[0].lineNumber, 1);
        });
      });
    
    it('Validate un-wrapped buggy script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: false,
                script: 'for(var i=0; i<10; j++){ LOG.debug("test");}',
                context: null,
                permissions: null,
                logLevel: 'DEBUG'
              }
        }).then(response => {
            assert.strictEqual(response.data.errors.length, 1);
            assert.strictEqual(response.data.errors[0].lineNumber, 1);
        });
      });
    
    it('Validate un-wrapped String result script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: false,
                script: 'function test(){ return "hello";}\ntest();',
                context: null,
                permissions: null,
                logLevel: 'DEBUG'
              }
        }).then(response => {
            assert.strictEqual(response.data.result, 'hello');
        });
      });
    it('Validate script null script', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: false,
                script: null,
                context: null,
                permissions: null,
                logLevel: 'INFO'
              }
        }).then(response => {
            throw new Error('Should not have returned a valid response.');
        }, error => {
            assert.strictEqual(error.status, 422);
        });
      });
    
    it('Validate script with result type exception.', () => {
        return client.restRequest({
            path: '/rest/v2/script/validate',
            method: 'POST',
            data: {
                wrapInFunction: true,
                script: 'return "testing";',
                context: null,
                permissions: ['superadmin'],
                logLevel: 'INFO',
                resultDataType: 'NUMERIC'
              }
        }).then(response => {
            assert.strictEqual(response.data.errors.length, 1);
            assert.isNull(response.data.errors[0].lineNumber);
            assert.isNull(response.data.errors[0].columnNumber);
            assert.strictEqual(response.data.errors[0].message, 'Could not convert result "testing" to Numeric');
            assert.strictEqual(response.data.result, 'testing');
        });
      });
});
