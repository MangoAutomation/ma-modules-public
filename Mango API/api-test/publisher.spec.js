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

describe('Publisher service', () => {
    before('Login', config.login);
    before('Create data source and point', function() {
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

          return global.dp.save().then((savedDp) => {
            assert.equal(savedDp.xid, 'dp_mango_client_test');
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            global.dp.id = savedDp.id; //Save the ID for later
          });

      });
    });

    it('Creates an HTTP Sender publisher', () => {
      global.publisher = {
        enabled : false,
        dateFormat : "DATE_FORMAT_BASIC",
        url : "http://www.terrypacker.com",
        usePost : false,
        useJSON : false,
        staticHeaders : [ {
          key : "test",
          value : "value"
        } ],
        staticParameters : [ {
          key : "test",
          value : "value"
        } ],
        raiseResultWarning : true,
        modelType : "HTTP_SENDER",
        points : [ {
          parameterName : "Meter 3 - Power Factor A",
          includeTimestamp : true,
          modelType : "PUB-POINT-HTTP_SENDER",
          dataPointXid : global.dp.xid
        } ],
        publishType : "ALL",
        cacheWarningSize : 100,
        cacheDiscardSize : 1000,
        sendSnapshot : false,
        snapshotSendPeriodType : "MINUTES",
        snapshotSendPeriods : 5,
        validationMessages : [ ],
        name : "HTTP",
        xid : "PUB_mango-client-test"
      };
      return client.restRequest({
          path: '/rest/v2/publishers',
          method: 'POST',
          data: global.publisher
      }).then(response => {
        global.publisher.id = response.data.id;
      });
    });

    it('Updates an HTTP Sender publisher', () => {
      global.publisher.name = "HTTP-Modified";
      return client.restRequest({
          path: `/rest/v2/publishers/${global.publisher.xid}`,
          method: 'PUT',
          data: global.publisher
      }).then(response => {
        assert.equal(response.data.name, "HTTP-Modified");
      });
    });

    //TODO Get that detector
    //TODO Get the detectors for that point
    //TODO Get the detectors for the data source

    it('Query publishers', () => {
      return client.restRequest({
          path: '/rest/v2/publishers',
          method: 'GET'
      }).then(response => {
        //TODO Confirm length of 1?
      });
    });

    it('Deletes the HTTP Sender publisher', () => {
      return client.restRequest({
          path: `/rest/v2/publishers/${global.publisher.xid}`,
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.id, global.publisher.id);
      });
    });

    //Clean up when done
    after('Deletes the new virtual data source and its points to clean up', () => {
        return DataSource.delete('mango_client_test');
    });
});
