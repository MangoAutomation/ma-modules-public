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

describe('VMStat data source', function() {
    before('Login', config.login);

    const dsv1 = {
            xid: 'DS_TEST',
            name: 'Test',
            enabled: false,
            alarmLevels: {
                DATA_SOURCE_EXCEPTION: 'INFORMATION',
            },
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            editPermission: '"superadmin","test"',
            pollSeconds: 20,
            outputScale: 'LOWER_K',
            modelType: 'VMSTAT'
    };
    
    const dsv2 = {
            xid: 'DS_TEST',
            name: 'Test',
            enabled: false,
            eventAlarmLevels: [
                {
                    eventType: 'DATA_SOURCE_EXCEPTION',
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
            pollSeconds: 20,
            outputScale: 'LOWER_K',
            modelType: 'VMSTAT'
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
                for(var m in error.data.result.messages)
                    msg += error.data.result.messages[m].property + '-->' + error.data.result.messages[m].message.key;
                assert.fail(msg);
            }else{
                assert.fail(error)
            }
        });
      });

      it('Update data source v1', () => {
          dsv1.name='Test again';
          dsv1.pollSeconds = 25;
          dsv1.outputScale = 'UPPER_K';
          return client.restRequest({
              path:  `/rest/v1/data-sources/${dsv1.xid}`,
              method: 'PUT',
              data: dsv1
          }).then((response) => {
              assertV1(response);
          });
        });
      
      it('Delete data source v1', () => {
          return client.restRequest({
              path: `/rest/v1/data-sources/${dsv1.xid}`,
              method: 'DELETE',
              data: {}
          }).then(response => {
              assertV1(response);
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
                  msg += error.data.result.messages[m].property + '-->' + error.data.result.messages[m].message.key;
              assert.fail(msg);
          }else{
              assert.fail(error)
          }
      });
    });

    it('Update data source v2', () => {
        dsv2.name='Test again';
        dsv2.pollSeconds = 25;
        dsv2.outputScale = 'UPPER_K';
        return client.restRequest({
            path:  `/rest/v2/data-sources/${dsv2.xid}`,
            method: 'PUT',
            data: dsv2
        }).then((response) => {
            assertV2(response);
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

        assert.strictEqual(response.data.pollSeconds, dsv1.pollSeconds);
        assert.strictEqual(response.data.outputScale, dsv1.outputScale);
    }
    
    function assertV2(response){
        assert.isNumber(response.data.id);
        assert.strictEqual(response.data.xid, dsv2.xid);
        assert.strictEqual(response.data.name, dsv2.name);
        assert.strictEqual(response.data.enabled, dsv2.enabled);
        assertPermissions(response.data.editPermission, dsv2.editPermission);
        assertAlarmLevels(response.data.alarmLevels, dsv2.alarmLevels);

        assert.strictEqual(response.data.pollSeconds, dsv2.pollSeconds);
        assert.strictEqual(response.data.outputScale, dsv2.outputScale);

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
