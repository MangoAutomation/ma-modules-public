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

const {createClient, login, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

// Mango REST V1 API - Mango Application Threads
describe('thread-monitor-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    // Get all threads - Larger stack depth will slow this request
    it('GET /rest/v1/threads', function() {
        const params = {
            asFile: false, // in = query, description = Return as file, required = false, type = boolean, default = false, enum = 
            orderBy: 'cpuTime', // in = query, description = Order by this member, required = false, type = string, default = , enum = 
            stackDepth: 10 // in = query, description = Limit size of stack trace, required = false, type = integer, default = 10, enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/v1/threads`,
            params: {
                asFile: params.asFile,
                orderBy: params.orderBy,
                stackDepth: params.stackDepth
            }
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            response.data.forEach((item, index) => {
                // MODEL: ThreadModel
                assert.isObject(item, 'data[]');
                assert.isNumber(item.cpuTime, 'data[].cpuTime');
                assert.isNumber(item.id, 'data[].id');
                assert.isArray(item.location, 'data[].location');
                item.location.forEach((item, index) => {
                    // MODEL: StackTraceElement
                    assert.isObject(item, 'data[].location[]');
                    if (item.classLoaderName != null) {
                        assert.isString(item.classLoaderName, 'data[].location[].classLoaderName');
                    }
                    assert.isString(item.className, 'data[].location[].className');
                    assert.isString(item.fileName, 'data[].location[].fileName');
                    assert.isNumber(item.lineNumber, 'data[].location[].lineNumber');
                    assert.isString(item.methodName, 'data[].location[].methodName');
                    if (item.moduleName != null) {
                        assert.isString(item.moduleName, 'data[].location[].moduleName');
                    }
                    if (item.moduleVersion != null) {
                        assert.isString(item.moduleVersion, 'data[].location[].moduleVersion');
                    }
                    assert.isBoolean(item.nativeMethod, 'data[].location[].nativeMethod');
                    // END MODEL: StackTraceElement
                });
                if (item.lockInfo != null) {
                    // MODEL: LockInfo
                    assert.isObject(item.lockInfo, 'data[].lockInfo');
                    assert.isString(item.lockInfo.className, 'data[].lockInfo.className');
                    assert.isNumber(item.lockInfo.identityHashCode, 'data[].lockInfo.identityHashCode');
                    // END MODEL: LockInfo
                }
                assert.isNumber(item.lockOwnerId, 'data[].lockOwnerId');
                if (item.lockOwnerName != null) {
                    assert.isString(item.lockOwnerName, 'data[].lockOwnerName');
                }
                assert.isString(item.name, 'data[].name');
                assert.isNumber(item.priority, 'data[].priority');
                assert.isString(item.state, 'data[].state');
                assert.include(["NEW","RUNNABLE","BLOCKED","WAITING","TIMED_WAITING","TERMINATED"], item.state, 'data[].state');
                assert.isNumber(item.userTime, 'data[].userTime');
                // END MODEL: ThreadModel
            });
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        });
    });

});
