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

const emailVerificationUrl = '/rest/v2/users/email-verification';

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
    
    beforeEach('Create public client', function() {
        this.publicClient = new MangoClient(config);
    });
    
    describe('With registration disabled', function() {
        it('Triggers a verification email when not authenticated (public)', function() {
            const randomEmail = `${uuidV4()}@example.com`;
            
            return this.publicClient.restRequest({
                path: `${emailVerificationUrl}/send-email`,
                method: 'POST',
                data: randomEmail
            }).then(response => {
                throw new Error(`Should not succeed, however got a ${response.status} response`);
            }, error => {
                assert.strictEqual(error.status, 500);
            });
        });
    });
});