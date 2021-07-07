/**
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('Permissions endpoint tests', function() {
    before('Login', function() { return login.call(this, client); });

    function assertPermissionSchema(data) {
        assert.isString(data.name);
        assert.isString(data.description);
        assert.isArray(data.permission);
        for (let minterm of data.permission) {
            if (typeof minterm !== 'string') {
                assert.isArray(minterm);
            }
        }
    }
    
    it('Update create user permission with single array', function() {
        return client.restRequest({
            path: '/rest/latest/system-permissions/users.create',
            method: 'PUT',
            data: {
                systemSettingName: 'users.create',
                permission: ['user','superadmin']
            }
        }).then(response => {
            assertPermissionSchema(response.data);
            assert.strictEqual(response.data.name, 'users.create');
            assert.strictEqual(response.data.permission.length, 2);
            assert.include(response.data.permission, 'user');
            assert.include(response.data.permission, 'superadmin');
        }).finally(() => {
            return client.restRequest({
                path: '/rest/latest/system-permissions/users.create',
                method: 'PUT',
                data: {
                    systemSettingName: 'users.create',
                    permission: ['user','superadmin']
                }
            });
        });
    });
    
    it('Update create user permission with multiple arrays', function() {
        return client.restRequest({
            path: '/rest/latest/system-permissions/users.create',
            method: 'PUT',
            data: {
                systemSettingName: 'users.create',
                permission: [['user', 'superadmin'], 'superadmin']
            }
        }).then(response => {
            assertPermissionSchema(response.data);
            assert.strictEqual(response.data.name, 'users.create');
            assert.strictEqual(response.data.permission.length, 2);
            assert.include(response.data.permission, 'superadmin');
        })
    });
    
    it('Can list permissions', function() {
        return client.restRequest({
            path: '/rest/latest/system-permissions',
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            assert.isArray(response.data.items);
            for (let item of response.data.items) {
                assertPermissionSchema(item);
            }
        })
    });
    
    it('Can get create user permission', function() {
        return client.restRequest({
            path: '/rest/latest/system-permissions/users.create',
            method: 'GET'
        }).then(response => {
            assertPermissionSchema(response.data);
            assert.strictEqual(response.data.name, 'users.create');
            assert.strictEqual(response.data.permission.length, 2);
            assert.include(response.data.permission, 'superadmin');
        })
    });
});