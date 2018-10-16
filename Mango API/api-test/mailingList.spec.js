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

describe('Mailing lists', function() {
    before('Login', config.login);

    it('Creates a mailing list of type address', () => {
      global.addressMailingList = {
        xid: 'ML_TEST_ADDRESS',
        name: 'Test address mailing list',
        entries: [{
          recipientType: 'USER',
          username: 'admin'
        }],
        receiveAlarmEmails: 'URGENT',
        readPermissions: ['user'],
        editPermissions: ['superadmin'],
        inactiveSchedule: [
          ['08:00','10:00','13:00'],
          ['09:00','12:00'],
          [],
          [],
          ['07:00', '14:00', '15:00'],
          [],
          ['08:00', '17:00']
        ]
      };

      return client.restRequest({
          path: '/rest/v2/mailing-lists',
          method: 'POST',
          data: global.addressMailingList
      }).then(response => {
          global.addressMailingList.id = response.data.id;
          console.log(response.data);
          assert.equal(response.data.xid, global.addressMailingList.xid);
          assert.equal(response.data.name, global.addressMailingList.name);
          assert.equal(response.data.receiveAlarmEmails, global.addressMailingList.receiveAlarmEmails);
          
          assert.lengthOf(response.data.readPermissions, global.addressMailingList.readPermissions.length);
          for(var i=0; i<response.data.readPermissions.length; i++)
              assert.include(global.addressMailingList.readPermissions, response.data.readPermissions[i]);
          
          assert.lengthOf(response.data.editPermissions, global.addressMailingList.editPermissions.length);
          for(var i=0; i<response.data.editPermissions.length; i++)
              assert.include(global.addressMailingList.editPermissions, response.data.editPermissions[i]);
          
          assert.equal(response.data.inactiveSchedule.length, global.addressMailingList.inactiveSchedule.length)
          for(var i=0; i<response.data.inactiveSchedule.length; i++){
              var responseSched = response.data.inactiveSchedule[i];
              var globalSched = global.addressMailingList.inactiveSchedule[i];
              assert.lengthOf(responseSched, globalSched.length);
              for(var j=0; j<responseSched.length; j++)
                  assert.equal(responseSched[j], globalSched[j]);
          }
          
          global.addressMailingList = response.data;
      });
    });

    //TODO Update valid
    //TODO Update invalid

    it('Deletes a mailing list of type address', () => {
        return client.restRequest({
            path: `/rest/v2/mailing-lists/${global.addressMailingList.xid}`,
            method: 'DELETE',
            data: {}
        }).then(response => {
            assert.equal(response.data.id, global.addressMailingList.id);
        });
    });
    //TODO Get them to ensure they are 404
});
