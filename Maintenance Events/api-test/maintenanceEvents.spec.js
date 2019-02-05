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

const config = require('@infinite-automation/mango-client/test/setup');

describe('Maintenance events', function() {
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
        return dp1.save().then(dp =>{
            global.dp1 = dp;
        });
    });
    
    before('Create test DP 2', function() {
        const dp2 = this.point('test point 2');
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
    
    it('Creates a data point based maintenance event', () => {
      global.maintEventWithDataPoint = {
        xid: 'MAINT_TEST_ONE_DATA_POINT',
        name: 'Test maintenance event',
        dataPoints: [global.dp1.xid],
        scheduleType: 'MANUAL',
        alarmLevel: "URGENT"
      };

      return client.restRequest({
          path: '/rest/v2/maintenance-events',
          method: 'POST',
          data: global.maintEventWithDataPoint
      }).then(response => {
          assert.equal(response.data.xid, global.maintEventWithDataPoint.xid);
          assert.equal(response.data.name, global.maintEventWithDataPoint.name);
          assert.equal(response.data.dataPoints.length, global.maintEventWithDataPoint.dataPoints.length);
          assert.equal(response.data.dataPoints[0], global.maintEventWithDataPoint.dataPoints[0]);
          assert.isTrue(typeof response.data.dataSources === 'undefined');
          assert.equal(response.data.alarmLevel, global.maintEventWithDataPoint.alarmLevel);
          global.maintEventWithDataPoint = response.data;
        
      });
    });

    it('Creates a data points based maintenance event', () => {
        global.maintEventWithDataPoints = {
          xid: 'MAINT_TEST_ONE_DATA_POINTS',
          name: 'Test maintenance event',
          dataPoints: [global.dp1.xid, global.dp2.xid],
          scheduleType: 'MANUAL',
          alarmLevel: "URGENT"
        };

        return client.restRequest({
            path: '/rest/v2/maintenance-events',
            method: 'POST',
            data: global.maintEventWithDataPoints
        }).then(response => {
            assert.equal(response.data.xid, global.maintEventWithDataPoints.xid);
            assert.equal(response.data.name, global.maintEventWithDataPoints.name);
            assert.equal(response.data.dataPoints.length, global.maintEventWithDataPoints.dataPoints.length);
            var found = 0;
            for(var i=0; i<global.maintEventWithDataPoints.dataPoints.length; i++){
                for(var j=0; j<response.data.dataPoints.length; j++){
                    if(global.maintEventWithDataPoints.dataPoints[i] === response.data.dataPoints[j]){
                        found++;
                        break;
                    }
                }
            }
            assert.isTrue(found === global.maintEventWithDataPoints.dataPoints.length);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, global.maintEventWithDataPoints.alarmLevel);
            global.maintEventWithDataPoints = response.data;
          
        });
    });
    
    it('Creates a data source based maintenance event', () => {
        global.maintEventWithDataSource = {
          xid: 'MAINT_TEST_ONE_DATA_SOURCE',
          name: 'Test maintenance event',
          dataSources: [global.ds1.xid],
          scheduleType: 'MANUAL',
          alarmLevel: "URGENT"
        };

        return client.restRequest({
            path: '/rest/v2/maintenance-events',
            method: 'POST',
            data: global.maintEventWithDataSource
        }).then(response => {
            assert.equal(response.data.xid, global.maintEventWithDataSource.xid);
            assert.equal(response.data.name, global.maintEventWithDataSource.name);
            assert.equal(response.data.dataSources.length, global.maintEventWithDataSource.dataSources.length);
            assert.equal(response.data.dataSources[0], global.maintEventWithDataSource.dataSources[0]);
            assert.isTrue(typeof response.data.dataPoints === 'undefined');
            assert.equal(response.data.alarmLevel, global.maintEventWithDataSource.alarmLevel);
            global.maintEventWithDataSource = response.data;
          
        });
    });

    it('Creates a data sources based maintenance event', () => {
        global.maintEventWithDataSources = {
          xid: 'MAINT_TEST_ONE_DATA_SOURCES',
          name: 'Test maintenance event',
          dataSources: [global.ds1.xid, global.ds2.xid],
          scheduleType: 'MANUAL',
          alarmLevel: "URGENT"
        };

        return client.restRequest({
            path: '/rest/v2/maintenance-events',
            method: 'POST',
            data: global.maintEventWithDataSources
        }).then(response => {
            assert.equal(response.data.xid, global.maintEventWithDataSources.xid);
            assert.equal(response.data.name, global.maintEventWithDataSources.name);
            assert.equal(response.data.dataSources.length, global.maintEventWithDataSources.dataSources.length);
            var found = 0;
            for(var i=0; i<global.maintEventWithDataSources.dataSources.length; i++){
                for(var j=0; j<response.data.dataSources.length; j++){
                    if(global.maintEventWithDataSources.dataSources[i] === response.data.dataSources[j]){
                        found++;
                        break;
                    }
                }
            }
            assert.isTrue(found === global.maintEventWithDataSources.dataSources.length);
            assert.isTrue(typeof response.data.dataPoints === 'undefined');
            assert.equal(response.data.alarmLevel, global.maintEventWithDataSources.alarmLevel);
            global.maintEventWithDataSources = response.data;
          
        });
    });
    
    it('Patch a data point based maintenance event', () => {
        global.maintEventWithDataPoint.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${global.maintEventWithDataPoint.xid}`,
            method: 'PATCH',
            data: {
                name: 'updated name'
            }
        }).then(response => {
            assert.equal(response.data.xid, global.maintEventWithDataPoint.xid);
            assert.equal(response.data.name, global.maintEventWithDataPoint.name);
            assert.equal(response.data.dataPoints.length, global.maintEventWithDataPoint.dataPoints.length);
            assert.equal(response.data.dataPoints[0], global.maintEventWithDataPoint.dataPoints[0]);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, global.maintEventWithDataPoint.alarmLevel);
            global.maintEventWithDataPoint = response.data;
          
        });
    });
    
    it('Get a data point based maintenance event', () => {
        global.maintEventWithDataPoint.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${global.maintEventWithDataPoint.xid}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.xid, global.maintEventWithDataPoint.xid);
            assert.equal(response.data.name, global.maintEventWithDataPoint.name);
            assert.equal(response.data.dataPoints.length, global.maintEventWithDataPoint.dataPoints.length);
            assert.equal(response.data.dataPoints[0], global.maintEventWithDataPoint.dataPoints[0]);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, global.maintEventWithDataPoint.alarmLevel);
        });
    });
    
    it('Put a data points based maintenance event', () => {
        global.maintEventWithDataPoints.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${global.maintEventWithDataPoints.xid}`,
            method: 'PUT',
            data: global.maintEventWithDataPoints
        }).then(response => {
            assert.equal(response.data.xid, global.maintEventWithDataPoints.xid);
            assert.equal(response.data.name, global.maintEventWithDataPoints.name);
            assert.equal(response.data.dataPoints.length, global.maintEventWithDataPoints.dataPoints.length);
            var found = 0;
            for(var i=0; i<global.maintEventWithDataPoints.dataPoints.length; i++){
                for(var j=0; j<response.data.dataPoints.length; j++){
                    if(global.maintEventWithDataPoints.dataPoints[i] === response.data.dataPoints[j]){
                        found++;
                        break;
                    }
                }
            }
            assert.isTrue(found === global.maintEventWithDataPoints.dataPoints.length);
            assert.isTrue(typeof response.data.dataSources === 'undefined');
            assert.equal(response.data.alarmLevel, global.maintEventWithDataPoints.alarmLevel);
            global.maintEventWithDataPoints = response.data;
          
        });
    });
    
    it('Toggle a data points based maintenance event', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/toggle/${global.maintEventWithDataPoints.xid}`,
            method: 'PUT'
        }).then(response => {
            assert.equal(response.data, true);
        });
    });
    
    it('Query by xid', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events?xid=${global.maintEventWithDataPoint.xid}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].xid, global.maintEventWithDataPoint.xid);
            assert.equal(response.data.items[0].name, global.maintEventWithDataPoint.name);
            assert.equal(response.data.items[0].dataPoints.length, global.maintEventWithDataPoint.dataPoints.length);
            assert.equal(response.data.items[0].dataPoints[0], global.maintEventWithDataPoint.dataPoints[0]);
            assert.isTrue(typeof response.data.items[0].dataSources === 'undefined');
            assert.equal(response.data.items[0].alarmLevel, global.maintEventWithDataPoint.alarmLevel);
        });
    });
    //TODO By data point xid
    
    it('Deletes data point me', () => {
      return client.restRequest({
          path: `/rest/v2/maintenance-events/${global.maintEventWithDataPoint.xid}`,
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.id, global.maintEventWithDataPoint.id);
      });
    });
    it('Deletes data points me', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${global.maintEventWithDataPoints.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, global.maintEventWithDataPoints.id);
        });
    });
    it('Deletes data source me', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${global.maintEventWithDataSource.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, global.maintEventWithDataSource.id);
        });
    });
    it('Deletes data sources me', () => {
        return client.restRequest({
            path: `/rest/v2/maintenance-events/${global.maintEventWithDataSources.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, global.maintEventWithDataSources.id);
        });
    });
});