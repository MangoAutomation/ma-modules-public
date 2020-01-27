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

const uuidV4 = require('uuid/v4');
const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const {DataSource, DataPoint} = client;

describe('Legacy MBus Data Source tests ', function() {
    before('Login', function() { return login.call(this, client); });

    it('Create MBus Serial data source', () => {

      const ds = new DataSource({
          xid : "DS_MBUS_SERIAL_TEST",
          name : "MBus Serial",
          enabled : false,
          modelType : "MBUS",
          connection : {
            bitPerSecond : 2400,
            responseTimeoutOffset : 50,
            portName : "/dev/ttys000",
            modelType : "mbusSerial"
          },
          quantize : false,
          pollPeriod : {
            periods : 1,
            type : "DAYS"
          },
          editPermission : "edit-test",
          purgeSettings : {
            override : false,
            frequency : {
              periods : 1,
              type : "YEARS"
            }
          },
          alarmLevels : {
            POINT_WRITE_EXCEPTION : "URGENT",
            DATA_SOURCE_EXCEPTION : "URGENT",
            POINT_READ_EXCEPTION : "URGENT"
          }
      });

      return ds.save().then((savedDs) => {
        assert.equal(savedDs.xid, 'DS_MBUS_SERIAL_TEST');
        assert.equal(savedDs.name, 'MBus Serial');
        assert.equal(savedDs.enabled, false);
        assert.equal(savedDs.connection.portName, '/dev/ttys000');
        assert.equal(savedDs.connection.bitPerSecond, 2400);
        assert.equal(savedDs.connection.responseTimeoutOffset, 50);
        assert.equal(savedDs.editPermission, "edit-test");
        assert.isNumber(savedDs.id);
      });
    });

    it('Create MBUS data point', () => {

      const dp = new DataPoint({
            xid : "DP_MBUS_SERIAL_TEST",
            deviceName : "MBus",
            name : "MBus Test Point 1",
            enabled : false,
            loggingProperties : {
              tolerance : 0.0,
              discardExtremeValues : false,
              discardLowLimit : -1.7976931348623157E308,
              discardHighLimit : 1.7976931348623157E308,
              loggingType : "ON_CHANGE",
              intervalLoggingType: "INSTANT",
              intervalLoggingPeriod : {
                periods : 15,
                type : "MINUTES"
              },
              overrideIntervalLoggingSamples : false,
              intervalLoggingSampleWindowSize : 0,
              cacheSize : 1
            },
            textRenderer : {
              type : 'textRendererAnalog',
              useUnitAsSuffix: true,
              unit: '',
              renderedUnit: '',
              format: '0.00'
            },
            chartRenderer : {
              limit : 10,
              type : "chartRendererTable"
            },
            dataSourceXid : "DS_MBUS_SERIAL_TEST",
            useIntegralUnit : false,
            useRenderedUnit : false,
            readPermission : "read",
            setPermission : "write",
            chartColour : "",
            rollup : "NONE",
            plotType : "STEP",
            purgeOverride : false,
            purgePeriod : {
              periods : 1,
              type : "YEARS"
            },
            unit : "",
            integralUnit : "s",
            renderedUnit : "",
            modelType : "DATA_POINT",
            pointLocator : {
              modelType: 'PL.MBUS',
              addressing: 'primary',
              address:1,
              dbIndex:0,
              difCode:"16 Bit Integer",
              effectiveSiPrefix:"",
              exponent:0,
              functionField:"Instantaneous value",
              identNumber:0,
              manufacturer:"ADF",
              medium:"Other",
              responseFrame:"default",
              siPrefix:"",
              storageNumber:0,
              subUnit:0,
              tariff:0,
              unitOfMeasurement:"V",
              version:2,
              vifLabel:"Voltage",
              vifType:"extention FD",
              vifeLabels:[],
              vifeTypes:[]
            },
          });

      return dp.save().then((savedDp) => {
        assert.equal(savedDp.xid, 'DP_MBUS_SERIAL_TEST');
        assert.equal(savedDp.name, 'MBus Test Point 1');
        assert.equal(savedDp.deviceName, 'MBus');
        assert.equal(savedDp.enabled, false);

        assert.equal(savedDp.pointLocator.address, 1);
        assert.equal(savedDp.pointLocator.dbIndex, 0);
        assert.equal(savedDp.pointLocator.difCode, "16 Bit Integer");
        assert.equal(savedDp.pointLocator.effectiveSiPrefix, "");

        assert.isNumber(savedDp.id);
      });
    });

    it('Copy MBus data source', () => {
      return client.restRequest({
          path: '/rest/v2/data-sources/copy/DS_MBUS_SERIAL_TEST?copyXid=DS_MBUS_SERIAL_TEST_COPY&copyName=MBUS_SERIAL_TEST_COPY_NAME',
          method: 'PUT'
      }).then(response => {
        assert.equal(response.data.xid, 'DS_MBUS_SERIAL_TEST_COPY');
        assert.equal(response.data.name, 'MBUS_SERIAL_TEST_COPY_NAME');
        assert.isNumber(response.data.id);
      });
    });

    it('Deletes the copy serial MBus data source and its point', () => {
        return DataSource.delete('DS_MBUS_SERIAL_TEST_COPY');
    });

    it('Deletes the new serial MBus data source and its point', () => {
        return DataSource.delete('DS_MBUS_SERIAL_TEST');
    });

    it('Create MBus tcpip data source', () => {

      const ds = new DataSource({
          xid : "DS_MBUS_TCPIP_TEST",
          name : "MBus TcpIp",
          enabled : false,
          modelType : "MBUS",
          connection : {
            bitPerSecond : 2400,
            responseTimeoutOffset : 50,
            host : "localhost",
            port : 12000,
            modelType : "mbusTcpIp"
          },
          quantize : false,
          pollPeriod : {
            periods : 1,
            type : "DAYS"
          },
          editPermission : "edit-test",
          purgeSettings : {
            override : false,
            frequency : {
              periods : 1,
              type : "YEARS"
            }
          },
          alarmLevels : {
            POINT_WRITE_EXCEPTION : "URGENT",
            DATA_SOURCE_EXCEPTION : "URGENT",
            POINT_READ_EXCEPTION : "URGENT"
          }
      });

      return ds.save().then((savedDs) => {
        assert.equal(savedDs.xid, 'DS_MBUS_TCPIP_TEST');
        assert.equal(savedDs.name, 'MBus TcpIp');
        assert.equal(savedDs.enabled, false);
        assert.equal(savedDs.connection.host, 'localhost');
        assert.equal(savedDs.connection.port, 12000);
        assert.equal(savedDs.connection.bitPerSecond, 2400);
        assert.equal(savedDs.connection.responseTimeoutOffset, 50);

        assert.equal(savedDs.editPermission, "edit-test");
        assert.isNumber(savedDs.id);
      });
    });

    it('Deletes the new tcpip MBus data source and its point', () => {
        return DataSource.delete('DS_MBUS_TCPIP_TEST');
    });
});

describe('Test MBus Data Source v1 ', function() {
    before('Login', function() { return login.call(this, client); });
    
    const ds = function() {
        return {
            name: 'Test',
            enabled: false,
            alarmLevels: {
                DATA_SOURCE_EXCEPTION: 'INFORMATION',
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
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            connection : {
                bitPerSecond : 2400,
                responseTimeoutOffset : 50,
                host : "localhost",
                port : 12000,
                modelType : "mbusTcpIp"
            },
            modelType: 'MBUS'
        };
    }
    
    beforeEach('Create serial data source v1', function() {
        const dsv1 = ds();
        return client.restRequest({
            path: '/rest/v2/data-sources',
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
    
    it('Get data source', function() {
        const dsv1 = ds();
        return client.restRequest({
            path: `/rest/v2/data-sources/${this.ds.xid}`,
            method: 'GET'
        }).then(response => {
            assertV1(response, dsv1);
        });
    });  
    
      it('Update data source v1', function() {
          const dsv1 = ds();
          dsv1.xid = uuidV4();
          dsv1.name = 'Test too';
          dsv1.enabled = false;
          dsv1.alarmLevels = {
                  POINT_WRITE_EXCEPTION: 'URGENT',
                  POINT_READ_EXCEPTION: 'URGENT'
          };
          dsv1.purgeSettings = {
              override: true,
              frequency: {
                  periods: 12,
                  type:'MONTHS'
              }
          };
          dsv1.editPermission = 'superadmin,testing';
          dsv1.pollPeriod = {
              periods: 5,
              type: 'SECONDS'
          };
          dsv1.connection = {
              bitPerSecond : 4800,
              responseTimeoutOffset : 503,
              portName: '/dev/null',
              modelType : "mbusSerial"
          };
          return client.restRequest({
              path:  `/rest/v2/data-sources/${this.ds.xid}`,
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
      
      afterEach('Delete data source', function() {
          return client.restRequest({
              path: `/rest/v2/data-sources/${this.ds.xid}`,
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

        assert.strictEqual(response.data.pollPeriod.periods, dsv1.pollPeriod.periods);
        assert.strictEqual(response.data.pollPeriod.type, dsv1.pollPeriod.type);

        assert.strictEqual(response.data.connection.bitPerSecond, dsv1.connection.bitPerSecond);
        assert.strictEqual(response.data.connection.responseTimeoutOffset, dsv1.connection.responseTimeoutOffset);
        if(dsv1.connection.modelType === 'mbusSerial'){
            assert.strictEqual(response.data.connection.portName, dsv1.connection.portName);
        }else {
            assert.strictEqual(response.data.connection.host, dsv1.connection.host);
            assert.strictEqual(response.data.connection.port, dsv1.connection.port);
        }
    }


});


describe('Test MBus Data Source v2 ', function() {
    before('Login', function() { return login.call(this, client); });
    
    const ds = function() {
        return {
            name: 'Test',
            enabled: false,
            eventAlarmLevels: [
                {
                    eventType: 'POINT_WRITE_EXCEPTION',
                    level: 'INFORMATION',
                 },
                 {
                     eventType: 'POINT_READ_EXCEPTION',
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
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            connection : {
                bitPerSecond : 2400,
                responseTimeoutOffset : 50,
                host : "localhost",
                port : 12000,
                modelType : "mbusTcpIp"
            },
            modelType: 'MBUS'
        };
    }
      
    beforeEach('Create data source', function() {
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
    
    it('Get data source', function() {
        const dsv2 = ds();
        return client.restRequest({
            path: `/rest/v2/data-sources/${this.ds.xid}`,
            method: 'GET'
        }).then(response => {
            assertV2(response, dsv2);
        });
    });  
    
    it('Update data source v2', function() {
        dsv2 = ds();
        dsv2.xid = uuidV4();
        dsv2.name = 'Test too';
        dsv2.enabled = false;
        dsv2.eventAlarmLevels = [
            {
                eventType: 'DATA_SOURCE_EXCEPTION',
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
        dsv2.pollPeriod = {
                periods: 5,
                type: 'SECONDS'
        };
        dsv2.connection = {
                bitPerSecond : 4800,
                responseTimeoutOffset : 503,
                commPortId: '/dev/null',
                modelType : "mbusSerial"
        };
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
    
    it('Delete data source v2', function() {
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
        
        assert.strictEqual(response.data.pollPeriod.periods, dsv2.pollPeriod.periods);
        assert.strictEqual(response.data.pollPeriod.type, dsv2.pollPeriod.type);

        assert.strictEqual(response.data.connection.bitPerSecond, dsv2.connection.bitPerSecond);
        assert.strictEqual(response.data.connection.responseTimeoutOffset, dsv2.connection.responseTimeoutOffset);
        if(dsv2.connection.modelType === 'mbusSerial'){
            assert.strictEqual(response.data.connection.commPortId, dsv2.connection.commPortId);
        }else {
            assert.strictEqual(response.data.connection.host, dsv2.connection.host);
            assert.strictEqual(response.data.connection.port, dsv2.connection.port);
        }
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
