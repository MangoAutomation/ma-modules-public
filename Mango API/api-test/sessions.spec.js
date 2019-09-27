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

const {createClient, login, uuid} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const User = client.User;

describe('Sessions and expiry', function() {
    before('Login', login.bind(this, client));
    
    beforeEach('Create a test user', function() {
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: 'user',
            password: this.testUserPassword
        });
        return this.testUser.save();
    });
    
    afterEach('Delete the test user', function() {
        return this.testUser.delete();
    });

    it('User\'s sessions are expired when they are disabled', function() {
        const loginClient = createClient();

        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            this.testUser.disabled = true;
            return this.testUser.save();
        }).then(() => {
            return loginClient.User.current().then(response => {
                throw new Error('Session should be expired');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
    
    it('User\'s sessions are expired when their password is changed', function() {
        const loginClient = createClient();

        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            this.testUser.password = uuid();
            return this.testUser.save();
        }).then(() => {
            return loginClient.User.current().then(response => {
                throw new Error('Session should be expired');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
    
    it('User\'s sessions are expired when their permissions are changed', function() {
        const loginClient = createClient();

        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            this.testUser.permissions = `user,${uuid()}`;
            return this.testUser.save();
        }).then(() => {
            return loginClient.User.current().then(response => {
                throw new Error('Session should be expired');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
    
    it('User\'s sessions are not expired when other details are changed', function() {
        const loginClient = createClient();

        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            this.testUser.name = uuid();
            return this.testUser.save();
        }).then(() => {
            return loginClient.User.current();
        });
    });
    
    it('User\'s current session is not expired when changing own password', function() {
        const loginClient = createClient();

        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(user => {
            user.password = uuid();
            return user.save();
        }).then(() => {
            return loginClient.User.current();
        });
    });
    
    it('User\'s other sessions are expired when changing own password', function() {
        const loginClient = createClient();
        const loginClient2 = createClient();

        return loginClient2.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return loginClient.User.login(this.testUser.username, this.testUserPassword);
        }).then(user => {
            user.password = uuid();
            return user.save();
        }).then(() => {
            return loginClient.User.current();
        }).then(() => {
            return loginClient2.User.current().then(response => {
                throw new Error('Session should be expired');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
    
    it('Session is invalidated when logging out', function() {
        const loginClient = createClient();
        let oldCookies;

        return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
            return loginClient.User.current();
        }).then(user => {
            oldCookies = Object.assign({}, loginClient.cookies);
            return loginClient.User.logout();
        }).then(() => {
            // restore the cookies to the pre-login state
            loginClient.cookies = oldCookies;
            
            return loginClient.User.current().then(response => {
                throw new Error('Session should be invalid');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
});
