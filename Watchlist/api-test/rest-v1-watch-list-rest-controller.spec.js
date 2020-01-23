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

const {createClient, addLoginHook, itWithData, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

const validate = {
    WatchListQueryResult() {
        // MODEL: WatchListQueryResult
        assert.isObject(response.data, 'data');
        assert.isArray(response.data.items, 'data.items');
        response.data.items.forEach((item, index) => {
            this.WatchListSummary(item);
        });
        assert.isNumber(response.data.total, 'data.total');
        // END MODEL: WatchListQueryResult
    },
    
    WatchListSummary(item, path) {
        // MODEL: WatchListSummaryModel
        assert.isObject(item, 'data.items[]');
        assert.isObject(item.data, 'data.items[].data');
        assert.isString(item.editPermission, 'data.items[].editPermission');
        if (item.folderIds != null) {
            assert.isArray(item.folderIds, 'data.items[].folderIds');
            item.folderIds.forEach((item, index) => {
                assert.isNumber(item, 'data.items[].folderIds[]');
            });
        }
        // DESCRIPTION: ID of object in database
        assert.isNumber(item.id, 'data.items[].id');
        // DESCRIPTION: Model Type Definition
        assert.isString(item.modelType, 'data.items[].modelType');
        // DESCRIPTION: Name of object
        assert.isString(item.name, 'data.items[].name');
        if (item.params != null) {
            assert.isArray(item.params, 'data.items[].params');
            item.params.forEach((item, index) => {
                this.WatchListParameter(item);
            });
        }
        if (item.query != null) {
            assert.isString(item.query, 'data.items[].query');
        }
        assert.isString(item.readPermission, 'data.items[].readPermission');
        assert.isString(item.type, 'data.items[].type');
        assert.isString(item.username, 'data.items[].username');
        // DESCRIPTION: Messages for validation of data
        assert.isArray(item.validationMessages, 'data.items[].validationMessages');
        item.validationMessages.forEach((item, index) => {
            this.RestValidationMessage(item);
        });
        // DESCRIPTION: XID of object
        assert.isString(item.xid, 'data.items[].xid');
        // END MODEL: WatchListSummaryModel
    },
    
    WatchListParameter(item) {
        // MODEL: WatchListParameter
        assert.isObject(item, 'data.items[].params[]');
        assert.isString(item.label, 'data.items[].params[].label');
        assert.isString(item.name, 'data.items[].params[].name');
        assert.isObject(item.options, 'data.items[].params[].options');
        assert.isString(item.type, 'data.items[].params[].type');
        // END MODEL: WatchListParameter
    },
    
    RestValidationMessage(item) {
        // MODEL: RestValidationMessage
        assert.isObject(item, 'data.items[].validationMessages[]');
        assert.isString(item.level, 'data.items[].validationMessages[].level');
        assert.include(["INFORMATION","WARNING","ERROR"], item.level, 'data.items[].validationMessages[].level');
        assert.isString(item.message, 'data.items[].validationMessages[].message');
        assert.isString(item.property, 'data.items[].validationMessages[].property');
        // END MODEL: RestValidationMessage
    }
};

// Mango REST V1 API - Watch List Rest Controller
describe('watch-list-rest-controller', function() {
    addLoginHook(client);

    beforeEach('Create watchlist', function() {
        const data = this.currentTest.data || {};
        this.currentTest.xid = uuid();
        
        this.currentTest.expectedResult = Object.assign({
            xid: this.currentTest.xid,
            name: this.currentTest.xid,
            editPermission: '',
            readPermission: '',
            type: 'query',
            folderIds: null,
            params: [],
            query: 'limit(1)',
            data: {}
        }, data.createData);
        
        if (!data.skipCreate) {
            return client.restRequest({
                method: 'POST',
                path: '/rest/v1/watch-lists',
                data: this.currentTest.expectedResult
            });
        }
    });
    
    afterEach('Verify expected result', function() {
        if (this.currentTest.expectedResult && this.currentTest.result) {
            for (let [key, value] of Object.entries(this.currentTest.expectedResult)) {
                assert.deepEqual(this.currentTest.result[key], value);
            }
        }
    });

    afterEach('Delete watchlist', function() {
        if (this.currentTest.xid) {
            return client.restRequest({
                method: 'DELETE',
                path: `/rest/v1/watch-lists/${this.currentTest.xid}`,
            }).catch(noop);
        }
    });

    // Query WatchLists - 
    it('GET /rest/v1/watch-lists', function() {
        const params = {
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/v1/watch-lists`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            validate.WatchListQueryResult();
        });
    });

    // Create New WatchList - 
    itWithData('POST /rest/v1/watch-lists', {skipCreate: true}, function() {
        const requestBody = {
            xid: this.test.xid,
            name: this.test.xid,
            editPermission: '',
            readPermission: '',
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
            path: `/rest/v1/watch-lists`,
            data: requestBody
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            // MODEL: WatchListModel
            assert.isObject(response.data, 'data');
            assert.isObject(response.data.data, 'data.data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            assert.isArray(response.data.folderIds, 'data.folderIds');
            response.data.folderIds.forEach((item, index) => {
                assert.isNumber(item, 'data.folderIds[]');
            });
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isArray(response.data.params, 'data.params');
            response.data.params.forEach((item, index) => {
                // MODEL: WatchListParameter
                assert.isObject(item, 'data.params[]');
                assert.isString(item.label, 'data.params[].label');
                assert.isString(item.name, 'data.params[].name');
                assert.isObject(item.options, 'data.params[].options');
                assert.isString(item.type, 'data.params[].type');
                // END MODEL: WatchListParameter
            });
            assert.isArray(response.data.points, 'data.points');
            response.data.points.forEach((item, index) => {
                // MODEL: WatchListDataPointModel
                assert.isObject(item, 'data.points[]');
                assert.isString(item.deviceName, 'data.points[].deviceName');
                assert.isString(item.name, 'data.points[].name');
                assert.isNumber(item.pointFolderId, 'data.points[].pointFolderId');
                assert.isString(item.readPermission, 'data.points[].readPermission');
                assert.isString(item.setPermission, 'data.points[].setPermission');
                assert.isString(item.xid, 'data.points[].xid');
                // END MODEL: WatchListDataPointModel
            });
            assert.isString(response.data.query, 'data.query');
            assert.isString(response.data.readPermission, 'data.readPermission');
            assert.isString(response.data.type, 'data.type');
            assert.isString(response.data.username, 'data.username');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            response.data.validationMessages.forEach((item, index) => {
                // MODEL: RestValidationMessage
                assert.isObject(item, 'data.validationMessages[]');
                assert.isString(item.level, 'data.validationMessages[].level');
                assert.include(["INFORMATION","WARNING","ERROR"], item.level, 'data.validationMessages[].level');
                assert.isString(item.message, 'data.validationMessages[].message');
                assert.isString(item.property, 'data.validationMessages[].property');
                // END MODEL: RestValidationMessage
            });
            // DESCRIPTION: XID of object
            assert.isString(response.data.xid, 'data.xid');
            // END MODEL: WatchListModel
            this.test.result = response.data;
        });
    });

    // Get a Watchlist - 
    it('GET /rest/v1/watch-lists/{xid}', function() {
        const params = {
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/v1/watch-lists/${params.xid}`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: WatchListModel
            assert.isObject(response.data, 'data');
            assert.isObject(response.data.data, 'data.data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            assert.isArray(response.data.folderIds, 'data.folderIds');
            response.data.folderIds.forEach((item, index) => {
                assert.isNumber(item, 'data.folderIds[]');
            });
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isArray(response.data.params, 'data.params');
            response.data.params.forEach((item, index) => {
                // MODEL: WatchListParameter
                assert.isObject(item, 'data.params[]');
                assert.isString(item.label, 'data.params[].label');
                assert.isString(item.name, 'data.params[].name');
                assert.isObject(item.options, 'data.params[].options');
                assert.isString(item.type, 'data.params[].type');
                // END MODEL: WatchListParameter
            });
            assert.isArray(response.data.points, 'data.points');
            response.data.points.forEach((item, index) => {
                // MODEL: WatchListDataPointModel
                assert.isObject(item, 'data.points[]');
                assert.isString(item.deviceName, 'data.points[].deviceName');
                assert.isString(item.name, 'data.points[].name');
                assert.isNumber(item.pointFolderId, 'data.points[].pointFolderId');
                assert.isString(item.readPermission, 'data.points[].readPermission');
                assert.isString(item.setPermission, 'data.points[].setPermission');
                assert.isString(item.xid, 'data.points[].xid');
                // END MODEL: WatchListDataPointModel
            });
            assert.isString(response.data.query, 'data.query');
            assert.isString(response.data.readPermission, 'data.readPermission');
            assert.isString(response.data.type, 'data.type');
            assert.isString(response.data.username, 'data.username');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            response.data.validationMessages.forEach((item, index) => {
                // MODEL: RestValidationMessage
                assert.isObject(item, 'data.validationMessages[]');
                assert.isString(item.level, 'data.validationMessages[].level');
                assert.include(["INFORMATION","WARNING","ERROR"], item.level, 'data.validationMessages[].level');
                assert.isString(item.message, 'data.validationMessages[].message');
                assert.isString(item.property, 'data.validationMessages[].property');
                // END MODEL: RestValidationMessage
            });
            // DESCRIPTION: XID of object
            assert.isString(response.data.xid, 'data.xid');
            // END MODEL: WatchListModel
            this.test.result = response.data;
        });
    });

    // Update a WatchList - 
    it('PUT /rest/v1/watch-lists/{xid}', function() {
        const requestBody =
        { // title: WatchListModel
            data: { // title: undefined
            },
            editPermission: 'string',
            folderIds: [
                0
            ],
            id: 0,
            modelType: 'string',
            name: 'string',
            params: [
                { // title: WatchListParameter
                    label: 'string',
                    name: 'string',
                    options: { // title: undefined
                    },
                    type: 'string'
                }
            ],
            points: [
                { // title: WatchListDataPointModel
                    deviceName: 'string',
                    name: 'string',
                    pointFolderId: 0,
                    readPermission: 'string',
                    setPermission: 'string',
                    xid: 'string'
                }
            ],
            query: 'string',
            readPermission: 'string',
            type: 'string',
            username: 'string',
            validationMessages: [
                { // title: RestValidationMessage
                    level: 'INFORMATION',
                    message: 'string',
                    property: 'string'
                }
            ],
            xid: 'string'
        };
        const params = {
            model: requestBody, // in = body, description = model, required = true, type = , default = , enum = 
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'PUT',
            path: `/rest/v1/watch-lists/${params.xid}`,
            data: requestBody
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: WatchListModel
            assert.isObject(response.data, 'data');
            assert.isObject(response.data.data, 'data.data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            assert.isArray(response.data.folderIds, 'data.folderIds');
            response.data.folderIds.forEach((item, index) => {
                assert.isNumber(item, 'data.folderIds[]');
            });
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isArray(response.data.params, 'data.params');
            response.data.params.forEach((item, index) => {
                // MODEL: WatchListParameter
                assert.isObject(item, 'data.params[]');
                assert.isString(item.label, 'data.params[].label');
                assert.isString(item.name, 'data.params[].name');
                assert.isObject(item.options, 'data.params[].options');
                assert.isString(item.type, 'data.params[].type');
                // END MODEL: WatchListParameter
            });
            assert.isArray(response.data.points, 'data.points');
            response.data.points.forEach((item, index) => {
                // MODEL: WatchListDataPointModel
                assert.isObject(item, 'data.points[]');
                assert.isString(item.deviceName, 'data.points[].deviceName');
                assert.isString(item.name, 'data.points[].name');
                assert.isNumber(item.pointFolderId, 'data.points[].pointFolderId');
                assert.isString(item.readPermission, 'data.points[].readPermission');
                assert.isString(item.setPermission, 'data.points[].setPermission');
                assert.isString(item.xid, 'data.points[].xid');
                // END MODEL: WatchListDataPointModel
            });
            assert.isString(response.data.query, 'data.query');
            assert.isString(response.data.readPermission, 'data.readPermission');
            assert.isString(response.data.type, 'data.type');
            assert.isString(response.data.username, 'data.username');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            response.data.validationMessages.forEach((item, index) => {
                // MODEL: RestValidationMessage
                assert.isObject(item, 'data.validationMessages[]');
                assert.isString(item.level, 'data.validationMessages[].level');
                assert.include(["INFORMATION","WARNING","ERROR"], item.level, 'data.validationMessages[].level');
                assert.isString(item.message, 'data.validationMessages[].message');
                assert.isString(item.property, 'data.validationMessages[].property');
                // END MODEL: RestValidationMessage
            });
            // DESCRIPTION: XID of object
            assert.isString(response.data.xid, 'data.xid');
            // END MODEL: WatchListModel
            this.test.result = response.data;
        });
    });

    // Delete a WatchList  - Only the owner or an admin can delete
    it('DELETE /rest/v1/watch-lists/{xid}', function() {
        const params = {
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'DELETE',
            path: `/rest/v1/watch-lists/${params.xid}`,
        }).then(response => {
            // No content
            assert.strictEqual(response.status, 204);
        });
    });

    it('GET /rest/v1/watch-lists/{xid}/data-points', function() {
        const params = {
            xid: this.test.xid // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/v1/watch-lists/${params.xid}/data-points`,
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data, 'data');
            assert.isArray(response.data.items, 'data.items');
            assert.isNumber(response.data.total, 'data.total');
            response.data.items.forEach((item, index) => {
                assert.isObject(item, 'data.items[]');
                assert.isString(item.xid, 'data.items[].xid');
            });
        });
    });

});
