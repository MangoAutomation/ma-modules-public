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

describe('Event types v2', function() {
    before('Login', config.login);
    
    it('Query data point event types', () => {
        
        return client.restRequest({
            path: `/rest/v2/event-types/DATA_POINT`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            for(var i=0; i<response.data.items.length; i++){
                assert.isString(response.data.items[i].alarmLevel);
                assert.isString(response.data.items[i].type.eventType);
                assert.strictEqual('DATA_POINT', response.data.items[i].type.eventType)
            }           
        });
    });
    
    it('Query data source event types', () => {
        
        return client.restRequest({
            path: `/rest/v2/event-types/DATA_SOURCE`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            for(var i=0; i<response.data.items.length; i++){
                assert.isString(response.data.items[i].alarmLevel);
                assert.isString(response.data.items[i].type.eventType);
                assert.strictEqual('DATA_SOURCE', response.data.items[i].type.eventType)
            }           
        });
    });
    
    it('Query publiser event types', () => {
        
        return client.restRequest({
            path: `/rest/v2/event-types/PUBLISHER`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            for(var i=0; i<response.data.items.length; i++){
                assert.isString(response.data.items[i].alarmLevel);
                assert.isString(response.data.items[i].type.eventType);
                assert.strictEqual('PUBLISHER', response.data.items[i].type.eventType)
            }           
        });
    });
    
    it('Query system event types', () => {
        
        return client.restRequest({
            path: `/rest/v2/event-types/SYSTEM`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            for(var i=0; i<response.data.items.length; i++){
                assert.isString(response.data.items[i].alarmLevel);
                assert.isString(response.data.items[i].type.eventType);
                assert.strictEqual('SYSTEM', response.data.items[i].type.eventType)
            }           
        });
    });
    
    it('Query audit event types', () => {
        
        return client.restRequest({
            path: `/rest/v2/event-types/AUDIT`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            for(var i=0; i<response.data.items.length; i++){
                assert.isString(response.data.items[i].alarmLevel);
                assert.isString(response.data.items[i].type.eventType);
                assert.strictEqual('AUDIT', response.data.items[i].type.eventType)
            }           
        });
    });
    
    //TODO Module defined events...
    
});
