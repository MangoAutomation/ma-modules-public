/**
 * Copyright 2019 Infinite Automation Systems Inc.
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

const emailVerificationUrl = '/rest/v2/email-verification';
const publicRegistrationSystemSetting = 'users.publicRegistration.enabled';

describe('Email verification', function() {
    const createUserV2 = function() {
        const newUsername = uuidV4();
        return {
            username: newUsername,
            email: `${newUsername}@example.com`,
            name: `${newUsername}`,
            permissions: [],
            password: newUsername,
            locale: '',
            receiveAlarmEmails: 'IGNORE'
        };
    };
    
    
    before('Login', config.login);
    
    before('Create a test user', function() {
        const username = uuidV4();
        this.testUserPassword = uuidV4();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: '',
            password: this.testUserPassword
        });
        return this.testUser.save();
    });
    
    after('Delete the test user', function() {
        return this.testUser.delete();
    });
    
    before('Disable email sending', function() {
        this.smtpHostSetting = new SystemSetting({id: 'emailSmtpHost'});
        return this.smtpHostSetting.getValue().then(value => {
            this.smtpHost = value;
            return this.smtpHostSetting.setValue('disabled.example.com');
        });
    });
    
    after('Restore email sending', function() {
        if (this.smtpHost) {
            return this.smtpHostSetting.setValue(this.smtpHost);
        }
    });

    describe('Public registration email verification', function() {
        const tryPublicEmailVerify = function(emailAddress = `${uuidV4()}@example.com`) {
            return this.publicClient.restRequest({
                path: `${emailVerificationUrl}/public/send-email`,
                method: 'POST',
                data: {emailAddress}
            });
        };
        
        beforeEach('Create public client', function() {
            this.publicClient = new MangoClient(config);
        });
        
        describe('With public registration disabled', function() {
            before('Disable public registration', function() {
                SystemSetting.setValue(publicRegistrationSystemSetting, false, 'BOOLEAN');
            });

            it('Cannot send a verification email via public endpoint', function() {
                return tryPublicEmailVerify.call(this).then(response => {
                    assert.fail('Sending verification email should not have succeeded');
                }, error => {
                    assert.strictEqual(error.status, 500);
                });
            });
        });
        
        describe('With public registration enabled', function() {
            before('Enable public registration', function() {
                SystemSetting.setValue(publicRegistrationSystemSetting, true, 'BOOLEAN');
            });

            it('Can send a verification email via public endpoint', function() {
                return tryPublicEmailVerify.call(this);
            });

            it('Cannot distinguish if the email is already in use', function() {
                return tryPublicEmailVerify.call(this, this.testUser.email);
            });

            it('Can use a public registration token to register', function() {
                const newUser = createUserV2();

                // use default client logged in as admin to generate a token
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {emailAddress: newUser.email}
                }).then(response => {
                    assert.isString(response.data.token);

                    // use the public client to actually register the new user using the token
                    return this.publicClient.restRequest({
                        path: `${emailVerificationUrl}/public/register`,
                        method: 'POST',
                        data: {
                            token: response.data.token,
                            user: newUser
                        }
                    });
                }).then(response => {
                    assert.strictEqual(response.data.username, newUser.username);
                }).finally(() => {
                    return User.delete(newUser.username).catch(e => null);
                });
            });

            it('Administrator can\'t create registration token for email address that is already in use', function() {
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {emailAddress: this.testUser.email}
                }).then(response => {
                    assert.fail('Creating token should not have succeeded');
                }, error => {
                    assert.strictEqual(error.status, 500);
                });
            });

            it('Can\'t use a user token for public registration', function() {
                const newUser = createUserV2();

                // use default client logged in as admin to generate a token
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {
                        emailAddress: newUser.email,
                        username: this.testUser.username
                    }
                }).then(response => {
                    assert.isString(response.data.token);

                    // use the public client to actually register the new user using the token
                    return this.publicClient.restRequest({
                        path: `${emailVerificationUrl}/public/register`,
                        method: 'POST',
                        data: {
                            token: response.data.token,
                            user: newUser
                        }
                    });
                }).then(response => {
                    assert.fail('Creating user should not have succeeded');
                }, error => {
                    assert.strictEqual(error.status, 400);
                }).finally(() => {
                    return User.delete(newUser.username).catch(e => null);
                });
            });
        });
    });
});