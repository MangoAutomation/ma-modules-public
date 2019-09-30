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

const {createClient, login, defer, delay} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;

describe('Event detector service', function() {
    this.timeout(5000);
    
    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};
    
    before('Login', function() { return login.call(this, client); });
    before('Create data source and points', function() {
      testContext.ds = new DataSource({
          xid: 'mango_client_test',
          name: 'Mango client test',
          enabled: true,
          modelType: 'VIRTUAL',
          pollPeriod: { periods: 5, type: 'SECONDS' },
          purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
          alarmLevels: { POLL_ABORTED: 'URGENT' },
          editPermission: null
      });

      return testContext.ds.save().then((savedDs) => {
          assert.strictEqual(savedDs, testContext.ds);
          assert.equal(savedDs.xid, 'mango_client_test');
          assert.equal(savedDs.name, 'Mango client test');
          assert.isNumber(savedDs.id);
          testContext.ds.id = savedDs.id;

          let promises = [];
          testContext.dp = new DataPoint({
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

          promises.push(testContext.dp.save().then((savedDp) => {
            assert.equal(savedDp.xid, 'dp_mango_client_test');
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            testContext.dp.id = savedDp.id; //Save the ID for later
          }));

          testContext.numDp = new DataPoint({
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

          promises.push(testContext.numDp.save().then((savedDp) => {
          assert.equal(savedDp.xid, 'dp_mango_client_test_num');
          assert.equal(savedDp.name, 'Virtual Test Point 3');
          assert.equal(savedDp.enabled, false);
          assert.isNumber(savedDp.id);
          testContext.numDp.id = savedDp.id; //Save the ID for later
        }));

        testContext.mulDp = new DataPoint({
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

    promises.push(testContext.mulDp.save().then((savedDp) => {
        assert.equal(savedDp.xid, 'dp_mango_client_test_mul');
        assert.equal(savedDp.name, 'Virtual Test Point 4');
        assert.equal(savedDp.enabled, false);
        assert.isNumber(savedDp.id);
        testContext.mulDp.id = savedDp.id; //Save the ID for later
      }));

          testContext.alphaDp = new DataPoint({
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

      promises.push(testContext.alphaDp.save().then((savedDp) => {
          assert.equal(savedDp.xid, 'dp_mango_client_test_alpha');
          assert.equal(savedDp.name, 'Virtual Test Point 2');
          assert.equal(savedDp.enabled, false);
          assert.isNumber(savedDp.id);
          testContext.alphaDp.id = savedDp.id; //Save the ID for later
        }));
      return Promise.all(promises);
      });
    });

    it('Creates an event detector', () => {
      testContext.ped = {
        xid : "PED_mango_client_test",
        name : "When true.",
        duration : 10,
        durationType : "SECONDS",
        alarmLevel : "NONE",
        alias : "When true.",
        rtnApplicable : true,
        state: true,
        detectorSourceType : "DATA_POINT",
        sourceId : testContext.dp.id,
        detectorType : "BINARY_STATE",
      };
      return client.restRequest({
          path: '/rest/v2/event-detectors',
          method: 'POST',
          data: testContext.ped
      }).then(response => {
          testContext.ped.id = response.data.id;
      });
    });

    it('Updates an event detector', () => {
      testContext.ped.state = false;
      return client.restRequest({
          path: `/rest/v2/event-detectors/${testContext.ped.xid}`,
          method: 'PUT',
          data: testContext.ped
      }).then(response => {
        assert.equal(response.data.state, false);
      });
    });

    /* Validation Testing */
    it('Fails to create a no update detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_zsnu",
    	        name : "No update for zero seconds.",
    	        duration : 0,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "No update for zero seconds.",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.dp.id,
    	        detectorType : "NO_UPDATE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('No update detector created despite having a duration of zero.');
    	}).catch(response => {
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity") === -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create a no change detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_zsnc",
    	        name : "No change for zero seconds.",
    	        duration : 0,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "No change for zero seconds.",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.dp.id,
    	        detectorType : "NO_CHANGE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('No change detector created despite having a duration of zero.');
    	}).catch(response => {
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create a state change count detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_sccd",
    	        name : "No change for zero seconds.",
    	        count : 1,
    	        duration : 0,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "State changes once in zero seconds",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.dp.id,
    	        detectorType : "STATE_CHANGE_COUNT",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('State change count detector created despite 1 change in 0s');
    	}).catch(response => {
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Creates an alphanumeric regex detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_arsd",
    	        name : "Alphanumeric Regex detector.",
    	        state : ".*",
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Any alphanumeric state for ten or more seconds",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.alphaDp.id,
    	        detectorType : "ALPHANUMERIC_REGEX_STATE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		testContext.ped.id = response.data.id;
    	});
    });

    it('Fails to create an alphanumeric regex detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_arsd2",
    	        name : "Alphanumeric Regex detector.",
    	        state : "(.*{}",
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Illegal state",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.alphaDp.id,
    	        detectorType : "ALPHANUMERIC_REGEX_STATE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Alphanumeric regex event detector created even without a valid regex.');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog change detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_acd",
    	        name : "Analog change detector.",
    	        checkIncrease : false,
    	        checkDecrease : false,
    	        limit : 15,
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "UNCHECKED CHANGE",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "ANALOG_CHANGE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Analog change detector created that doesn\'t check for analog changes');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog high limit detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_hld",
    	        name : "High limit detector.",
    	        resetLimit : 10, //Cannot be below the limit if notHigher
    	        useResetLimit : true,
    	        notHigher : true,
    	        limit : 15,
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Infinite Resets",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "HIGH_LIMIT",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('High limit with invalid reset configuration created');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog low limit detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_lld",
    	        name : "Low limit detector.",
    	        resetLimit : 10, //Cannot be below the limit if !notLower
    	        useResetLimit : true,
    	        notLower : false,
    	        limit : 15,
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Infinite Resets",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "LOW_LIMIT",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Low limit with invalid reset configuration created');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog range detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_range",
    	        name : "Range detector.",
    	        high : 10,
    	        low : 50,
    	        withinRange : true,
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Lower high than low",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "RANGE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Analog range detector created with invalid range');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog negative cusum detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_ncu",
    	        name : "Range detector.",
    	        limit : 50,
    	        weight : "NaN",
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "NaN weight",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "NEGATIVE_CUSUM",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Analog negative cusum detector created with invalid weight');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog positive cusum detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_pcu",
    	        name : "Range detector.",
    	        limit : "NaN",
    	        weight : 50,
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "NaN limit",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "POSITIVE_CUSUM",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Analog positive cusum detector created with invalid limit');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create an analog smoothness detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_smooth",
    	        name : "Range detector.",
    	        limit : "NaN",
    	        boxcar : 1,
    	        duration : 10,
    	        durationType : "SECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Not a short boxcar",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.numDp.id,
    	        detectorType : "SMOOTHNESS",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Analog positive cusum detector created with invalid limit');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    it('Fails to create a multistate state detector', () => {
    	testContext.ped = {
    	        xid : "PED_mango_client_test_multi",
    	        name : "Range detector.",
    	        state : 1,
    	        duration : 0,
    	        durationType : "MICROSECONDS",
    	        alarmLevel : "NONE",
    	        alias : "Not a valid duration",
    	        rtnApplicable : true,
    	        detectorSourceType : "DATA_POINT",
    	        sourceId : testContext.mulDp.id,
    	        detectorType : "MULTISTATE_STATE",
    	      };
    	return client.restRequest({
    		path: '/rest/v2/event-detectors',
            method: 'POST',
            data: testContext.ped
    	}).then(response => {
    		throw new Error('Multistate state detector created with invalid duration');
    	}).catch(response => {
    		if(typeof response.response === 'undefined')
    			throw response;
    		if(typeof response.response.statusMessage !== 'string' || response.response.statusMessage.indexOf("Unprocessable Entity")=== -1)
    			throw "Received non-string or non 422 response: " + response.response.statusMessage;
    		assert.equal(response.response.statusCode, 422);
    	});
    });

    //TODO Get that detector
    //TODO Get the detectors for that point
    //TODO Get the detectors for the data source

    it('Query event detectors', () => {
      return client.restRequest({
          path: '/rest/v2/event-detectors',
          method: 'GET'
      }).then(response => {
        //TODO Confirm length of 1?
      });
    });

    it('Deletes an event detector', () => {
      return client.restRequest({
          path: `/rest/v2/event-detectors/PED_mango_client_test`,
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.xid, 'PED_mango_client_test');
      });
    });

    //Tests for websockets
    it('Gets websocket notifications for event detectors', function() {
      this.timeout(5000);
      
      const socketOpenDeferred = defer();
      const gotAddEventDeferred = defer();
      const gotUpdateEventDeferred = defer();
      const gotDeleteEventDeferred = defer();

      return Promise.resolve().then(() => {
          const ws = client.openWebSocket({
              path: '/rest/v1/websocket/event-detectors'
          });

          ws.on('open', () => {
              socketOpenDeferred.resolve();
          });

          ws.on('error', error => {
              const msg = new Error(`WebSocket error, error: ${error}`);
              socketOpenDeferred.reject(msg);
              gotAddEventDeferred.reject(msg);
              gotUpdateEventDeferred.reject(msg);
              gotDeleteEventDeferred.reject(msg);
          });

          ws.on('close', (code, reason) => {
              const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
              socketOpenDeferred.reject(msg);
              gotAddEventDeferred.reject(msg);
              gotUpdateEventDeferred.reject(msg);
              gotDeleteEventDeferred.reject(msg);
          });

          ws.on('message', msgStr => {
              assert.isString(msgStr);

              const msg = JSON.parse(msgStr);
              //console.log(msg.payload);
              assert.strictEqual(msg.status, 'OK');
              if(msg.payload.action === 'add'){
                assert.strictEqual(msg.payload.object.xid, 'PED_mango_client_test');
                assert.strictEqual(msg.payload.originalXid, null);
                gotAddEventDeferred.resolve();
              }else if(msg.payload.action === 'update'){
                assert.strictEqual(msg.payload.object.xid, 'PED_mango_client_test_update');
                assert.strictEqual(msg.payload.originalXid, 'PED_mango_client_test');
                gotUpdateEventDeferred.resolve();
              }else if(msg.payload.action === 'delete'){
                assert.strictEqual(msg.payload.object.xid, 'PED_mango_client_test_update');
                assert.strictEqual(msg.payload.originalXid, null);
                ws.close();
                gotDeleteEventDeferred.resolve();
              }
          });
          
          return socketOpenDeferred.promise;
        }).then(() => delay(500)).then(() => {
            //Create the event detector for add message
            //console.log('adding');
            return client.restRequest({
                path: '/rest/v2/event-detectors',
                method: 'POST',
                data: {
                  xid : "PED_mango_client_test",
                  name : "When true.",
                  duration : 10,
                  durationType : "SECONDS",
                  alarmLevel : "NONE",
                  alias : "When true.",
                  rtnApplicable : true,
                  state: true,
                  detectorSourceType : "DATA_POINT",
                  sourceId : testContext.dp.id,
                  detectorType : "BINARY_STATE",
                }
            });
        }).then(() => gotAddEventDeferred.promise).then(()=>{
          //Update the detector for update statusMessage
          //console.log('updating');
          return client.restRequest({
              path: '/rest/v2/event-detectors/PED_mango_client_test',
              method: 'PUT',
              data: {
                xid : "PED_mango_client_test_update",
                name : "When true.",
                duration : 10,
                durationType : "SECONDS",
                alarmLevel : "NONE",
                alias : "When true.",
                rtnApplicable : true,
                state: true,
                detectorSourceType : "DATA_POINT",
                sourceId : testContext.dp.id,
                detectorType : "BINARY_STATE",
              }
          });
        }).then(() => gotUpdateEventDeferred.promise).then(()=>{
          //Update the detector for update statusMessage
          //console.log('deleting');
          return client.restRequest({
              path: '/rest/v2/event-detectors/PED_mango_client_test_update',
              method: 'DELETE',
              data: {}
          });
        }).then(() => gotDeleteEventDeferred.promise);
    });

    //Clean up when done
    after('Deletes the new virtual data source and its points to clean up', () => {
        return DataSource.delete('mango_client_test');
    });
});
