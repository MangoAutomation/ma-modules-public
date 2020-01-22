/**
 * Copyright 2018 Infinite Automation Systems Inc.
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

/* global describe, before, after, it, assert */
const {createClient, login, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Maintenance events', function() {
    
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
            xid: 'me_test_2',
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
        return dp1.save().then(dp =>{
            testContext.dp1 = dp;
        });
    });
    
    before('Create test DP 2', function() {
        const dp2 = this.point('test point 2');
        return dp2.save().then(dp =>{
            testContext.dp2 = dp;
        });
    });
    
    after('Delete DS 1', function() {
        return testContext.ds1.delete();
    });
    after('Delete DS2', function() {
        return testContext.ds2.delete();
    });
    
    it('Creates a data point based maintenance event', () => {
      testContext.maintEventWithDataPoint = {
        xid: 'MAINT_TEST_ONE_DATA_POINT',
        name: 'Test maintenance event',
        dataPoints: [testContext.dp1.xid],
        scheduleType: 'MANUAL',
        alarmLevel: 'URGENT'
      };

      return client.restRequest({
          path: '/rest/v2/maintenance-events',
          method: 'POST',
          data: testContext.maintEventWithDataPoint
      }).then(response => {
          assert.equal(response.data.xid, testContext.maintEventWithDataPoint.xid);
          assert.equal(response.data.name, testContext.maintEventWithDataPoint.name);
          assert.equal(response.data.dataPoints.length, testContext.maintEventWithDataPoint.dataPoints.length);
          assert.equal(response.data.dataPoints[0], testContext.maintEventWithDataPoint.dataPoints[0]);
          assert.isTrue(typeof response.data.dataSources === 'undefined');
          assert.equal(response.data.alarmLevel, testContext.maintEventWithDataPoint.alarmLevel);
          testContext.maintEventWithDataPoint = response.data;
        
      });
    });

    it('Creates a data points based maintenance event', () => {
        testContext.maintEventWithDataPoints = {
          xid: 'MAINT_TEST_ONE_DATA_POINTS',
          name: 'Test maintenance event',
          dataPoints: [testContext.dp1.xid, testContext.dp2.xid],
          scheduleType: 'MANUAL',
          alarmLevel: 'URGENT'
        };

        return client.restRequest({
            path: '/rest/v2/maintenance-events',
            method: 'POST',
            data: testContext.maintEventWithDataPoints
        }).then(response => {
            assert.equal(response.data.xid, testContext.maintEventWithDataPoints.xid);
            assert.equal(response.data.name, testContext.maintEventWithDataPoints.name);
            assert.equal(response.data.dataPoints.length, testContext.maintEventWithDataPoints.dataPoints.length);
            var found = 0;
            for(var i=0; i<testContext.maintEventWithDataPoints.dataPoints.length; i++){
                for(var j=0; j<response.data.dataPoints.length; j++){
                    if(testContext.maintEventWithDataPoints.dataPoints[i] === response.data.dataPoints[j]){
                        found++;
                        break;
                    }
                }
            }
            assert.isTrue(found === testContext.maintEventWithDataPoints.dataPoints.length);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, testContext.maintEventWithDataPoints.alarmLevel);
            testContext.maintEventWithDataPoints = response.data;
          
        });
    });
    
    it('Creates a data source based maintenance event', () => {
        testContext.maintEventWithDataSource = {
          xid: 'MAINT_TEST_ONE_DATA_SOURCE',
          name: 'Test maintenance event',
          dataSources: [testContext.ds1.xid],
          scheduleType: 'MANUAL',
          alarmLevel: 'URGENT'
        };

        return client.restRequest({
            path: '/rest/v2/maintenance-events',
            method: 'POST',
            data: testContext.maintEventWithDataSource
        }).then(response => {
            assert.equal(response.data.xid, testContext.maintEventWithDataSource.xid);
            assert.equal(response.data.name, testContext.maintEventWithDataSource.name);
            assert.equal(response.data.dataSources.length, testContext.maintEventWithDataSource.dataSources.length);
            assert.equal(response.data.dataSources[0], testContext.maintEventWithDataSource.dataSources[0]);
            assert.isTrue(typeof response.data.dataPoints === 'undefined');
            assert.equal(response.data.alarmLevel, testContext.maintEventWithDataSource.alarmLevel);
            testContext.maintEventWithDataSource = response.data;
          
        });
    });

    it('Creates a data sources based maintenance event', () => {
        testContext.maintEventWithDataSources = {
          xid: 'MAINT_TEST_ONE_DATA_SOURCES',
          name: 'Test maintenance event',
          dataSources: [testContext.ds1.xid, testContext.ds2.xid],
          scheduleType: 'MANUAL',
          alarmLevel: 'URGENT'
        };

        return client.restRequest({
            path: '/rest/v2/maintenance-events',
            method: 'POST',
            data: testContext.maintEventWithDataSources
        }).then(response => {
            assert.equal(response.data.xid, testContext.maintEventWithDataSources.xid);
            assert.equal(response.data.name, testContext.maintEventWithDataSources.name);
            assert.equal(response.data.dataSources.length, testContext.maintEventWithDataSources.dataSources.length);
            var found = 0;
            for(var i=0; i<testContext.maintEventWithDataSources.dataSources.length; i++){
                for(var j=0; j<response.data.dataSources.length; j++){
                    if(testContext.maintEventWithDataSources.dataSources[i] === response.data.dataSources[j]){
                        found++;
                        break;
                    }
                }
            }
            assert.isTrue(found === testContext.maintEventWithDataSources.dataSources.length);
            assert.isTrue(typeof response.data.dataPoints === 'undefined');
            assert.equal(response.data.alarmLevel, testContext.maintEventWithDataSources.alarmLevel);
            testContext.maintEventWithDataSources = response.data;
          
        });
    });
    
    it('Patch a data point based maintenance event', () => {
        testContext.maintEventWithDataPoint.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataPoint.xid}`,
            method: 'PATCH',
            data: {
                name: 'updated name'
            }
        }).then(response => {
            assert.equal(response.data.xid, testContext.maintEventWithDataPoint.xid);
            assert.equal(response.data.name, testContext.maintEventWithDataPoint.name);
            assert.equal(response.data.dataPoints.length, testContext.maintEventWithDataPoint.dataPoints.length);
            assert.equal(response.data.dataPoints[0], testContext.maintEventWithDataPoint.dataPoints[0]);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, testContext.maintEventWithDataPoint.alarmLevel);
            testContext.maintEventWithDataPoint = response.data;
          
        });
    });
    
    it('Get a data point based maintenance event', () => {
        testContext.maintEventWithDataPoint.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataPoint.xid}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.xid, testContext.maintEventWithDataPoint.xid);
            assert.equal(response.data.name, testContext.maintEventWithDataPoint.name);
            assert.equal(response.data.dataPoints.length, testContext.maintEventWithDataPoint.dataPoints.length);
            assert.equal(response.data.dataPoints[0], testContext.maintEventWithDataPoint.dataPoints[0]);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, testContext.maintEventWithDataPoint.alarmLevel);
        });
    });
    
    it('Put a data points based maintenance event', () => {
        testContext.maintEventWithDataPoints.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataPoints.xid}`,
            method: 'PUT',
            data: testContext.maintEventWithDataPoints
        }).then(response => {
            assert.equal(response.data.xid, testContext.maintEventWithDataPoints.xid);
            assert.equal(response.data.name, testContext.maintEventWithDataPoints.name);
            assert.equal(response.data.dataPoints.length, testContext.maintEventWithDataPoints.dataPoints.length);
            var found = 0;
            for(var i=0; i<testContext.maintEventWithDataPoints.dataPoints.length; i++){
                for(var j=0; j<response.data.dataPoints.length; j++){
                    if(testContext.maintEventWithDataPoints.dataPoints[i] === response.data.dataPoints[j]){
                        found++;
                        break;
                    }
                }
            }
            assert.isTrue(found === testContext.maintEventWithDataPoints.dataPoints.length);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, testContext.maintEventWithDataPoints.alarmLevel);
            testContext.maintEventWithDataPoints = response.data;
          
        });
    });
    
    it('Toggle a data points based maintenance event', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/toggle/${testContext.maintEventWithDataPoints.xid}`,
            method: 'PUT'
        }).then(response => {
            assert.equal(response.data, true);
        });
    });
    
    it('Query by xid', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events?xid=${testContext.maintEventWithDataPoint.xid}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].xid, testContext.maintEventWithDataPoint.xid);
            assert.equal(response.data.items[0].name, testContext.maintEventWithDataPoint.name);
            assert.equal(response.data.items[0].dataPoints.length, testContext.maintEventWithDataPoint.dataPoints.length);
            assert.equal(response.data.items[0].dataPoints[0], testContext.maintEventWithDataPoint.dataPoints[0]);
            assert.isTrue(typeof response.data.items[0].dataSources === 'undefined');
            assert.equal(response.data.items[0].alarmLevel, testContext.maintEventWithDataPoint.alarmLevel);
        });
    });
    //TODO By data point xid
    
    it('Deletes data point me', () => {
      return client.restRequest({
          path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataPoint.xid}`,
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.id, testContext.maintEventWithDataPoint.id);
      });
    });
    it('Deletes data points me', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataPoints.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, testContext.maintEventWithDataPoints.id);
        });
    });
    it('Deletes data source me', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataSource.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, testContext.maintEventWithDataSource.id);
        });
    });
    it('Deletes data sources me', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${testContext.maintEventWithDataSources.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, testContext.maintEventWithDataSources.id);
        });
    });
});