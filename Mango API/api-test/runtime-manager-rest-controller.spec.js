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

// Mango REST V1 API - Operations on Data Source Runtime Manager
describe('runtime-manager-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    before('Create data source', function() {
        this.dsXid = uuid();
        
        return client.restRequest({
            method: 'POST',
            path: '/rest/latest/data-sources',
            data: {
                xid: this.dsXid,
                name: this.dsXid,
                deviceName: this.dsXid,
                enabled: true,
                modelType: 'VIRTUAL',
                pollPeriod: { periods: 5, type: 'SECONDS' },
                purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
                eventAlarmLevels: [],
                editPermission: []
            }
        });
    });
    
    after('Delete data source', function() {
        return client.restRequest({
            method: 'DELETE',
            path: `/rest/latest/data-sources/${this.dsXid}`,
        }).catch(noop);
    });

    beforeEach('Create a data point', function() {
        this.currentTest.dpXid = uuid();
        
        return client.restRequest({
            method: 'POST',
            path: '/rest/latest/data-points',
            data: {
                xid: this.currentTest.dpXid,
                enabled: true,
                name: this.currentTest.dpXid,
                deviceName: this.currentTest.dpXid,
                dataSourceXid : this.dsXid,
                pointLocator : {
                    startValue : '0',
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    changeType : 'NO_CHANGE',
                }
            }
        });
    });

    // Force Refresh a data point - Not all data sources implement this feature
    it('PUT /rest/latest/runtime-manager/force-refresh/{xid}', function() {
        return client.restRequest({
            method: 'PUT',
            path: `/rest/latest/runtime-manager/force-refresh/${this.test.dpXid}`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
        });
    });

    // Relinquish the value of a data point - Only BACnet data points allow this
    it('POST /rest/latest/runtime-manager/relinquish/{xid}', function() {
        return client.restRequest({
            method: 'POST',
            path: `/rest/latest/runtime-manager/relinquish/${this.test.dpXid}`,
        }).then(response => {
            assert.fail('Relinquish should not be supported on virtual data source');
        }, response => {
            // OK
            assert.strictEqual(response.status, 500);
        });
    });

});
