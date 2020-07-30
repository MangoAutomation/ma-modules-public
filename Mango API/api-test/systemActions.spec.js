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

describe('System Action Endpoints', function() {
    before('Login', function() { return login.call(this, client); });
    this.timeout(20000);

    it('Lists available actions', () => {
      return client.restRequest({
          path: '/rest/latest/actions',
          method: 'GET',
      }).then(response => {
        var actions = [
          'purgeUsingSettings',
          'purgeAllPointValues',
          'purgeAllEvents',
          'backupConfiguration',
          'sqlBackup',
          'sqlRestore',
          'excelReportPurgeUsingSettings',
          'excelReportPurgeAll',
          'noSqlBackup',
          'noSqlRestore',
          'noSqlReloadLinks'];

        for(var i=0; i<response.data.length; i++){
          expect(actions).to.contain(response.data[i]);
        }
      });
    });

    it('Kick off purgeUsingSettings', () => {

      return client.restRequest({
          path: '/rest/latest/actions/trigger/purgeUsingSettings',
          method: 'PUT'
      }).then(response => {

        return delay(5000).then(() => {
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
            assert.equal(response.data.results.finished, true);
          });
        });
      });
    });

    it('Kick off purgeAllPointValues', () => {

      return client.restRequest({
          path: '/rest/latest/actions/trigger/purgeAllPointValues',
          method: 'PUT'
      }).then(response => {

        return delay(3000).then(() => {
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
            assert.equal(response.data.results.finished, true);
          });
        });
      });
    });

    it('Kick off purgeAllEvents', () => {

      return client.restRequest({
          path: '/rest/latest/actions/trigger/purgeAllEvents',
          method: 'PUT'
      }).then(response => {

        return delay(3000).then(() => {
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
            assert.equal(response.data.results.finished, true);
          });
        });
      });
    });

    it('Kick off backupConfiguration', () => {

      return client.restRequest({
          path: '/rest/latest/actions/trigger/backupConfiguration',
          method: 'PUT'
      }).then(response => {

        return delay(10000).then(() => {
          //console.log(`Backup Config Status: ${response.headers.location}`)
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
            //console.log(`Backup Config to: ${response.data.results.backupFile}`);
            assert.equal(response.data.results.finished, true);
          });
        });
      });
    });

    it('Kick off sqlBackup then sqlRestore', () => {
      return client.restRequest({
          path: '/rest/latest/actions/trigger/sqlBackup',
          method: 'PUT'
      }).then(response => {
        return delay(9000).then(() => {
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
              assert.equal(response.data.results.finished, true);
              debugger;
              //Now Restore it, the endpoint expects a filename only not the full path
              const parts = response.data.results.backupFile.split("/");
              return client.restRequest({
                  path: '/rest/latest/actions/trigger/sqlRestore',
                  method: 'PUT',
                  data: {filename: parts[parts.length-1]}
              }).then(response => {
                return delay(9000).then(() => {
                  return client.restRequest({
                    path: response.headers.location,
                    method: 'GET'
                  }).then(response => {
                      assert.equal(response.data.results.finished, true);
                  });
              });
            });
          });
        });
      });
    });

    it('Kick off excelReportPurgeUsingSettings action', () => {

      return client.restRequest({
          path: '/rest/latest/actions/trigger/excelReportPurgeUsingSettings',
          method: 'PUT'
      }).then(response => {

        return delay(3000).then(() => {
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
            assert.equal(response.data.results.finished, true);
          });
        });
      });
    });

    it('Kick off excelReportPurgeAll action', () => {

      return client.restRequest({
          path: '/rest/latest/actions/trigger/excelReportPurgeAll',
          method: 'PUT'
      }).then(response => {

        return delay(3000).then(() => {
          return client.restRequest({
            path: response.headers.location,
            method: 'GET'
          }).then(response => {
            assert.equal(response.data.results.finished, true);
          });
        });
      });
    });

/*    it('Kick off purgeAllPointValues action', () => {
      return client.restRequest({
          path: '/rest/latest/actions/trigger/purgeAllPointValues',
          method: 'PUT',
      }).then(response => {
        console.log(response);
      });
    });
*/
    function delay(time) {
        return new Promise((resolve) => {
            setTimeout(resolve, time);
        });
    }


});
