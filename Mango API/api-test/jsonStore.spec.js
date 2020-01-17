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

const {createClient, login, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('JSON store', function() {
    before('Login', function() { return login.call(this, client); });

    const createJsonStoreItem = (jsonData) => {
        const xid = uuid();
        const storeItem = {
            name: 'my json store item',
            publicData: false,
            readPermission: '',
            editPermission: ''
        };

        return client.restRequest({
            path: '/rest/v2/json-data/' + encodeURIComponent(xid),
            method: 'POST',
            params: storeItem,
            data: jsonData
        }).then(response => {
            assert.strictEqual(response.data.xid, xid);
            return response.data;
        });
    };

    const deleteJsonStoreItem = (xid, path) => {
        let url = '/rest/v2/json-data/' + encodeURIComponent(xid);
        if (path) {
            url += '/' + encodeURIComponent(path.join('.'));
        }

        return client.restRequest({
            path: url,
            method: 'DELETE'
        }).then(response => {
            return response.data;
        });
    };

    const getJsonStoreItem = (xid, path) => {
        let url = '/rest/v2/json-data/' + encodeURIComponent(xid);
        if (path) {
            url += '/' + encodeURIComponent(path.join('.'));
        }

        return client.restRequest({
            path: url,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.xid, xid);
            return response.data;
        });
    };

    const mergeJsonStoreItem = (storeItem, jsonData, path) => {
        let url = '/rest/v2/json-data/' + encodeURIComponent(storeItem.xid);
        if (path) {
            url += '/' + encodeURIComponent(path.join('.'));
        }

        return client.restRequest({
            path: url,
            method: 'PUT',
            params: {
                name: storeItem.name,
                publicData: storeItem.publicData,
                readPermission: storeItem.readPermission,
                editPermission: storeItem.editPermission,
            },
            data: jsonData
        }).then(response => {
            assert.strictEqual(response.data.xid, storeItem.xid);
            return response.data;
        });
    };

    const replaceJsonStoreItem = (storeItem, jsonData, path) => {
        let url = '/rest/v2/json-data/' + encodeURIComponent(storeItem.xid);
        if (path) {
            url += '/' + encodeURIComponent(path.join('.'));
        }

        return client.restRequest({
            path: url,
            method: 'POST',
            params: {
                name: storeItem.name,
                publicData: storeItem.publicData,
                readPermission: storeItem.readPermission,
                editPermission: storeItem.editPermission,
            },
            data: jsonData
        }).then(response => {
            assert.strictEqual(response.data.xid, storeItem.xid);
            return response.data;
        });
    };

    it('Can retrieve an object', () => {
        const myString = uuid();
        const data = {
            myString
        };

        return createJsonStoreItem(data).then((storeItem) => {
            return getJsonStoreItem(storeItem.xid);
        }).then((item) => {
            assert.strictEqual(item.jsonData.myString, myString);
            return deleteJsonStoreItem(item.xid);
        });
    });

    it('Can merge data into an object', () => {
        const string1 = uuid();
        const string2 = uuid();
        const data1 = {
            string1
        };
        const data2 = {
            string2
        };

        return createJsonStoreItem(data1).then((storeItem) => {
            return mergeJsonStoreItem(storeItem, data2);
        }).then((item) => {
            assert.strictEqual(item.jsonData.string1, string1);
            assert.strictEqual(item.jsonData.string2, string2);
            return deleteJsonStoreItem(item.xid);
        });
    });

    it('Can appended data into an array', () => {
        const string1 = uuid();
        const string2 = uuid();
        const data = [string1];

        return createJsonStoreItem(data).then((storeItem) => {
            return mergeJsonStoreItem(storeItem, string2);
        }).then((item) => {
            assert.strictEqual(item.jsonData[0], string1);
            assert.strictEqual(item.jsonData[1], string2);
            return deleteJsonStoreItem(item.xid);
        });
    });

    it('Can get partial data', () => {
        const string1 = uuid();
        const string2 = uuid();

        const data = {
            myString: string1,
            data2: {
                myString: string2
            }
        };

        return createJsonStoreItem(data).then((storeItem) => {
            return getJsonStoreItem(storeItem.xid, ['data2']);
        }).then((item) => {
            assert.strictEqual(item.jsonData.myString, string2);
            return deleteJsonStoreItem(item.xid);
        });
    });

    it('Can delete partial data', () => {
        const string1 = uuid();
        const string2 = uuid();

        const data = {
            myString: string1,
            data2: {
                myString: string2
            }
        };

        return createJsonStoreItem(data).then((storeItem) => {
            return deleteJsonStoreItem(storeItem.xid, ['data2']);
        }).then((item) => {
            assert.notProperty(item.jsonData, 'data2');
            return deleteJsonStoreItem(item.xid);
        });
    });

    it('Can replace partial data', () => {
        const string1 = uuid();
        const string2 = uuid();
        const string3 = uuid();

        const data = {
            myString: string1,
            data2: {
                myString: string2
            }
        };

        return createJsonStoreItem(data).then((storeItem) => {
            return replaceJsonStoreItem(storeItem, string3, ['data2']);
        }).then((item) => {
            assert.strictEqual(item.jsonData, string3);
            return getJsonStoreItem(item.xid);
        }).then((item) => {
            assert.strictEqual(item.jsonData.data2, string3);
            return deleteJsonStoreItem(item.xid);
        });
    });
});
