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

const config = require('@infinite-automation/mango-client/test/setup');

describe('Internal data source', function() {
    before('Login', config.login);
    
    const dsv1 = {
            xid: 'DS_TEST',
            name: 'Test',
            enabled: false,
            alarmLevels: {
                POLL_ABORTED: 'INFORMATION'
            },
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            editPermission: 'superadmin,test',
            createPointsPattern: '.*',
            modelType: 'INTERNAL'
    };
    
    const dsv2 = {
            xid: 'DS_TEST',
            name: 'Test',
            enabled: false,
            eventAlarmLevels: [
                {
                    eventType: 'POLL_ABORTED',
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
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            editPermission: ['superadmin', 'test'],
            createPointsPattern: '.*',
            modelType: 'INTERNAL'
    };
    
    it('Create data source v1', () => {
        return client.restRequest({
            path: '/rest/v1/data-sources',
            method: 'POST',
            data: dsv1
        }).then((response) => {
            assertV1(response);
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
    
      it('Update data source v1', () => {
          dsv1.xid = 'DS_TEST_TOO';
          dsv1.name = 'Test too';
          dsv1.enabled = false;
          dsv1.alarmLevels = {
                  POLL_ABORTED: 'URGENT'
          };
          dsv1.purgeSettings = {
              override: true,
              frequency: {
                  periods: 12,
                  type:'MONTHS'
              }
          };
          dsv1.pollPeriod = {
                  periods: 5,
                  type: 'MINUTES'
              };
          dsv1.editPermission = 'superadmin,testing';
          dsv1.createPointsPattern = "test*";
          return client.restRequest({
              path:  `/rest/v1/data-sources/DS_TEST`,
              method: 'PUT',
              data: dsv1
          }).then((response) => {
              assertV1(response);
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
      
      it('Delete data source v1', () => {
          return client.restRequest({
              path: `/rest/v1/data-sources/${dsv1.xid}`,
              method: 'DELETE',
              data: {}
          }).then(response => {
              assert.strictEqual(response.data.xid, dsv1.xid);
              assert.strictEqual(response.data.name, dsv1.name);
          });
      });
      
    it('Create data source v2', () => {
      return client.restRequest({
          path: '/rest/v2/data-sources',
          method: 'POST',
          data: dsv2
      }).then((response) => {
          assertV2(response);
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

    it('Update data source v2', () => {
        dsv2.xid = 'DS_TEST_TWO';
        dsv2.name = 'Test too';
        dsv2.enabled = false;
        dsv2.eventAlarmLevels = [
             {
                 eventType: 'POLL_ABORTED',
                 level: 'URGENT',
             }
        ];
        dsv2.pollPeriod = {
                periods: 5,
                type: 'MINUTES'
            };
        dsv2.purgeSettings = {
            override: true,
            frequency: {
                periods: 4,
                type: 'MONTHS'
            }
        };
        
        dsv2.editPermission = ['superadmin', 'test2'];
        dsv1.createPointsPattern = "test2*";
        return client.restRequest({
            path:  `/rest/v2/data-sources/DS_TEST`,
            method: 'PUT',
            data: dsv2
        }).then((response) => {
            assertV2(response);
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
    
    it('Delete data source v2', () => {
        return client.restRequest({
            path: `/rest/v2/data-sources/${dsv2.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assertV2(response);
        });
    });
    
    function assertV1(response){
        assert.isNumber(response.data.id);
        assert.strictEqual(response.data.xid, dsv1.xid);
        assert.strictEqual(response.data.name, dsv1.name);
        assert.strictEqual(response.data.enabled, dsv1.enabled);
        assertPermissions(response.data.editPermission, dsv1.editPermission);
        assertAlarmLevels(response.data.alarmLevels, dsv1.alarmLevels);

        assert.strictEqual(response.data.pollPeriod.periods, dsv1.pollPeriod.periods);
        assert.strictEqual(response.data.pollPeriod.type, dsv1.pollPeriod.type);
        
        assert.strictEqual(response.data.createPointsPattern, dsv1.createPointsPattern);
    }
    
    function assertV2(response){
        assert.isNumber(response.data.id);
        assert.strictEqual(response.data.xid, dsv2.xid);
        assert.strictEqual(response.data.name, dsv2.name);
        assert.strictEqual(response.data.enabled, dsv2.enabled);
        assertPermissions(response.data.editPermission, dsv2.editPermission);
        assertAlarmLevels(response.data.alarmLevels, dsv2.alarmLevels);
        
        assert.strictEqual(response.data.pollPeriod.periods, dsv2.pollPeriod.periods);
        assert.strictEqual(response.data.pollPeriod.type, dsv2.pollPeriod.type);
        
        assert.strictEqual(response.data.createPointsPattern, dsv2.createPointsPattern);
        
    }
    
    function assertArray(saved, stored){
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            assert.strictEqual(saved[i], stored[i]);
        }
    }
    
    function assertScriptContext(saved, stored){
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            if(typeof saved[i].dataPointXid === 'undefined')
                assert.strictEqual(saved[i].xid, stored[i].xid);
            else
                assert.strictEqual(saved[i].dataPointXid, stored[i].dataPointXid);
            assert.strictEqual(saved[i].variableName, stored[i].variableName);
            assert.strictEqual(saved[i].contextUpdate, stored[i].contextUpdate);
        }
    }
    
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
});
