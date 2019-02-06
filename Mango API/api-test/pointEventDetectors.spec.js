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

describe('Point Event detector service', function() {
    this.timeout(5000);
    before('Login', config.login);
    
    it('Creates an event detector', () => {
        global.ped = {
          xid : "PED_mango_client_test",
          name : "When true.",
          sourceId : global.numDp.id,
          alarmLevel : 'URGENT',
          duration : {
              periods: 10,
              type: 'SECONDS'
          },
          limit: 10.0,
          resetLimit: 9.0,
          useResetLimit: true,
          notHigher: false,
          detectorType : "HIGH_LIMIT",
        };
        return client.restRequest({
            path: '/rest/v2/point-event-detectors',
            method: 'POST',
            data: global.ped
        }).then(response => {
            assert.strictEqual(response.data.name, global.ped.name);
            assert.strictEqual(response.data.sourceId, global.ped.sourceId);
            assert.strictEqual(response.data.alarmLevel, global.ped.alarmLevel);
            
            assert.strictEqual(response.data.duration.periods, global.ped.duration.periods);
            assert.strictEqual(response.data.duration.type, global.ped.duration.type);
            
            assert.strictEqual(response.data.limit, global.ped.limit);
            assert.strictEqual(response.data.resetLimit, global.ped.resetLimit);
            assert.strictEqual(response.data.useResetLimit, global.ped.useResetLimit);
            assert.strictEqual(response.data.notHigher, global.ped.notHigher);
            
            
            global.ped.id = response.data.id;
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
      });
    
    it('Query point event detector', () => {
        return client.restRequest({
            path: `/rest/v2/point-event-detectors?xid=${global.ped.xid}`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, global.ped.xid);
        });
      });
    
    function printValidationErrors(errors){
        var messages = '';
        for(var i=0; i<errors.result.messages.length; i++){
           messages += errors.result.messages[i].property + ' --> ' + errors.result.messages[i].message;
        }
        assert.fail(messages);
    }
    
    before('Create data source and points', function() {
      global.ds = new DataSource({
          xid: 'mango_client_test',
          name: 'Mango client test',
          enabled: true,
          modelType: 'VIRTUAL',
          pollPeriod: { periods: 5, type: 'SECONDS' },
          purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
          alarmLevels: { POLL_ABORTED: 'URGENT' },
          editPermission: null
      });

      return global.ds.save().then((savedDs) => {
          assert.strictEqual(savedDs, global.ds);
          assert.equal(savedDs.xid, 'mango_client_test');
          assert.equal(savedDs.name, 'Mango client test');
          assert.isNumber(savedDs.id);
          global.ds.id = savedDs.id;

          let promises = [];
          global.dp = new DataPoint({
                xid : "dp_mango_client_test",
                deviceName : "_",
                name : "Virtual Test Point 1",
                enabled : false,
                templateXid : "Binary_Default",
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
                  zeroLabel : "zero",
                  zeroColour : "blue",
                  oneLabel : "one",
                  oneColour : "black",
                  type : "textRendererBinary"
                },
                chartRenderer : {
                  limit : 10,
                  type : "chartRendererTable"
                },
                dataSourceXid : "mango_client_test",
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
                  startValue : "true",
                  modelType : "PL.VIRTUAL",
                  dataType : "BINARY",
                  settable : true,
                  changeType : "ALTERNATE_BOOLEAN",
                  relinquishable : false
                }
              });

          promises.push(global.dp.save().then((savedDp) => {
            assert.equal(savedDp.xid, 'dp_mango_client_test');
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            global.dp.id = savedDp.id; //Save the ID for later
          }));

          global.numDp = new DataPoint({
              xid : "dp_mango_client_test_num",
              deviceName : "_",
              name : "Virtual Test Point 3",
              enabled : false,
              templateXid : "Numeric_Default",
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
                  unit : "",
                  renderedUnit:"",
                  suffix:"",
                  type : "textRendererPlain"
              },
              chartRenderer : {
                limit : 10,
                type : "chartRendererTable"
              },
              dataSourceXid : "mango_client_test",
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
                startValue : "true",
                modelType : "PL.VIRTUAL",
                dataType : "NUMERIC",
                settable : true,
                changeType : "NO_CHANGE",
                relinquishable : false
              }
            });

          promises.push(global.numDp.save().then((savedDp) => {
          assert.equal(savedDp.xid, 'dp_mango_client_test_num');
          assert.equal(savedDp.name, 'Virtual Test Point 3');
          assert.equal(savedDp.enabled, false);
          assert.isNumber(savedDp.id);
          global.numDp.id = savedDp.id; //Save the ID for later
        }));

        global.mulDp = new DataPoint({
            xid : "dp_mango_client_test_mul",
            deviceName : "_",
            name : "Virtual Test Point 4",
            enabled : false,
            templateXid : "Multistate_Default",
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
                unit : "",
                renderedUnit:"",
                suffix:"",
                type : "textRendererPlain"
            },
            chartRenderer : {
              limit : 10,
              type : "chartRendererTable"
            },
            dataSourceXid : "mango_client_test",
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
              startValue : "3",
              modelType : "PL.VIRTUAL",
              dataType : "MULTISTATE",
              settable : true,
              changeType : "NO_CHANGE",
              relinquishable : false
            }
          });

    promises.push(global.mulDp.save().then((savedDp) => {
        assert.equal(savedDp.xid, 'dp_mango_client_test_mul');
        assert.equal(savedDp.name, 'Virtual Test Point 4');
        assert.equal(savedDp.enabled, false);
        assert.isNumber(savedDp.id);
        global.mulDp.id = savedDp.id; //Save the ID for later
      }));

          global.alphaDp = new DataPoint({
              xid : "dp_mango_client_test_alpha",
              deviceName : "_",
              name : "Virtual Test Point 2",
              enabled : false,
              templateXid : "Alphanumeric_Default",
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
                unit : "",
                renderedUnit:"",
                suffix:"",
                type : "textRendererPlain"
              },
              chartRenderer : {
                limit : 10,
                type : "chartRendererTable"
              },
              dataSourceXid : "mango_client_test",
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
                startValue : "",
                modelType : "PL.VIRTUAL",
                dataType : "ALPHANUMERIC",
                settable : true,
                changeType : "NO_CHANGE",
                relinquishable : false
              }
            });

      promises.push(global.alphaDp.save().then((savedDp) => {
          assert.equal(savedDp.xid, 'dp_mango_client_test_alpha');
          assert.equal(savedDp.name, 'Virtual Test Point 2');
          assert.equal(savedDp.enabled, false);
          assert.isNumber(savedDp.id);
          global.alphaDp.id = savedDp.id; //Save the ID for later
        }));
      return Promise.all(promises);
      });
    });

    //Clean up when done
    after('Deletes the new virtual data source and its points to clean up', () => {
        return DataSource.delete('mango_client_test');
    });
});
