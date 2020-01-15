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

const {createClient, assertValidationErrors, login} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const Role = client.Role;
const User = client.User;

describe('Role endpoint tests', function() {
    before('Login', function() { return login.call(this, client); });
    
    it('Create a new role', () => {
        const role = new Role(client);
        const local = Object.assign({}, role);
        return role.save().then(saved => {
            assert.strictEqual(saved, role);
            assert.isNumber(saved.id);
            assert.strictEqual(saved.xid, local.xid);
            assert.strictEqual(saved.name, local.name);
        }).finally(() => role.delete());
    });
    
    it('Cannot create a role with a space', () => {
        const role = new Role(client);
        role.xid = 'xid with spaces';
        return role.save().then(savedRole => {
            assert.fail('Should not have created role ' + savedRole.xid);
        }, error => {
            assertValidationErrors(['xid'], error);
        });
    });
    
    it('Cannot change a role xid', () => {
        const role = new Role(client);
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
        const role = new Role(client);
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
        const role = new Role(client);
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
        const role = new Role(client);
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
        const role = new Role(client);
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
        const role = new Role(client);
        return role.save().then(saved => {
            return Role.query(`xid=${saved.xid}`).then(result => {
                assert.strictEqual(result.total, 1);
                assert.strictEqual(result[0].xid, saved.xid);
            });
        });
    });
});