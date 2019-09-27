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

const {createClient, login, defer, uuid, delay} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Point Event detector service', function() {
    this.timeout(5000);
    before('Login', login.bind(this, client));
    
    const highLimitDetector = function() {
            return {
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
    };
    
    it('Creates a HIGH_LIMIT event detector', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
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
            assert.strictEqual(response.data.dataPoint.xid, this.numDp.xid);
            assert.strictEqual(response.data.dataPoint.pointLocator.dataType, this.numDp.pointLocator.dataType);
            
            
            highLimitPed.xid = response.data.xid;
            highLimitPed.id = response.data.id;
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
    });
    
    it('Add event handler to a HIGH_LIMIT event detector', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            highLimitPed.xid = response.data.xid;
            highLimitPed.handlerXids = [this.staticValueSetPointEventHandler.xid];
            return client.restRequest({
                path: `/rest/v2/full-event-detectors/${response.data.xid}`,
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
    });
    
    it('Fail to add non-existant event handler to a HIGH_LIMIT event detector', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            highLimitPed.xid = response.data.xid;
            highLimitPed.handlerXids = ['nothing-at-all'];
            return client.restRequest({
                path: `/rest/v2/full-event-detectors/${highLimitPed.xid}`,
                method: 'PUT',
                data: highLimitPed
            }).then(response => {
                assert.fail('Should have been invalid');
            }, error => {
                if(error.status !== 422){
                    assert.fail('Should have been invalid');
                }
            });
        });
    });
    
    it('Get a HIGH_LIMIT event detector', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            highLimitPed.xid = response.data.xid;
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
                
            }, error => {
                if(error.status === 422){
                    printValidationErrors(error.data);
                }else
                    assert.fail(error);
            });
        });
    });
    
    it('Remove event handler from a HIGH_LIMIT event detector', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        highLimitPed.handlerXids = [this.staticValueSetPointEventHandler.xid];
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            assert.strictEqual(response.data.handlerXids.length, 1);
            highLimitPed.xid = response.data.xid;
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
    });
    
    it('Query point event detector on xid', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            highLimitPed.xid = response.data.xid;
            return client.restRequest({
                path: `/rest/v2/full-event-detectors?xid=${highLimitPed.xid}`,
                method: 'GET'
            }).then(response => {
                assert.strictEqual(response.data.total, 1);
                assert.strictEqual(response.data.items[0].xid, highLimitPed.xid);
            });
        });
      });
    
    it('Query point event detector on data point id', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            highLimitPed.xid = response.data.xid;
            return client.restRequest({
                path: `/rest/v2/full-event-detectors?sourceId=${highLimitPed.sourceId}`,
                method: 'GET'
            }).then(response => {
                assert.strictEqual(response.data.total, 1);
                assert.strictEqual(response.data.items[0].xid, highLimitPed.xid);
            });
        });
      });
    
    it('Query point event detector on source type name', function() {
        const highLimitPed = highLimitDetector();
        highLimitPed.sourceId = this.numDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: highLimitPed
        }).then(response => {
            highLimitPed.xid = response.data.xid;
            return client.restRequest({
                path: `/rest/v2/full-event-detectors?detectorSourceType=DATA_POINT`,
                method: 'GET'
            }).then(response => {
                assert.strictEqual(response.data.total, 1);
                assert.strictEqual(response.data.items[0].xid, highLimitPed.xid);
            });
        });
      });
    
    function printValidationErrors(errors){
        var messages = '';
        for(var i=0; i<errors.result.messages.length; i++){
           messages += errors.result.messages[i].property + ' --> ' + errors.result.messages[i].message;
        }
        assert.fail(messages);
    }

    //Alphanumeric Regex State
    const AlphaRegexStateDetector = function() {
        return {
            name : "When matches",
            alarmLevel : 'URGENT',
            duration : {
                periods: 10,
                type: 'SECONDS'
            },
            state: 'TEST',
            detectorType : "ALPHANUMERIC_REGEX_STATE",
        };
    };
    
    it('Creates a ALPHANUMERIC_REGEX_STATE event detector', function() {
        const alphaRegexState = new AlphaRegexStateDetector();
        alphaRegexState.sourceId = this.alphaDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: alphaRegexState
        }).then(response => {
            alphaRegexState.xid = response.data.xid;
            
            assert.strictEqual(response.data.name, alphaRegexState.name);
            assert.strictEqual(response.data.sourceId, alphaRegexState.sourceId);
            assert.strictEqual(response.data.alarmLevel, alphaRegexState.alarmLevel);
            
            assert.strictEqual(response.data.duration.periods, alphaRegexState.duration.periods);
            assert.strictEqual(response.data.duration.type, alphaRegexState.duration.type);
            
            assert.strictEqual(response.data.state, alphaRegexState.state);            
            
            alphaRegexState.xid = response.data.xid;
            alphaRegexState.id = response.data.id;
        }, error => {
            if(error.status === 422){
                printValidationErrors(error.data);
            }else
                assert.fail(error);
        });
    });
    
    it('Deletes a ALPHANUMERIC_REGEX_STATE event detector', function() {
        const alphaRegexState = new AlphaRegexStateDetector();
        alphaRegexState.sourceId = this.alphaDp.id;
        return client.restRequest({
            path: '/rest/v2/full-event-detectors',
            method: 'POST',
            data: alphaRegexState
        }).then(response => {
            alphaRegexState.xid = response.data.xid;
            return client.restRequest({
                path: `/rest/v2/full-event-detectors/${alphaRegexState.xid}`,
                method: 'DELETE',
            }).then(response => {
                
                assert.strictEqual(response.data.name, alphaRegexState.name);
                assert.strictEqual(response.data.sourceId, alphaRegexState.sourceId);
                assert.strictEqual(response.data.alarmLevel, alphaRegexState.alarmLevel);
                
                assert.strictEqual(response.data.duration.periods, alphaRegexState.duration.periods);
                assert.strictEqual(response.data.duration.type, alphaRegexState.duration.type);
                
                assert.strictEqual(response.data.state, alphaRegexState.state);            
            }, error => {
                if(error.status === 422){
                    printValidationErrors(error.data);
                }else
                    assert.fail(error);
            });
        });
    });
    
    it('Gets websocket notifications for create', function() {
        
        const ed = {
                xid: uuid(),
                name : "When matches",
                alarmLevel : 'URGENT',
                duration : {
                    periods: 10,
                    type: 'SECONDS'
                },
                state: true,
                sourceId: this.dp.id,
                detectorType : "BINARY_STATE",
        };
        
        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/full-event-detectors'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                try{
                    assert.isString(msgStr);
                    const msg = JSON.parse(msgStr);
                    assert.strictEqual(msg.status, 'OK');
                    assert.strictEqual(msg.payload.action, 'add');
                    assert.strictEqual(msg.payload.object.xid, ed.xid);
                    listUpdatedDeferred.resolve();   
                }catch(e){
                    listUpdatedDeferred.reject(e);
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
            
        }).then(() => delay(1000)).then(() => {
            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start
            return client.restRequest({
                path: `/rest/v2/full-event-detectors`,
                method: 'POST',
                data: ed
            }).then(response =>{
                assert.strictEqual(response.data.xid, ed.xid);
                assert.strictEqual(response.data.name, ed.name);
                assert.strictEqual(response.data.sourceId, ed.sourceId);
                assert.strictEqual(response.data.alarmLevel, ed.alarmLevel);
                
                assert.strictEqual(response.data.duration.periods, ed.duration.periods);
                assert.strictEqual(response.data.duration.type, ed.duration.type);
                
                assert.strictEqual(response.data.state, ed.state);            

                assert.isNumber(response.data.id);
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        });
    });
    
    beforeEach('Create data source and points', function() {

        this.ds = new DataSource({
            name: 'Mango client test',
            enabled: false,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });
        
        this.dp = new DataPoint({
            name: 'Virtual Test Point 1',
            pointLocator : {
                startValue : "true",
                modelType : "PL.VIRTUAL",
                dataType : "BINARY",
                settable : true,
                changeType : "ALTERNATE_BOOLEAN",
            }
        });
        
        this.numDp = new DataPoint({
            name: 'Virtual Test Point 2',
            pointLocator : {
                startValue : "0",
                modelType : "PL.VIRTUAL",
                dataType : "NUMERIC",
                settable : true,
                changeType : "NO_CHANGE",
            }
        });
        
        this.mulDp = new DataPoint({
            name: 'Virtual Test Point 3',
            pointLocator : {
                startValue : "3",
                modelType : "PL.VIRTUAL",
                dataType : "MULTISTATE",
                settable : true,
                changeType : "NO_CHANGE",
            }
        });
        
        this.alphaDp = new DataPoint({
            name: 'Virtual Test Point 4',
            pointLocator : {
                startValue : "",
                modelType : "PL.VIRTUAL",
                dataType : "ALPHANUMERIC",
                settable : true,
                changeType : "NO_CHANGE",
            }
        });
        
      return this.ds.save().then((savedDs) => {
          assert.strictEqual(savedDs, this.ds);
          assert.equal(savedDs.name, 'Mango client test');
          assert.isNumber(savedDs.id);
          this.ds.xid = savedDs.xid;
          this.ds.id = savedDs.id;

          let promises = [];
          
          this.dp.dataSourceXid = this.ds.xid;
          promises.push(this.dp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.dp.xid = savedDp.xid;
            this.dp.id = savedDp.id; //Save the ID for later
          }));

          this.numDp.dataSourceXid = this.ds.xid;
          promises.push(this.numDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 2');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.numDp.xid = savedDp.xid;
            this.numDp.id = savedDp.id; //Save the ID for later
          }));

          this.mulDp.dataSourceXid = this.ds.xid;
          promises.push(this.mulDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 3');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.mulDp.xid = savedDp.xid;
            this.mulDp.id = savedDp.id; //Save the ID for later
          }));
          
          this.alphaDp.dataSourceXid = this.ds.xid;
          promises.push(this.alphaDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 4');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.alphaDp.xid = savedDp.xid;
            this.alphaDp.id = savedDp.id; //Save the ID for later
          }));
      return Promise.all(promises);
      });
    });

    beforeEach('Create static set point event handler', function() {
        this.staticValueSetPointEventHandler = {
                name : "Testing setpoint",
                disabled : false,
                activeAction : "STATIC_VALUE",
                inactiveAction : "STATIC_VALUE",
                activeValueToSet : 1,
                inactiveValueToSet : 2,
                eventTypes: null,
                handlerType : "SET_POINT"
              };
        this.staticValueSetPointEventHandler.targetPointXid = this.numDp.xid;
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: this.staticValueSetPointEventHandler
        }).then(response => {
            this.staticValueSetPointEventHandler.xid = response.data.xid;
            assert.strictEqual(response.data.name, this.staticValueSetPointEventHandler.name);
            assert.strictEqual(response.data.disabled, this.staticValueSetPointEventHandler.disabled);

            assert.strictEqual(response.data.activePointXid, this.staticValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.inactivePointXid, this.staticValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.activeAction, this.staticValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.inactiveAction, this.staticValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.activeValueToSet, this.staticValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.inactiveValueToSet, this.staticValueSetPointEventHandler.inactiveValueToSet);

            assert.isNumber(response.data.id);
        });
    });
    
    //Clean up when done
    afterEach('Deletes the new virtual data source and its points to clean up', function() {
        return DataSource.delete(this.ds.xid);
    });
    
    afterEach('Delete static set point handler', function() {
        return client.restRequest({
            path: `/rest/v2/event-handlers/${this.staticValueSetPointEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {

        });
    });
    

});
