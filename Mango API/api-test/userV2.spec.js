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

describe('User V2 endpoint tests', function() {
    before('Login', config.login);
    before('Create test User', function() {
        global.testUser = {
            name: 'name',
            username: 'test',
            password: 'password',
            email: 'test@test.com',
            phone: '808-888-8888',
            disabled: false,
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'NONE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: ['user', 'test'],
        };
        return client.restRequest({
            path: '/rest/v2/users',
            method: 'POST',
            data: global.testUser
        }).then(response => {
            assert.equal(response.data.username, global.testUser.username);
        });
    });
    
    after('Deletes the test user', function() {
        return client.restRequest({
            path: `/rest/v2/users/test`,
            method: 'DELETE'
        }).then(response => {
            assert.equal(response.data.username, global.testUser.username);
        });
    });
    
    it('Fails to create user without password', () =>{
        return client.restRequest({
            path: '/rest/v2/users',
            method: 'POST',
            data: {
                name: 'name',
                username: 'test',
                email: 'test@test.com',
                phone: '808-888-8888',
                disabled: false,
                homeUrl: 'www.google.com',
                receiveAlarmEmails: 'NONE',
                receiveOwnAuditEvents: false,
                muted: false,
                //permissions: ['user', 'test'],
            }
        }).then(response => {
            throw new Error('Should not have created user');
        }, error => {
           assert.strictEqual(error.status, 422); 
        });
    });
    
    
    it('Queries to match all user permissions', () => {
        return client.restRequest({
            path: `/rest/v2/users?permissionsContainsAll(permissions,user,test)`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].username, global.testUser.username);
        });
    });
    
    it('Queries to match one user permission', () => {
        return client.restRequest({
            path: `/rest/v2/users?permissionsContainsAny(permissions,superadmin)`,
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.total, 1);
            assert.equal(response.data.items[0].username, 'admin');
        });
    });
    
    it('Patch test user', ()=>{
        return client.restRequest({
            path: `/rest/v2/users/test`,
            method: 'PATCH',
            data: {
                name: 'test user'
            }
        }).then(response => {
            assert.equal(response.data.username, global.testUser.username);
            assert.equal(response.data.name, 'test user');
        });
    });
    
});
