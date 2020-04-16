/**
 * Copyright 2017 Infinite Automation Systems Inc.
 * http://infiniteautomation.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

const {createClient, assertValidationErrors, defer, delay, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const Role = client.Role;
const User = client.User;

describe('Permissions endpoint tests', function() {
    before('Login', function() { return login.call(this, client); });
    
    it('Update create user permission with single array', () => {
        return client.restRequest({
            path: '/rest/v2/permissions/users.create',
            method: 'PUT',
            data: {
                systemSettingName: 'users.create',
                permission: ['user','superadmin']
            }
        }).then(response => {
            assert.strictEqual(response.data.systemSettingName, 'users.create');
            assert.strictEqual(response.data.permission.length, 2);
            assert.include(response.data.permission, 'user');
            assert.include(response.data.permission, 'superadmin');
        })
    });
    
    
    it('Update create user permission with multiple arrays', () => {
        return client.restRequest({
            path: '/rest/v2/permissions/users.create',
            method: 'PUT',
            data: {
                systemSettingName: 'users.create',
                permission: [['user', 'superadmin'], 'superadmin']
            }
        }).then(response => {
            assert.strictEqual(response.data.systemSettingName, 'users.create');
            assert.strictEqual(response.data.permission.length, 2);
            assert.include(response.data.permission, 'superadmin');
        })
    });
    
});