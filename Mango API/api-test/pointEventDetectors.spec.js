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
    
    const highLimitPed = {
                name : "When true.",
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
    
    it('Creates a HIGH_LIMIT event detector', () => {
        highLimitPed.sourceId = numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            
            assert.strictEqual(response.data.name, highLimitPed.name);
            assert.strictEqual(response.data.sourceId, highLimitPed.sourceId);
            assert.strictEqual(response.data.alarmLevel, highLimitPed.alarmLevel);
            
            assert.strictEqual(response.data.duration.periods, highLimitPed.duration.periods);
            assert.strictEqual(response.data.duration.type, highLimitPed.duration.type);
            
            assert.strictEqual(response.data.limit, highLimitPed.limit);
            assert.strictEqual(response.data.resetLimit, highLimitPed.resetLimit);
            assert.strictEqual(response.data.useResetLimit, highLimitPed.useResetLimit);
            assert.strictEqual(response.data.notHigher, highLimitPed.notHigher);
            
            assert.strictEqual(response.data.dataPoint.id, highLimitPed.sourceId);
            assert.strictEqual(response.data.dataPoint.xid, numDp.xid);
            assert.strictEqual(response.data.dataPoint.pointLocator.dataType, numDp.pointLocator.dataType);
            
            
            highLimitPed.xid = response.data.xid;
            highLimitPed.id = response.data.id;
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
    });
    
    it('Add event handler to a HIGH_LIMIT event detector', () => {
        highLimitPed.handlerXids = [staticValueSetPointEventHandler.xid];
        return client.restRequest({
            path: `/rest/v2/full-event-detectors/${highLimitPed.xid}`,
            method: 'PUT',
            data: highLimitPed
        }).then(response => {
            assert.strictEqual(response.data.name, highLimitPed.name);
            assert.strictEqual(response.data.sourceId, highLimitPed.sourceId);
            assert.strictEqual(response.data.alarmLevel, highLimitPed.alarmLevel);
            
            assert.strictEqual(response.data.duration.periods, highLimitPed.duration.periods);
            assert.strictEqual(response.data.duration.type, highLimitPed.duration.type);
            
            assert.strictEqual(response.data.limit, highLimitPed.limit);
            assert.strictEqual(response.data.resetLimit, highLimitPed.resetLimit);
            assert.strictEqual(response.data.useResetLimit, highLimitPed.useResetLimit);
            assert.strictEqual(response.data.notHigher, highLimitPed.notHigher);
            
            assert.strictEqual(response.data.handlerXids.length, highLimitPed.handlerXids.length);
            for(var i=0; i<response.data.handlerXids.length; i++)
                assert.strictEqual(response.data.handlerXids[i], highLimitPed.handlerXids[i]);
            
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
    });
    
    it('Fail to add non-existant event handler from a HIGH_LIMIT event detector', () => {
        const oldHandlerXids = highLimitPed.handlerXids.slice();
        highLimitPed.handlerXids = ['nothing-at-all'];
        return client.restRequest({
            path: `/rest/v2/full-event-detectors/${highLimitPed.xid}`,
            method: 'PUT',
            data: highLimitPed
        }).then(response => {
            highLimitPed.handlerXids = oldHandlerXids;
            assert.fail('Should have been invalid');
        }, error => {
            highLimitPed.handlerXids = oldHandlerXids;
            if(error.status !== 422){
                assert.fail('Should have been invalid');
            }
        });
    });
    
    it('Get a HIGH_LIMIT event detector', () => {
        return client.restRequest({
            path: `/rest/v2/full-event-detectors/${highLimitPed.xid}`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.name, highLimitPed.name);
            assert.strictEqual(response.data.sourceId, highLimitPed.sourceId);
            assert.strictEqual(response.data.alarmLevel, highLimitPed.alarmLevel);
            
            assert.strictEqual(response.data.duration.periods, highLimitPed.duration.periods);
            assert.strictEqual(response.data.duration.type, highLimitPed.duration.type);
            
            assert.strictEqual(response.data.limit, highLimitPed.limit);
            assert.strictEqual(response.data.resetLimit, highLimitPed.resetLimit);
            assert.strictEqual(response.data.useResetLimit, highLimitPed.useResetLimit);
            assert.strictEqual(response.data.notHigher, highLimitPed.notHigher);
            
            assert.strictEqual(response.data.handlerXids.length, highLimitPed.handlerXids.length);
            for(var i=0; i<response.data.handlerXids.length; i++)
                assert.strictEqual(response.data.handlerXids[i], highLimitPed.handlerXids[i]);
            
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
    });
    
    it('Remove event handler from a HIGH_LIMIT event detector', () => {
        highLimitPed.handlerXids = [];
        return client.restRequest({
            path: `/rest/v2/full-event-detectors/${highLimitPed.xid}`,
            method: 'PUT',
            data: highLimitPed
        }).then(response => {
            assert.strictEqual(response.data.name, highLimitPed.name);
            assert.strictEqual(response.data.sourceId, highLimitPed.sourceId);
            assert.strictEqual(response.data.alarmLevel, highLimitPed.alarmLevel);
            
            assert.strictEqual(response.data.duration.periods, highLimitPed.duration.periods);
            assert.strictEqual(response.data.duration.type, highLimitPed.duration.type);
            
            assert.strictEqual(response.data.limit, highLimitPed.limit);
            assert.strictEqual(response.data.resetLimit, highLimitPed.resetLimit);
            assert.strictEqual(response.data.useResetLimit, highLimitPed.useResetLimit);
            assert.strictEqual(response.data.notHigher, highLimitPed.notHigher);
            
            assert.strictEqual(response.data.handlerXids.length, highLimitPed.handlerXids.length);
            for(var i=0; i<response.data.handlerXids.length; i++)
                assert.strictEqual(response.data.handlerXids[i], highLimitPed.handlerXids[i]);
            
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
    });
    
    it('Query point event detector on xid', () => {
        return client.restRequest({
            path: `/rest/v2/full-event-detectors?xid=${highLimitPed.xid}`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, highLimitPed.xid);
        });
      });
    
    it('Query point event detector on data point id', () => {
        return client.restRequest({
            path: `/rest/v2/full-event-detectors?sourceId=${highLimitPed.sourceId}`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, highLimitPed.xid);
        });
      });
    
    it('Query point event detector on source type name', () => {
        return client.restRequest({
            path: `/rest/v2/full-event-detectors?detectorSourceType=DATA_POINT`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, highLimitPed.xid);
        });
      });
    
    function printValidationErrors(errors){
        var messages = '';
        for(var i=0; i<errors.result.messages.length; i++){
           messages += errors.result.messages[i].property + ' --> ' + errors.result.messages[i].message;
        }
        assert.fail(messages);
    }

    //Data Points and Sources for tests
    
    const ds = new DataSource({
        name: 'Mango client test',
        enabled: false,
        modelType: 'VIRTUAL',
        pollPeriod: { periods: 5, type: 'SECONDS' },
        purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
        alarmLevels: { POLL_ABORTED: 'URGENT' },
        editPermission: null
    });
    
    const dp = new DataPoint({
        name: 'Virtual Test Point 1',
        pointLocator : {
            startValue : "true",
            modelType : "PL.VIRTUAL",
            dataType : "BINARY",
            settable : true,
            changeType : "ALTERNATE_BOOLEAN",
        }
    });
    
    const numDp = new DataPoint({
        name: 'Virtual Test Point 2',
        pointLocator : {
            startValue : "0",
            modelType : "PL.VIRTUAL",
            dataType : "NUMERIC",
            settable : true,
            changeType : "NO_CHANGE",
        }
    });
    
    const mulDp = new DataPoint({
        name: 'Virtual Test Point 3',
        pointLocator : {
            startValue : "3",
            modelType : "PL.VIRTUAL",
            dataType : "MULTISTATE",
            settable : true,
            changeType : "NO_CHANGE",
        }
    });
    
    const alphaDp = new DataPoint({
        name: 'Virtual Test Point 4',
        pointLocator : {
            startValue : "",
            modelType : "PL.VIRTUAL",
            dataType : "ALPHANUMERIC",
            settable : true,
            changeType : "NO_CHANGE",
        }
    });
    
    before('Create data source and points', function() {

      return ds.save().then((savedDs) => {
          assert.strictEqual(savedDs, ds);
          assert.equal(savedDs.name, 'Mango client test');
          assert.isNumber(savedDs.id);
          ds.xid = savedDs.xid;
          ds.id = savedDs.id;

          let promises = [];
          
          dp.dataSourceXid = ds.xid;
          promises.push(dp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            dp.xid = savedDp.xid;
            dp.id = savedDp.id; //Save the ID for later
          }));

          numDp.dataSourceXid = ds.xid;
          promises.push(numDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 2');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            numDp.xid = savedDp.xid;
            numDp.id = savedDp.id; //Save the ID for later
          }));

          mulDp.dataSourceXid = ds.xid;
          promises.push(mulDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 3');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            mulDp.xid = savedDp.xid;
            mulDp.id = savedDp.id; //Save the ID for later
          }));
          
          alphaDp.dataSourceXid = ds.xid;
          promises.push(alphaDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 4');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            alphaDp.xid = savedDp.xid;
            alphaDp.id = savedDp.id; //Save the ID for later
          }));
      return Promise.all(promises);
      });
    });

    const staticValueSetPointEventHandler = {
            name : "Testing setpoint",
            disabled : false,
            activeAction : "STATIC_VALUE",
            inactiveAction : "STATIC_VALUE",
            activeValueToSet : 1,
            inactiveValueToSet : 2,
            eventTypes: null,
            handlerType : "SET_POINT"
          };
    before('Create static set point event handler', () => {
        staticValueSetPointEventHandler.targetPointXid = numDp.xid;
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: staticValueSetPointEventHandler
        }).then(response => {
            staticValueSetPointEventHandler.xid = response.data.xid;
            assert.strictEqual(response.data.name, staticValueSetPointEventHandler.name);
            assert.strictEqual(response.data.disabled, staticValueSetPointEventHandler.disabled);

            assert.strictEqual(response.data.activePointXid, staticValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.inactivePointXid, staticValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.activeAction, staticValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.inactiveAction, staticValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.activeValueToSet, staticValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.inactiveValueToSet, staticValueSetPointEventHandler.inactiveValueToSet);

            assert.isNumber(response.data.id);
        });
    });
    
    //Clean up when done
    after('Deletes the new virtual data source and its points to clean up', () => {
        return DataSource.delete(ds.xid);
    });
    
    after('Delete static set point handler', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers/${staticValueSetPointEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {

        });
    });
    

});
