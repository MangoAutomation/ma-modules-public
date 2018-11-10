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
    this.timeout(100000);
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
    
    it('Query mailing lists', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers?xid=${global.staticValueSetPointEventHandler.xid}`,
            method: 'GET',
            data: global.addressMailingList
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.strictEqual(response.data.items[0].xid, global.staticValueSetPointEventHandler.xid);
            assert.strictEqual(response.data.items[0].name, global.staticValueSetPointEventHandler.name);
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
    });
    
    it('Delete point set point event handler', () => {
        return client.restRequest({
            path: `/rest/v2/event-handlers/${global.pointValueSetPointEventHandler.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.xid, global.pointValueSetPointEventHandler.xid);
            assert.equal(response.data.name, global.pointValueSetPointEventHandler.name);
            assert.isNumber(response.data.id);
        });
    });
});
