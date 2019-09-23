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

describe('Email verification', function() {
    const emailVerificationUrl = '/rest/v2/email-verification';

    const createUserV1 = function() {
        const username = uuidV4();
        return new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: '',
            password: username
        });
    };
    
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
        this.testUser = createUserV1();
        return this.testUser.save();
    });
    
    after('Delete the test user', function() {
        return this.testUser.delete().catch(e => null);
    });
    
    before('Disable sending email', function() {
        this.smtpHostSetting = new SystemSetting({id: 'emailSmtpHost'});

        return this.smtpHostSetting.getValue().then(value => {
            this.smtpHost = value;
            return this.smtpHostSetting.setValue('disabled.example.com');
        });
    });
    
    after('Restore sending email', function() {
        if (this.smtpHost != null) {
            return this.smtpHostSetting.setValue(this.smtpHost);
        }
    });
    
    before('Get public registration setting', function() {
        this.publicRegistrationSetting = new SystemSetting({id: 'users.publicRegistration.enabled', type: 'BOOLEAN'});
        return this.publicRegistrationSetting.getValue(value => {
            this.publicRegistrationWasEnabled = value;
        });
    });
    
    before('Restore public registration setting', function() {
        if (this.publicRegistrationWasEnabled != null) {
            return this.publicRegistrationSetting.setValue(this.publicRegistrationWasEnabled);
        }
    });
    
    beforeEach('Create public client', function() {
        this.publicClient = new MangoClient(config);
    });

    describe('Public registration email verification', function() {
        const tryPublicEmailVerify = function(emailAddress = `${uuidV4()}@example.com`) {
            return this.publicClient.restRequest({
                path: `${emailVerificationUrl}/public/send-email`,
                method: 'POST',
                data: {emailAddress}
            });
        };

        describe('With public registration disabled', function() {
            before('Disable public registration', function() {
                return this.publicRegistrationSetting.setValue(false);
            });

            it('Cannot send a verification email via public endpoint', function() {
                return tryPublicEmailVerify.call(this).then(response => {
                    assert.fail('Sending verification email should not have succeeded');
                }, error => {
                    assert.strictEqual(error.status, 409);
                });
            });
            
            it('Administrator can\'t create registration token', function() {
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {emailAddress: `${uuidV4()}@example.com`}
                }).then(response => {
                    assert.fail('Creating token should not have succeeded');
                }, error => {
                    assert.strictEqual(error.status, 409);
                });
            });
        });
        
        describe('With public registration enabled', function() {
            before('Enable public registration', function() {
                return this.publicRegistrationSetting.setValue(true);
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
                    const createdUser = response.data;
                    assert.strictEqual(createdUser.username, newUser.username);
                    assert.isTrue(createdUser.disabled);
                    assert.isString(createdUser.emailVerified);
                    assert.isAbove(new Date(createdUser.emailVerified).valueOf(), 0);
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
                    assert.strictEqual(error.status, 409);
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
            
            it('Validation works when creating new users', function() {
                const newUser = createUserV2();
                newUser.password = '';

                // use default client logged in as admin to generate a token
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {
                        emailAddress: newUser.email
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
                    assert.strictEqual(error.status, 422);
                    assert.strictEqual(error.data.mangoStatusName, 'VALIDATION_FAILED');
                    assert.isArray(error.data.result.messages);
                    assert.isObject(error.data.result.messages.find(m => m.property === 'password'));
                }).finally(() => {
                    return User.delete(newUser.username).catch(e => null);
                });
            });
            
            it('Can\'t set permissions when registering', function() {
                const newUser = createUserV2();
                newUser.disabled = false;
                newUser.permissions = ['naughty-permission'];

                // use default client logged in as admin to generate a token
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {
                        emailAddress: newUser.email
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
                    const createdUser = response.data;
                    assert.strictEqual(createdUser.username, newUser.username);
                    assert.isTrue(createdUser.disabled);
                    assert.isString(createdUser.emailVerified);
                    assert.isAbove(new Date(createdUser.emailVerified).valueOf(), 0);
                    assert.lengthOf(createdUser.permissions, 1);
                    assert.include(createdUser.permissions, 'user');
                }).finally(() => {
                    return User.delete(newUser.username).catch(e => null);
                });
            });
        });
    });

    describe('Existing user email verification', function() {
        const createUserAndVerifyEmail = function() {
            const user = createUserV1();
            
            const promise = user.save().then(() => {
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {
                        emailAddress: user.email,
                        username: user.username
                    }
                });
            }).then(response => {
                return this.publicClient.restRequest({
                    path: `${emailVerificationUrl}/public/update-email`,
                    method: 'POST',
                    data: {
                        token: response.data.token
                    }
                });
            }).then(response => {
                const updatedUser = response.data;

                assert.strictEqual(updatedUser.email, user.email);
                assert.isString(updatedUser.emailVerified);
                assert.isAbove(new Date(updatedUser.emailVerified).valueOf(), 0);
                
                user.email = updatedUser.email;
                user.emailVerified = updatedUser.emailVerified;
                return user;
            });
            
            return {user, promise};
        };
        
        it('Wont send email without specifing the username', function() {
            return client.restRequest({
                path: `${emailVerificationUrl}/send-email`,
                method: 'POST',
                data: {emailAddress: this.testUser.email}
            }).then(response => {
                assert.fail('Request should fail');
            }, error => {
                assert.strictEqual(error.status, 409);
            });
        });
        
        it('Sends email for an existing user', function() {
            return client.restRequest({
                path: `${emailVerificationUrl}/send-email`,
                method: 'POST',
                data: {
                    emailAddress: this.testUser.email,
                    username: this.testUser.username
                }
            });
        });
        
        it.skip('Won\'t send email for a disabled user', function() {
            const user = createUserV1();
            user.disabled = true;
            return user.save().then(() => {
                return client.restRequest({
                    path: `${emailVerificationUrl}/send-email`,
                    method: 'POST',
                    data: {
                        emailAddress: this.testUser.email,
                        username: this.testUser.username
                    }
                });
            }).then(response => {
                assert.fail('Request should fail');
            }, error => {
                assert.strictEqual(error.status, 500);
            }).finally(() => {
                return user.delete().catch(e => null);
            });
        });
        
        it('Verifies a user\'s email address', function() {
            const {user, promise} = createUserAndVerifyEmail.call(this);
            return promise.finally(() => {
                return user.delete().catch(e => null);
            });
        });
        
        it('Updates a user\'s email address', function() {
            const user = createUserV1();
            const newEmailAddress = `${uuidV4()}@example.com`;
            
            return user.save().then(() => {
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {
                        emailAddress: newEmailAddress,
                        username: user.username
                    }
                });
            }).then(response => {
                return this.publicClient.restRequest({
                    path: `${emailVerificationUrl}/public/update-email`,
                    method: 'POST',
                    data: {
                        token: response.data.token
                    }
                });
            }).then(response => {
                const updatedUser = response.data;

                assert.strictEqual(updatedUser.email, newEmailAddress);
                assert.isString(updatedUser.emailVerified);
                assert.isAbove(new Date(updatedUser.emailVerified).valueOf(), 0);
            }).finally(() => {
                return user.delete().catch(e => null);
            });
        });
        
        it('Verified date updates if user re-verifies their email', function() {
            const {user, promise} = createUserAndVerifyEmail.call(this);
            return promise.then(() => {
                const dateFirstVerified = new Date(user.emailVerified);
                
                return client.restRequest({
                    path: `${emailVerificationUrl}/create-token`,
                    method: 'POST',
                    data: {
                        emailAddress: user.email,
                        username: user.username
                    }
                }).then(response => {
                    return this.publicClient.restRequest({
                        path: `${emailVerificationUrl}/public/update-email`,
                        method: 'POST',
                        data: {
                            token: response.data.token
                        }
                    });
                }).then(response => {
                    const updatedUser = response.data;

                    assert.strictEqual(updatedUser.email, user.email);
                    assert.isString(updatedUser.emailVerified);
                    assert.isAbove(new Date(updatedUser.emailVerified).valueOf(), dateFirstVerified.valueOf());
                });
            }).finally(() => {
                return user.delete().catch(e => null);
            });
        });
        
        it('Manually updating email address sets emailVerified property back to null', function() {
            const {user, promise} = createUserAndVerifyEmail.call(this);
            return promise.then(user => {
                user.email = `${uuidV4()}@example.com`;
                return user.save();
            }).then(() => {
                assert.isNull(user.emailVerified);
            }).finally(() => {
                return user.delete().catch(e => null);
            });
        });
        
        it('Manually updating other properties do not set emailVerified property back to null', function() {
            const {user, promise} = createUserAndVerifyEmail.call(this);
            return promise.then(user => {
                user.name = uuidV4();
                return user.save();
            }).then(() => {
                assert.isString(user.emailVerified);
                assert.isAbove(new Date(user.emailVerified).valueOf(), 0);
            }).finally(() => {
                return user.delete().catch(e => null);
            });
        });
        
        it('Administrator can\'t create verification token for email address that is already in use', function() {
            return client.restRequest({
                path: `${emailVerificationUrl}/create-token`,
                method: 'POST',
                data: {
                    emailAddress: this.testUser.email
                }
            }).then(response => {
                assert.fail('Request should fail');
            }, error => {
                assert.strictEqual(error.status, 409);
            });
        });
    });
    
    it('Can retrieve the public key', function() {
        return this.publicClient.restRequest({
            path: `${emailVerificationUrl}/public/public-key`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isString(response.data);
        });
    });
    
    it('Can verify a token', function() {
        const emailAddress = `${uuidV4()}@example.com`;
        return client.restRequest({
            path: `${emailVerificationUrl}/create-token`,
            method: 'POST',
            data: {
                emailAddress
            }
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);

            return this.publicClient.restRequest({
                path: `${emailVerificationUrl}/public/verify`,
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
            assert.strictEqual(parsedToken.body.sub, emailAddress);
            assert.strictEqual(parsedToken.body.typ, 'emailverify');
            assert.isNumber(parsedToken.body.exp);
        });
    });
    
    it('Token does not verify if tampered with', function() {
        const emailAddress = `${uuidV4()}@example.com`;
        return client.restRequest({
            path: `${emailVerificationUrl}/create-token`,
            method: 'POST',
            data: {
                emailAddress
            }
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data);
            assert.isString(response.data.token);
            assert.isString(response.data.relativeUrl);
            assert.isString(response.data.fullUrl);

            const token = response.data.token;
            const tamperedToken = token.slice(0, -1) + String.fromCharCode(token.charCodeAt(token.length - 1) + 1);

            return this.publicClient.restRequest({
                path: `${emailVerificationUrl}/public/verify`,
                method: 'GET',
                params: {
                    token: tamperedToken
                }
            });
        }).then(response => {
            assert.fail('Token should not verify');
        }, error => {
            assert.strictEqual(error.status, 400);
        });
    });
});