/**
 * Copyright 2020 Infinite Automation Systems Inc.
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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

// Mango REST V1 API - Restore/Read Configuration From History
describe('audit-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    // Query Audit Events - Admin access only
    it('GET /rest/v1/audit', function() {
        return client.restRequest({
            method: 'GET',
            path: '/rest/v1/audit?limit(10)'
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: AuditQueryResult
            assert.isObject(response.data, 'data');
            assert.isArray(response.data.items, 'data.items');
            assert.isAbove(response.data.items.length, 0, 'data.items');
            response.data.items.forEach((item, index) => {
                // MODEL: AuditEventInstanceModel
                assert.isObject(item, 'data.items[]');
                assert.isString(item.alarmLevel, 'data.items[].alarmLevel');
                assert.include(["NONE","INFORMATION","IMPORTANT","WARNING","URGENT","CRITICAL","LIFE_SAFETY","DO_NOT_LOG","IGNORE"], item.alarmLevel, 'data.items[].alarmLevel');
                assert.isString(item.changeType, 'data.items[].changeType');
                if (item.context != null) {
                    assert.isObject(item.context, 'data.items[].context');
                }
                assert.isString(item.message, 'data.items[].message');
                // DESCRIPTION: Model Type Definition
                assert.isString(item.modelType, 'data.items[].modelType');
                assert.isNumber(item.objectId, 'data.items[].objectId');
                assert.isNumber(item.timestamp, 'data.items[].timestamp');
                assert.isString(item.typeName, 'data.items[].typeName');
                assert.isString(item.username, 'data.items[].username');
                // END MODEL: AuditEventInstanceModel
            });
            assert.isNumber(response.data.total, 'data.total');
            // END MODEL: AuditQueryResult
        });
    });

    // List all Audit Event Types in the system - Admin access only
    it('GET /rest/v1/audit/list-event-types', function() {
        return client.restRequest({
            method: 'GET',
            path: '/rest/v1/audit/list-event-types',
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            assert.isAbove(response.data.length, 0, 'data');
            response.data.forEach((item, index) => {
                // MODEL: EventTypeInfo
                assert.isObject(item, 'data[]');
                assert.isString(item.alarmLevel, 'data[].alarmLevel');
                assert.include(["NONE","INFORMATION","IMPORTANT","WARNING","URGENT","CRITICAL","LIFE_SAFETY","DO_NOT_LOG","IGNORE"], item.alarmLevel, 'data[].alarmLevel');
                assert.isString(item.description, 'data[].description');
                assert.isString(item.subtype, 'data[].subtype');
                assert.isString(item.type, 'data[].type');
                // END MODEL: EventTypeInfo
            });
        });
    });

});
