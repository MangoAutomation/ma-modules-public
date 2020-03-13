/**
 * Copyright 2020 Infinite Automation Systems Inc.
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

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const client = createClient();
const User = client.User;

describe('User bulk operations ', function() {
    before('Login', function() { return login.call(this, client); });

    const newUser = (username) => {
        return new User({
            name: 'name',
            username: username,
            password: uuid(),
            email: `${username}@example.com`,
            phone: '808-888-8888',
            disabled: false,
            locale: '',
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'IGNORE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: ['user'],
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
    };
    
    beforeEach('Create 3 users', function() {
        this.users = [];
        this.users.push(newUser(uuid()));
        this.users.push(newUser(uuid()));
        this.users.push(newUser(uuid()));

        return Promise.all([this.users[0].save(), this.users[1].save(), this.users[2].save()]);
    });
    
    afterEach('Delete the created users', function() {
        return Promise.all([this.users[0].delete(), this.users[1].delete(), this.users[2].delete()]);
    });

    it('Can bulk update users', function() {
        this.timeout(5000);
        
        this.users[0].organization = 'User 0 Organization';
        this.users[1].organization = 'User 1 Organization';
        
        return client.restRequest({
            path: `/rest/v2/users/bulk`,
            method: 'POST',
            data: {
                action: 'UPDATE',
                body: null, //defaults?
                requests: [
                    {
                        action: 'UPDATE',
                        username: this.users[0].username,
                        body: this.users[0]
                    },
                    {
                        action: 'UPDATE',
                        username: this.users[1].username,
                        body: this.users[1]
                    }
                ]
            }
        }).then(response => {
            assert.strictEqual(response.status, 201);
            assert.isString(response.data.id);
            assert.isString(response.data.status);
            assert.notStrictEqual(response.data.status, 'TIMED_OUT');
            assert.notStrictEqual(response.data.status, 'CANCELLED');
            assert.notStrictEqual(response.data.status, 'ERROR');
            
            return delay(500).then(() => {
                return client.restRequest({
                    path: response.headers.location
                });
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.strictEqual(response.data.status, 'SUCCESS');
            assert.strictEqual(response.data.position, 2);
            assert.strictEqual(response.data.maximum, 2);
            assert.strictEqual(response.data.progress, 100);
            assert.isNumber(response.data.expiration);
            
            const results = response.data.result.responses;
            assert.strictEqual(response.data.result.hasError, false);
            assert.isArray(results);
            assert.strictEqual(results.length, 2);

            assert.strictEqual(results[0].httpStatus, 200);
            assert.strictEqual(results[0].body.organization, this.users[0].organization);
            assert.strictEqual(results[1].httpStatus, 200);
            assert.strictEqual(results[1].body.organization, this.users[1].organization);
        });
    });
});
