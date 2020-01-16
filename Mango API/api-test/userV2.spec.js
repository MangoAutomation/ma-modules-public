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

const {createClient, assertValidationErrors, login, uuid, delay} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const User = client.User;
const Role = client.Role;

describe('User endpoint tests', function() {
    this.timeout(10000);
    before('Login', function() { return login.call(this, client); });
    
    before('Helper functions', function() {
        //Setup login using a given configuration
        this.login = function(localConfig, LocalUser) {
            this.timeout(localConfig.loginRetries * localConfig.loginRetryDelay + 5000);
            return LocalUser.login(localConfig.username, localConfig.password, localConfig.loginRetries, localConfig.loginRetryDelay);
        };
        this.clients = {};
        this.configs = {};
    });
    
    beforeEach('Create test User', function() {
        const username = uuid();
        const testUserPassword = uuid();
        const testUserRole = new Role();
        this.testUserSettings = {
            name: 'name',
            username: username,
            password: testUserPassword,
            email: `${username}@example.com`,
            phone: '808-888-8888',
            disabled: false,
            locale: '',
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'IGNORE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: [testUserRole.xid],
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
        
        const testUser = new User(this.testUserSettings);
        return testUserRole.save().then(()=>{
            //Save user and setup a session user for it
            return testUser.save().then(user => {
                assert.equal(user.username, username);
                this.clients.user = createClient();
                return this.clients.user.User.login(user.username, testUserPassword).then(loggedIn => {
                    //Save a reference to the created user
                    this.clients.user.user = loggedIn;
                });
            });
        });
    });
    
    beforeEach('Create test admin User', function() {
        const username = uuid();
        const testAdminUserPassword = uuid();
        this.adminUserSettings = {
                name: 'admin name',
                username: username,
                password: testAdminUserPassword,
                email: `${username}@example.com`,
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
        const testAdminUser = new User(this.adminUserSettings);
        
        //Save user and setup a session user for it
        return testAdminUser.save().then(user => {
            assert.equal(user.username, username);
            this.clients.admin = createClient();
            return this.clients.admin.User.login(user.username, testAdminUserPassword).then(loggedIn => {
                //Save a reference to the created user
                this.clients.admin.user = loggedIn;
            });
        });
    });
    
    afterEach('Deletes the test user', function() {
        return client.User.delete(this.clients.user.user.username).then(user => {
            assert.equal(user.username, this.clients.user.user.username);
        });
    });
    
    afterEach('Deletes the test admin user', function() {
        return client.User.delete(this.clients.admin.user.username).then(user => {
            assert.equal(user.username, this.clients.admin.user.username);
        });
    });
    
    before('Create a session reference that uses session authentication', function() {
        this.sessionTimeoutRef = createClient();
    });
    
    it('Gets self as non admin', function() {
        return this.clients.user.user.get().then(user => {
            assert.equal(user.username, this.testUserSettings.username);
            assert.strictEqual(user.organization, this.testUserSettings.organization);
            assert.strictEqual(user.organizationalRole, this.testUserSettings.organizationalRole);
            assert.strictEqual(user.data.stringField, this.testUserSettings.data.stringField);
            assert.strictEqual(user.data.numberField, this.testUserSettings.data.numberField);
            assert.strictEqual(user.data.booleanField, this.testUserSettings.data.booleanField);
            assert.isNull(user.emailVerified);
            assert.isString(user.created);
            assert.isAbove(new Date(user.created).valueOf(), 0);
            assert.include(user.permissions, this.testUserSettings.permissions[0]);
        });
    });
    
    it('Fails to create user without password', function() {
        const username = uuid();
        const invalidUser = new User({
                name: 'name',
                username: username,
                password: null,
                email: `${username}@example.com`,
                phone: '808-888-8888',
                disabled: false,
                homeUrl: 'www.google.com',
                receiveAlarmEmails: 'NONE',
                receiveOwnAuditEvents: false,
                muted: false,
                locale: ''
            });
        return invalidUser.save().then(saved => {
            throw new Error('Should not have created user ' + saved.username);
        }, error => {
            assertValidationErrors(['password'], error);
        });
    });
    
    it('Cannot use empty strings and nulls in permission', function() {
        const username = uuid();
        const testUser = new User({
                name: 'name',
                username: username,
                email: `${username}@example.com`,
                password: 'testing1234',
                phone: '808-888-8888',
                disabled: false,
                homeUrl: 'www.google.com',
                receiveAlarmEmails: 'NONE',
                receiveOwnAuditEvents: false,
                permissions: [null, ''],
                muted: false,
                locale: ''
            });
        return testUser.save().then(user => {
            throw new Error('Should not have created user ' + user.username);
        }, error => {
            assertValidationErrors(['permissions', 'permissions'], error);
        });
    });
    
    it('Can lock other users password as admin ', function() {
        return this.clients.admin.User.lockPassword(this.testUserSettings.username).then(response => {
            assert.strictEqual(response.status, 200);
            return this.clients.user.User.login(
                    this.testUserSettings.username,
                    this.testUserSettings.password).then(user => {
                throw new Error('Should not have logged in as user ' + user.username);
            }, error => {
                assert.strictEqual(error.status, 401); 
            });
        });
    });
    
    it('Can\'t lock other users password as user ', function() {
        return this.clients.admin.User.lockPassword(this.clients.admin.user.username).then(user => {
            throw new Error('Should not have locked password ' + user.username);
        }, error => {
            assert.strictEqual(error.status, 403); 
        });
    });
    
    it('Can\'t lock own password', function() {
        return this.clients.admin.User.lockPassword(this.clients.admin.user.username).then(user => {
            throw new Error('Should not have locked password for ' + user.username);
        }, error => {
            assert.strictEqual(error.status, 403); 
        });
    });
    
    it('Can\'t make self non admin', function() {
        return this.clients.admin.user.patch({
            permissions: ['user']
        }).then(user => {
            throw new Error('Should not have updated user ' + user.username);
        }, error => {
            assertValidationErrors(['permissions'], error);
        });  
    });
    
    it('Can\'t disable self as admin', function() {
        return this.clients.admin.user.patch({
            disabled: true
        }).then(user => {
            throw new Error('Should not have updated user ' + user.username);
        }, error => {
            assertValidationErrors(['disabled'], error);
        }); 
    });
    
    it('Can\'t disable self as user', function() {
        return this.clients.user.user.patch({
            disabled: true
        }).then(user => {
            throw new Error('Should not have updated user ' + user.username);
        }, error => {
            assertValidationErrors(['disabled'], error);
        }); 
    });
    
    it('Can\'t update permissions as user', function() {
        return this.clients.user.user.patch({
            permissions: ['user']
        }).then(user => {
            throw new Error('Should not have updated user ' + user.username);
        }, error => {
            assertValidationErrors(['permissions'], error);
        }); 
    });
    
    it('Can\'t rename self to existing user as user', function() {
        return this.clients.user.user.patch({
            username: this.clients.admin.user.username,
        }).then(user  => {
            throw new Error('Should not have updated user '  + user.username);
        }, error => {
            assertValidationErrors(['username'], error);
        }); 
    });

    it('Can rename self as user', function() {
        return this.clients.user.user.patch({
            username: 'Iamnew'
        }).then(user => {
            assert.strictEqual(user.username, 'Iamnew');
        });
    });
    
    it('Queries to match all user permissions as admin', function() {
        return this.clients.admin.User.query(`permissionsContainsAll(permissions,testuser,test)&username=${this.clients.user.user.username}`).then(result => {
            assert.equal(result.total, 1);
            assert.equal(result[0].username, this.testUserSettings.username);
        });
    });
    
    it('Queries to match one user permission as admin', function() {
        return this.clients.admin.User.query(`permissionsContainsAny(permissions,testuser)&username=${this.clients.user.user.username}`).then(result => {
            assert.equal(result.total, 1);
            assert.equal(result[0].username, this.testUserSettings.username);
        });
    });
    
    it('Queries for disabled users as admin', function() {
        //First disable the test user
        return this.clients.admin.User.patch(this.clients.user.user.username, {
            disabled: true
        }).then(user => {
            assert.equal(user.username, this.testUserSettings.username);
            assert.equal(user.disabled, true);
            return this.clients.admin.User.query('disabled=true').then(result => {
                assert.equal(result.total, 1);
                assert.equal(result[0].username, this.testUserSettings.username);
            });
        });
    });
    
    it('Patch test user as user', function() {
        return this.clients.user.user.patch({
            name: 'test user'
        }).then(user => {
            assert.equal(user.username, this.testUserSettings.username);
            assert.equal(user.name, 'test user');
        });
    });
    
    it('Can\'t Set session expiry to less than 1 second', function() {
        const user = new User({
            sessionExpirationOverride: true,
            sessionExpirationPeriod: {
                    periods: 100,
                    type: 'MILLISECONDS'           
                }
        });
        return user.save().then(user => {
            throw new Error('Timeout cannot be < 1s for ' + user.username);
        }, error => {
            assert.strictEqual(error.status, 422);
        });   
    });
    
    it('Returns the current user', () => {
        return this.clients.admin.User.current().then(user => {
            assert.equal(user.username, this.adminUserSettings.username);
        });
    });
    
    it('User session timeout override expires session', function() {
        this.timeout(5000);
        const loginClient = createClient();
        return loginClient.User.login(this.testUserSettings.username, this.testUserSettings.password).then(() => {
            return delay(2000);
        }).then(() => {
            return loginClient.User.current().then(user => {
                throw new Error('Session should be expired for '  + user.username);
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });

    it('Non admin can update self', function() {
        this.clients.user.user.name = 'test user'; 
        return this.clients.user.user.save().then(user => {
            assert.equal(user.username, this.clients.user.user.username);
            return this.clients.user.user.get().then(user => {
                assert.strictEqual(user.name, 'test user');
            });
        });
    });
    it('Admin can update self', function() {
        this.clients.admin.user.name = 'test user'; 
        return this.clients.admin.user.save().then(user => {
            assert.equal(user.username, this.clients.admin.user.username);
            return this.clients.admin.user.get().then(user => {
                assert.strictEqual(user.name, 'test user');
            });
        });
    });
    
    it('Can\'t manually set the emailVerified or created property when creating user', function() {
    
        const username = uuid();
        const dateCreated = new Date(100000).toISOString();
        const emailVerified = new Date(100000).toISOString();
        
        const invalidUser = new User({
            name: 'name',
            username: username,
            password: uuid(),
            email: `${username}@example.com`,
            emailVerified: emailVerified,
            created: dateCreated,
            phone: '808-888-8888',
            disabled: false,
            locale: '',
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'IGNORE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: [],
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
        });
        
        return invalidUser.save().then(user => {
            assert.notStrictEqual(user.created, dateCreated);
            assert.notStrictEqual(user.emailVerified, emailVerified);
        });
        
    });
    
    it('Can\'t manually update the emailVerified or created property', function() {

        const dateCreated = new Date(100000).toISOString();
        const emailVerified = new Date(100000).toISOString();
        this.clients.admin.user.created = dateCreated;
        this.clients.admin.user.emailVerified = emailVerified;
        
        return this.clients.admin.user.save().then(user => {
            assert.notStrictEqual(user.created, dateCreated);
            assert.notStrictEqual(user.emailVerified, emailVerified);
        });
        
    });
    
    it('Can\'t set email to a used email address', function() {
        this.clients.admin.user.email = this.clients.user.user.email;
        return this.clients.admin.user.save().then(user => {
            throw new Error('Should not have saved user ' + user.username);
        }, error => {
            assertValidationErrors(['email'], error);
        });
    });

    it('Can update own home url as non admin', function() {
        return this.clients.user.user.updateHomeUrl('/test-url').then(user => {
            assert.strictEqual(user.homeUrl, '/test-url');
        });
    });
    
    it('Can set audio mute setting as non admin', function() {
        return this.clients.user.user.toggleMuted(true).then(user => {
            assert.strictEqual(user.muted, true);
        });
    });

    it('Can toggle audio mute setting as non admin', function() {
        assert.strictEqual(this.clients.user.user.muted, false);
        return this.clients.user.user.toggleMuted().then(user => {
            assert.strictEqual(user.muted, true);
        });
    });
    
    it('Can switch user from admin to other user and back', function() {
        return this.clients.admin.user.su(this.clients.user.user.username).then(user => {
            assert.strictEqual(user.username, this.clients.user.user.username);
            return this.clients.admin.user.exitSu().then(user => {
                assert.strictEqual(user.username, this.clients.admin.user.username);
            });
        });
    });
    
    it('Cannot change password to something with too few Uppercase letters', function() {
        this.testUser.password = "testings";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.upperCaseCount', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.upperCaseCount', 6, 'INTEGER').then(response => {
                return this.testUser.save().then(user => {
                    throw new Error('Should not have changed password for user' + user.username);
                }, error => {
                    assertValidationErrors(['password'], error);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.upperCaseCount', currentSettingValue, 'INTEGER');
            }); 
        });
    });
    
    it('Cannot change password to something with too few Lowercase letters', function() {
        this.testUser.password = "TESTINGS";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.lowerCaseCount', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.lowerCaseCount', 6, 'INTEGER').then(response => {
                return this.testUser.save().then(user => {
                    throw new Error('Should not have changed password for ' + user.username);
                }, error => {
                    assertValidationErrors(['password'], error);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.lowerCaseCount', currentSettingValue, 'INTEGER');
            });
        });
    });
    
    it('Cannot change password to something with too few digits', function() {
        this.testUser.password = "112TESTINGS";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.digitCount', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.digitCount', 6, 'INTEGER').then(response => {
                return this.testUser.save().then(user => {
                    throw new Error('Should not have changed password for ' + user.username);
                }, error => {
                    assertValidationErrors(['password'], error);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.digitCount', currentSettingValue, 'INTEGER');
            }); 
        });
    });
    
    it('Cannot change password to something with too few special chars', function() {
        this.testUser.password = "%%%&TESTINGS";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.specialCount', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.specialCount', 6, 'INTEGER').then(response => {
                return this.testUser.save().then(user => {
                    throw new Error('Should not have changed password for '  + user.username);
                }, error => {
                    assertValidationErrors(['password'], error);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.specialCount', currentSettingValue, 'INTEGER');
            });  
        });
    });
    
    it('Cannot change password to something with too short', function() {
        this.testUser.password = "12345678910";

        return SystemSettings.getValue('password.rule.lengthMin', 'INTEGER').then(response => {
            const currentSettingValue = response;
            return SystemSettings.setValue('password.rule.lengthMin', 12, 'INTEGER').then(response => {
                return this.testUser.save().then(user => {
                    throw new Error('Should not have changed password for ' + user.username);
                }, error => {
                    assertValidationErrors(['password'], error);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.lengthMin', currentSettingValue, 'INTEGER');
            });  
        });
    });
    
    it('Cannot change password to something with too long', function() {
        this.testUser.password = "12345678910";
        return SystemSettings.getValue('password.rule.lengthMax', 'INTEGER').then(response => {
            const currentSettingValue = response;
            return SystemSettings.setValue('password.rule.lengthMax', 8, 'INTEGER').then(response => {
                return this.testUser.save().then(user => {
                    throw new Error('Should not have changed password for ' + user.username);
                }, error => {
                    assertValidationErrors(['password'], error);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.lengthMax', currentSettingValue, 'INTEGER');
            }); 
        });
    });
    
});
