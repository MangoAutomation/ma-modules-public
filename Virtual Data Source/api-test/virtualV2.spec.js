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

describe('Virtual data source v2', function() {
    before('Login', config.login);

    const vrtDs = {
            xid: 'DS_VIRT_TEST',
            name: 'Virtual Test',
            enabled: false,
            eventAlarmLevels: [
                {
                    dataSourceXid: 'DS_VIRT_TEST',
                    eventType: 'POLL_ABORTED',
                    duplicateHandling: 'IGNORE',
                    level: 'INFORMATION',
                    description: 'Poll aborted'
                 }
            ],
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            editPermission: ['superadmin', 'test'],
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            polling: true,
            modelType: 'VIRTUAL'
    };
    
    it('Create virtual data source', () => {
      return client.restRequest({
          path: '/rest/v2/data-sources',
          method: 'POST',
          data: vrtDs
      }).then((response) => {
          assert.isNumber(response.data.id);
          assert.strictEqual(response.data.xid, vrtDs.xid);
          assert.strictEqual(response.data.name, vrtDs.name);
          assert.strictEqual(response.data.enabled, vrtDs.enabled);
          assert.strictEqual(response.data.polling, vrtDs.polling);
          assert.strictEqual(response.data.pollPeriod.periods, vrtDs.pollPeriod.periods);
          assert.strictEqual(response.data.pollPeriod.type, vrtDs.pollPeriod.type);
          assertPermissions(response.data.editPermission, vrtDs.editPermission);
          assertAlarmLevels(response.data.eventAlarmLevels, vrtDs.eventAlarmLevels);
      });
    });

    it('Update virtual data source', () => {
        vrtDs.name='Test again';
        return client.restRequest({
            path:  `/rest/v2/data-sources/${vrtDs.xid}`,
            method: 'PUT',
            data: vrtDs
        }).then((response) => {
            assert.isNumber(response.data.id);
            assert.strictEqual(response.data.xid, vrtDs.xid);
            assert.strictEqual(response.data.name, vrtDs.name);
            assert.strictEqual(response.data.enabled, vrtDs.enabled);
            assert.strictEqual(response.data.polling, vrtDs.polling);
            assert.strictEqual(response.data.pollPeriod.periods, vrtDs.pollPeriod.periods);
            assert.strictEqual(response.data.pollPeriod.type, vrtDs.pollPeriod.type);
            assertPermissions(response.data.editPermission, vrtDs.editPermission);
            assertAlarmLevels(response.data.eventAlarmLevels, vrtDs.eventAlarmLevels);
        });
      });
    
    it('Delete virtual data source', () => {
        return client.restRequest({
            path: `/rest/v2/data-sources/${vrtDs.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.strictEqual(response.data.xid, vrtDs.xid);
            assert.strictEqual(response.data.name, vrtDs.name);
            assert.strictEqual(response.data.enabled, vrtDs.enabled);
            assert.strictEqual(response.data.polling, vrtDs.polling);
            assert.strictEqual(response.data.pollPeriod.periods, vrtDs.pollPeriod.periods);
            assert.strictEqual(response.data.pollPeriod.type, vrtDs.pollPeriod.type);
            assertPermissions(response.data.editPermission, vrtDs.editPermission);
            assertAlarmLevels(response.data.eventAlarmLevels, vrtDs.eventAlarmLevels);
        });
    });
    
    function assertPermissions(saved, stored) {
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            assert.include(saved, stored[i], stored[i] + ' was not found in permissions')
        }
    }
    function assertAlarmLevels(saved, stored){
        var assertedEventTypes = [];
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            var found = false;
            for(var j=0; j<saved.length; j++){
                if(stored[i].eventType === saved[j].eventType){
                    found = true;
                    assert.strictEqual(saved.level, stored.level);
                    assertedEventTypes.push(saved[i].eventType)
                    break;
                }
            }
            if(found === false)
                assert.fail('Did not find event type: ' + stored[i].eventType);
        }
    }
});
