/**
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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

const {createClient, addLoginHook, assertValidationErrors, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

const validateSchema = {
    'WatchListQueryResult': function(item, path) {
        assert.isObject(item, path);
        assert.isArray(item.items, path + '.items');
        item.items.forEach((item, index) => {
            this['WatchListSummaryModel'](item, path + '.items' + `[${index}]`);
        });
        assert.isNumber(item.total, path + '.total');
    },
    'WatchListSummaryModel': function(item, path) {
        assert.isObject(item, path);
        assert.isObject(item.data, path + '.data');
        assert.isArray(item.editPermission, path + '.editPermission');
        if (item.folderIds != null) {
            assert.isArray(item.folderIds, path + '.folderIds');
            item.folderIds.forEach((item, index) => {
                assert.isNumber(item, path + '.folderIds' + `[${index}]`);
            });
        }
        // DESCRIPTION: ID of object in database
        assert.isNumber(item.id, path + '.id');
        // DESCRIPTION: Name of object
        assert.isString(item.name, path + '.name');
        if (item.params) {
            assert.isArray(item.params, path + '.params');
            item.params.forEach((item, index) => {
                this['WatchListParameter'](item, path + '.params' + `[${index}]`);
            });
        }
        if (item.query) {
            assert.isString(item.query, path + '.query');
        }
        assert.isArray(item.readPermission, path + '.readPermission');
        assert.isString(item.type, path + '.type');
        if (item.username) {
            assert.isString(item.username, path + '.username');
        }
        // DESCRIPTION: XID of object
        assert.isString(item.xid, path + '.xid');
    },
    'WatchListParameter': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.label, path + '.label');
        assert.isString(item.name, path + '.name');
        assert.isObject(item.options, path + '.options');
        assert.isString(item.type, path + '.type');
    },
    'RestValidationMessage': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.level, path + '.level');
        assert.include(["INFORMATION","WARNING","ERROR"], item.level, path + '.level');
        assert.isString(item.message, path + '.message');
        assert.isString(item.property, path + '.property');
    },
    'WatchListModel': function(item, path) {
        this['WatchListSummaryModel'](item, path);
        if (Array.isArray(item.points)) {
            item.points.forEach((item, index) => {
                this['WatchListDataPointModel'](item, path + '.points' + `[${index}]`);
            });
        }
    },
    'WatchListDataPointModel': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.deviceName, path + '.deviceName');
        assert.isString(item.name, path + '.name');
        assert.isArray(item.readPermission, path + '.readPermission');
        assert.isArray(item.setPermission, path + '.setPermission');
        assert.isString(item.xid, path + '.xid');
    },
    'QueryAttribute': function(item, path) {
        assert.isObject(item, path);
        assert.isArray(item.aliases, path + '.aliases');
        item.aliases.forEach((item, index) => {
            assert.isString(item, path + '.aliases' + `[${index}]`);
        });
        assert.isString(item.columnName, path + '.columnName');
        assert.isString(item.sqlType, path + '.sqlType');
    },
    'WatchListPointsResult': function(item, path) {
        assert.isObject(item, path);
        assert.isArray(item.items, path + '.items');
        item.items.forEach((item, index) => {
            assert.isObject(item, path + '.items' + `[${index}]`);
        });
        assert.isNumber(item.total, path + '.total');
    }
};

// Mango REST V1 API - Watch List Rest Controller
describe('watch-list-rest-controller', function() {
    addLoginHook(client);

    beforeEach('Create watchlist', function() {
        if (!this.currentTest.hasOwnProperty('createObject')) {
            this.currentTest.createObject = {
                name: 'Test watchlist',
                editPermission: ['superadmin'],
                readPermission: ['user'],
                type: 'query',
                params: [],
                query: 'limit(1)',
                data: {}
            };
        }

        if (this.currentTest.createObject) {
            this.currentTest.expectedResult = this.currentTest.createObject;
            
            return client.restRequest({
                method: 'POST',
                path: '/rest/latest/watch-lists',
                data: this.currentTest.createObject
            }).then((response) => {
                this.currentTest.savedObject = response.data;
                this.currentTest.xid = response.data.xid;
            }, error => {
                assertValidationErrors([''], error);
            });
        }
    });

    afterEach('Verify expected result', function() {
        if (this.currentTest.expectedResult && this.currentTest.actualResult) {
            for (let [key, value] of Object.entries(this.currentTest.expectedResult)) {
                assert.deepEqual(this.currentTest.actualResult[key], value);
            }
        }
    });

    afterEach('Delete watchlist', function() {
        if (this.currentTest.xid) {
            return client.restRequest({
                method: 'DELETE',
                path: `/rest/latest/watch-lists/${this.currentTest.xid}`,
            }).catch(noop);
        }
    });

    // Query WatchLists - 
    it('GET /rest/latest/watch-lists', function() {
        const params = {
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/watch-lists?xid=${encodeURIComponent(this.test.xid)}`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            validateSchema['WatchListQueryResult'](response.data, 'data');

            assert.strictEqual(response.data.items.length, 1);
            assert.strictEqual(response.data.total, 1);
            
            // actualResult is verified against expectedResult in afterEach hook
            this.test.actualResult = response.data.items[0];
        });
    });

    // Create New WatchList - 
    it('POST /rest/latest/watch-lists', function() {
        this.test.xid = uuid();
        const requestBody = {
            xid: this.test.xid,
            name: 'Test watchlist',
            editPermission: ['superadmin'],
            readPermission: ['user'],
            type: 'query',
            folderIds: null,
            params: [],
            query: 'limit(1)',
            data: {}
        };
        const params = {
            model: requestBody // in = body, description = Watchlist to save, required = true, type = , default = , enum = 
        };
        
        return client.restRequest({
            method: 'POST',
            path: `/rest/latest/watch-lists`,
            data: requestBody
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            validateSchema['WatchListModel'](response.data, 'data');
            
            // actualResult is verified against expectedResult in afterEach hook
            this.test.actualResult = response.data;
        });
    }).createObject = false;

    // Get a Watchlist - 
    it('GET /rest/latest/watch-lists/{xid}', function() {
        const params = {
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/watch-lists/${params.xid}`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            validateSchema['WatchListModel'](response.data, 'data');
            
            // actualResult is verified against expectedResult in afterEach hook
            this.test.actualResult = response.data;
        });
    });

    // Update a WatchList - 
    it('PUT /rest/latest/watch-lists/{xid}', function() {
        const requestBody = this.test.expectedResult = {
            xid: this.test.xid,
            name: 'Test watchlist - renamed',
            editPermission: [],
            readPermission: [],
            type: 'query',
            params: [],
            query: 'limit(1)',
            data: {}
        };
        const params = {
            model: requestBody, // in = body, description = model, required = true, type = , default = , enum = 
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'PUT',
            path: `/rest/latest/watch-lists/${params.xid}`,
            data: requestBody
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            validateSchema['WatchListModel'](response.data, 'data');
            
            // actualResult is verified against expectedResult in afterEach hook
            this.test.actualResult = response.data;
        }, error => {
            assertValidationErrors([''], error);
        });
    });

    // Delete a WatchList  - Only the owner or an admin can delete
    it('DELETE /rest/latest/watch-lists/{xid}', function() {
        const params = {
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'DELETE',
            path: `/rest/latest/watch-lists/${params.xid}`,
        }).then(response => {
            // No Content
            assert.strictEqual(response.status, 200);
            
            // actualResult is verified against expectedResult in afterEach hook
            this.test.actualResult = response.data;
        });
    });

    // Get Data Points for a Watchlist - 
    it('GET /rest/latest/watch-lists/{xid}/data-points', function() {
        const params = {
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/latest/watch-lists/${params.xid}/data-points`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            validateSchema['WatchListPointsResult'](response.data, 'data');
        });
    });

});
