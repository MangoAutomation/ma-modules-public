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

const {createClient, login, defer, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const Publisher = client.Publisher;

describe('Publishers service', function() {
    before('Login', function() { return login.call(this, client); });
    
    it('Gets websocket notifications for publisher create', function() {

        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        const pub = new Publisher({
            modelType: 'MOCK'
        });
        
        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/publishers'
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
                    assert.strictEqual(msg.payload.object.name, pub.name);
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
            return pub.save().then(saved => {
                assert.isNumber(saved.id);
                saved.id = saved.id;
                assert.strictEqual(pub.xid, saved.xid);
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        }).finally(() => {
            return pub.delete();
        });
    });

    it('Gets websocket notifications for publisher update', function() {

        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();
        
        const pub = new Publisher({
            modelType: 'MOCK'
        });

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/publishers'
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
                    if(msg.payload.action === 'update') {
                        assert.strictEqual(msg.status, 'OK');
                        assert.strictEqual(msg.payload.object.id, pub.id);
                        listUpdatedDeferred.resolve();                           
                    }
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
            return pub.save().then(saved => {
                assert.isNumber(saved.id);
                saved.id = saved.id;
                pub.name = 'new name';
                assert.strictEqual(pub.xid, saved.xid);
                return pub.save().then(updated =>{
                    assert.strictEqual(pub.xid, updated.xid);
                    assert.strictEqual(pub.name, updated.name);
                });
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        }).finally(() => {
            return pub.delete();
        });
    });
    
    it('Gets websocket notifications for publisher delete', function() {

        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        const pub = new Publisher({
            modelType: 'MOCK'
        });
        
        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/publishers'
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
                    if(msg.payload.action === 'delete') {
                        assert.strictEqual(msg.status, 'OK');
                        assert.strictEqual(msg.payload.object.id, pub.id);
                    }
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
            return pub.save().then(saved => {
                assert.isNumber(saved.id);
                saved.id = saved.id;
                pub.name = 'new name';
                assert.strictEqual(pub.xid, saved.xid);
                return pub.delete();
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        })
    });
});
