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

const config = require('@infinite-automation/mango-client/test/setup');
const uuidV4 = require('uuid/v4');

describe('Events v2 tests', function(){
    before('Login', config.login);

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
        
        const socketOpenDeferred = config.defer();
        const gotAlarmSummaries = config.defer();
        const gotEventDeferred = config.defer();
        
        const testId = uuidV4();

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
            const send = config.defer();
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

    it('Can query events via websocket', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            actions: ['RAISED'],
            levels: ['NONE'],
            sendEventLevelSummaries: true,
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION'
        };
        
        const socketOpenDeferred = config.defer();
        const gotAlarmSummaries = config.defer();
        const gotEventQueryResult = config.defer();

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
                    assert.isNumber(msg.payload.total);
                    assert.isArray(msg.payload.items);
                    gotEventQueryResult.resolve();
                }
            });
            return socketOpenDeferred.promise;
        }).then(function() {
            const send = config.defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotAlarmSummaries.promise).then(function() {
            const send = config.defer();
            ws.send(JSON.stringify({
                messageType: 'REQUEST',
                requestType: 'QUERY',
                rqlQuery: "limit(10)",
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
    
});