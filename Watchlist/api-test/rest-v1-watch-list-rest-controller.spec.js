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
        assert.isString(item.editPermission, path + '.editPermission');
        if (item.folderIds != null) {
            assert.isArray(item.folderIds, path + '.folderIds');
            item.folderIds.forEach((item, index) => {
                assert.isNumber(item, path + '.folderIds' + `[${index}]`);
            });
        }
        // DESCRIPTION: ID of object in database
        assert.isNumber(item.id, path + '.id');
        // DESCRIPTION: Model Type Definition
        assert.isString(item.modelType, path + '.modelType');
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
        assert.isString(item.readPermission, path + '.readPermission');
        assert.isString(item.type, path + '.type');
        if (item.username) {
            assert.isString(item.username, path + '.username');
        }
        // DESCRIPTION: Messages for validation of data
        assert.isArray(item.validationMessages, path + '.validationMessages');
        item.validationMessages.forEach((item, index) => {
            this['RestValidationMessage'](item, path + '.validationMessages' + `[${index}]`);
        });
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
        assert.isObject(item, path);
        assert.isObject(item.data, path + '.data');
        assert.isString(item.editPermission, path + '.editPermission');
        if (item.folderIds != null) {
            assert.isArray(item.folderIds, path + '.folderIds');
            item.folderIds.forEach((item, index) => {
                assert.isNumber(item, path + '.folderIds' + `[${index}]`);
            });
        }
        // DESCRIPTION: ID of object in database
        assert.isNumber(item.id, path + '.id');
        // DESCRIPTION: Model Type Definition
        assert.isString(item.modelType, path + '.modelType');
        // DESCRIPTION: Name of object
        assert.isString(item.name, path + '.name');
        assert.isArray(item.params, path + '.params');
        item.params.forEach((item, index) => {
            this['WatchListParameter'](item, path + '.params' + `[${index}]`);
        });
        assert.isArray(item.points, path + '.points');
        item.points.forEach((item, index) => {
            this['WatchListDataPointModel'](item, path + '.points' + `[${index}]`);
        });
        assert.isString(item.query, path + '.query');
        assert.isString(item.readPermission, path + '.readPermission');
        assert.isString(item.type, path + '.type');
        if (item.username) {
            assert.isString(item.username, path + '.username');
        }
        // DESCRIPTION: Messages for validation of data
        assert.isArray(item.validationMessages, path + '.validationMessages');
        item.validationMessages.forEach((item, index) => {
            this['RestValidationMessage'](item, path + '.validationMessages' + `[${index}]`);
        });
        // DESCRIPTION: XID of object
        assert.isString(item.xid, path + '.xid');
    },
    'WatchListDataPointModel': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.deviceName, path + '.deviceName');
        assert.isString(item.name, path + '.name');
        assert.isNumber(item.pointFolderId, path + '.pointFolderId');
        assert.isString(item.readPermission, path + '.readPermission');
        assert.isString(item.setPermission, path + '.setPermission');
        assert.isString(item.xid, path + '.xid');
    },
    'TableModel': function(item, path) {
        assert.isObject(item, path);
        assert.isArray(item.attributes, path + '.attributes');
        item.attributes.forEach((item, index) => {
            this['QueryAttribute'](item, path + '.attributes' + `[${index}]`);
        });
        assert.isString(item.tableName, path + '.tableName');
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
            //this['DataPointModel'](item, path + '.items' + `[${index}]`);
        });
        assert.isNumber(item.total, path + '.total');
    },
    'DataPointModel': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.chartColour, path + '.chartColour');
        this['BaseChartRendererModel«object»'](item.chartRenderer, path + '.chartRenderer');
        assert.isArray(item.dataSourceEditRoles, path + '.dataSourceEditRoles');
        item.dataSourceEditRoles.forEach((item, index) => {
            assert.isString(item, path + '.dataSourceEditRoles' + `[${index}]`);
        });
        assert.isNumber(item.dataSourceId, path + '.dataSourceId');
        assert.isString(item.dataSourceName, path + '.dataSourceName');
        assert.isString(item.dataSourceTypeName, path + '.dataSourceTypeName');
        assert.isString(item.dataSourceXid, path + '.dataSourceXid');
        assert.isString(item.deviceName, path + '.deviceName');
        assert.isBoolean(item.enabled, path + '.enabled');
        assert.isNumber(item.id, path + '.id');
        assert.isString(item.integralUnit, path + '.integralUnit');
        this['LoggingPropertiesModel'](item.loggingProperties, path + '.loggingProperties');
        assert.isString(item.name, path + '.name');
        assert.isString(item.plotType, path + '.plotType');
        assert.isNumber(item.pointFolderId, path + '.pointFolderId');
        this['PointLocatorModel«object»'](item.pointLocator, path + '.pointLocator');
        assert.isBoolean(item.preventSetExtremeValues, path + '.preventSetExtremeValues');
        assert.isBoolean(item.purgeOverride, path + '.purgeOverride');
        this['TimePeriodModel'](item.purgePeriod, path + '.purgePeriod');
        assert.isString(item.readPermission, path + '.readPermission');
        assert.isString(item.renderedUnit, path + '.renderedUnit');
        assert.isString(item.rollup, path + '.rollup');
        assert.isNumber(item.setExtremeHighLimit, path + '.setExtremeHighLimit');
        assert.isNumber(item.setExtremeLowLimit, path + '.setExtremeLowLimit');
        assert.isString(item.setPermission, path + '.setPermission');
        assert.isNumber(item.simplifyTarget, path + '.simplifyTarget');
        assert.isNumber(item.simplifyTolerance, path + '.simplifyTolerance');
        assert.isString(item.simplifyType, path + '.simplifyType');
        assert.isObject(item.tags, path + '.tags');
        assert.isString(item.templateName, path + '.templateName');
        assert.isString(item.templateXid, path + '.templateXid');
        this['BaseTextRendererModel«object»'](item.textRenderer, path + '.textRenderer');
        assert.isString(item.unit, path + '.unit');
        assert.isBoolean(item.useIntegralUnit, path + '.useIntegralUnit');
        assert.isBoolean(item.useRenderedUnit, path + '.useRenderedUnit');
        assert.isString(item.xid, path + '.xid');
    },
    'BaseChartRendererModel«object»': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.type, path + '.type');
    },
    'LoggingPropertiesModel': function(item, path) {
        assert.isObject(item, path);
        assert.isNumber(item.cacheSize, path + '.cacheSize');
        assert.isBoolean(item.discardExtremeValues, path + '.discardExtremeValues');
        assert.isNumber(item.discardHighLimit, path + '.discardHighLimit');
        assert.isNumber(item.discardLowLimit, path + '.discardLowLimit');
        this['TimePeriodModel'](item.intervalLoggingPeriod, path + '.intervalLoggingPeriod');
        assert.isNumber(item.intervalLoggingSampleWindowSize, path + '.intervalLoggingSampleWindowSize');
        assert.isString(item.intervalLoggingType, path + '.intervalLoggingType');
        assert.isString(item.loggingType, path + '.loggingType');
        assert.isBoolean(item.overrideIntervalLoggingSamples, path + '.overrideIntervalLoggingSamples');
        assert.isNumber(item.tolerance, path + '.tolerance');
    },
    'TimePeriodModel': function(item, path) {
        assert.isObject(item, path);
        assert.isNumber(item.periods, path + '.periods');
        assert.isString(item.type, path + '.type');
    },
    'PointLocatorModel«object»': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.dataType, path + '.dataType');
        assert.isString(item.modelType, path + '.modelType');
        assert.isBoolean(item.relinquishable, path + '.relinquishable');
        assert.isBoolean(item.settable, path + '.settable');
    },
    'BaseTextRendererModel«object»': function(item, path) {
        assert.isObject(item, path);
        assert.isString(item.type, path + '.type');
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
            validateSchema['WatchListQueryResult'](response.data, 'data');
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
            validateSchema['WatchListModel'](response.data, 'data');
        });
    });

    // Get Explaination For Query - What is Query-able on this model
    it('GET /rest/v1/watch-lists/explain-query', function() {
        const params = {
        };
        
        return client.restRequest({
            method: 'GET',
            path: `/rest/v1/watch-lists/explain-query`,
        }).then(response => {
            // Ok
            assert.strictEqual(response.status, 200);
            validateSchema['TableModel'](response.data, 'data');
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
            validateSchema['WatchListModel'](response.data, 'data');
        });
    });

    // Update a WatchList - 
    it('PUT /rest/v1/watch-lists/{xid}', function() {
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
            validateSchema['WatchListModel'](response.data, 'data');
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
            // No Content
            assert.strictEqual(response.status, 204);
        });
    });

    // Get Data Points for a Watchlist - 
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
            validateSchema['WatchListPointsResult'](response.data, 'data');
        });
    });

});
