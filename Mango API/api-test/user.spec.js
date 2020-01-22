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

const {createClient, login, uuid, noop, config, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const User = client.User;
const SystemSettings = client.SystemSetting;

describe('User service', function() {
    before('Login', function() { return login.call(this, client); });
    
    beforeEach('Create a test user', function() {
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: 'user',
            password: this.testUserPassword,
            sessionExpirationOverride: true,
            sessionExpirationPeriod: {
                periods: 1,
                type: 'SECONDS'           
            }
        });
        return this.testUser.save();
    });

    afterEach('Delete the test user', function() {
        return this.testUser.delete().catch(noop);
    });
    
    it('Returns the current user', () => {
        return User.current().then(user => {
            assert.equal(user.username, config.username);
        });
    });
    
    it('Can\'t Set session expiry to less than 1 second', function() {
        this.testUser.sessionExpirationPeriod = {
                periods: 100,
                type: 'MILLISECONDS'           
            };
        return this.testUser.save().then(response =>{
            throw new Error('Timeout cannot be < 1s');
        }, error => {
            assert.strictEqual(error.status, 422);
        });   
    });
    
    it('User session timeout override expires session', function() {
        this.timeout(5000);
        const loginClient = createClient();
        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return delay(2000);
        }).then(() => {
            return loginClient.User.current().then(response => {
                throw new Error('Session should be expired');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
   
    it('Cannot change password to something with too few Uppercase letters', function() {
        this.testUser.password = "testings";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.upperCaseCount', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.upperCaseCount', 6, 'INTEGER').then(response => {
                return this.testUser.save().then(response => {
                    throw new Error('Should not have changed password');
                }, error => {
                    assert.strictEqual(error.status, 422);
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
                return this.testUser.save().then(response => {
                    throw new Error('Should not have changed password');
                }, error => {
                    assert.strictEqual(error.status, 422);
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
                return this.testUser.save().then(response => {
                    throw new Error('Should not have changed password');
                }, error => {
                    assert.strictEqual(error.status, 422);
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
                return this.testUser.save().then(response => {
                    throw new Error('Should not have changed password');
                }, error => {
                    assert.strictEqual(error.status, 422);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.specialCount', currentSettingValue, 'INTEGER');
            });  
        });
    });
    
    it('Cannot change password to something with too short', function() {
        this.testUser.password = "12345678910";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.lengthMin', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.lengthMin', 12, 'INTEGER').then(response => {
                return this.testUser.save().then(response => {
                    throw new Error('Should not have changed password');
                }, error => {
                    assert.strictEqual(error.status, 422);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.lengthMin', 8, 'INTEGER');
            });  
        });
    });
    
    it('Cannot change password to something with too long', function() {
        this.testUser.password = "12345678910";
        let currentSettingValue;
        return SystemSettings.getValue('password.rule.lengthMax', 'INTEGER').then(response => {
            currentSettingValue = response;
            return SystemSettings.setValue('password.rule.lengthMax', 8, 'INTEGER').then(response => {
                return this.testUser.save().then(response => {
                    throw new Error('Should not have changed password');
                }, error => {
                    assert.strictEqual(error.status, 422);
                });  
            }).finally(() => {
                return SystemSettings.setValue('password.rule.lengthMax', 255, 'INTEGER');
            }); 
        });
    });
    
});
