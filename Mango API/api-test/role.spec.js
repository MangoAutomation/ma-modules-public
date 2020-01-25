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

const {createClient, assertValidationErrors, defer, delay, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const client = createClient();
const Role = client.Role;
const User = client.User;

describe('Role endpoint tests', function() {
    before('Login', function() { return login.call(this, client); });
    
    it('Create a new role', () => {
        const role = new Role();
        const local = Object.assign({}, role);
        return role.save().then(saved => {
            assert.strictEqual(saved, role);
            assert.isNumber(saved.id);
            assert.strictEqual(saved.xid, local.xid);
            assert.strictEqual(saved.name, local.name);
        }).finally(() => role.delete());
    });
    
    it('Cannot create a role with a space', () => {
        const role = new Role();
        role.xid = 'xid with spaces';
        return role.save().then(savedRole => {
            assert.fail('Should not have created role ' + savedRole.xid);
        }, error => {
            assertValidationErrors(['xid'], error);
        });
    });
    
    it('Cannot change a role xid', () => {
        const role = new Role();
        return role.save().then(saved => {
            saved.xid = saved.xid + 'updated';
            return saved.save().then(updated => {
                assert.fail('Should not have created role ' + updated.xid);
            }, error => {
                assertValidationErrors(['xid'], error);
            });
        }).finally(() => role.delete());
    });
    
    it('Update a role', () => {
        const role = new Role();
        return role.save().then(saved => {
            saved.name = saved.name + 'updated';
            const local = Object.assign({}, saved);
            return saved.save().then(updated => {
                assert.strictEqual(updated.id, local.id);
                assert.strictEqual(updated.xid, local.xid);
                assert.strictEqual(updated.name, local.name);
            });
        }).finally(() => role.delete());
    });
    
    
    it('Get a role', () => {
        const role = new Role();
        const local = Object.assign({}, role);
        return role.save().then(saved => {
            return Role.get(saved.xid).then(gotten => {
                assert.strictEqual(gotten.id, saved.id);
                assert.strictEqual(gotten.xid, local.xid);
                assert.strictEqual(gotten.name, local.name);
            });
        }).finally(() => role.delete());
    });
    
    it('Patch a role', () => {
        const role = new Role();
        return role.save().then(saved => {
            const local = Object.assign({}, saved);
            return saved.patch({name: local.name + 'updated'}).then(updated => {
                assert.strictEqual(updated.id, local.id);
                assert.strictEqual(updated.xid, local.xid);
                assert.strictEqual(updated.name, local.name + 'updated');
            });
        }).finally(() => role.delete());
    });
    
    it('Delete a role', () => {
        const role = new Role();
        return role.save().then(saved => {
            return saved.delete().then(deleted => {
                return Role.get(deleted.xid).then(gotten => {
                    assert.fail('Should not have found role ' + gotten.xid);
                }, error => {
                   assert.strictEqual(error.status, 404); 
                });
            });
        });
    });
    
    it('Cannot create role as non-admin', () => {
        const user = new User();
        const local = Object.assign({}, user);
        return user.save().then(() => {
            //Create new client, login and then create a role
            const userClient = createClient();
            return userClient.User.login(local.username, local.password).then(() => {
                const newRole = new userClient.Role();
                return newRole.save().then(saved => {
                   assert.fail('should not have saved role ' + saved.xid); 
                }, error => {
                    assert.strictEqual(error.status, 403);
                });
            });
        });
    });
    
    it('Cannot update user role', () => {
        return Role.get('user').then(userRole => {
            userRole.name = 'new name';
            return userRole.save().then(saved => {
                assert.fail('Should not have changed role ' + saved.xid);
            }, error =>{
                assertValidationErrors(['xid'], error);
            });
        });
    });
    
    it('Cannot update superadmin role', () => {
        return Role.get('superadmin').then(userRole => {
            userRole.name = 'new name';
            return userRole.save().then(saved => {
                assert.fail('Should not have changed role ' + saved.xid);
            }, error =>{
                assertValidationErrors(['xid'], error);
            });
        });
    });
    
    it('Cannot delete user role', () => {
        return Role.get('user').then(userRole => {
            return userRole.delete().then(saved => {
                assert.fail('Should not have deleted role ' + saved.xid);
            }, error =>{
                assert.strictEqual(error.status, 403)
            });
        });
    });
    
    it('Cannot delete superadmin role', () => {
        return Role.get('superadmin').then(userRole => {
            return userRole.delete().then(saved => {
                assert.fail('Should not have deleted role ' + saved.xid);
            }, error =>{
                assert.strictEqual(error.status, 403)
            });
        });
    });
    
    it('Can query for user role via xid', () => {
        return Role.query('xid=user').then(result => {
            assert.strictEqual(result.total, 1);
            assert.strictEqual(result[0].xid, 'user');
        });
    });
    
    it('Can query for role via name', () => {
        const role = new Role();
        return role.save().then(saved => {
            return Role.query(`xid=${saved.xid}`).then(result => {
                assert.strictEqual(result.total, 1);
                assert.strictEqual(result[0].xid, saved.xid);
            });
        });
    });
    
    it('Gets websocket notifications for role create', function() {

        let ws;
        const subscription = {
            notificationTypes: ['create'],
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION'
        };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        const role = new Role();
        
        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/roles'
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
                    if(msg.messageType === 'NOTIFICATION') {
                        assert.strictEqual(msg.notificationType, 'create');
                        assert.strictEqual(msg.payload.name, role.name);
                        assert.strictEqual(msg.payload.xid, role.xid);
                        listUpdatedDeferred.resolve();                           
                    }
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
            
        }).then(() => {
            return role.save().then(saved => {
                assert.isNumber(saved.id);
                assert.strictEqual(role.xid, saved.xid);
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        }).finally(() => {
            role.delete();
        });
    });

    it('Gets websocket notifications for role update', function() {

        let ws;
        const subscription = {
                notificationTypes: ['update'],
                messageType: 'REQUEST',
                requestType: 'SUBSCRIPTION'
            };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();
        
        const role = new Role();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/roles'
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
                    if(msg.messageType === 'NOTIFICATION') {
                        assert.strictEqual(msg.notificationType, 'update');
                        assert.strictEqual(msg.payload.name, role.name);
                        assert.strictEqual(msg.payload.xid, role.xid);
                        listUpdatedDeferred.resolve();                           
                        listUpdatedDeferred.resolve(); 
                    }
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
            return role.save().then(saved => {
                assert.isNumber(saved.id);
                role.id = saved.id;
                role.name = 'new name';
                assert.strictEqual(role.xid, saved.xid);
                return role.save().then(updated =>{
                    assert.strictEqual(role.xid, updated.xid);
                    assert.strictEqual(role.name, updated.name);
                });
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        }).finally(() => {
            role.delete();
        });
    });
    
    it('Gets websocket notifications for role delete', function() {

        let ws;
        const subscription = {
                notificationTypes: ['delete'],
                messageType: 'REQUEST',
                requestType: 'SUBSCRIPTION'
            };
        
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        const role = new Role();
        
        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/roles'
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
                    if(msg.messageType === 'NOTIFICATION') {
                        assert.strictEqual(msg.notificationType, 'delete');
                        assert.strictEqual(msg.payload.name, role.name);
                        assert.strictEqual(msg.payload.xid, role.xid);
                        listUpdatedDeferred.resolve();                           
                        listUpdatedDeferred.resolve(); 
                    }
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
            return role.save().then(saved => {
                assert.isNumber(saved.id);
                role.id = saved.id;
                role.name = 'new name';
                assert.strictEqual(role.xid, saved.xid);
                return role.delete();
            });
        }).then(() => listUpdatedDeferred.promise).then((r)=>{
            ws.close();
            return r;
        },e => {
            ws.close();
            return Promise.reject(e);
        })
    });
});