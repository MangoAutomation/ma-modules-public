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

describe('Test MBus Data Source REST', function() {
    before('Login', config.login);

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

    //TODO Create SNMP Point
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
            pointFolderId : 0,
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
          path: '/rest/v1/data-sources/copy/DS_MBUS_SERIAL_TEST?copyXid=DS_MBUS_SERIAL_TEST_COPY&copyName=MBUS_SERIAL_TEST_COPY_NAME',
          method: 'PUT'
      }).then(response => {
        assert.equal(response.data.xid, 'DS_MBUS_SERIAL_TEST_COPY');
        assert.equal(response.data.name, 'MBUS_SERIAL_TEST_COPY_NAME');
        assert.isNumber(response.data.id);
      });
    });

    it('Deletes the copy serial snmp data source and its point', () => {
        return DataSource.delete('DS_MBUS_SERIAL_TEST_COPY');
    });

    it('Deletes the new serial snmp data source and its point', () => {
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

    it('Deletes the new tcpip snmp data source and its point', () => {
        return DataSource.delete('DS_MBUS_TCPIP_TEST');
    });

});
