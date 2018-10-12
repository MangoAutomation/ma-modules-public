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
const uuidV4 = require('uuid/v4');
const MangoClient = require('@infinite-automation/mango-client');

describe('Basic authentication', function() {
    before('Login', config.login);
    
    before('Create a test user', function() {
        const username = uuidV4();
        this.testUserPassword = uuidV4();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: 'superadmin',
            password: this.testUserPassword
        });
        return this.testUser.save();
    });
    
    before('Create a client that uses basic authentication', function() {
        const noCookieConfig = Object.assign({
            enableCookies: false
        }, config);
        this.basicAuthClient = new MangoClient(noCookieConfig);
        this.basicAuthClient.setBasicAuthentication(this.testUser.username, this.testUserPassword);
    });
    
    after('Delete the test user', function() {
        return this.testUser.delete();
    });

    it('Can get current user using basic authentication', function() {
        return this.basicAuthClient.User.current();
    });
});
