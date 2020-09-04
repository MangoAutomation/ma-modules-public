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

const {createClient, login, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
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

    const createFileStore = (xid) => {
        return client.restRequest({
            path: '/rest/latest/user-file-stores',
            method: 'POST',
            data: {
                xid,
                name: 'name for ' + xid,
                readPermission: [],
                writePermission: []
            }
        });
    };

    const deleteFileStore = (xid) => {
        return client.restRequest({
            path: `/rest/latest/user-file-stores/${encodeURIComponent(xid)}`,
            method: 'DELETE',
            params: {purgeFiles: true}
        }).catch(e => null);
    };

    it('Lists all file stores', () => {
        return client.restRequest({
            path: '/rest/latest/user-file-stores',
            method: 'GET',
        }).then(response => {
            assert.isObject(response.data);
            assert.isArray(response.data.items);
            assert.isNotEmpty(response.data.items);
            assert.isObject(response.data.items.find(s => s.xid === 'default'), 'Cant find default store');
        });
    });

    it('Can create user file store', function() {
        const xid = uuid();
        return createFileStore(xid).then(response => {
            assert.isObject(response.data);
            assert.strictEqual(response.data.xid, xid);
            assert.isNumber(response.data.id);
            assert.isAtLeast(response.data.id, 1);
            assert.isFalse(response.data.builtIn);
            assert.isString(response.data.name);
            assert.isArray(response.data.readPermission);
            assert.isEmpty(response.data.readPermission);
            assert.isArray(response.data.writePermission);
            assert.isEmpty(response.data.writePermission);
        }).finally(() => {
            return deleteFileStore(xid);
        });
    });

    for (const invalidChar of ['\\', '/', '.']) {
        it(`Fails with invalid character ${invalidChar} in XID`, function() {
            const xid = uuid() + invalidChar;
            return createFileStore(xid).then(response => {
                assert.fail('Should fail');
            }, error => {
                assert.strictEqual(error.status, 422);
                assert.isObject(error.data);
                assert.isObject(error.data.result);
                assert.isArray(error.data.result.messages);
                assert.isNotEmpty(error.data.result.messages);
                const message = error.data.result.messages.find(m => m.property === 'xid');
                assert.isObject(message);
                assert.strictEqual(message.level, 'ERROR');
            }).finally(() => {
                return deleteFileStore(xid);
            });
        });
    }
});
