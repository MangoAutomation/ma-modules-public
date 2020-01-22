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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;

describe('Test Event Handlers Endpoints', function() {
    
    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};
    
    before('Login', function() { return login.call(this, client); });

    before('create data source and points', () => {
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
            return Promise.all(promises);
    	});

    });

    it('Create set point event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers',
          method: 'POST',
          data: {
              xid : "EVTH_SET_POINT_TEST",
              name : "Testing setpoint",
              disabled : false,
              targetPointId : testContext.dp.id,
              activePointId : testContext.dp.id,
              inactivePointId : testContext.dp.id,
              activeAction : "STATIC_VALUE",
              inactiveAction : "STATIC_VALUE",
              activeValueToSet : "false",
              inactiveValueToSet : "true",
              eventType: [],
              additionalContext: [],
              scriptPermissions: null,
              eventTypes: [{
                  dataSourceId: testContext.ds.id,
                  dataSourceEventTypeId: 1, //POLL_ABORTED,
                  typeName: 'DATA_SOURCE'
              }],
              handlerType : "SET_POINT"
            }
      }).then(response => {
        assert.equal(response.data.xid, 'EVTH_SET_POINT_TEST');
        assert.equal(response.data.name, 'Testing setpoint');
        assert.equal(response.data.targetPointId, testContext.dp.id);
        assert.equal(response.data.activePointId, testContext.dp.id);
        assert.equal(response.data.inactivePointId, testContext.dp.id);
        assert.equal(response.data.activeAction, "STATIC_VALUE");
        assert.equal(response.data.inactiveAction, "STATIC_VALUE");
        assert.equal(response.data.activeValueToSet, "false");
        assert.equal(response.data.inactiveValueToSet, "true");
        assert.equal(response.data.eventTypes[0].dataSourceId, testContext.ds.id);
        assert.equal(response.data.eventTypes[0].dataSourceEventTypeId, 1);
        assert.isNumber(response.data.id);
      });
    });

    it('Delete set point event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers/EVTH_SET_POINT_TEST',
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.xid, 'EVTH_SET_POINT_TEST');
          assert.equal(response.data.name, 'Testing setpoint');
          assert.isNumber(response.data.id);
      });
    });


    it('Create email event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers',
          method: 'POST',
          data: {
              xid : "EVTH_EMAIL_TEST",
              name : "Testing email",
              disabled : false,
              activeRecipients: [ {
                username : "admin",
                type : "USER"
              } ],
              escalationDelay : 5,
              escalationDelayType : "MINUTES",
              escalationRecipients : [ ],
              sendEscalation : false,
              sendInactive : false,
              inactiveOverride : false,
              inactiveRecipients : [ ],
              includeSystemInfo : true,
              includePointValueCount : 10,
              includeLogfile : true,
              customTemplate : "",
              eventType: [],
//              eventType : {
//                refId1 : 0,
//                duplicateHandling : "ALLOW",
//                typeName : "SYSTEM",
//                systemEventType : "SYSTEM_STARTUP",
//                rateLimited : false
//              },
              handlerType : "EMAIL"
            }
      }).then(response => {
        assert.equal(response.data.xid, 'EVTH_EMAIL_TEST');
        assert.equal(response.data.name, 'Testing email');
        assert.equal(response.data.activeRecipients[0].username, "admin");
        assert.equal(response.data.escalationDelay, 5);
        assert.equal(response.data.escalationDelayType, "MINUTES");
        assert.equal(response.data.includePointValueCount, 10);
        assert.equal(response.data.includeSystemInfo, true);
        assert.equal(response.data.includeLogfile, true);

        assert.isNumber(response.data.id);
      });
    });

    it('Delete email event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers/EVTH_EMAIL_TEST',
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.xid, 'EVTH_EMAIL_TEST');
          assert.equal(response.data.name, 'Testing email');
          assert.isNumber(response.data.id);
      });
    });

    it('Create process event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers',
          method: 'POST',
          data: {
              xid : "EVTH_PROCESS_TEST",
              name : "Testing process",
              disabled : false,
              activeProcessCommand : "ls",
              activeProcessTimeout : 19,
              inactiveProcessCommand : "ls -la",
              inactiveProcessTimeout : 7,
              eventType: [],
//              eventType : {
//                refId1 : 0,
//                duplicateHandling : "ALLOW",
//                typeName : "SYSTEM",
//                systemEventType : "SYSTEM_STARTUP",
//                rateLimited : false
//              },
              handlerType : "PROCESS"
            }
      }).then(response => {
        assert.equal(response.data.xid, 'EVTH_PROCESS_TEST');
        assert.equal(response.data.name, 'Testing process');
        assert.equal(response.data.activeProcessCommand, "ls");
        assert.equal(response.data.activeProcessTimeout, 19);
        assert.equal(response.data.inactiveProcessCommand, "ls -la");
        assert.equal(response.data.inactiveProcessTimeout, 7);
        assert.isNumber(response.data.id);
      });
    });

    it('Update process event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers/EVTH_PROCESS_TEST',
          method: 'PUT',
          data: {
              xid : "EVTH_PROCESS_TEST",
              name : "Testing process edit",
              disabled : false,
              activeProcessCommand : "ls",
              activeProcessTimeout : 19,
              inactiveProcessCommand : "ls -la",
              inactiveProcessTimeout : 7,
              eventType: [],
//              eventType : {
//                refId1 : 0,
//                duplicateHandling : "ALLOW",
//                typeName : "SYSTEM",
//                systemEventType : "SYSTEM_STARTUP",
//                rateLimited : false
//              },
              handlerType : "PROCESS"
            }
      }).then(response => {
        assert.equal(response.data.xid, 'EVTH_PROCESS_TEST');
        assert.equal(response.data.name, 'Testing process edit');
        assert.equal(response.data.activeProcessCommand, "ls");
        assert.equal(response.data.activeProcessTimeout, 19);
        assert.equal(response.data.inactiveProcessCommand, "ls -la");
        assert.equal(response.data.inactiveProcessTimeout, 7);
        assert.isNumber(response.data.id);
      });
    });

    it('Get process event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers/EVTH_PROCESS_TEST',
          method: 'GET'
      }).then(response => {
        assert.equal(response.data.xid, 'EVTH_PROCESS_TEST');
        assert.equal(response.data.name, 'Testing process edit');
        assert.equal(response.data.activeProcessCommand, "ls");
        assert.equal(response.data.activeProcessTimeout, 19);
        assert.equal(response.data.inactiveProcessCommand, "ls -la");
        assert.equal(response.data.inactiveProcessTimeout, 7);
        assert.isNumber(response.data.id);
      });
    });

    it('Query process event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers?xid=EVTH_PROCESS_TEST',
          method: 'GET'
      }).then(response => {
        assert.equal(response.data.items.length, 1);
        assert.equal(response.data.total, 1);
        assert.equal(response.data.items[0].xid, 'EVTH_PROCESS_TEST');
        assert.equal(response.data.items[0].name, 'Testing process edit');
        assert.equal(response.data.items[0].activeProcessCommand, "ls");
        assert.equal(response.data.items[0].activeProcessTimeout, 19);
        assert.equal(response.data.items[0].inactiveProcessCommand, "ls -la");
        assert.equal(response.data.items[0].inactiveProcessTimeout, 7);
        assert.isNumber(response.data.items[0].id);
      });
    });

    it('Delete process event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers/EVTH_PROCESS_TEST',
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.xid, 'EVTH_PROCESS_TEST');
          assert.equal(response.data.name, 'Testing process edit');
          assert.isNumber(response.data.id);
      });
    });

  //Clean up when done
    after('Deletes the new virtual data source and its points to clean up', () => {
        return DataSource.delete('mango_client_test');
    });
});
