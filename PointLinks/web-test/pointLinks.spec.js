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

const config = require('./setup');

describe('Point links', function() {
    before('Login', config.login);

    before('Create DS 1', function() {
        this.point = (name) => {
            return new DataPoint({
                enabled: true,
                name: name,
                deviceName: 'Data point test deviceName',
                dataSourceXid : global.ds1.xid,
                pointLocator : {
                    settable: true,
                    startValue : '0',
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    changeType : 'NO_CHANGE',
                }
            });
        };

        global.ds1 = new DataSource({
            xid: 'pl_test_1',
            name: 'PL Testing 1',
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
            xid: 'pl_test_2',
            name: 'PL Testing 2',
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


    it('Creates a point link', () => {
      global.pointLinkOne = {
        xid: 'PL_TEST_ONE',
        name: 'Test point link one',
        sourcePointXid: global.dp1.xid,
        targetPointXid: global.dp2.xid,
        script: 'function test(){return 1;}',
        event: 'UPDATE',
        writeAnnotation: true,
        disabled: false,
        logLevel: 'NONE',
        scriptPermissions: 'admin',
        logSize: 2.1,
        logCount: 1
      };

      return client.restRequest({
          path: '/rest/v2/point-links',
          method: 'POST',
          data: global.pointLinkOne
      }).then(response => {
          assert.equal(response.data.xid, global.pointLinkOne.xid);
          assert.equal(response.data.name, global.pointLinkOne.name);
          assert.equal(response.data.sourcePointXid, global.pointLinkOne.sourcePointXid);
          assert.equal(response.data.targetPointXid, global.pointLinkOne.targetPointXid);
          assert.equal(response.data.script, global.pointLinkOne.script);
          assert.equal(response.data.event, global.pointLinkOne.event);
          assert.equal(response.data.writeAnnotation, global.pointLinkOne.writeAnnotation);
          assert.equal(response.data.disabled, global.pointLinkOne.disabled);
          assert.equal(response.data.logLevel, global.pointLinkOne.logLevel);
          assert.equal(response.data.scriptPermissions, global.pointLinkOne.scriptPermissions);
          assert.equal(response.data.logSize, global.pointLinkOne.logSize);
          assert.equal(response.data.logCount, global.pointLinkOne.logCount);
          global.pointLinkOne = response.data;
      });
    });

    it('Creates a second point link', () => {
      global.pointLinkTwo = {
        xid: 'PL_TEST_TWO',
        name: 'Test point link one',
        sourcePointXid: global.dp2.xid,
        targetPointXid: global.dp1.xid,
        script: 'function test(){return 1;}',
        event: 'UPDATE',
        writeAnnotation: false,
        disabled: false,
        logLevel: 'ERROR',
        scriptPermissions: 'admin,user',
        logSize: 3.0,
        logCount: 10

      };

      return client.restRequest({
          path: '/rest/v2/point-links',
          method: 'POST',
          data: global.pointLinkTwo
      }).then(response => {
        assert.equal(response.data.xid, global.pointLinkTwo.xid);
        assert.equal(response.data.name, global.pointLinkTwo.name);
        assert.equal(response.data.sourcePointXid, global.pointLinkTwo.sourcePointXid);
        assert.equal(response.data.targetPointXid, global.pointLinkTwo.targetPointXid);
        assert.equal(response.data.script, global.pointLinkTwo.script);
        assert.equal(response.data.event, global.pointLinkTwo.event);
        assert.equal(response.data.writeAnnotation, global.pointLinkTwo.writeAnnotation);
        assert.equal(response.data.disabled, global.pointLinkTwo.disabled);
        assert.equal(response.data.logLevel, global.pointLinkTwo.logLevel);
        assert.equal(response.data.scriptPermissions, global.pointLinkTwo.scriptPermissions);
        assert.equal(response.data.logSize, global.pointLinkTwo.logSize);
        assert.equal(response.data.logCount, global.pointLinkTwo.logCount);
        global.pointLinkTwo = response.data;
      });
    });

    it('Update point link one', () => {
        global.pointLinkOne.name = 'updated name';
        return client.restRequest({
            path: `/rest/v2/point-links/${global.pointLinkOne.xid}`,
            method: 'PUT',
            data: global.pointLinkOne
        }).then(response => {
          assert.equal(response.data.xid, global.pointLinkOne.xid);
          assert.equal(response.data.name, global.pointLinkOne.name);
          assert.equal(response.data.sourcePointXid, global.pointLinkOne.sourcePointXid);
          assert.equal(response.data.targetPointXid, global.pointLinkOne.targetPointXid);
          assert.equal(response.data.script, global.pointLinkOne.script);
          assert.equal(response.data.event, global.pointLinkOne.event);
          assert.equal(response.data.writeAnnotation, global.pointLinkOne.writeAnnotation);
          assert.equal(response.data.disabled, global.pointLinkOne.disabled);
          assert.equal(response.data.logLevel, global.pointLinkOne.logLevel);
          assert.equal(response.data.scriptPermissions, global.pointLinkOne.scriptPermissions);
          assert.equal(response.data.logSize, global.pointLinkOne.logSize);
          assert.equal(response.data.logCount, global.pointLinkOne.logCount);
          global.pointLinkOne = response.data;
        });
    });

    it('Get point link one', () => {
        return client.restRequest({
            path: `/rest/v2/point-links/${global.pointLinkOne.xid}`,
            method: 'GET'
        }).then(response => {
          assert.equal(response.data.xid, global.pointLinkOne.xid);
          assert.equal(response.data.name, global.pointLinkOne.name);
          assert.equal(response.data.sourcePointXid, global.pointLinkOne.sourcePointXid);
          assert.equal(response.data.targetPointXid, global.pointLinkOne.targetPointXid);
          assert.equal(response.data.script, global.pointLinkOne.script);
          assert.equal(response.data.event, global.pointLinkOne.event);
          assert.equal(response.data.writeAnnotation, global.pointLinkOne.writeAnnotation);
          assert.equal(response.data.disabled, global.pointLinkOne.disabled);
          assert.equal(response.data.logLevel, global.pointLinkOne.logLevel);
          assert.equal(response.data.scriptPermissions, global.pointLinkOne.scriptPermissions);
          assert.equal(response.data.logSize, global.pointLinkOne.logSize);
          assert.equal(response.data.logCount, global.pointLinkOne.logCount);
          global.pointLinkOne = response.data;
        });
    });

    it('Query by xid', () => {
        return client.restRequest({
            path: `/rest/v2/point-links?xid=${global.pointLinkOne.xid}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].xid, global.pointLinkOne.xid);
            assert.equal(response.data.items[0].name, global.pointLinkOne.name);
            assert.equal(response.data.items[0].sourcePointXid, global.pointLinkOne.sourcePointXid);
            assert.equal(response.data.items[0].targetPointXid, global.pointLinkOne.targetPointXid);
            assert.equal(response.data.items[0].script, global.pointLinkOne.script);
            assert.equal(response.data.items[0].event, global.pointLinkOne.event);
            assert.equal(response.data.items[0].writeAnnotation, global.pointLinkOne.writeAnnotation);
            assert.equal(response.data.items[0].disabled, global.pointLinkOne.disabled);
            assert.equal(response.data.items[0].logLevel, global.pointLinkOne.logLevel);
            assert.equal(response.data.items[0].scriptPermissions, global.pointLinkOne.scriptPermissions);
            assert.equal(response.data.items[0].logSize, global.pointLinkOne.logSize);
            assert.equal(response.data.items[0].logCount, global.pointLinkOne.logCount);
        });
    });
    //TODO Query by script contents or other fields

    it('Query by event', () => {
        return client.restRequest({
            path: `/rest/v2/point-links?event=${global.pointLinkOne.event}`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].xid, global.pointLinkOne.xid);
            assert.equal(response.data.items[0].name, global.pointLinkOne.name);
            assert.equal(response.data.items[0].sourcePointXid, global.pointLinkOne.sourcePointXid);
            assert.equal(response.data.items[0].targetPointXid, global.pointLinkOne.targetPointXid);
            assert.equal(response.data.items[0].script, global.pointLinkOne.script);
            assert.equal(response.data.items[0].event, global.pointLinkOne.event);
            assert.equal(response.data.items[0].writeAnnotation, global.pointLinkOne.writeAnnotation);
            assert.equal(response.data.items[0].disabled, global.pointLinkOne.disabled);
            assert.equal(response.data.items[0].logLevel, global.pointLinkOne.logLevel);
            assert.equal(response.data.items[0].scriptPermissions, global.pointLinkOne.scriptPermissions);
            assert.equal(response.data.items[0].logSize, global.pointLinkOne.logSize);
            assert.equal(response.data.items[0].logCount, global.pointLinkOne.logCount);
        });
    });
    //TODO Query on logLevel

    it('Deletes point link one', () => {
      return client.restRequest({
          path: `/rest/v2/point-links/${global.pointLinkOne.xid}`,
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.id, global.pointLinkOne.id);
      });
    });
    it('Deletes point link two', () => {
        return client.restRequest({
            path: `/rest/v2/point-links/${global.pointLinkTwo.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, global.pointLinkTwo.id);
        });
    });
    //TODO Get them to ensure they are 404
});
