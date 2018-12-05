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

const config = require('@infinite-automation/mango-client/test/setup');
const uuidV4 = require('uuid/v4');

describe('Event handlers v2', function() {
    before('Login', config.login);

    before('Create DS 1', function() {
        this.point = (name) => {
            return new DataPoint({
                enabled: true,
                name: name,
                deviceName: 'Data point test deviceName',
                dataSourceXid : global.ds1.xid,
                pointLocator : {
                    startValue : '0',
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    changeType : 'NO_CHANGE',
                }
            });
        };

        global.ds1 = new DataSource({
            xid: 'me_test_1',
            name: 'ME Testing 1',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return global.ds1.save();
    });

    before('Create DS 2', function() {
        global.ds2 = new DataSource({
            xid: 'me_test_2',
            name: 'ME Testing 2',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return global.ds2.save();
    });
    
    before('Create test DP 1', function() {
        const dp1 = this.point('test point 1');
        dp1.pointLocator.dataType = 'BINARY';
        return dp1.save().then(dp =>{
            global.dp1 = dp;
        });
    });
    
    before('Create test DP 2', function() {
        const dp2 = this.point('test point 2');
        dp2.pointLocator.dataType = 'BINARY';
        return dp2.save().then(dp =>{
            global.dp2 = dp;
        });
    });
    
    after('Delete DS 1', function() {
        return global.ds1.delete();
    });
    after('Delete DS2', function() {
        return global.ds2.delete();
    });
    
    
    it('Create static set point event handler', () => {
        global.staticValueSetPointEventHandler = {
                xid : "EVTH_SET_POINT_TEST",
                name : "Testing setpoint",
                disabled : false,
                eventTypes: [
                    {
                        eventType: 'SYSTEM_FAIL',
                        subType: 'SYSTEM_STARTUP',
                        referenceId1: 0,
                        referenceId2: 0
                    }
                ],
                targetPointXid : global.dp1.xid,
                activeAction : "STATIC_VALUE",
                inactiveAction : "STATIC_VALUE",
                activeValueToSet : false,
                inactiveValueToSet : true,
                activeScript: 'return 0;',
                inactiveScript: 'return 1;',
                scriptContext: [{xid: global.dp2.xid, variableName:'point2'}],
                scriptPermissions: ['admin', 'testing'],
                handlerType : "SET_POINT"
              };
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: global.staticValueSetPointEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, global.staticValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.name, global.staticValueSetPointEventHandler.name);
            assert.strictEqual(response.data.disabled, global.staticValueSetPointEventHandler.disabled);
            assert.strictEqual(response.data.eventTypes.length, global.staticValueSetPointEventHandler.eventTypes.length);
            for(var i=0; i<response.data.eventTypes.length; i++){
                assert.strictEqual(response.data.eventTypes[i].eventType, global.staticValueSetPointEventHandler.eventTypes[i].eventType);
                assert.strictEqual(response.data.eventTypes[0].subType, global.staticValueSetPointEventHandler.eventTypes[i].subType);
                assert.strictEqual(response.data.eventTypes[0].referenceId1, global.staticValueSetPointEventHandler.eventTypes[i].referenceId1);
                assert.strictEqual(response.data.eventTypes[0].referenceId2, global.staticValueSetPointEventHandler.eventTypes[i].referenceId2);
            }

            assert.strictEqual(response.data.activePointXid, global.staticValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.inactivePointXid, global.staticValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.activeAction, global.staticValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.inactiveAction, global.staticValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.activeValueToSet, global.staticValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.inactiveValueToSet, global.staticValueSetPointEventHandler.inactiveValueToSet);
            
            assert.strictEqual(response.data.activeScript, global.staticValueSetPointEventHandler.activeScript);
            assert.strictEqual(response.data.inactiveScript, global.staticValueSetPointEventHandler.inactiveScript);
            
            assert.strictEqual(response.data.scriptContext.length, global.staticValueSetPointEventHandler.scriptContext.length);
            for(var i=0; i<response.data.scriptContext.length; i++){
                assert.strictEqual(response.data.scriptContext[i].xid, global.staticValueSetPointEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.scriptContext[i].variableName, global.staticValueSetPointEventHandler.scriptContext[i].variableName);
            }
            for(var i=0; i<response.data.scriptPermissions.length; i++)
                assert.include(global.staticValueSetPointEventHandler.scriptPermissions, response.data.scriptPermissions[i]);

            assert.isNumber(response.data.id);
        });
    });
    
    it('Query event handlers lists', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers?xid=${global.staticValueSetPointEventHandler.xid}`,
            method: 'GET',
            data: global.addressMailingList
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, global.staticValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.items[0].name, global.staticValueSetPointEventHandler.name);
            assert.strictEqual(response.data.items[0].disabled, global.staticValueSetPointEventHandler.disabled);
            
            assert.strictEqual(response.data.items[0].eventTypes.length, global.staticValueSetPointEventHandler.eventTypes.length);
            for(var i=0; i<response.data.items[0].eventTypes.length; i++){
                assert.strictEqual(response.data.items[0].eventTypes[i].eventType, global.staticValueSetPointEventHandler.eventTypes[i].eventType);
                assert.strictEqual(response.data.items[0].eventTypes[0].subType, global.staticValueSetPointEventHandler.eventTypes[i].subType);
                assert.strictEqual(response.data.items[0].eventTypes[0].referenceId1, global.staticValueSetPointEventHandler.eventTypes[i].referenceId1);
                assert.strictEqual(response.data.items[0].eventTypes[0].referenceId2, global.staticValueSetPointEventHandler.eventTypes[i].referenceId2);
            }

            assert.strictEqual(response.data.items[0].activePointXid, global.staticValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.items[0].inactivePointXid, global.staticValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.items[0].activeAction, global.staticValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.items[0].inactiveAction, global.staticValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.items[0].activeValueToSet, global.staticValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.items[0].inactiveValueToSet, global.staticValueSetPointEventHandler.inactiveValueToSet);
            
            assert.strictEqual(response.data.items[0].activeScript, global.staticValueSetPointEventHandler.activeScript);
            assert.strictEqual(response.data.items[0].inactiveScript, global.staticValueSetPointEventHandler.inactiveScript);
            
            assert.strictEqual(response.data.items[0].scriptContext.length, global.staticValueSetPointEventHandler.scriptContext.length);
            for(var i=0; i<response.data.items[0].scriptContext.length; i++){
                assert.strictEqual(response.data.items[0].scriptContext[i].xid, global.staticValueSetPointEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.items[0].scriptContext[i].variableName, global.staticValueSetPointEventHandler.scriptContext[i].variableName);
            }
            for(var i=0; i<response.data.items[0].scriptPermissions.length; i++)
                assert.include(global.staticValueSetPointEventHandler.scriptPermissions, response.data.items[0].scriptPermissions[i]);

            assert.isNumber(response.data.items[0].id);
        });
    });
    
    it('Delete static set point event handler', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers/${global.staticValueSetPointEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.xid, global.staticValueSetPointEventHandler.xid);
            assert.equal(response.data.name, global.staticValueSetPointEventHandler.name);
            assert.isNumber(response.data.id);
        });
    });
    
    it('Create point set point event handler', () => {
        global.pointValueSetPointEventHandler = {
                xid : "EVTH_SET_POINT_VALUE_TEST",
                name : "Testing setpoint",
                disabled : false,
                targetPointXid : global.dp1.xid,
                activePointXid : global.dp2.xid,
                inactivePointXid : global.dp1.xid,
                activeAction : "POINT_VALUE",
                inactiveAction : "POINT_VALUE",
                activeScript: 'return 0;',
                inactiveScript: 'return 1;',
                scriptContext: [{xid: global.dp2.xid, variableName:'point2'}],
                scriptPermissions: ['admin', 'testing'],
                handlerType : "SET_POINT"
              };
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: global.pointValueSetPointEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, global.pointValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.name, global.pointValueSetPointEventHandler.name);
            assert.strictEqual(response.data.disabled, global.pointValueSetPointEventHandler.disabled);
            assert.strictEqual(response.data.activePointXid, global.pointValueSetPointEventHandler.activePointXid);
            assert.strictEqual(response.data.inactivePointXid, global.pointValueSetPointEventHandler.inactivePointXid);
            assert.strictEqual(response.data.activeAction, global.pointValueSetPointEventHandler.activeAction);
            assert.strictEqual(response.data.inactiveAction, global.pointValueSetPointEventHandler.inactiveAction);
            assert.strictEqual(response.data.activeValueToSet, global.pointValueSetPointEventHandler.activeValueToSet);
            assert.strictEqual(response.data.inactiveValueToSet, global.pointValueSetPointEventHandler.inactiveValueToSet);
            
            assert.strictEqual(response.data.activeScript, global.pointValueSetPointEventHandler.activeScript);
            assert.strictEqual(response.data.inactiveScript, global.pointValueSetPointEventHandler.inactiveScript);
            
            assert.strictEqual(response.data.scriptContext.length, global.pointValueSetPointEventHandler.scriptContext.length);
            for(var i=0; i<response.data.scriptContext.length; i++){
                assert.strictEqual(response.data.scriptContext[i].xid, global.pointValueSetPointEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.scriptContext[i].variableName, global.pointValueSetPointEventHandler.scriptContext[i].variableName);
            }
            for(var i=0; i<response.data.scriptPermissions.length; i++)
                assert.include(global.pointValueSetPointEventHandler.scriptPermissions, response.data.scriptPermissions[i]);

            assert.isNumber(response.data.id);
            global.pointValueSetPointEventHandler.id = response.data.id;

        });
    });
    
    it('Test invalid set point event handler', () => {
        global.invalidSetPointEventHandler = {
                xid : "EVTH_INVALID_TEST",
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
                scriptPermissions: ['admin', 'testing'],
                handlerType : "SET_POINT"
            };
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: global.invalidSetPointEventHandler
        }).then(response => {
            throw new Error('Should not have created set point event handler');
        }, error => {
            assert.strictEqual(error.status, 422);
            assert.strictEqual(error.data.result.messages.length, 4);
            
            //Missing user
            assert.strictEqual(error.data.result.messages[0].property, 'targetPointId');
            //Missing user
            assert.strictEqual(error.data.result.messages[1].property, 'activePointId');
            //Invalid FTL
            assert.strictEqual(error.data.result.messages[2].property, 'inactivePointId');
            //Missing point
            assert.strictEqual(error.data.result.messages[3].property, 'scriptContext[0].id');
        });
    });
    
    it('Gets websocket notifications for update', function() {
        
        global.staticValueSetPointEventHandler.name = 'New event handler name';
        
        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = config.defer();
        const listUpdatedDeferred = config.defer();
        
        const testId = uuidV4();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/event-handlers'
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
                    assert.strictEqual(msg.payload.object.xid, global.pointValueSetPointEventHandler.xid);
                    listUpdatedDeferred.resolve();   
                }catch(e){
                    listUpdatedDeferred.reject(e);
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
            
        }).then(() => config.delay(1000)).then(() => {
            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start
            return client.restRequest({
                path: `/rest/v2/event-handlers/${global.pointValueSetPointEventHandler.xid}`,
                method: 'PUT',
                data: global.pointValueSetPointEventHandler
            }).then(response =>{
                assert.strictEqual(response.data.xid, global.pointValueSetPointEventHandler.xid);
                assert.strictEqual(response.data.name, global.pointValueSetPointEventHandler.name);
                assert.strictEqual(response.data.disabled, global.pointValueSetPointEventHandler.disabled);
                assert.strictEqual(response.data.activePointXid, global.pointValueSetPointEventHandler.activePointXid);
                assert.strictEqual(response.data.inactivePointXid, global.pointValueSetPointEventHandler.inactivePointXid);
                assert.strictEqual(response.data.activeAction, global.pointValueSetPointEventHandler.activeAction);
                assert.strictEqual(response.data.inactiveAction, global.pointValueSetPointEventHandler.inactiveAction);
                assert.strictEqual(response.data.activeValueToSet, global.pointValueSetPointEventHandler.activeValueToSet);
                assert.strictEqual(response.data.inactiveValueToSet, global.pointValueSetPointEventHandler.inactiveValueToSet);
                
                assert.strictEqual(response.data.activeScript, global.pointValueSetPointEventHandler.activeScript);
                assert.strictEqual(response.data.inactiveScript, global.pointValueSetPointEventHandler.inactiveScript);
                
                assert.strictEqual(response.data.scriptContext.length, global.pointValueSetPointEventHandler.scriptContext.length);
                for(var i=0; i<response.data.scriptContext.length; i++){
                    assert.strictEqual(response.data.scriptContext[i].xid, global.pointValueSetPointEventHandler.scriptContext[i].xid);
                    assert.strictEqual(response.data.scriptContext[i].variableName, global.pointValueSetPointEventHandler.scriptContext[i].variableName);
                }
                for(var i=0; i<response.data.scriptPermissions.length; i++)
                    assert.include(global.pointValueSetPointEventHandler.scriptPermissions, response.data.scriptPermissions[i]);
    
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
        
        const socketOpenDeferred = config.defer();
        const listUpdatedDeferred = config.defer();
        
        const testId = uuidV4();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/event-handlers'
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
                    assert.strictEqual(msg.payload.object.xid, global.pointValueSetPointEventHandler.xid);
                    listUpdatedDeferred.resolve();   
                }catch(e){
                    listUpdatedDeferred.reject(e);
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
            
        }).then(() => config.delay(1000)).then(() => {
            return client.restRequest({
                path: `/rest/v2/event-handlers/${global.pointValueSetPointEventHandler.xid}`,
                method: 'DELETE',
                data: {}
            }).then(response => {
                assert.equal(response.data.id, global.pointValueSetPointEventHandler.id);
                assert.equal(response.data.name, global.pointValueSetPointEventHandler.name);
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
        global.processEventHandler = {
                xid : "EVTH_PROCESS_TEST",
                name : "Testing process",
                disabled : true,
                activeProcessCommand : 'ls',
                activeProcessTimeout : 1000,
                inactiveProcessCommand: 'cd /',
                inactiveProcessTimeout: 1000,
                handlerType : "PROCESS"
              };
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: global.processEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, global.processEventHandler.xid);
            assert.strictEqual(response.data.name, global.processEventHandler.name);
            assert.strictEqual(response.data.disabled, global.processEventHandler.disabled);
            assert.strictEqual(response.data.activeProcessCommand, global.processEventHandler.activeProcessCommand);
            assert.strictEqual(response.data.activeProcessTimeout, global.processEventHandler.activeProcessTimeout);
            assert.strictEqual(response.data.inactiveProcessCommand, global.processEventHandler.inactiveProcessCommand);
            assert.strictEqual(response.data.inactiveProcessTimeout, global.processEventHandler.inactiveProcessTimeout);
            
            assert.isNumber(response.data.id);
            global.processEventHandler.id = response.data.id;
        });
    });
    
    it('Patch process event handler', () => {
        global.processEventHandler.disabled = false;
        return client.restRequest({
            path: '/rest/v2/event-handlers/EVTH_PROCESS_TEST',
            method: 'PATCH',
            data: {
                disabled: false
            }
        }).then(response => {
            assert.strictEqual(response.data.xid, global.processEventHandler.xid);
            assert.strictEqual(response.data.name, global.processEventHandler.name);
            assert.strictEqual(response.data.disabled, global.processEventHandler.disabled);
            assert.strictEqual(response.data.activeProcessCommand, global.processEventHandler.activeProcessCommand);
            assert.strictEqual(response.data.activeProcessTimeout, global.processEventHandler.activeProcessTimeout);
            assert.strictEqual(response.data.inactiveProcessCommand, global.processEventHandler.inactiveProcessCommand);
            assert.strictEqual(response.data.inactiveProcessTimeout, global.processEventHandler.inactiveProcessTimeout);
            
            assert.isNumber(response.data.id);
            global.processEventHandler.id = response.data.id;
        });
    });
    
    it('Delete process event handler', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers/${global.processEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.strictEqual(response.data.xid, global.processEventHandler.xid);
            assert.strictEqual(response.data.name, global.processEventHandler.name);
            assert.strictEqual(response.data.id, global.processEventHandler.id);
        });
    });
    
    //Email Event Handler Tests
    it('Create email event handler', () => {
        global.emailEventHandler = {
                xid : "EVTH_EMAIL_TEST",
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
                        {xid: global.dp1.xid, variableName:'point1'},
                        {xid: global.dp2.xid, variableName:'point2'}
                    ],
                scriptPermissions: ['admin', 'testing'],
                script: 'return 0;',
                handlerType : "EMAIL"
              };
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: global.emailEventHandler
        }).then(response => {
            assert.strictEqual(response.data.xid, global.emailEventHandler.xid);
            assert.strictEqual(response.data.name, global.emailEventHandler.name);
            assert.strictEqual(response.data.disabled, global.emailEventHandler.disabled);
            
            assert.strictEqual(response.data.activeRecipients.length, global.emailEventHandler.activeRecipients.length);
            assert.strictEqual(response.data.activeRecipients[0].username, global.emailEventHandler.activeRecipients[0].username);
            assert.strictEqual(response.data.activeRecipients[1].address, global.emailEventHandler.activeRecipients[1].address);
            
            assert.strictEqual(response.data.sendEscalation, global.emailEventHandler.sendEscalation);
            assert.strictEqual(response.data.repeatEscalations, global.emailEventHandler.repeatEscalations);
            assert.strictEqual(response.data.escalationDelayType, global.emailEventHandler.escalationDelayType);
            assert.strictEqual(response.data.escalationDelay, global.emailEventHandler.escalationDelay);
            
            assert.strictEqual(response.data.escalationRecipients.length, global.emailEventHandler.escalationRecipients.length);
            assert.strictEqual(response.data.escalationRecipients[0].username, global.emailEventHandler.escalationRecipients[0].username);
            assert.strictEqual(response.data.escalationRecipients[1].address, global.emailEventHandler.escalationRecipients[1].address);
            
            assert.strictEqual(response.data.sendInactive, global.emailEventHandler.sendInactive);
            assert.strictEqual(response.data.inactiveOverride, global.emailEventHandler.inactiveOverride);
            
            assert.strictEqual(response.data.inactiveRecipients.length, global.emailEventHandler.inactiveRecipients.length);
            assert.strictEqual(response.data.inactiveRecipients[0].username, global.emailEventHandler.inactiveRecipients[0].username);
            assert.strictEqual(response.data.inactiveRecipients[1].address, global.emailEventHandler.inactiveRecipients[1].address);
            
            assert.strictEqual(response.data.includeSystemInfo, global.emailEventHandler.includeSystemInfo);
            assert.strictEqual(response.data.includeLogfile, global.emailEventHandler.includeLogfile);
            assert.strictEqual(response.data.customTemplate, global.emailEventHandler.customTemplate);
            
            assert.strictEqual(response.data.scriptContext.length, global.emailEventHandler.scriptContext.length);
            for(var i=0; i<response.data.scriptContext.length; i++){
                assert.strictEqual(response.data.scriptContext[i].xid, global.emailEventHandler.scriptContext[i].xid);
                assert.strictEqual(response.data.scriptContext[i].variableName, global.emailEventHandler.scriptContext[i].variableName);
            }
            for(var i=0; i<response.data.scriptPermissions.length; i++)
                assert.include(global.emailEventHandler.scriptPermissions, response.data.scriptPermissions[i]);

            assert.isNumber(response.data.id);
            global.emailEventHandler.id = response.data.id;
        });
    });
    
    it('Test invalid email event handler', () => {
        global.invalidEmailEventHandler = {
                xid : "EVTH_EMAIL_TEST_INVALID",
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
                        {xid: global.dp2.xid, variableName:'point2'}
                    ],
                scriptPermissions: ['admin', 'testing'],
                script: 'return 0;',
                handlerType : "EMAIL"
              };
        return client.restRequest({
            path: '/rest/v2/event-handlers',
            method: 'POST',
            data: global.invalidEmailEventHandler
        }).then(response => {
            throw new Error('Should not have created email event handler');
        }, error => {
            assert.strictEqual(error.status, 422);
            assert.strictEqual(error.data.result.messages.length, 4);
            
            //Missing user
            assert.strictEqual(error.data.result.messages[0].property, 'activeRecipients[0]');
            //Missing user
            assert.strictEqual(error.data.result.messages[1].property, 'escalationRecipients[0]');
            //Invalid FTL
            assert.strictEqual(error.data.result.messages[2].property, 'customTemplate');
            //Missing point
            assert.strictEqual(error.data.result.messages[3].property, 'scriptContext[0].id');
        });
    });
    
    it('Delete email event handler', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers/${global.emailEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.strictEqual(response.data.xid, global.emailEventHandler.xid);
            assert.strictEqual(response.data.name, global.emailEventHandler.name);
            assert.strictEqual(response.data.id, global.emailEventHandler.id);
        });
    });
    
});
