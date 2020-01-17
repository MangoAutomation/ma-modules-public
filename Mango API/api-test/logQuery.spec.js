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

    it('List log files', () => {
      return client.restRequest({
          path: '/rest/v2/logging/files',
          method: 'GET'
      }).then(response => {
        //Set this for next test
        testContext.logfileCount = response.data.length;
        assert.isAbove(response.data.length, 0);
        for(var i=0; i<response.data.length; i++){
          assert.notEqual(response.data[i].folderPath, null);
          assert.notEqual(response.data[i].filename, null);
          assert.equal(response.data[i].mimeType, "text/plain");
          assert.notEqual(response.data[i].lastModified, null);
          assert.notEqual(response.data[i].size, null);
        }
      });
    });

    it('List log files with limit', () => {
      return client.restRequest({
          path: '/rest/v2/logging/files',
          method: 'GET',
          params: {
            limit: testContext.logfileCount - 1
          }
      }).then(response => {
        assert.equal(response.data.length, testContext.logfileCount - 1);
      });
    });

    it('Simple level query', () => {
      return client.restRequest({
          path: '/rest/v2/logging/by-filename/ma.log?level=INFO&limit(1)',
          method: 'GET'
      }).then(response => {
        //Looking for the Starting Mango Message
        assert.equal(response.data.length, 1);
        assert.equal(response.data[0].level, 'INFO');
      });
    });

    it('Simple time query', () => {
      testContext.fiveHourAgo = new Date(new Date().getTime() - 18000000);
      return client.restRequest({
          path: '/rest/v2/logging/by-filename/ma.log?time=gt=' + testContext.fiveHourAgo.toISOString() + '&limit(5)',
          method: 'GET'
      }).then(response => {
        //Test that all timestamps are > five min ago
        assert.isAtLeast(response.data.length, 1);
        for(var i=0; i<response.data.length; i++){
          assert.isAbove(response.data[i].time, testContext.fiveHourAgo.getTime());
        }

      });
    });

    it('Simple classname eq query', () => {
        testContext.classname = 'com.infiniteautomation.mango.rest.v2.TestingRestController';
        return client.restRequest({
            path: '/rest/v2/testing/log-error-message',
            method: 'POST',
            data: 'REST api log query test'
        }).then(()=> {
          return client.restRequest({
              path: '/rest/v2/logging/by-filename/ma.log?classname=eq=' + testContext.classname  + '&limit(5)',
              method: 'GET'
          }).then(response => {
              assert.isAtLeast(response.data.length, 1);
              for(var i=0; i<response.data.length; i++){
              assert.equal(response.data[i].classname, testContext.classname);
              }
          });          
        });
    });

    it('Simple classname like query', () => {
        return client.restRequest({
            path: '/rest/v2/testing/log-error-message',
            method: 'POST',
            data: 'REST api log query test'
        }).then(()=> {
            return client.restRequest({
                path: '/rest/v2/logging/by-filename/ma.log?like(classname,.*infiniteautomation.mango.rest.v2.*)&limit(5)',
                method: 'GET'
            }).then(response => {
                assert.isAtLeast(response.data.length, 1);
                for(var i=0; i<response.data.length; i++){
                    assert.match(response.data[i].classname, /.*infiniteautomation.mango.rest.v2.*/);
                }
            });            
        });
    });

    it('Simple method eq query', () => {
        //Note mispelled method
        testContext.method = 'logErorMessage';
        return client.restRequest({
          path: '/rest/v2/testing/log-error-message',
          method: 'POST',
          data: 'REST api log query test'
        }).then(()=> {
          return client.restRequest({
              path: '/rest/v2/logging/by-filename/ma.log?method=eq=' + testContext.method + '&limit(5)',
              method: 'GET'
          }).then(response => {
              assert.isAtLeast(response.data.length, 1);
              for(var i=0; i<response.data.length; i++){
                  assert.equal(response.data[i].method, testContext.method);
              }
          });          
        });
    });

    it('Simple method like query', () => {
        //Note mispelled method
        testContext.method = 'logErorMessage';
        return client.restRequest({
          path: '/rest/v2/testing/log-error-message',
          method: 'POST',
          data: 'REST api log query test'
        }).then(()=> {
          return client.restRequest({
              path: '/rest/v2/logging/by-filename/ma.log?like(method,' + encodeURIComponent('logEr.*') + ')&limit(5)',
              method: 'GET'
          }).then(response => {
              assert.isAtLeast(response.data.length, 1);
              for(var i=0; i<response.data.length; i++){
                  assert.equal(response.data[i].method, testContext.method);
              }
          });          
        });
    });

    it('Simple message eq query', () => {
        //Known bug that the message has a trailing space when parsed out of the file on the server
        return client.restRequest({
            path: '/rest/v2/testing/log-error-message',
            method: 'POST',
            data: 'REST api log query test '
        }).then(()=> {
            return client.restRequest({
                path: '/rest/v2/logging/by-filename/ma.log?message=eq=' + encodeURIComponent('REST api log query test ') + '&limit(1)',
                method: 'GET'
            }).then(response => {
                assert.isAtLeast(response.data.length, 1);
                assert.equal(response.data[0].message, 'REST api log query test ');
            });              
        });
    });

    it('Simple message like query', () => {
        //Known bug that the message has a trailing space when parsed out of the file on the server
        return client.restRequest({
            path: '/rest/v2/testing/log-error-message',
            method: 'POST',
            data: 'REST api log query test'
        }).then(()=> {
            return client.restRequest({
                path: '/rest/v2/logging/by-filename/ma.log?like(message,' + encodeURIComponent('REST api log query.*') + ')&limit(1)',
                method: 'GET'
            }).then(response => {
                assert.isAtLeast(response.data.length, 1);
                assert.equal(response.data[0].message, 'REST api log query test ');
            });              
        });
    });

    it('Downloads ma.log', function(){
      return client.restRequest({
          path: '/rest/v2/logging/view/ma.log',
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
          path: '/rest/v2/logging/view/ma.log',
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
    
    it('Expects 403 when trying to query an existing logfile that is not log4J ', function() {
        return client.restRequest({
            path: '/rest/v2/logging/by-filename/createTables.log',
            method: 'GET'
        }).then(response => {
            throw new Error('Returned successful response', response.status);
        }, error => {
            assert.strictEqual(error.response.statusCode, 403);
        });
    });
});
