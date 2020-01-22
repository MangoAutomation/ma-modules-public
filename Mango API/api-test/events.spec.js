/**
 * Copyright 2019 Infinite Automation Systems Inc.
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

const {createClient, login, defer, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Events v2 tests', function(){
    before('Login', function() { return login.call(this, client); });

    const newDataPoint = (xid, dsXid, rollupType, simplifyType, simplifyValue) => {
        return new DataPoint({
            xid: xid,
            enabled: true,
            name: 'Point values test',
            deviceName: 'Point values test',
            dataSourceXid : dsXid,
            pointLocator : {
                startValue : '0',
                modelType : 'PL.VIRTUAL',
                dataType : 'NUMERIC',
                changeType : 'NO_CHANGE',
                settable: true
            },
            textRenderer: {
                type: 'textRendererAnalog',
                format: '0.00',
                suffix: '',
                useUnitAsSuffix: false,
                unit: '',
                renderedUnit: ''
            },
            rollup: rollupType,
            simplifyType: simplifyType,
            simplifyTolerance: simplifyValue
        });
    };
    
    const raiseDelay = 1000; //Delay to raise alarm
    const testPointXid1 = uuid();
    
    before('Create a virtual data source, points, raise event', function() {
        this.timeout(raiseDelay * 200);

        this.ds = new DataSource({
            xid: uuid(),
            name: 'Mango client test',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'HOURS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return this.ds.save().then((savedDs) => {
            assert.strictEqual(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
            this.ds.id = savedDs.id;
        }).then(() => {
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid, 'FIRST', 'NONE', 0);
            return this.testPoint1.save().then((savedDp) =>{
               this.testPoint1.id = savedDp.id;
            });
        }).then(() => {
            return client.restRequest({
                path: '/rest/v2/example/raise-event',
                method: 'POST',
                data: {
                    event: {
                        typeName: 'DATA_POINT',
                        dataSourceId: this.ds.id,
                        dataPointId: this.testPoint1.id,
                        duplicateHandling: 'ALLOW'
                    },
                    level: 'INFORMATION',
                    message: 'Dummy Point event'
                }
            });
        }).then(() => delay(raiseDelay));
    });

    after('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });
    
    it('Gets websocket notifications for raised events', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            actions: ['RAISED'],
            levels: ['NONE'],
            sendEventLevelSummaries: true,
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION'
        };
        
        const socketOpenDeferred = defer();
        const gotAlarmSummaries = defer();
        const gotEventDeferred = defer();
        
        const testId = uuid();

        return Promise.resolve().then(function() {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/events'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                gotEventDeferred.reject(msg);
                gotAlarmSummaries.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                gotEventDeferred.reject(msg);
                gotAlarmSummaries.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                if(msg.messageType === 'RESPONSE') {
                    assert.strictEqual(msg.sequenceNumber, 0);
                    assert.property(msg, 'payload');
                    assert.strictEqual(msg.payload.length, 8);
                    gotAlarmSummaries.resolve();
                }
                if(msg.messageType === 'NOTIFICATION' && msg.payload.message === 'test id ' + testId) {
                    assert.strictEqual(msg.notificationType, 'RAISED');
                    assert.property(msg.payload, 'eventType');
                    assert.strictEqual(msg.payload.eventType.eventType, 'SYSTEM');
                    assert.strictEqual(msg.payload.eventType.subType, 'Test event');
                    assert.strictEqual(msg.payload.alarmLevel, 'NONE');
                    gotEventDeferred.resolve();                    
                }
            });
            return socketOpenDeferred.promise;
        }).then(function() {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotAlarmSummaries.promise).then(function() {
            return client.restRequest({
                path: '/rest/v2/example/raise-event',
                method: 'POST',
                data: {
                    event: {
                        typeName: 'SYSTEM',
                        systemEventType: 'Test event'
                    },
                    level: 'NONE',
                    message: 'test id ' + testId
                }
            });
        }).then(() => gotEventDeferred.promise).then(function(r) {
            ws.close();
            return r;
        }, function(e) {
            ws.close();
            return Promise.reject(e);
        });
    });

    it('Can get data point totals via websocket', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            actions: ['RAISED'],
            levels: ['NONE'],
            sendEventLevelSummaries: true,
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION'
        };
        
        const socketOpenDeferred = defer();
        const gotAlarmSummaries = defer();
        const gotEventQueryResult = defer();

        return Promise.resolve().then(function() {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/events'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                gotEventQueryResult.reject(msg);
                gotAlarmSummaries.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                gotEventQueryResult.reject(msg);
                gotAlarmSummaries.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                if(msg.messageType === 'RESPONSE' && msg.sequenceNumber === 0) {
                    assert.property(msg, 'payload');
                    assert.strictEqual(msg.payload.length, 8);
                    gotAlarmSummaries.resolve();
                }else if(msg.messageType === 'RESPONSE' && msg.sequenceNumber === 1) {
                    assert.property(msg, 'payload');
                    assert.isArray(msg.payload);
                    assert.strictEqual(msg.payload.length, 1);
                    assert.strictEqual(msg.payload[0].xid, testPointXid1);
                    assert.strictEqual(msg.payload[0].counts.INFORMATION, 1);
                    
                    gotEventQueryResult.resolve();
                }
            });
            return socketOpenDeferred.promise;
        }).then(function() {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotAlarmSummaries.promise).then(function() {
            const send = defer();
            ws.send(JSON.stringify({
                messageType: 'REQUEST',
                requestType: 'DATA_POINT_SUMMARY',
                dataPointXids: [testPointXid1],
                sequenceNumber: 1
            }), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotEventQueryResult.promise).then(function(r) {
            ws.close();
            return r;
        }, function(e) {
            ws.close();
            return Promise.reject(e);
        });
    });
    
    it('Can query for data point events', function() {
        return client.restRequest({
            path: `/rest/v2/events?eq(eventType,DATA_POINT)&eq(referenceId1,${this.testPoint1.id})&sort(-activeTimestamp)&limit(15,0)`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.items.length, 1);
            assert.isAtLeast(response.data.total, 1);
        });
    });
    
});