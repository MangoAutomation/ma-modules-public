/**
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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

const {createClient, login, defer} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('Log4J Utilities', function() {
    before('Login', function() { return login.call(this, client); });
    
    it('Gets websocket notifications for RESET', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            sequenceNumber: 0,
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION',
            showResultWhenIncomplete: true,
            showResultWhenComplete: true,
            anyStatus: true,
            resourceTypes: ['log4JUtil']
        };
        const testData = {id: ''};
        const socketOpenDeferred = defer();
        const subscribeDeferred = defer();
        const actionFinishedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/temporary-resources'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                actionFinishedDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                actionFinishedDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                if(msg.messageType === 'RESPONSE'){
                    assert.strictEqual(msg.sequenceNumber, 0);
                    subscribeDeferred.resolve();
                }else{
                    if(msg.payload.status === 'SUCCESS'){
                        actionFinishedDeferred.resolve();
                    }
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
            
        }).then(() => subscribeDeferred.promise ).then(() => {
            return client.restRequest({
                path: '/rest/latest/system-actions/log4JUtil',
                method: 'POST',
                data: {
                    action: 'RESET'
                }
            }).then(response => {
                //Keep this id to confirm it finished later
                testData.id = response.data.id;
                return client.restRequest({
                    path: `/rest/latest/system-actions/status/${response.data.id}`,
                    method: 'GET'
                }).then(response => {
                    assert.isNotNull(response.data.startTime);
                })
            });
        }).then(() => actionFinishedDeferred.promise).then(() => {
            //Close websocket 
            ws.close();
            //Make a request to get the status
            return client.restRequest({
                path: `/rest/latest/system-actions/status/${testData.id}`,
                method: 'GET'
            }).then(response => {
                assert.strictEqual(response.data.status, 'SUCCESS');
            });
        });
    });
    
    it('Cancel RESET action', function() {
        return client.restRequest({
            path: '/rest/latest/system-actions/log4JUtil',
            method: 'POST',
            data: {
                action: 'RESET'
            }
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/system-actions/status/${response.data.id}`,
                method: 'DELETE'
            }).then(response => {
                assert.isNotNull(response.data.startTime);
                assert.isNotNull(response.data.completionTime);
                //Check its gone
                return client.restRequest({
                    path: `/rest/latest/system-actions/status/${response.data.id}`,
                    method: 'GET'
                }).then(response => {
                   assert.fail('should not get result');
                }, error => {
                    assert.strictEqual(error.status, 404);
                });
            });
        });
    });
});
