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
const jsonQueryUrl = '/rest/v2/json/query';

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
        "m~n": 8,
        "testArray": [
            {name: 'nameA', value: 1, optional: null, permissions: ['read']},
            {name: 'nameB', value: 2, optional: false, permissions: []},
            {name: 'nameC', value: 3, optional: false, permissions: []}
        ]
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

    const queryJsonData = (xid, pointer, rql = '') => {
        const pointerEncoded = [''].concat(pointer).map(p => encodePointerComponent(p)).join('/');
        return client.restRequest({
            path: `${jsonQueryUrl}/${encodeURIComponent(xid)}${pointerEncoded}${rql}`,
            method: 'GET'
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

    describe('Query data', function() {
        it('Whole document', function() {
            return queryJsonData(this.test.xid, []).then(data => {
                assert.strictEqual(data.total, Object.keys(this.test.item.jsonData).length);
                assert.deepEqual(data.items, Object.values(this.test.item.jsonData));
            });
        });

        it('/foo', function() {
            return queryJsonData(this.test.xid, ['foo']).then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.foo.length);
                assert.deepEqual(data.items, this.test.item.jsonData.foo);
            });
        });

        it('/testArray', function() {
            return queryJsonData(this.test.xid, ['testArray']).then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray);
            });
        });

        it('/testArray?sort(name)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?sort(name)').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray);
            });
        });

        it('/testArray?sort(-name)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?sort(-name)').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.reverse());
            });
        });

        it('/testArray?limit(1)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?limit(1)').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 1));
            });
        });

        it('/testArray?limit(1,1)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?limit(1,1)').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(1, 2));
            });
        });

        it('/testArray?limit(0)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?limit(0)').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.isArray(data.items);
                assert.strictEqual(data.items.length, 0);
            });
        });

        it('/testArray?name=match=name%3F (single-character wildcard)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?name=match=name%3F').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray);
            });
        });

        it('/testArray?name=match=n*A (multi-character wildcard)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?name=match=n*A').then(data => {
                assert.strictEqual(data.total, 1);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 1));
            });
        });

        it('/testArray?name=contains=A', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?name=contains=A').then(data => {
                assert.strictEqual(data.total, 1);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 1));
            });
        });

        it('/testArray?permissions=contains=read', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?permissions=contains=read').then(data => {
                assert.strictEqual(data.total, 1);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 1));
            });
        });

        it('/testArray?not(name=nameA)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?not(name=nameA)').then(data => {
                assert.strictEqual(data.total, 2);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(1, 3));
            });
        });

        it('/testArray?in(name,nameA,nameB)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?in(name,nameA,nameB)').then(data => {
                assert.strictEqual(data.total, 2);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 2));
            });
        });

        it('/testArray?in(name,(nameA,nameB)) (2nd arg is array)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?in(name,(nameA,nameB))').then(data => {
                assert.strictEqual(data.total, 2);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 2));
            });
        });

        it('/testArray?optional=null', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?optional=null').then(data => {
                assert.strictEqual(data.total, 1);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 1));
            });
        });

        it('/testArray?not(optional=null)', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?not(optional=null)').then(data => {
                assert.strictEqual(data.total, 2);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(1, 3));
            });
        });

        it('/testArray?value>1', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?value>1').then(data => {
                assert.strictEqual(data.total, 2);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(1, 3));
            });
        });

        it('/testArray?value>=1', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?value>=1').then(data => {
                assert.strictEqual(data.total, this.test.item.jsonData.testArray.length);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray);
            });
        });

        it('/testArray?value<1', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?value<1').then(data => {
                assert.strictEqual(data.total, 0);
                assert.isArray(data.items);
            });
        });

        it('/testArray?value<=1', function() {
            return queryJsonData(this.test.xid, ['testArray'], '?value<=1').then(data => {
                assert.strictEqual(data.total, 1);
                assert.deepEqual(data.items, this.test.item.jsonData.testArray.slice(0, 1));
            });
        });
    });
});
