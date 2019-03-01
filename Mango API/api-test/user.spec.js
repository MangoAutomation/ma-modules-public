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

describe('User service', () => {
    before('Login', config.login);
    
    beforeEach('Create a test user', function() {
        const username = uuidV4();
        this.testUserPassword = uuidV4();
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
        return this.testUser.delete().catch(config.noop);
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
        })    
    })
    
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
