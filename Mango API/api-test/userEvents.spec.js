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
const uuidV4 = require('uuid/v4');

describe('User Event query tests', function(){
    before('Login', config.login);

    before('Insert a User Event', function(){
        return client.restRequest({
            path: '/rest/v2/example/raise-event',
            method: 'POST',
            data: {
                event: {
                    typeName: "SYSTEM",
                    systemEventType: 'Test event',
                    ref1: -1,
                },
                level: 'URGENT',
                message: 'testing',
                context: {
                    'value': "Testing"
                }
            }
        }).then(response => {
          //console.log(response.data);
        });
    });

    it('Describe event query', () => {
      return client.restRequest({
          path: '/rest/v2/user-events/explain-query',
          method: 'GET'
      }).then(response => {
        //console.log(response.data);
      });
    });

    it('Query inserted event', () => {
      return client.restRequest({
          path: '/rest/v2/user-events?sort(-activeTimestamp)&limit(1)',
          method: 'GET'
      }).then(response => {
          assert.isAbove(response.data.total, 0);
          assert.equal(response.data.items.length, 1);
          assert.equal(response.data.items[0].eventType.eventType, 'SYSTEM');
          assert.equal(response.data.items[0].eventType.eventSubtype, 'Test event');
      });
    });
    
    it('Gets websocket notifications for raised events', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            eventTypes: ['RAISED'],
            levels: ['NONE']
        };
        
        const socketOpenDeferred = config.defer();
        const gotEventDeferred = config.defer();
        
        const testId = uuidV4();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v1/websocket/events'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                gotEventDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                gotEventDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                assert.strictEqual(msg.status, 'OK');
                assert.strictEqual(msg.payload.type, 'RAISED');
                assert.strictEqual(msg.payload.event.alarmLevel, 'NONE');
                assert.property(msg.payload.event, 'eventType');

                if (msg.payload.event.message === 'test id ' + testId) {
                    assert.strictEqual(msg.payload.event.eventType.eventType, 'SYSTEM');
                    assert.strictEqual(msg.payload.event.eventType.eventSubtype, 'Test event');

                    gotEventDeferred.resolve();
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => {
            const send = config.defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
            
            // wait a second after sending subscription, test fails otherwise on a cold start
        }).then(() => config.delay(1000)).then(() => {
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
        }).then(() => gotEventDeferred.promise);
    });
});
