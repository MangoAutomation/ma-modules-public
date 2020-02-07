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

const {createClient, addLoginHook, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

const jsonDataUrl = '/rest/v2/json-data';
const jsonUrl = '/rest/v2/json/data';

/**
 * Test data taken from JSON pointer spec - https://tools.ietf.org/html/rfc6901
 */

describe('JSON data', function() {
    addLoginHook(client);
    
    const testData = {
        "foo": [
            "bar",
            "baz"
        ],
        "": 0,
        "a/b": 1,
        "c%d": 2,
        "e^f": 3,
        "g|h": 4,
        "i\\j": 5,
        "k\"l": 6,
        " ": 7,
        "m~n": 8
    };
    
    const encodePointerComponent = c => {
        c = c.replace(/~/g, '~0');
        c = c.replace(/\//g, '~1');
        return encodeURIComponent(c);
    };
    
    beforeEach('Create test item', function() {
        this.currentTest.xid = uuid();
        const storeItem = {
            name: 'test json store',
            publicData: false,
            readPermission: '',
            editPermission: ''
        };

        return client.restRequest({
            path: `${jsonDataUrl}/${encodeURIComponent(this.currentTest.xid)}`,
            method: 'POST',
            params: storeItem,
            data: testData
        }).then(response => {
            this.currentTest.item = response.data;
        });
    });
    
    afterEach('Delete test item', function() {
        return client.restRequest({
            path: `${jsonDataUrl}/${encodeURIComponent(this.currentTest.xid)}`,
            method: 'DELETE'
        });
    });

    const getJsonData = (xid, pointer) => {
        const pointerEncoded = [''].concat(pointer).map(p => encodePointerComponent(p)).join('/');
        return client.restRequest({
            path: `${jsonUrl}/${encodeURIComponent(xid)}${pointerEncoded}`,
            method: 'GET'
        }).then(response => {
            return response.data;
        });
    };

    const setJsonData = (xid, pointer, data) => {
        const pointerEncoded = [''].concat(pointer).map(p => encodePointerComponent(p)).join('/');
        return client.restRequest({
            path: `${jsonUrl}/${encodeURIComponent(xid)}${pointerEncoded}`,
            method: 'POST',
            data
        }).then(response => {
            return response.data;
        });
    };

    const deleteJsonData = (xid, pointer) => {
        const pointerEncoded = [''].concat(pointer).map(p => encodePointerComponent(p)).join('/');
        return client.restRequest({
            path: `${jsonUrl}/${encodeURIComponent(xid)}${pointerEncoded}`,
            method: 'DELETE'
        }).then(response => {
            return response.data;
        });
    };

    describe('Get data', function() {
        it('Whole document', function() {
            return getJsonData(this.test.xid, []).then(data => {
                assert.deepEqual(data, this.test.item.jsonData);
            });
        });
    
        it('/foo/0', function() {
            return getJsonData(this.test.xid, ['foo', '0']).then(data => {
                assert.deepEqual(data, this.test.item.jsonData.foo[0]);
            });
        });
        
        for (let key of Object.keys(testData)) {
            it(`/${encodePointerComponent(key)}`, function() {
                return getJsonData(this.test.xid, [key]).then(data => {
                    assert.deepEqual(data, this.test.item.jsonData[key]);
                });
            });
        }
    });

    describe('Set data', function() {
        it('Whole document', function() {
            const newValue = uuid();
            return setJsonData(this.test.xid, [], newValue).then(data => {
                return getJsonData(this.test.xid, []).then(data => {
                    assert.strictEqual(data, newValue);
                });
            });
        });
    
        it('/foo/0', function() {
            const newValue = uuid();
            return setJsonData(this.test.xid, ['foo', '0'], newValue).then(data => {
                return getJsonData(this.test.xid, ['foo', '0']).then(data => {
                    assert.strictEqual(data, newValue);
                });
            });
        });
        
        for (let key of Object.keys(testData)) {
            it(`/${encodePointerComponent(key)}`, function() {
                const newValue = uuid();
                return setJsonData(this.test.xid, [key], newValue).then(data => {
                    return getJsonData(this.test.xid, [key]).then(data => {
                        assert.strictEqual(data, newValue);
                    });
                });
            });
        }
    });
    
    describe('Delete data', function() {
        it('Whole document', function() {
            return deleteJsonData(this.test.xid, []).then(data => {
                return getJsonData(this.test.xid, []).then(data => {
                    assert.fail('Should be 404');
                }, error => {
                    assert.strictEqual(error.status, 404);
                });
            });
        });

        it('/foo/0', function() {
            return deleteJsonData(this.test.xid, ['foo', '0']).then(data => {
                return getJsonData(this.test.xid, ['foo', '0']).then(data => {
                    // array item 1 has shifted into index 0
                    assert.strictEqual(data, this.test.item.jsonData.foo[1]);
                });
            });
        });
        
        for (let key of Object.keys(testData)) {
            it(`/${encodePointerComponent(key)}`, function() {
                return deleteJsonData(this.test.xid, [key]).then(data => {
                    return getJsonData(this.test.xid, [key]).then(data => {
                        assert.fail('Should be 404');
                    }, error => {
                        assert.strictEqual(error.status, 404);
                    });
                });
            });
        }
    });

});
