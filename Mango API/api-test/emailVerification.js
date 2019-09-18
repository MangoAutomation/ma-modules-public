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

    describe('Public registration email verification', function() {
        const enablePublicRegistration = function(value) {
            return client.restRequest({
                path: `/rest/v1/system-settings/${encodeURIComponent(publicRegistrationSystemSetting)}`,
                method: 'PUT',
                params: {type: 'BOOLEAN'},
                data: !!value
            });
        };
        
        const tryPublicEmailVerify = function() {
            const randomEmail = `${uuidV4()}@example.com`;
            
            return this.publicClient.restRequest({
                path: `${emailVerificationUrl}/public/send-email`,
                method: 'POST',
                data: randomEmail
            });
        };
        
        beforeEach('Create public client', function() {
            this.publicClient = new MangoClient(config);
        });
        
        describe('With public registration disabled', function() {
            before('Disable public registration', function() {
                return enablePublicRegistration.call(this, false);
            });

            it('Cannot send a verification email via public endpoint', function() {
                return tryPublicEmailVerify.call(this).then(response => {
                    throw new Error(`Should not succeed, however got a ${response.status} response`);
                }, error => {
                    assert.strictEqual(error.status, 500);
                });
            });
        });
        
        describe('With public registration enabled', function() {
            before('Enable public registration', function() {
                return enablePublicRegistration.call(this, true);
            });

            it('Can send a verification email via public endpoint', function() {
                return tryPublicEmailVerify.call(this);
            });
        });
    });
});