/**
 * Copyright 2018 Infinite Automation Systems Inc.
 * http://infiniteautomation.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

const {createClient, login, defer, delay, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Event handlers', function() {
    
    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};
    
    before('Login', function() { return login.call(this, client); });

    before('Create DS 1', function() {
        this.point = (name) => {
            return new DataPoint({
                enabled: true,
                name: name,
                deviceName: 'Data point test deviceName',
                dataSourceXid : testContext.ds1.xid,
                pointLocator : {
                    startValue : '0',
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    changeType : 'NO_CHANGE',
                    settable: true
                }
            });
        };

        testContext.ds1 = new DataSource({
            xid: `DS_${uuid()}`,
            name: 'ME Testing 1',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return testContext.ds1.save();
    });

    before('Create DS 2', function() {
        testContext.ds2 = new DataSource({
            xid: `DS_${uuid()}`,
            name: 'ME Testing 2',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return testContext.ds2.save();
    });
    
    before('Create test DP 1', function() {
        const dp1 = this.point('test point 1');
        dp1.pointLocator.dataType = 'BINARY';
        return dp1.save().then(dp =>{
            testContext.dp1 = dp;
        });
    });
    
    before('Create test DP 2', function() {
        const dp2 = this.point('test point 2');
        dp2.pointLocator.dataType = 'BINARY';
        return dp2.save().then(dp => {
            testContext.dp2 = dp;
        });
    });
    
    after('Delete DS 1', function() {
        return testContext.ds1.delete();
    });
    after('Delete DS2', function() {
        return testContext.ds2.delete();
    });
    
    it('Create static set point event handler', () => {
        testContext.staticValueSetPointEventHandler = {
                xid : `DS_${uuid()}`,
                name : "Testing setpoint",
                disabled : false,
                targetPointXid : testContext.dp1.xid,
                activeAction : "STATIC_VALUE",
                inactiveAction : "STATIC_VALUE",
                activeValueToSet : false,
                inactiveValueToSet : true,
                activeScript: 'return 0;',
                inactiveScript: 'return 1;',
                scriptContext: [{xid: testContext.dp2.xid, variableName:'point2'}],
                scriptPermissions: ['superadmin', 'user'],
                eventTypes: [
                    {
                        eventType: 'DATA_SOURCE',
                        subType: null,
                        referenceId1: testContext.ds1.id,
                        referenceId2: 1 //Poll Aborted
                    }
                ],
                handlerType : "SET_POINT"
              };
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: testContext.staticValueSetPointEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.staticValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.staticValueSetPointEventHandler.name);
            assert.strictEqual(response.data.disabled, testContext.staticValueSetPointEventHandler.disabled);
            assert.strictEqual(response.data.eventTypes.length, testContext.staticValueSetPointEventHandler.eventTypes.length);
            for(let i=0; i<response.data.eventTypes.length; i++){
                assert.strictEqual(response.data.eventTypes[i].eventType, testContext.staticValueSetPointEventHandler.eventTypes[i].eventType);
                assert.strictEqual(response.data.eventTypes[0].referenceId1, testContext.staticValueSetPointEventHandler.eventTypes[i].referenceId1);
                assert.strictEqual(response.data.eventTypes[0].referenceId2, testContext.staticValueSetPointEventHandler.eventTypes[i].referenceId2);
            }

            assert.strictEqual(response.data.activePointXid, testContext.staticValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.inactivePointXid, testContext.staticValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.activeAction, testContext.staticValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.inactiveAction, testContext.staticValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.activeValueToSet, testContext.staticValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.inactiveValueToSet, testContext.staticValueSetPointEventHandler.inactiveValueToSet);
            
            assert.strictEqual(response.data.activeScript, testContext.staticValueSetPointEventHandler.activeScript);
            assert.strictEqual(response.data.inactiveScript, testContext.staticValueSetPointEventHandler.inactiveScript);
            
            assert.strictEqual(response.data.scriptContext.length, testContext.staticValueSetPointEventHandler.scriptContext.length);
            for(let i=0; i<response.data.scriptContext.length; i++) {
                assert.strictEqual(response.data.scriptContext[i].xid, testContext.staticValueSetPointEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.scriptContext[i].variableName, testContext.staticValueSetPointEventHandler.scriptContext[i].variableName);
            }
            for(let i=0; i<response.data.scriptPermissions.length; i++)
                assert.include(testContext.staticValueSetPointEventHandler.scriptPermissions, response.data.scriptPermissions[i]);

            assert.isNumber(response.data.id);
        });
    });
    
    it('Query event handlers lists', () => {
        return client.restRequest({
            path: `/rest/latest/event-handlers?xid=${testContext.staticValueSetPointEventHandler.xid}`,
            method: 'GET',
            data: testContext.addressMailingList
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, testContext.staticValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.items[0].name, testContext.staticValueSetPointEventHandler.name);
            assert.strictEqual(response.data.items[0].disabled, testContext.staticValueSetPointEventHandler.disabled);
            
            assert.strictEqual(response.data.items[0].eventTypes.length, testContext.staticValueSetPointEventHandler.eventTypes.length);
            for(let i=0; i<response.data.items[0].eventTypes.length; i++){
                assert.strictEqual(response.data.items[0].eventTypes[i].eventType, testContext.staticValueSetPointEventHandler.eventTypes[i].eventType);
                assert.strictEqual(response.data.items[0].eventTypes[0].subType, testContext.staticValueSetPointEventHandler.eventTypes[i].subType);
                assert.strictEqual(response.data.items[0].eventTypes[0].referenceId1, testContext.staticValueSetPointEventHandler.eventTypes[i].referenceId1);
                assert.strictEqual(response.data.items[0].eventTypes[0].referenceId2, testContext.staticValueSetPointEventHandler.eventTypes[i].referenceId2);
            }

            assert.strictEqual(response.data.items[0].activePointXid, testContext.staticValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.items[0].inactivePointXid, testContext.staticValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.items[0].activeAction, testContext.staticValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.items[0].inactiveAction, testContext.staticValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.items[0].activeValueToSet, testContext.staticValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.items[0].inactiveValueToSet, testContext.staticValueSetPointEventHandler.inactiveValueToSet);
            
            assert.strictEqual(response.data.items[0].activeScript, testContext.staticValueSetPointEventHandler.activeScript);
            assert.strictEqual(response.data.items[0].inactiveScript, testContext.staticValueSetPointEventHandler.inactiveScript);
            
            assert.strictEqual(response.data.items[0].scriptContext.length, testContext.staticValueSetPointEventHandler.scriptContext.length);
            for(let i=0; i<response.data.items[0].scriptContext.length; i++){
                assert.strictEqual(response.data.items[0].scriptContext[i].xid, testContext.staticValueSetPointEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.items[0].scriptContext[i].variableName,
                        testContext.staticValueSetPointEventHandler.scriptContext[i].variableName);
            }
            for(let i=0; i<response.data.items[0].scriptPermissions.length; i++)
                assert.include(testContext.staticValueSetPointEventHandler.scriptPermissions, response.data.items[0].scriptPermissions[i]);

            assert.isNumber(response.data.items[0].id);
        });
    });
    
    it('Delete static set point event handler', () => {
        return client.restRequest({
            path: `/rest/latest/event-handlers/${testContext.staticValueSetPointEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.xid, testContext.staticValueSetPointEventHandler.xid);
            assert.equal(response.data.name, testContext.staticValueSetPointEventHandler.name);
            assert.isNumber(response.data.id);
        });
    });
    
    it('Create point set point event handler', () => {
        testContext.pointValueSetPointEventHandler = {
                xid : `DS_${uuid()}`,
                name : "Testing setpoint",
                disabled : false,
                targetPointXid : testContext.dp1.xid,
                activePointXid : testContext.dp2.xid,
                inactivePointXid : testContext.dp1.xid,
                activeAction : "POINT_VALUE",
                inactiveAction : "POINT_VALUE",
                activeScript: 'return 0;',
                inactiveScript: 'return 1;',
                scriptContext: [{xid: testContext.dp2.xid, variableName:'point2'}],
                scriptPermissions: ['superadmin', 'user'],
                handlerType : "SET_POINT"
              };
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: testContext.pointValueSetPointEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.pointValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.pointValueSetPointEventHandler.name);
            assert.strictEqual(response.data.disabled, testContext.pointValueSetPointEventHandler.disabled);
            assert.strictEqual(response.data.activePointXid, testContext.pointValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.inactivePointXid, testContext.pointValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.activeAction, testContext.pointValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.inactiveAction, testContext.pointValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.activeValueToSet, testContext.pointValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.inactiveValueToSet, testContext.pointValueSetPointEventHandler.inactiveValueToSet);
            
            assert.strictEqual(response.data.activeScript, testContext.pointValueSetPointEventHandler.activeScript);
            assert.strictEqual(response.data.inactiveScript, testContext.pointValueSetPointEventHandler.inactiveScript);
            
            assert.strictEqual(response.data.scriptContext.length, testContext.pointValueSetPointEventHandler.scriptContext.length);
            for(let i=0; i<response.data.scriptContext.length; i++){
                assert.strictEqual(response.data.scriptContext[i].xid, testContext.pointValueSetPointEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.scriptContext[i].variableName, testContext.pointValueSetPointEventHandler.scriptContext[i].variableName);
            }
            for(let i=0; i<response.data.scriptPermissions.length; i++)
                assert.include(testContext.pointValueSetPointEventHandler.scriptPermissions, response.data.scriptPermissions[i]);

            assert.isNumber(response.data.id);
            testContext.pointValueSetPointEventHandler.id = response.data.id;

        });
    });
    
    it('Test invalid set point event handler', () => {
        testContext.invalidSetPointEventHandler = {
                xid : `DS_${uuid()}`,
                name : "Testing setpoint",
                disabled : false,
                targetPointXid : 'missingTarget',
                activePointXid : 'missingActive',
                inactivePointXid : 'missingInactive',
                activeAction : "POINT_VALUE",
                inactiveAction : "POINT_VALUE",
                activeScript: 'return 0;',
                inactiveScript: 'return 1;',
                scriptContext: [{xid: 'missing', variableName:'point2'}],
                scriptPermissions: ['superadmin', 'user'],
                handlerType : "SET_POINT"
            };
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: testContext.invalidSetPointEventHandler
        }).then(response => {
            throw new Error('Should not have created set point event handler');
        }, error => {
            assert.strictEqual(error.status, 422);
            assert.strictEqual(error.data.result.messages.length, 4);
            
            assert.strictEqual(error.data.result.messages[0].property, 'targetPointId');
            assert.strictEqual(error.data.result.messages[1].property, 'activePointId');
            assert.strictEqual(error.data.result.messages[2].property, 'inactivePointId');
            assert.strictEqual(error.data.result.messages[3].property, 'scriptContext[0].id');
        });
    });
    
    it('Gets websocket notifications for update', function() {
        
        testContext.pointValueSetPointEventHandler.name = 'New event handler name';
        testContext.pointValueSetPointEventHandler.eventTypes = [
            {
                eventType: 'SYSTEM',
                subType: 'USER_LOGIN',
                referenceId1: 0,
                referenceId2: 0
                
            }
        ];
        
        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/event-handlers'
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
                    assert.strictEqual(msg.payload.action, 'update');
                    assert.strictEqual(msg.payload.object.xid, testContext.pointValueSetPointEventHandler.xid);
                    assert.strictEqual(msg.payload.object.eventTypes.length, 1);
                    assert.strictEqual(msg.payload.object.eventTypes[0].eventType, testContext.pointValueSetPointEventHandler.eventTypes[0].eventType);
                    assert.strictEqual(msg.payload.object.eventTypes[0].subType, testContext.pointValueSetPointEventHandler.eventTypes[0].subType);
                    
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
                path: `/rest/latest/event-handlers/${testContext.pointValueSetPointEventHandler.xid}`,
                method: 'PUT',
                data: testContext.pointValueSetPointEventHandler
            }).then(response =>{
                assert.strictEqual(response.data.xid, testContext.pointValueSetPointEventHandler.xid);
                assert.strictEqual(response.data.name, testContext.pointValueSetPointEventHandler.name);
                assert.strictEqual(response.data.disabled, testContext.pointValueSetPointEventHandler.disabled);
                assert.strictEqual(response.data.activePointXid, testContext.pointValueSetPointEventHandler.activePointXid);
                assert.strictEqual(response.data.inactivePointXid, testContext.pointValueSetPointEventHandler.inactivePointXid);
                assert.strictEqual(response.data.activeAction, testContext.pointValueSetPointEventHandler.activeAction);
                assert.strictEqual(response.data.inactiveAction, testContext.pointValueSetPointEventHandler.inactiveAction);
                assert.strictEqual(response.data.activeValueToSet, testContext.pointValueSetPointEventHandler.activeValueToSet);
                assert.strictEqual(response.data.inactiveValueToSet, testContext.pointValueSetPointEventHandler.inactiveValueToSet);
                
                assert.strictEqual(response.data.activeScript, testContext.pointValueSetPointEventHandler.activeScript);
                assert.strictEqual(response.data.inactiveScript, testContext.pointValueSetPointEventHandler.inactiveScript);
                
                assert.strictEqual(response.data.scriptContext.length, testContext.pointValueSetPointEventHandler.scriptContext.length);
                for(let i=0; i<response.data.scriptContext.length; i++){
                    assert.strictEqual(response.data.scriptContext[i].xid, testContext.pointValueSetPointEventHandler.scriptContext[i].xid);
                    assert.strictEqual(response.data.scriptContext[i].variableName, testContext.pointValueSetPointEventHandler.scriptContext[i].variableName);
                }
                for(let i=0; i<response.data.scriptPermissions.length; i++)
                    assert.include(testContext.pointValueSetPointEventHandler.scriptPermissions, response.data.scriptPermissions[i]);
    
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
    
    it('Gets websocket notifications for delete', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/event-handlers'
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
                    assert.strictEqual(msg.payload.action, 'delete');
                    assert.strictEqual(msg.payload.object.xid, testContext.pointValueSetPointEventHandler.xid);
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
            return client.restRequest({
                path: `/rest/latest/event-handlers/${testContext.pointValueSetPointEventHandler.xid}`,
                method: 'DELETE',
                data: {}
            }).then(response => {
                assert.equal(response.data.id, testContext.pointValueSetPointEventHandler.id);
                assert.equal(response.data.name, testContext.pointValueSetPointEventHandler.name);
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
    
    //Process Event Handler Tests
    it('Create process event handler', () => {
        testContext.processEventHandler = {
                xid : `DS_${uuid()}`,
                name : "Testing process",
                disabled : true,
                activeProcessCommand : 'ls',
                activeProcessTimeout : 1000,
                inactiveProcessCommand: 'cd /',
                inactiveProcessTimeout: 1000,
                handlerType : "PROCESS"
              };
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: testContext.processEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.processEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.processEventHandler.name);
            assert.strictEqual(response.data.disabled, testContext.processEventHandler.disabled);
            assert.strictEqual(response.data.activeProcessCommand, testContext.processEventHandler.activeProcessCommand);
            assert.strictEqual(response.data.activeProcessTimeout, testContext.processEventHandler.activeProcessTimeout);
            assert.strictEqual(response.data.inactiveProcessCommand, testContext.processEventHandler.inactiveProcessCommand);
            assert.strictEqual(response.data.inactiveProcessTimeout, testContext.processEventHandler.inactiveProcessTimeout);
            
            assert.isNumber(response.data.id);
            testContext.processEventHandler.id = response.data.id;
        });
    });
    
    it('Patch process event handler', () => {
        testContext.processEventHandler.disabled = false;
        return client.restRequest({
            path: '/rest/latest/event-handlers/' + testContext.processEventHandler.xid,
            method: 'PATCH',
            data: {
                disabled: false
            }
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.processEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.processEventHandler.name);
            assert.strictEqual(response.data.disabled, testContext.processEventHandler.disabled);
            assert.strictEqual(response.data.activeProcessCommand, testContext.processEventHandler.activeProcessCommand);
            assert.strictEqual(response.data.activeProcessTimeout, testContext.processEventHandler.activeProcessTimeout);
            assert.strictEqual(response.data.inactiveProcessCommand, testContext.processEventHandler.inactiveProcessCommand);
            assert.strictEqual(response.data.inactiveProcessTimeout, testContext.processEventHandler.inactiveProcessTimeout);
            
            assert.isNumber(response.data.id);
            testContext.processEventHandler.id = response.data.id;
        });
    });
    
    it('Delete process event handler', () => {
        return client.restRequest({
            path: `/rest/latest/event-handlers/${testContext.processEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.processEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.processEventHandler.name);
            assert.strictEqual(response.data.id, testContext.processEventHandler.id);
        });
    });
    
    //Email Event Handler Tests
    it('Create email event handler', () => {
        testContext.emailEventHandler = {
                xid : `DS_${uuid()}`,
                name : "Testing email",
                disabled : false,
                activeRecipients: [
                    {username: 'admin', recipientType: 'USER'},
                    {address: 'test@test.com', recipientType: 'ADDRESS'}
                ],
                sendEscalation: true,
                repeatEscalations: true,
                escalationDelayType: 'HOURS',
                escalationDelay: 1,
                escalationRecipients: [
                    {username: 'admin', recipientType: 'USER'},
                    {address: 'test@testingEscalation.com', recipientType: 'ADDRESS'}
                ],
                sendInactive: true,
                inactiveOverride: true,
                inactiveRecipients: [
                    {username: 'admin', recipientType: 'USER'},
                    {address: 'test@testingInactive.com', recipientType: 'ADDRESS'}
                ],
                includeSystemInfo: true,
                includeLogfile: true,
                customTemplate: '<h2></h2>',
                scriptContext: [
                        {xid: testContext.dp1.xid, variableName:'point1'},
                        {xid: testContext.dp2.xid, variableName:'point2'}
                    ],
                scriptPermissions: ['superadmin', 'user'],
                script: 'return 0;',
                subject: 'INCLUDE_EVENT_MESSAGE',
                handlerType : "EMAIL"
              };
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: testContext.emailEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.emailEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.emailEventHandler.name);
            assert.strictEqual(response.data.disabled, testContext.emailEventHandler.disabled);
            
            assert.strictEqual(response.data.activeRecipients.length, testContext.emailEventHandler.activeRecipients.length);
            assert.strictEqual(response.data.activeRecipients[0].username, testContext.emailEventHandler.activeRecipients[0].username);
            assert.strictEqual(response.data.activeRecipients[1].address, testContext.emailEventHandler.activeRecipients[1].address);
            
            assert.strictEqual(response.data.sendEscalation, testContext.emailEventHandler.sendEscalation);
            assert.strictEqual(response.data.repeatEscalations, testContext.emailEventHandler.repeatEscalations);
            assert.strictEqual(response.data.escalationDelayType, testContext.emailEventHandler.escalationDelayType);
            assert.strictEqual(response.data.escalationDelay, testContext.emailEventHandler.escalationDelay);
            
            assert.strictEqual(response.data.escalationRecipients.length, testContext.emailEventHandler.escalationRecipients.length);
            assert.strictEqual(response.data.escalationRecipients[0].username, testContext.emailEventHandler.escalationRecipients[0].username);
            assert.strictEqual(response.data.escalationRecipients[1].address, testContext.emailEventHandler.escalationRecipients[1].address);
            
            assert.strictEqual(response.data.sendInactive, testContext.emailEventHandler.sendInactive);
            assert.strictEqual(response.data.inactiveOverride, testContext.emailEventHandler.inactiveOverride);
            
            assert.strictEqual(response.data.inactiveRecipients.length, testContext.emailEventHandler.inactiveRecipients.length);
            assert.strictEqual(response.data.inactiveRecipients[0].username, testContext.emailEventHandler.inactiveRecipients[0].username);
            assert.strictEqual(response.data.inactiveRecipients[1].address, testContext.emailEventHandler.inactiveRecipients[1].address);
            
            assert.strictEqual(response.data.includeSystemInfo, testContext.emailEventHandler.includeSystemInfo);
            assert.strictEqual(response.data.includeLogfile, testContext.emailEventHandler.includeLogfile);
            assert.strictEqual(response.data.customTemplate, testContext.emailEventHandler.customTemplate);
            
            assert.strictEqual(response.data.scriptContext.length, testContext.emailEventHandler.scriptContext.length);
            for(let i=0; i<response.data.scriptContext.length; i++){
                assert.strictEqual(response.data.scriptContext[i].xid, testContext.emailEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.scriptContext[i].variableName, testContext.emailEventHandler.scriptContext[i].variableName);
            }
            for(let i=0; i<response.data.scriptPermissions.length; i++)
                assert.include(testContext.emailEventHandler.scriptPermissions, response.data.scriptPermissions[i]);

            assert.isNumber(response.data.id);
            assert.strictEqual(response.data.subject, 'INCLUDE_EVENT_MESSAGE');
            testContext.emailEventHandler.id = response.data.id;
        });
    });
    
    it('Test invalid email event handler', () => {
        testContext.invalidEmailEventHandler = {
                xid : `DS_${uuid()}`,
                name : "Testing email",
                disabled : false,
                activeRecipients: [
                    {username: 'noone', recipientType: 'USER'},
                    {address: 'test@test.com', recipientType: 'ADDRESS'}
                ],
                sendEscalation: true,
                repeatEscalations: true,
                escalationDelayType: 'HOURS',
                escalationDelay: 1,
                escalationRecipients: [
                    {username: 'noone', recipientType: 'USER'},
                    {address: 'test@testingEscalation.com', recipientType: 'ADDRESS'}
                ],
                sendInactive: true,
                inactiveOverride: true,
                inactiveRecipients: [
                    {username: 'admin', recipientType: 'USER'},
                    {address: 'test@testingInactive.com', recipientType: 'ADDRESS'}
                ],
                includeSystemInfo: true,
                includeLogfile: true,
                customTemplate: '${empty',
                scriptContext: [
                        {xid: 'missing', variableName:'point1'},
                        {xid: testContext.dp2.xid, variableName:'point2'}
                    ],
                scriptPermissions: ['superadmin', 'user'],
                script: 'return 0;',
                handlerType : "EMAIL"
              };
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: testContext.invalidEmailEventHandler
        }).then(response => {
            throw new Error('Should not have created email event handler');
        }, error => {
            assert.strictEqual(error.status, 422);
            assert.strictEqual(error.data.result.messages.length, 5);
            
            //Missing user
            assert.strictEqual(error.data.result.messages[0].property, 'activeRecipients[0]');
            //Missing user
            assert.strictEqual(error.data.result.messages[1].property, 'escalationRecipients[0]');
            //Invalid FTL
            assert.strictEqual(error.data.result.messages[2].property, 'customTemplate');
            //Missing point
            assert.strictEqual(error.data.result.messages[3].property, 'scriptContext[0].id');
            //Invalid subject
            assert.strictEqual(error.data.result.messages[4].property, 'subject');
        });
    });
    
    it('Delete email event handler', () => {
        return client.restRequest({
            path: `/rest/latest/event-handlers/${testContext.emailEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.strictEqual(response.data.xid, testContext.emailEventHandler.xid);
            assert.strictEqual(response.data.name, testContext.emailEventHandler.name);
            assert.strictEqual(response.data.id, testContext.emailEventHandler.id);
        });
    });
    
});
