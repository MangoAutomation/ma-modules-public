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

const {createClient, login, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

// Mango REST V1 API - Mango Work Items
describe('work-item-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    // Get all work items - Returns a list of all work items, optionally filterable on classname
    it('GET /rest/latest/work-items', function() {
        const params = {
            classname: 'string' // in = query, description = classname, required = false, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/work-items`,
            params: {
                //classname: params.classname
            }
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            response.data.forEach((item, index) => {
                // MODEL: WorkItemModel
                assert.isObject(item, 'data[]');
                assert.isString(item.classname, 'data[].classname');
                assert.isString(item.description, 'data[].description');
                assert.isString(item.priority, 'data[].priority');
                // END MODEL: WorkItemModel
            });
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        });
    });

    // Get list of work items by classname - Returns the Work Item specified by the given classname and priority
    it('GET /rest/latest/work-items/by-priority/{priority}', function() {
        const params = {
            classname: 'string', // in = query, description = classname, required = false, type = string, default = , enum = 
            priority: 'HIGH' // in = path, description = priority, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/work-items/by-priority/${params.priority}`,
            params: {
                //classname: params.classname
            }
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            response.data.forEach((item, index) => {
                // MODEL: WorkItemModel
                assert.isObject(item, 'data[]');
                assert.isString(item.classname, 'data[].classname');
                assert.isString(item.description, 'data[].description');
                assert.isString(item.priority, 'data[].priority');
                // END MODEL: WorkItemModel
            });
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        });
    });

    // Get Queued Work Item Counts - Returns Work Item names to instance count for High, Medium and Low thread pools
    it('GET /rest/latest/work-items/queue-counts', function() {
        const params = {
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/work-items/queue-counts`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: BackgroundProcessingQueueCounts
            assert.isObject(response.data, 'data');
            assert.isObject(response.data.highPriorityServiceQueueClassCounts, 'data.highPriorityServiceQueueClassCounts');
            assert.isObject(response.data.lowPriorityServiceQueueClassCounts, 'data.lowPriorityServiceQueueClassCounts');
            assert.isObject(response.data.mediumPriorityServiceQueueClassCounts, 'data.mediumPriorityServiceQueueClassCounts');
            // END MODEL: BackgroundProcessingQueueCounts
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        });
    });

    // Get Rejected Task Statistics - Returns information on all tasks rejected from the High and Medium thread pools
    it('GET /rest/latest/work-items/rejected-stats', function() {
        const params = {
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/work-items/rejected-stats`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: BackgroundProcessingRejectedTaskStats
            assert.isObject(response.data, 'data');
            assert.isArray(response.data.highPriorityRejectedTaskStats, 'data.highPriorityRejectedTaskStats');
            response.data.highPriorityRejectedTaskStats.forEach((item, index) => {
                // MODEL: RejectedTaskStats
                assert.isObject(item, 'data.highPriorityRejectedTaskStats[]');
                assert.isNumber(item.currentlyRunning, 'data.highPriorityRejectedTaskStats[].currentlyRunning');
                if (item.id != null) {
                    assert.isString(item.id, 'data.highPriorityRejectedTaskStats[].id');
                }
                assert.isNumber(item.lastAccess, 'data.highPriorityRejectedTaskStats[].lastAccess');
                assert.isString(item.name, 'data.highPriorityRejectedTaskStats[].name');
                assert.isNumber(item.poolFull, 'data.highPriorityRejectedTaskStats[].poolFull');
                assert.isNumber(item.queueFull, 'data.highPriorityRejectedTaskStats[].queueFull');
                assert.isNumber(item.totalRejections, 'data.highPriorityRejectedTaskStats[].totalRejections');
                // END MODEL: RejectedTaskStats
            });
            assert.isArray(response.data.mediumPriorityRejectedTaskStats, 'data.mediumPriorityRejectedTaskStats');
            response.data.mediumPriorityRejectedTaskStats.forEach((item, index) => {
                // MODEL: RejectedTaskStats
                assert.isObject(item, 'data.mediumPriorityRejectedTaskStats[]');
                assert.isNumber(item.currentlyRunning, 'data.mediumPriorityRejectedTaskStats[].currentlyRunning');
                assert.isString(item.id, 'data.mediumPriorityRejectedTaskStats[].id');
                assert.isNumber(item.lastAccess, 'data.mediumPriorityRejectedTaskStats[].lastAccess');
                assert.isString(item.name, 'data.mediumPriorityRejectedTaskStats[].name');
                assert.isNumber(item.poolFull, 'data.mediumPriorityRejectedTaskStats[].poolFull');
                assert.isNumber(item.queueFull, 'data.mediumPriorityRejectedTaskStats[].queueFull');
                assert.isNumber(item.totalRejections, 'data.mediumPriorityRejectedTaskStats[].totalRejections');
                // END MODEL: RejectedTaskStats
            });
            // END MODEL: BackgroundProcessingRejectedTaskStats
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        });
    });

    // Get Running Work Item Statistics - Returns information on all tasks running in the High and Medium thread pools
    it('GET /rest/latest/work-items/running-stats', function() {
        const params = {
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/work-items/running-stats`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: BackgroundProcessingRunningStats
            assert.isObject(response.data, 'data');
            assert.isArray(response.data.highPriorityOrderedQueueStats, 'data.highPriorityOrderedQueueStats');
            response.data.highPriorityOrderedQueueStats.forEach((item, index) => {
                // MODEL: OrderedTaskInfo
                assert.isObject(item, 'data.highPriorityOrderedQueueStats[]');
                assert.isNumber(item.avgExecutionTimeMs, 'data.highPriorityOrderedQueueStats[].avgExecutionTimeMs');
                assert.isNumber(item.currentQueueSize, 'data.highPriorityOrderedQueueStats[].currentQueueSize');
                assert.isNumber(item.executionCount, 'data.highPriorityOrderedQueueStats[].executionCount');
                assert.isString(item.id, 'data.highPriorityOrderedQueueStats[].id');
                assert.isNumber(item.lastExecutionTimeMs, 'data.highPriorityOrderedQueueStats[].lastExecutionTimeMs');
                assert.isNumber(item.maxQueueSize, 'data.highPriorityOrderedQueueStats[].maxQueueSize');
                assert.isString(item.name, 'data.highPriorityOrderedQueueStats[].name');
                assert.isNumber(item.queueSizeLimit, 'data.highPriorityOrderedQueueStats[].queueSizeLimit');
                assert.isNumber(item.rejections, 'data.highPriorityOrderedQueueStats[].rejections');
                // END MODEL: OrderedTaskInfo
            });
            assert.isArray(response.data.mediumPriorityOrderedQueueStats, 'data.mediumPriorityOrderedQueueStats');
            response.data.mediumPriorityOrderedQueueStats.forEach((item, index) => {
                // MODEL: OrderedTaskInfo
                assert.isObject(item, 'data.mediumPriorityOrderedQueueStats[]');
                assert.isNumber(item.avgExecutionTimeMs, 'data.mediumPriorityOrderedQueueStats[].avgExecutionTimeMs');
                assert.isNumber(item.currentQueueSize, 'data.mediumPriorityOrderedQueueStats[].currentQueueSize');
                assert.isNumber(item.executionCount, 'data.mediumPriorityOrderedQueueStats[].executionCount');
                assert.isString(item.id, 'data.mediumPriorityOrderedQueueStats[].id');
                assert.isNumber(item.lastExecutionTimeMs, 'data.mediumPriorityOrderedQueueStats[].lastExecutionTimeMs');
                assert.isNumber(item.maxQueueSize, 'data.mediumPriorityOrderedQueueStats[].maxQueueSize');
                assert.isString(item.name, 'data.mediumPriorityOrderedQueueStats[].name');
                assert.isNumber(item.queueSizeLimit, 'data.mediumPriorityOrderedQueueStats[].queueSizeLimit');
                assert.isNumber(item.rejections, 'data.mediumPriorityOrderedQueueStats[].rejections');
                // END MODEL: OrderedTaskInfo
            });
            // END MODEL: BackgroundProcessingRunningStats
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        });
    });

});
