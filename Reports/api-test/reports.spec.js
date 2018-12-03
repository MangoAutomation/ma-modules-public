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

const config = require('@infinite-automation/mango-client/test/setup');

describe('Test Report v2 Endpoints', function() {
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
    
    it('Creates a simple report', () => {
        global.report1 = {
          xid: 'REPORT_1',
          name: 'Test Report',
          username: 'admin',
          points: [
              {
                  pointXid: global.dp1.xid,
                  pointKey: "p76",
                  colour: "#000000",
                  weight: 1,
                  consolidatedChart: true,
                  plotType: "STEP"
              }
          ],
          template: "reportChart.ftl",
          includeEvents: "ALARMS",
          includeUserComments: true,
          dateRangeType: "RELATIVE",
          relativeDateType: "PREVIOUS",
          relativePeriod: {
              periods: 1,
              type: "DAYS"
          },
          schedule: true,
          schedulePeriod: "DAYS",
          scheduleCron: "",
          runDelayMinutes: 5,
          email: true,
          recipients: [
              {
                  address: "test@test.com",
                  type: "ADDRESS"
              }
          ],
          includeData: true,
          zipData: true,
        };

        return client.restRequest({
            path: '/rest/v2/reports',
            method: 'POST',
            data: global.report1
        }).then(response => {
            assert.equal(response.data.xid, global.report1.xid);
            assert.equal(response.data.name, global.report1.name);
            assert.equal(response.data.points.length, global.report1.points.length);
            assert.equal(response.data.points[0].pointXid, global.report1.points[0].pointXid);
            //TODO assert more
          
        });
      });
    
    it('Patch a report', () => {
        global.report1.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/reports/${global.report1.xid}`,
            method: 'PATCH',
            data: {
                name: 'updated name'
            }
        }).then(response => {
            assert.equal(response.data.xid, global.report1.xid);
            assert.equal(response.data.name, global.report1.name);
            assert.equal(response.data.points.length, global.report1.points.length);
            assert.equal(response.data.points[0].pointXid, global.report1.points[0].pointXid);
            //TODO assert more
          
        });
    });

    it('Get a report', () => {
        global.report1.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/reports/${global.report1.xid}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.xid, global.report1.xid);
            assert.equal(response.data.name, global.report1.name);
            assert.equal(response.data.points.length, global.report1.points.length);
            assert.equal(response.data.points[0].pointXid, global.report1.points[0].pointXid);
            //TODO assert more
            global.report1.id = response.data.id;
        });
    });
    
    it('Update a report', () => {
        global.report1.name = 'updated name again';
        return client.restRequest({
            path: `/rest/v2/reports/${global.report1.xid}`,
            method: 'PUT',
            data: global.report1
        }).then(response => {
            assert.equal(response.data.xid, global.report1.xid);
            assert.equal(response.data.name, global.report1.name);
            assert.equal(response.data.points.length, global.report1.points.length);
            assert.equal(response.data.points[0].pointXid, global.report1.points[0].pointXid);
            //TODO assert more
          
        });
    });
    
    it('Create Report event handler', () => {

      return client.restRequest({
          path: '/rest/v2/event-handlers',
          method: 'POST',
          data: {
              xid : "EVTH_REPORT_TEST",
              name : 'Testing reports',
              disabled : false,
              activeReportXid: global.report1.xid,
              handlerType : "REPORT"
            }
      }).then(response => {
        var savedHandler = response.data;
        assert.strictEqual(savedHandler.xid, 'EVTH_REPORT_TEST');
        assert.strictEqual(savedHandler.name, 'Testing reports');
        assert.strictEqual(savedHandler.activeReportXid, global.report1.xid);
        assert.strictEqual(savedHandler.inactiveReportXid, null);
        assert.isNumber(savedHandler.id);
      });
    });

    it('Delete Report event handler', () => {
      return client.restRequest({
          path: '/rest/v2/event-handlers/EVTH_REPORT_TEST',
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.xid, 'EVTH_REPORT_TEST');
          assert.equal(response.data.name, 'Testing reports');
          assert.equal(response.data.activeReportXid, global.report1.xid);
          assert.equal(response.data.inactiveReportId, null);
          assert.isNumber(response.data.id);
      });
    });
    
    it('Delete a report', () => {
        return client.restRequest({
            path: `/rest/v2/reports/${global.report1.xid}`,
            method: 'DELETE'
        }).then(response => {
            assert.equal(response.data.xid, global.report1.xid);
            assert.equal(response.data.name, global.report1.name);
            assert.equal(response.data.points.length, global.report1.points.length);
            assert.equal(response.data.points[0].pointXid, global.report1.points[0].pointXid);
            //TODO assert more
          
        });
    });
});
