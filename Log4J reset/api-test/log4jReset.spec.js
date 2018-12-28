/**
 * Copyright 2018 Infinite Automation Systems Inc.
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
const path = require('path');

describe('Log4J Utilities', function() {
    before('Login', config.login);
    
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
        
        const socketOpenDeferred = config.defer();
        const subscribeDeferred = config.defer();
        const finishedBackupDeferred = config.defer();
        
        const testId = uuidV4();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/temporary-resources'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                finishedBackupDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                finishedBackupDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                if(msg.messageType === 'RESPONSE'){
                    assert.strictEqual(msg.sequenceNumber, 0);
                    subscribeDeferred.resolve();
                }else{
                    if(msg.payload.status === 'SUCCESS'){
                        finishedBackupDeferred.resolve();
                    }
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
            
        }).then(() => subscribeDeferred.promise ).then(() => {
            return client.restRequest({
                path: '/rest/v2/system-actions/log4JUtil',
                method: 'POST',
                data: {
                    action: 'RESET'
                }
            });
        }).then(() => finishedBackupDeferred.promise).then(() => {
            ws.close();
        });
    });
    
    //TODO Test failures
    
});
