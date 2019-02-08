/**
 * Copyright 2019 Infinite Automation Systems Inc.
 * http://infiniteautomation.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
const uuidV4 = require('uuid/v4');
const config = require('@infinite-automation/mango-client/test/setup');

describe('EnvCan data source v1', function() {
    before('Login', config.login);
    
    const ds = function () { 
        return {
            name: 'Test',
            enabled: false,
            alarmLevels: {
                PARSE_EXCEPTION: 'INFORMATION',
                POLL_ABORTED: 'INFORMATION'
            },
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            editPermission: 'superadmin,test',
            stationId: 3,
            dataStartTime: '2017-11-19T00:57:30.240Z',
            modelType: 'EnvCan'
        };
    }
    
    beforeEach('Create data source', function () {
        const dsv1 = ds();
        return client.restRequest({
            path: '/rest/v1/data-sources',
            method: 'POST',
            data: dsv1
        }).then((response) => {
            this.ds = response.data;
            assertV1(response, dsv1);
        }, (error) => {
            if(error.status === 422){
                var msg = 'Validation Failed: \n';
                for(var m in error.data.validationMessages)
                    msg += error.data.validationMessages[m].property + '-->' + error.data.validationMessages[m].message + '\n';
                assert.fail(msg);
            }else{
                assert.fail(error)
            }
        });
      });
    
      it('Get data source', function(){
          return client.restRequest({
              path: `/rest/v1/data-sources/${this.ds.xid}`,
              method: 'GET',
              data: {}
          }).then(response => {
              assertV1(response, this.ds);
          });
      });   
    
    
      it('Update data source', function() {
          const dsv1 = ds();
          dsv1.xid = uuidV4();
          dsv1.name = 'Test too';
          dsv1.enabled = false;
          dsv1.alarmLevels = {
                  DATA_RETRIEVAL_FAILURE_EVENT: 'URGENT',
                  POLL_ABORTED: 'URGENT'
          };
          dsv1.purgeSettings = {
              override: true,
              frequency: {
                  periods: 12,
                  type:'MONTHS'
              }
          };
          dsv1.editPermission = 'superadmin,testing';
          dsv1.stationId = 40;
          dsv1.dataStartTime = '2017-11-29T00:57:30.240Z';
          return client.restRequest({
              path:  `/rest/v1/data-sources/${this.ds.xid}`,
              method: 'PUT',
              data: dsv1
          }).then((response) => {
              this.ds = response.data;
              assertV1(response, dsv1);
          }, (error) => {
              if(error.status === 422){
                  var msg = 'Validation Failed: \n';
                  for(var m in error.data.validationMessages)
                      msg += error.data.validationMessages[m].property + '-->' + error.data.validationMessages[m].message + '\n';
                  assert.fail(msg);
              }else{
                  assert.fail(error)
              }
          });
        });
      
      afterEach('Delete data source', function () {
          return client.restRequest({
              path: `/rest/v1/data-sources/${this.ds.xid}`,
              method: 'DELETE',
              data: {}
          }).then(response => {
              assert.strictEqual(response.data.xid, this.ds.xid);
              assert.strictEqual(response.data.name, this.ds.name);
          });
      });
    
    function assertV1(response, dsv1){
        assert.isNumber(response.data.id);
        assert.isString(response.data.xid);
        assert.strictEqual(response.data.name, dsv1.name);
        assert.strictEqual(response.data.enabled, dsv1.enabled);
        assertPermissions(response.data.editPermission, dsv1.editPermission);
        assertAlarmLevels(response.data.alarmLevels, dsv1.alarmLevels);

        assert.strictEqual(response.data.stationId, dsv1.stationId);
        assert.strictEqual(new Date(response.data.dataStartTime).valueOf(), new Date(dsv1.dataStartTime).valueOf());
    }
});

describe('EnvCan data source v2', function() {
    before('Login', config.login);
    
    const ds = function() {
        return {
            name: 'Test',
            enabled: false,
            eventAlarmLevels: [
                {
                    eventType: 'DATA_RETRIEVAL_FAILURE_EVENT',
                    level: 'INFORMATION',
                 },
                 {
                     eventType: 'PARSE_EXCEPTION',
                     level: 'INFORMATION',
                 }
            ],
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            editPermission: ['superadmin', 'test'],
            stationId: 3,
            dataStartTime: '2017-11-19T00:57:30.240Z',
            modelType: 'EnvCan'
        }
    };
    
    beforeEach('Create data source', () => {
      const dsv2 = ds();
      return client.restRequest({
          path: '/rest/v2/data-sources',
          method: 'POST',
          data: dsv2
      }).then((response) => {
          this.ds = response.data;
          assertV2(response, dsv2);
      }, (error) => {
          if(error.status === 422){
              var msg = 'Validation Failed: \n';
              for(var m in error.data.result.messages)
                  msg += error.data.result.messages[m].property + '-->' + error.data.result.messages[m].message;
              assert.fail(msg);
          }else{
              assert.fail(error)
          }
      });
    });

    it('Update data source', () => {
        const dsv2 = ds();
        dsv2.xid = uuidV4();
        dsv2.name = 'Test too';
        dsv2.enabled = false;
        dsv2.eventAlarmLevels = [
            {
                eventType: 'DATA_RETRIEVAL_FAILURE_EVENT',
                level: 'URGENT',
             },
             {
                 eventType: 'POLL_ABORTED',
                 level: 'URGENT',
             }
        ];
        dsv2.purgeSettings = {
            override: true,
            frequency: {
                periods: 4,
                type: 'MONTHS'
            }
        };
        dsv2.editPermission = ['superadmin', 'test2'];
        dsv2.stationId = 40;
        dsv2.dataStartTime = '2013-11-19T00:57:30.240Z';
        return client.restRequest({
            path:  `/rest/v2/data-sources/${this.ds.xid}`,
            method: 'PUT',
            data: dsv2
        }).then((response) => {
            this.ds = response.data;
            assertV2(response, dsv2);
        }, (error) => {
            if(error.status === 422){
                var msg = 'Validation Failed: \n';
                for(var m in error.data.result.messages)
                    msg += error.data.result.messages[m].property + '-->' + error.data.result.messages[m].message;
                assert.fail(msg);
            }else{
                assert.fail(error)
            }
        });
      });
    
    afterEach('Delete data source', () => {
        return client.restRequest({
            path: `/rest/v2/data-sources/${this.ds.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.strictEqual(response.data.xid, this.ds.xid);
            assert.strictEqual(response.data.name, this.ds.name);
        });
    });
    
    function assertV2(response, dsv2){
        assert.isNumber(response.data.id);
        assert.isString(response.data.xid);
        assert.strictEqual(response.data.name, dsv2.name);
        assert.strictEqual(response.data.enabled, dsv2.enabled);
        assertPermissions(response.data.editPermission, dsv2.editPermission);
        assertAlarmLevels(response.data.alarmLevels, dsv2.alarmLevels);
        
        assert.strictEqual(response.data.stationId, dsv2.stationId);
        assert.strictEqual(new Date(response.data.dataStartTime).valueOf(), new Date(dsv2.dataStartTime).valueOf());
    }
});

function assertPermissions(saved, stored) {
    if(Array.isArray(saved)){
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            assert.include(saved, stored[i], stored[i] + ' was not found in permissions')
        }
    }else{
        assert.strictEqual(saved, stored);
    }

}

function assertAlarmLevels(saved, stored){
    if(Array.isArray(saved)) {
        
        var assertedEventTypes = [];
        for(var i=0; i<stored.length; i++){
            var found = false;
            for(var j=0; j<saved.length; j++){
                if(stored[i].eventType === saved[j].eventType){
                    found = true;
                    assert.strictEqual(saved.level, stored.level);
                    assertedEventTypes.push(saved[i].eventType)
                    break;
                }
            }
            if(found === false)
                assert.fail('Did not find event type: ' + stored[i].eventType);
            if(assertedEventTypes.length === stored.length)
                break;
        }
    }else{
        for(var i in stored){
            assert.strictEqual(saved[i], stored[i]);
        }
    }
    
}

