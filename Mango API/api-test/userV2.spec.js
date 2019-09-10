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
const MangoClient = require('@infinite-automation/mango-client');
const uuidV4 = require('uuid/v4');

describe('User V2 endpoint tests', function() {
    before('Login', config.login);
    
    beforeEach('Create test User', function() {
        const username = uuidV4();
        this.testUserPassword = uuidV4();
        this.testUser = {
            name: 'name',
            username: username,
            password: this.testUserPassword,
            email: 'test@test.com',
            phone: '808-888-8888',
            disabled: false,
            locale: '',
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'IGNORE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: ['testuser', 'test'],
            sessionExpirationOverride: true,
            sessionExpirationPeriod: {
                periods: 1,
                type: 'SECONDS'
            },
            organization: 'Infinite Automation Systems',
            organizationalRole: 'test engineer',
            data: {
                stringField: 'some random string',
                numberField: 123,
                booleanField: true
            }
                
        };
        return client.restRequest({
            path: '/rest/v2/users',
            method: 'POST',
            data: this.testUser
        }).then(response => {
            assert.equal(response.data.username, this.testUser.username);
            assert.strictEqual(response.data.organization, this.testUser.organization);
            assert.strictEqual(response.data.organizationalRole, this.testUser.organizationalRole);
            assert.strictEqual(response.data.data.stringField, this.testUser.data.stringField);
            assert.strictEqual(response.data.data.numberField, this.testUser.data.numberField);
            assert.strictEqual(response.data.data.booleanField, this.testUser.data.booleanField);
            this.testUser.id = response.data.id;
        });
    });
    
    beforeEach('Create test admin User', function() {
        const username = uuidV4();
        this.testAdminUserPassword = uuidV4();
        this.testAdminUser = {
            name: 'admin name',
            username: username,
            password: this.testAdminUserPassword,
            email: 'admin@test.com',
            phone: '808-888-8888',
            disabled: false,
            locale: '',
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'IGNORE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: ['superadmin'],
            sessionExpirationOverride: true,
            sessionExpirationPeriod: {
                periods: 1,
                type: 'HOURS'
            },
            organization: 'Infinite Automation Systems'
        };
        return client.restRequest({
            path: '/rest/v2/users',
            method: 'POST',
            data: this.testAdminUser
        }).then(response => {
            assert.equal(response.data.username, this.testAdminUser.username);
            this.testAdminUser.id = response.data.id;
        });
    });
    
    afterEach('Deletes the test user', function() {
        return client.restRequest({
            path: `/rest/v2/users/${this.testUser.username}`,
            method: 'DELETE'
        }).then(response => {
            assert.equal(response.data.username, this.testUser.username);
        });
    });
    
    afterEach('Deletes the test admin user', function() {
        return client.restRequest({
            path: `/rest/v2/users/${this.testAdminUser.username}`,
            method: 'DELETE'
        }).then(response => {
            assert.equal(response.data.username, this.testAdminUser.username);
        });
    });
    
    before('Create a session reference that uses session authentication', function() {
        this.sessionTimeoutRef = new MangoClient(config);
    });
    
    it('Fails to create user without password', function() {
        return client.restRequest({
            path: '/rest/v2/users',
            method: 'POST',
            data: {
                name: 'name',
                username: this.testUser.username,
                email: 'test@test.com',
                phone: '808-888-8888',
                disabled: false,
                homeUrl: 'www.google.com',
                receiveAlarmEmails: 'NONE',
                receiveOwnAuditEvents: false,
                muted: false
            }
        }).then(response => {
            throw new Error('Should not have created user');
        }, error => {
           assert.strictEqual(error.status, 422); 
        });
    });
    
    it('Can lock other users password', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testAdminUser.username, this.testAdminUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testUser.username}/lock-password`,
                method: 'PUT',
            }).then(response => {
                assert.strictEqual(response.status, 200); 
            });            
        });
    });
    
    it('Can not lock own password', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testAdminUser.username, this.testAdminUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testAdminUser.username}/lock-password`,
                method: 'PUT',
            }).then(response => {
                throw new Error('Should not have locked password');
            }, error => {
               assert.strictEqual(error.status, 403); 
            });            
        });
    });
    
    it('Can not make self non admin', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testAdminUser.username, this.testAdminUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testAdminUser.username}`,
                method: 'PATCH',
                data: {
                    permissions: ['test']
                }
            }).then(response => {
                throw new Error('Should not have updated user');
            }, error => {
               assert.strictEqual(error.status, 422); 
            });            
        });
    });
    
    it('Can not disable self', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testAdminUser.username, this.testAdminUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testAdminUser.username}`,
                method: 'PATCH',
                data: {
                    disabled: true
                }
            }).then(response => {
                throw new Error('Should not have updated user');
            }, error => {
               assert.strictEqual(error.status, 422); 
            });            
        });
    });
    
    it('Can not update permissions', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testUser.username}`,
                method: 'PATCH',
                data: {
                    permissions: ['new,permissions']
                }
            }).then(response => {
                throw new Error('Should not have updated user');
            }, error => {
               assert.strictEqual(error.status, 422); 
            });            
        });
    });
    
    it('Can not rename to existing user', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testUser.username}`,
                method: 'PATCH',
                data: {
                    username: this.testAdminUser.username
                }
            }).then(response => {
                throw new Error('Should not have updated user');
            }, error => {
               assert.strictEqual(error.status, 422); 
            });            
        });
    });
    
    it('Can rename self as non admin', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return loginClient.restRequest({
                path: `/rest/v2/users/${this.testUser.username}`,
                method: 'PATCH',
                data: {
                    username: 'Iamnew'
                }
            }).then(response => {
                assert.strictEqual(response.data.username, 'Iamnew');
                this.testUser.username = 'Iamnew';
            });            
        });
    });
    
    it('Queries to match all user permissions', function() {
        return client.restRequest({
            path: `/rest/v2/users?permissionsContainsAll(permissions,testuser,test)`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].username, this.testUser.username);
        });
    });
    
    it('Queries to match one user permission', function() {
        return client.restRequest({
            path: `/rest/v2/users?permissionsContainsAny(permissions,testuser)`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].username, this.testUser.username);
        });
    });
    
    it('Queries for disabled users', function() {
        return client.restRequest({
            path: `/rest/v2/users/${this.testUser.username}`,
            method: 'PATCH',
            data: {
                disabled: true
            }
        }).then(response => {
            assert.equal(response.data.username, this.testUser.username);
            assert.equal(response.data.disabled, true);
            return client.restRequest({
                path: `/rest/v2/users?disabled=true`,
                method: 'GET'
            }).then(response => {
                assert.equal(response.data.total, 1);
                assert.equal(response.data.items[0].username, this.testUser.username);
            });
        });
    });
    
    it('Patch test user', function() {
        return client.restRequest({
            path: `/rest/v2/users/${this.testUser.username}`,
            method: 'PATCH',
            data: {
                name: 'test user'
            }
        }).then(response => {
            assert.equal(response.data.username, this.testUser.username);
            assert.equal(response.data.name, 'test user');
        });
    });
    
    it('User session timeout override expires session', function() {
        this.timeout(5000);
        const loginClient = new MangoClient(config);
        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return config.delay(2000);
        }).then(() => {
            return loginClient.User.current().then(response => {
                throw new Error('Session should be expired');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
    
});
