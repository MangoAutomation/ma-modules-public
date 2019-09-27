/**
 * Copyright 2017 Infinite Automation Systems Inc.
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

const {createClient, login} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();

describe('Work Item tests', function(){
    before('Login', function() { return login.call(this, client); });

    it('Lists all work items', () => {
      return client.restRequest({
          path: '/rest/v1/work-items',
          method: 'GET'
      }).then(response => {
        assert.isAbove(response.data.length, 0);
        for(var i=0; i<response.data.length; i++){
          assert.notEqual(response.data[i].classname, null);
          assert.notEqual(response.data[i].description, null);
          assert.notEqual(response.data[i].priority, null);
        }
      });
    });

    it('Filters on DataPurgeTask', () => {
      return client.restRequest({
          path: '/rest/v1/work-items',
          method: 'GET',
          params: {
            classname: 'com.serotonin.m2m2.rt.maint.DataPurge.DataPurgeTask'
          }
      }).then(response => {
        assert.equal(response.data.length, 1);
        assert.equal(response.data[0].classname, 'com.serotonin.m2m2.rt.maint.DataPurge.DataPurgeTask');
        assert.equal(response.data[0].description, 'Data purge task');
        assert.equal(response.data[0].priority, 'HIGH');
      });
    });

    it('Lists all queued work item counts', () => {
      return client.restRequest({
          path: '/rest/v1/work-items/queue-counts',
          method: 'GET'
      }).then(response => {
        assert.notEqual(response.data.highPriorityServiceQueueClassCounts, null);
        assert.equal(response.data.highPriorityServiceQueueClassCounts['Data purge task'], 1);
        assert.notEqual(response.data.mediumPriorityServiceQueueClassCounts, null);
        assert.notEqual(response.data.lowPriorityServiceQueueClassCounts, null);
      });
    });

    it('Lists all running work item stats', () => {
      return client.restRequest({
          path: '/rest/v1/work-items/running-stats',
          method: 'GET'
      }).then(response => {
        assert.notEqual(response.data.highPriorityOrderedQueueStats, null);
        assert.notEqual(response.data.mediumPriorityOrderedQueueStats, null);
      });
    });

    it('Lists all rejected work item stats', () => {
      return client.restRequest({
          path: '/rest/v1/work-items/rejected-stats',
          method: 'GET'
      }).then(response => {
        assert.notEqual(response.data.highPriorityRejectedTaskStats, null);
        assert.notEqual(response.data.mediumPriorityRejectedTaskStats, null);
      });
    });

});
