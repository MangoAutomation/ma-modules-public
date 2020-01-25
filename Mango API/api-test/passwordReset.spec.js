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

const {createClient, login, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const User = client.User;

const resetUrl = '/rest/v2/password-reset';

describe('Password reset', function() {
    before('Login', function() { return login.call(this, client); });
    
    before('Create a test user', function() {
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: [],
            password: this.testUserPassword
        });
        return this.testUser.save();
    });
    
    after('Delete the test user', function() {
        return this.testUser.delete();
    });
    
    before('Helpers', function() {
        this.publicClient = createClient();
    });

    it('Can trigger a password reset email', function() {
        return this.testUser.save().then(() => {
            return this.publicClient.restRequest({
                path: `${resetUrl}/send-email`,
                method: 'POST',
                data: {
                    username: this.testUser.username,
                    email: this.testUser.email
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 204);
        });
    });
    
    it('Rejects incorrect email addresses', function() {
        return this.testUser.save().then(() => {
            return this.publicClient.restRequest({
                path: `${resetUrl}/send-email`,
                method: 'POST',
                data: {
                    username: this.testUser.username,
                    email: 'blah' + this.testUser.email 
                }
            });
        }).then(response => {
            throw new Error('Email should not have sent');
        }, error => {
            assert.strictEqual(error.status, 400);
        });
    });

    it(`Won't send emails for disabled users`, function() {
        const disabledUsername = uuid();
        const disabledUser = new User({
            username: disabledUsername,
            email: `${disabledUsername}@example.com`,
            name: 'This is a name',
            permissions: '',
            password: uuid(),
            disabled: true
        });
        const deleteDisabledUser = () => {
            return disabledUser.delete().then(null, noop);
        };
        
        return disabledUser.save().then(() => {
            return this.publicClient.restRequest({
                path: `${resetUrl}/send-email`,
                method: 'POST',
                data: {
                    username: disabledUser.username,
                    email: disabledUser.email 
                }
            });
        }).then(() => {
            throw new Error(`Shouldn't send emails to disabled users`);
        }, error => {
            assert.strictEqual(error.status, 400);
        }).then(result => {
            return deleteDisabledUser().then(() => result);
        }, error => {
            return deleteDisabledUser().then(() => Promise.reject(error));
        });
    });
    
    it('Can retrieve the public key', function() {
        return this.publicClient.restRequest({
            path: `${resetUrl}/public-key`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isString(response.data);
        });
    });
    
    it('Can generate a reset token for a user', function() {
        return this.testUser.save().then(() => {
            return client.restRequest({
                path: `${resetUrl}/create`,
                method: 'POST',
                data: {
                    username: this.testUser.username
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);
        });
    });

    it('Can verify a token', function() {
        return this.testUser.save().then(() => {
            return client.restRequest({
                path: `${resetUrl}/create`,
                method: 'POST',
                data: {
                    username: this.testUser.username
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);

            return this.publicClient.restRequest({
                path: `${resetUrl}/verify`,
                method: 'GET',
                params: {
                    token: response.data.token
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            
            const parsedToken = response.data;
            assert.isObject(parsedToken);
            assert.isObject(parsedToken.header);
            assert.isObject(parsedToken.body);
            assert.notProperty(parsedToken, 'signature');
            assert.strictEqual(parsedToken.header.alg, 'ES512');
            assert.strictEqual(parsedToken.body.sub, this.testUser.username);
            assert.strictEqual(parsedToken.body.typ, 'pwreset');
            assert.isNumber(parsedToken.body.exp);
            assert.isNumber(parsedToken.body.id);
            assert.isNumber(parsedToken.body.v);
        });
    });
    
    it('Can reset a user\'s password with a token', function() {
        const newPassword = uuid();
        
        return client.restRequest({
            path: `${resetUrl}/create`,
            method: 'POST',
            data: {
                username: this.testUser.username
            }
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);
            
            return this.publicClient.restRequest({
                path: `${resetUrl}/reset`,
                method: 'POST',
                data: {
                    token: response.data.token,
                    newPassword: newPassword
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 204);

            const loginClient = createClient();
            return loginClient.User.login(this.testUser.username, newPassword);
        });
    });
    
    it('Can\'t use a password reset token twice', function() {
        let resetToken;
        
        return client.restRequest({
            path: `${resetUrl}/create`,
            method: 'POST',
            data: {
                username: this.testUser.username
            }
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);
            
            resetToken = response.data.token;

            return this.publicClient.restRequest({
                path: `${resetUrl}/reset`,
                method: 'POST',
                data: {
                    token: resetToken,
                    newPassword: uuid()
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 204);
            
            return this.publicClient.restRequest({
                path: `${resetUrl}/reset`,
                method: 'POST',
                data: {
                    token: resetToken,
                    newPassword: uuid()
                }
            });
        }).then(response => {
            throw new Error('Shouldn\'t be able to use a token twice');
        }, error => {
            assert.strictEqual(error.status, 400);
            assert.isObject(error.data);
            assert.strictEqual(error.data.mangoStatusCode, 4005);
        });
    });
    
    it(`Can't use a password reset token after the user's password was updated`, function() {
        let resetToken;
        
        // create the token
        return client.restRequest({
            path: `${resetUrl}/create`,
            method: 'POST',
            data: {
                username: this.testUser.username
            }
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);
            
            resetToken = response.data.token;
            
            // change the test user's password
            this.testUserPassword = uuid();
            this.testUser.password = this.testUserPassword;
            return this.testUser.save();
        }).then(user => {

            // try to use the token
            return this.publicClient.restRequest({
                path: `${resetUrl}/reset`,
                method: 'POST',
                data: {
                    token: resetToken,
                    newPassword: uuid()
                }
            });
        }).then(response => {
            throw new Error('Shouldn\'t be able to use the token');
        }, error => {
            assert.strictEqual(error.status, 400);
            assert.isObject(error.data);
            assert.strictEqual(error.data.mangoStatusCode, 4005);
        });
    });
    
    it('Locks a user\'s password (by default) when an admin creates a reset token', function() {
        const newPassword = uuid();
        let resetToken;
        
        // ensure we have a known password beforehand
        this.testUserPassword = uuid();
        this.testUser.password = this.testUserPassword;
        
        return this.testUser.save().then(() => {
            return client.restRequest({
                path: `${resetUrl}/create`,
                method: 'POST',
                data: {
                    username: this.testUser.username
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);
            
            resetToken = response.data.token;
            
            const loginClient = createClient();
            return loginClient.User.login(this.testUser.username, this.testUserPassword).then(() => {
                throw new Error('Password should be locked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        }).then(() => {
            return this.publicClient.restRequest({
                path: `${resetUrl}/reset`,
                method: 'POST',
                data: {
                    token: resetToken,
                    newPassword: newPassword
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 204);

            this.testUserPassword = newPassword;
            
            const loginClient = createClient();
            return loginClient.User.login(this.testUser.username, newPassword);
        });
    });
});
