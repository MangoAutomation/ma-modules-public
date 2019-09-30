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

const {createClient, login, defer, delay} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();

describe('Mailing lists', function() {
    before('Login', function() { return login.call(this, client); });
    
    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};

    it('Creates a mailing list of type address', () => {
      testContext.addressMailingList = {
        xid: 'ML_TEST_ADDRESS',
        name: 'Test address mailing list',
        recipients: [{
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
          data: testContext.addressMailingList
      }).then(response => {
          testContext.addressMailingList.id = response.data.id;
          assert.equal(response.data.xid, testContext.addressMailingList.xid);
          assert.equal(response.data.name, testContext.addressMailingList.name);
          
          assert.equal(response.data.recipients.length, testContext.addressMailingList.recipients.length);
          assert.strictEqual(response.data.recipients[0].recipientType, testContext.addressMailingList.recipients[0].recipientType);
          assert.strictEqual(response.data.recipients[0].username, testContext.addressMailingList.recipients[0].username);
          
          assert.equal(response.data.receiveAlarmEmails, testContext.addressMailingList.receiveAlarmEmails);
          
          assert.lengthOf(response.data.readPermissions, testContext.addressMailingList.readPermissions.length);
          for(let i=0; i<response.data.readPermissions.length; i++)
              assert.include(testContext.addressMailingList.readPermissions, response.data.readPermissions[i]);
          
          assert.lengthOf(response.data.editPermissions, testContext.addressMailingList.editPermissions.length);
          for(let i=0; i<response.data.editPermissions.length; i++)
              assert.include(testContext.addressMailingList.editPermissions, response.data.editPermissions[i]);
          
          assert.equal(response.data.inactiveSchedule.length, testContext.addressMailingList.inactiveSchedule.length);
          for(let i=0; i<response.data.inactiveSchedule.length; i++){
              let responseSched = response.data.inactiveSchedule[i];
              let testContextSched = testContext.addressMailingList.inactiveSchedule[i];
              assert.lengthOf(responseSched, testContextSched.length);
              for(let j=0; j<responseSched.length; j++)
                  assert.equal(responseSched[j], testContextSched[j]);
          }
          
          testContext.addressMailingList = response.data;
      });
    });
    
    it('Can\'t create a mailing list without entries', () => {
        return client.restRequest({
            path: '/rest/v2/mailing-lists',
            method: 'POST',
            data: {
                xid: 'ML_TEST_ADDRESS',
                name: 'Test address mailing list',
                entries: null,
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
              }
        }).then(response => {
            assert.fail();
        }, error => {
            assert.strictEqual(error.response.statusCode, 422);
        });
      });

    it('Updates a mailing list of type address', () => {
        testContext.addressMailingList = {
          xid: 'ML_TEST_ADDRESS',
          name: 'Test address mailing list updated',
          recipients: [{
            recipientType: 'USER',
            username: 'admin'
          },{
              recipientType: 'ADDRESS',
              address: 'test@test.com'
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
            path: '/rest/v2/mailing-lists/ML_TEST_ADDRESS',
            method: 'PUT',
            data: testContext.addressMailingList
        }).then(response => {
            testContext.addressMailingList.id = response.data.id;
            assert.equal(response.data.xid, testContext.addressMailingList.xid);
            assert.equal(response.data.name, testContext.addressMailingList.name);
            
            assert.equal(response.data.recipients.length, testContext.addressMailingList.recipients.length);
            assert.strictEqual(response.data.recipients[0].recipientType, testContext.addressMailingList.recipients[0].recipientType);
            assert.strictEqual(response.data.recipients[0].username, testContext.addressMailingList.recipients[0].username);
            
            assert.equal(response.data.receiveAlarmEmails, testContext.addressMailingList.receiveAlarmEmails);
            
            assert.lengthOf(response.data.readPermissions, testContext.addressMailingList.readPermissions.length);
            for(let i=0; i<response.data.readPermissions.length; i++)
                assert.include(testContext.addressMailingList.readPermissions, response.data.readPermissions[i]);
            
            assert.lengthOf(response.data.editPermissions, testContext.addressMailingList.editPermissions.length);
            for(let i=0; i<response.data.editPermissions.length; i++)
                assert.include(testContext.addressMailingList.editPermissions, response.data.editPermissions[i]);
            
            assert.equal(response.data.inactiveSchedule.length, testContext.addressMailingList.inactiveSchedule.length);
            for(let i=0; i<response.data.inactiveSchedule.length; i++){
                let responseSched = response.data.inactiveSchedule[i];
                let testContextSched = testContext.addressMailingList.inactiveSchedule[i];
                assert.lengthOf(responseSched, testContextSched.length);
                for(let j=0; j<responseSched.length; j++)
                    assert.equal(responseSched[j], testContextSched[j]);
            }
        });
      });

    it('Patch a mailing list of type address', () => {
        testContext.addressMailingList.readPermission = ['user', 'admin'];

        return client.restRequest({
            path: '/rest/v2/mailing-lists/ML_TEST_ADDRESS',
            method: 'PATCH',
            data: {
                readPermission: ['user', 'admin']
            }
        }).then(response => {
            testContext.addressMailingList.id = response.data.id;
            assert.equal(response.data.xid, testContext.addressMailingList.xid);
            assert.equal(response.data.name, testContext.addressMailingList.name);
            
            assert.equal(response.data.recipients.length, testContext.addressMailingList.recipients.length);
            assert.strictEqual(response.data.recipients[0].recipientType, testContext.addressMailingList.recipients[0].recipientType);
            assert.strictEqual(response.data.recipients[0].username, testContext.addressMailingList.recipients[0].username);
            
            assert.equal(response.data.receiveAlarmEmails, testContext.addressMailingList.receiveAlarmEmails);
            
            assert.lengthOf(response.data.readPermissions, testContext.addressMailingList.readPermissions.length);
            for(let i=0; i<response.data.readPermissions.length; i++)
                assert.include(testContext.addressMailingList.readPermissions, response.data.readPermissions[i]);
            
            assert.lengthOf(response.data.editPermissions, testContext.addressMailingList.editPermissions.length);
            for(let i=0; i<response.data.editPermissions.length; i++)
                assert.include(testContext.addressMailingList.editPermissions, response.data.editPermissions[i]);
            
            assert.equal(response.data.inactiveSchedule.length, testContext.addressMailingList.inactiveSchedule.length);
            for(let i=0; i<response.data.inactiveSchedule.length; i++){
                let responseSched = response.data.inactiveSchedule[i];
                let testContextSched = testContext.addressMailingList.inactiveSchedule[i];
                assert.lengthOf(responseSched, testContextSched.length);
                for(let j=0; j<responseSched.length; j++)
                    assert.equal(responseSched[j], testContextSched[j]);
            }
            
            testContext.addressMailingList = response.data;
        });
      });

    it('Query mailing lists', () => {
        return client.restRequest({
            path: '/rest/v2/mailing-lists?xid=ML_TEST_ADDRESS',
            method: 'GET',
            data: testContext.addressMailingList
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].xid, testContext.addressMailingList.xid);
            assert.equal(response.data.items[0].name, testContext.addressMailingList.name);
            
            assert.equal(response.data.items[0].recipients.length, testContext.addressMailingList.recipients.length);
            assert.strictEqual(response.data.items[0].recipients[0].recipientType, testContext.addressMailingList.recipients[0].recipientType);
            assert.strictEqual(response.data.items[0].recipients[0].username, testContext.addressMailingList.recipients[0].username);
            
            assert.equal(response.data.items[0].receiveAlarmEmails, testContext.addressMailingList.receiveAlarmEmails);
            
            assert.lengthOf(response.data.items[0].readPermissions, testContext.addressMailingList.readPermissions.length);
            for(let i=0; i<response.data.items[0].readPermissions.length; i++)
                assert.include(testContext.addressMailingList.readPermissions, response.data.items[0].readPermissions[i]);
            
            assert.lengthOf(response.data.items[0].editPermissions, testContext.addressMailingList.editPermissions.length);
            for(let i=0; i<response.data.items[0].editPermissions.length; i++)
                assert.include(testContext.addressMailingList.editPermissions, response.data.items[0].editPermissions[i]);
            
            assert.equal(response.data.items[0].inactiveSchedule.length, testContext.addressMailingList.inactiveSchedule.length);
            for(let i=0; i<response.data.items[0].inactiveSchedule.length; i++){
                let responseSched = response.data.items[0].inactiveSchedule[i];
                let testContextSched = testContext.addressMailingList.inactiveSchedule[i];
                assert.lengthOf(responseSched, testContextSched.length);
                for(let j=0; j<responseSched.length; j++)
                    assert.equal(responseSched[j], testContextSched[j]);
            }
        });
      });
    
    it('Gets websocket notifications for update', function() {
        
        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/mailing-lists'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                try{
                    assert.isString(msgStr);
                    const msg = JSON.parse(msgStr);
                    assert.strictEqual(msg.status, 'OK');
                    assert.strictEqual(msg.payload.action, 'update');
                    assert.strictEqual(msg.payload.object.xid, testContext.addressMailingList.xid);
                    listUpdatedDeferred.resolve();   
                }catch(e){
                    listUpdatedDeferred.reject(e);
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
            
        }).then(() => delay(1000)).then(() => {
            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start
            return client.restRequest({
                path: '/rest/v2/mailing-lists/ML_TEST_ADDRESS',
                method: 'PUT',
                data: testContext.addressMailingList
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        });
    });
 
    it('Gets websocket notifications for delete', function() {
        this.timeout(5000);
        
        let ws;
        const subscription = {
            eventTypes: ['add', 'delete', 'update']
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/mailing-lists'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                try{
                    assert.isString(msgStr);
                    const msg = JSON.parse(msgStr);
                    assert.strictEqual(msg.status, 'OK');
                    assert.strictEqual(msg.payload.action, 'delete');
                    assert.strictEqual(msg.payload.object.xid, testContext.addressMailingList.xid);
                    listUpdatedDeferred.resolve();   
                }catch(e){
                    listUpdatedDeferred.reject(e);
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
            
        }).then(() => delay(1000)).then(() => {
            return client.restRequest({
                path: `/rest/v2/mailing-lists/${testContext.addressMailingList.xid}`,
                method: 'DELETE',
                data: {}
            }).then(response => {
                assert.equal(response.data.id, testContext.addressMailingList.id);
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        });
    });
    
    
});
