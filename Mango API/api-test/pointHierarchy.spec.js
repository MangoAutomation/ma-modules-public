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
//TODO This test is skipped because it currently doesn't test the PH.
const config = require('@infinite-automation/mango-client/test/setup');

describe.skip('Point Hierarchy service', () => {
    before('Login', config.login);

    it('Creates a new virtual data source', () => {
        const ds = new DataSource({
            xid: 'mango_client_test',
            name: 'Mango client test',
            deviceName: 'Mango client device name',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return ds.save().then((savedDs) => {
            assert.strictEqual(savedDs, ds);
            assert.equal(savedDs.xid, 'mango_client_test');
            assert.equal(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
        });
    });

    it('Create binary virtual data point', () => {

      const dp = new DataPoint({
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

      return dp.save().then((savedDp) => {
        assert.equal(savedDp.xid, 'dp_mango_client_test');
        assert.equal(savedDp.name, 'Virtual Test Point 1');
        assert.equal(savedDp.enabled, false);
        assert.isNumber(savedDp.id);
      });
    });

    //TODO Create a new folder for the Data Point

    it('Deletes the new virtual data source and its points', () => {
        return DataSource.delete('mango_client_test');
    });

});
