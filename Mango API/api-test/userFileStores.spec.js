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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const fs = require('fs');
const tmp = require('tmp');
const crypto = require('crypto');
const path = require('path');

describe('User file stores', function() {
    before('Login', function () {
        return login.call(this, client);
    });
    this.timeout(5000);

    it('Lists all file stores', () => {
        return client.restRequest({
            path: '/rest/latest/user-file-stores',
            method: 'GET',
        }).then(response => {
            assert.isObject(response.data);
            assert.isArray(response.data.items);
            assert.isObject(response.data.items.find(s => s.xid === 'default'), 'Cant find default store');
        });
    });

    it.skip('Can create user file store');

    for (const invalidChar of ['\\', '/', '.']) {
        it.skip(`Fails with invalid character ${invalidChar} in XID`);
    }
});
