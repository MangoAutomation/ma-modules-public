/**
 * Copyright 2017 Infinite Automation Systems Inc.
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

describe('Serial data source', function() {
    before('Login', config.login);

    const dsv1 = {
            xid: 'DS_TEST',
            name: 'Test',
            enabled: false,
            alarmLevels: {
                POINT_READ_PATTERN_MISMATCH_EVENT: 'INFORMATION',
                DATA_SOURCE_EXCEPTION: 'INFORMATION'
            },
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            editPermission: '"superadmin","test"',
            commPortId : '/dev/null',
            baudRate : 9600,
            flowControlIn: 'NONE',
            flowControlOut: 'NONE',
            dataBits: 'DATA_BITS_8',
            stopBits: 'STOP_BITS_1',
            parity: 'NONE',
            readTimeout : 500,
            retries : 5,
            useTerminator: false,
            messageTerminator: ';',
            messageRegex: '.*',
            pointIdentifierIndex: 1,
            hex: true,
            logIO: true,
            maxMessageSize: 512,
            ioLogFileSizeMBytes: 512,
            maxHistoricalIOLogs: 2,
            modelType: 'SERIAL'
    };
    
    const dsv2 = {
            xid: 'DS_TEST',
            name: 'Test',
            enabled: false,
            eventAlarmLevels: [
                {
                    eventType: 'DATA_SOURCE_EXCEPTION',
                    level: 'INFORMATION',
                 },
                 {
                     eventType: 'POINT_READ_PATTERN_MISMATCH_EVENT',
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
            commPortId : '/dev/null',
            baudRate : 9600,
            flowControlIn: 'NONE',
            flowControlOut: 'NONE',
            dataBits: 'DATA_BITS_8',
            stopBits: 'STOP_BITS_1',
            parity: 'NONE',
            readTimeout : 500,
            retries : 5,
            useTerminator: false,
            messageTerminator: ';',
            messageRegex: '.*',
            pointIdentifierIndex: 1,
            hex: true,
            logIO: true,
            maxMessageSize: 512,
            ioLogFileSizeMBytes: 512,
            maxHistoricalIOLogs: 2,
            modelType: 'SERIAL'
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
                    msg += error.data.validationMessages[m].property + '-->' + error.data.validationMessages[m].message.key;
                assert.fail(msg);
            }else{
                assert.fail(error)
            }
        });
      });

      it('Update data source v1', () => {
          dsv1.xid = 'DS_TEST_TOO';
          dsv1.name = 'Test too';
          dsv1.enabled = true;
          dsv1.alarmLevels = {
              POINT_READ_PATTERN_MISMATCH_EVENT: 'URGENT',
              DATA_SOURCE_EXCEPTION: 'URGENT'
          };
          dsv1.purgeSettings = {
              override: true,
              frequency: {
                  periods: 12,
                  type:'MONTHS'
              }
          };
          dsv1.editPermission = '"superadmin","testing"';
          
          dsv1.commPortId = '/dev/null';
          dsv1.baudRate = 9600;
          dsv1.flowControlIn = 'RTSCTS';
          dsv1.flowControlOut = 'RTSCTS';
          dsv1.dataBits = 'DATA_BITS_5';
          dsv1.stopBits = 'STOP_BITS_2';
          dsv1.parity = 'ODD';
          dsv1.readTimeout  = 5001;
          dsv1.retries  = 51;
          dsv1.useTerminator = true;
          dsv1.messageTerminator = ' =';
          dsv1.messageRegex = 'test';
          dsv1.pointIdentifierIndex = 2;
          dsv1.hex = false;
          dsv1.logIO = false;
          dsv1.maxMessageSize = 522;
          dsv1.ioLogFileSizeMBytes = 52;
          dsv1.maxHistoricalIOLogs = 22;
          
          return client.restRequest({
              path:  `/rest/v1/data-sources/DS_TEST`,
              method: 'PUT',
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
        dsv2.xid = 'DS_TEST_TWO';
        dsv2.name = 'Test too';
        dsv2.enabled = true;
        dsv2.eventAlarmLevels = [
            {
                eventType: 'DATA_SOURCE_EXCEPTION',
                level: 'URGENT',
             },
             {
                 eventType: 'POINT_READ_PATTERN_MISMATCH_EVENT',
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

        dsv2.commPortId = '/dev/null';
        dsv2.baudRate = 9600;
        dsv2.flowControlIn = 'RTSCTS';
        dsv2.flowControlOut = 'XONXOFF';
        dsv2.dataBits = 'DATA_BITS_7';
        dsv2.stopBits = 'STOP_BITS_1_5';
        dsv2.parity = 'EVEN';
        dsv2.readTimeout  = 5001;
        dsv2.retries  = 51;
        dsv2.useTerminator = true;
        dsv2.messageTerminator = ':';
        dsv2.messageRegex = '(.?)';
        dsv2.pointIdentifierIndex = 21;
        dsv2.hex = false;
        dsv2.logIO = false;
        dsv2.maxMessageSize = 511;
        dsv2.ioLogFileSizeMBytes = 32;
        dsv2.maxHistoricalIOLogs = 3;
        
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
                    msg += error.data.result.messages[m].property + '-->' + error.data.result.messages[m].message.key;
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
        
        assert.strictEqual(response.data.commPortId, dsv1.commPortId);
        assert.strictEqual(response.data.baudRate, dsv1.baudRate);
        assert.strictEqual(response.data.flowControlIn, dsv1.flowControlIn);
        assert.strictEqual(response.data.flowControlOut, dsv1.flowControlOut);
        assert.strictEqual(response.data.dataBits, dsv1.dataBits);
        assert.strictEqual(response.data.stopBits, dsv1.stopBits);
        assert.strictEqual(response.data.parity, dsv1.parity);
        assert.strictEqual(response.data.readTimeout, dsv1.readTimeout);
        assert.strictEqual(response.data.retries, dsv1.retries);
        assert.strictEqual(response.data.useTerminator, dsv1.useTerminator);
        assert.strictEqual(response.data.messageTerminator, dsv1.messageTerminator);
        assert.strictEqual(response.data.messageRegex, dsv1.messageRegex);
        assert.strictEqual(response.data.pointIdentifierIndex, dsv1.pointIdentifierIndex);
        assert.strictEqual(response.data.hex, dsv1.hex);
        assert.strictEqual(response.data.logIO, dsv1.logIO);
        assert.strictEqual(response.data.maxMessageSize, dsv1.maxMessageSize);
        assert.strictEqual(response.data.ioLogFileSizeMBytes, dsv1.ioLogFileSizeMBytes);
        assert.strictEqual(response.data.maxHistoricalIOLogs, dsv1.maxHistoricalIOLogs);
    }
    
    function assertV2(response){
        assert.isNumber(response.data.id);
        assert.strictEqual(response.data.xid, dsv2.xid);
        assert.strictEqual(response.data.name, dsv2.name);
        assert.strictEqual(response.data.enabled, dsv2.enabled);
        assertPermissions(response.data.editPermission, dsv2.editPermission);
        assertAlarmLevels(response.data.alarmLevels, dsv2.alarmLevels);
        
        assert.strictEqual(response.data.commPortId, dsv2.commPortId);
        assert.strictEqual(response.data.baudRate, dsv2.baudRate);
        assert.strictEqual(response.data.flowControlIn, dsv2.flowControlIn);
        assert.strictEqual(response.data.flowControlOut, dsv2.flowControlOut);
        assert.strictEqual(response.data.dataBits, dsv2.dataBits);
        assert.strictEqual(response.data.stopBits, dsv2.stopBits);
        assert.strictEqual(response.data.parity, dsv2.parity);
        assert.strictEqual(response.data.readTimeout, dsv2.readTimeout);
        assert.strictEqual(response.data.retries, dsv2.retries);
        assert.strictEqual(response.data.useTerminator, dsv2.useTerminator);
        assert.strictEqual(response.data.messageTerminator, dsv2.messageTerminator);
        assert.strictEqual(response.data.messageRegex, dsv2.messageRegex);
        assert.strictEqual(response.data.pointIdentifierIndex, dsv2.pointIdentifierIndex);
        assert.strictEqual(response.data.hex, dsv2.hex);
        assert.strictEqual(response.data.logIO, dsv2.logIO);
        assert.strictEqual(response.data.maxMessageSize, dsv2.maxMessageSize);
        assert.strictEqual(response.data.ioLogFileSizeMBytes, dsv2.ioLogFileSizeMBytes);
        assert.strictEqual(response.data.maxHistoricalIOLogs, dsv2.maxHistoricalIOLogs);
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
