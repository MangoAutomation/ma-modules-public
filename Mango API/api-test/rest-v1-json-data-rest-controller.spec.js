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

const {createClient, login, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

// Mango REST V1 API - Json Data Rest Controller
describe('json-data-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    const testSetup = function(params, requestBody) {
        // common test setup, e.g. create a VO object
        return client.restRequest({
            method: 'POST',
            path: `/rest/v2/json-data/${params.xid}`,
            params: {
                editPermission: params.editPermission || [],
                name: params.name || 'test data',
                publicData: params.publicData,
                readPermission: params.readPermission || []
            },
            data: requestBody || {
                key1: 'value1',
                key2: 'value2',
                objectKey: {
                    prop1: true,
                    prop2: false
                },
                arrayKey: [1,2,3]
            }
        });
    };
    
    const testTeardown = function(params) {
        // common test teardown, e.g. delete a VO object
        return client.restRequest({
            method: 'DELETE',
            path: `/rest/v2/json-data/${params.xid}`,
        });
    };
    
    // List all available xids - Shows any xids that you have read permissions for
    it('GET /rest/v2/json-data', function() {
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v2/json-data`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            assert.isAbove(response.data.length, 0, 'data');
            response.data.forEach((item, index) => {
                assert.isString(item, 'data[]');
            });
        });
    });

    // Get Public JSON Data - Returns only the data
    it('GET /rest/v2/json-data/public/{xid}', function() {
        const params = {
            xid: uuid(), // in = path, description = XID, required = true, type = string, default = , enum = 
            publicData: true
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            const publicClient = createClient();
            return publicClient.restRequest({
                method: 'GET',
                path: `/rest/v2/json-data/public/${params.xid}`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData.key1, 'value1');
            assert.strictEqual(response.data.jsonData.key2, 'value2');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get JSON Data - Returns only the data
    it('GET /rest/v2/json-data/{xid}', function() {
        const params = {
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v2/json-data/${params.xid}`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData.key1, 'value1');
            assert.strictEqual(response.data.jsonData.key2, 'value2');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Create/replace JSON Data - 
    it('POST /rest/v2/json-data/{xid}', function() {
        const requestBody = {
            key1: 'value1'
        };
        const params = {
            data: requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/v2/json-data/${params.xid}`,
                params: {
                    editPermission: params.editPermission,
                    name: params.name,
                    publicData: params.publicData,
                    readPermission: params.readPermission
                },
                data: requestBody
            });
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData.key1, 'value1');
            assert.doesNotHaveAnyKeys(response.data.jsonData, ['value2', 'value3']);
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Append JSON Data to existing - 
    it('PUT /rest/v2/json-data/{xid}', function() {
        const requestBody = {
            key3: 'value3'
        };
        const params = {
            data: requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v2/json-data/${params.xid}`,
                params: {
                    editPermission: params.editPermission,
                    name: params.name,
                    publicData: params.publicData,
                    readPermission: params.readPermission
                },
                data: requestBody
            });
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData.key1, 'value1');
            assert.strictEqual(response.data.jsonData.key2, 'value2');
            assert.strictEqual(response.data.jsonData.key3, 'value3');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Append JSON Data to existing - append to arrays
    it('PUT /rest/v2/json-data/{xid} append to arrays', function() {
        const requestBody = 1;
        const params = {
            data: requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['user'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['user'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params, [0]);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v2/json-data/${params.xid}`,
                params: {
                    editPermission: params.editPermission,
                    name: params.name,
                    publicData: params.publicData,
                    readPermission: params.readPermission
                },
                data: requestBody
            });
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isArray(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData[0], 0);
            assert.strictEqual(response.data.jsonData[1], 1);
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Fully Delete JSON Data - 
    it('DELETE /rest/v2/json-data/{xid}', function() {
        const params = {
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'DELETE',
                path: `/rest/v2/json-data/${params.xid}`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get JSON Data using a path - To get a sub component of the data use a path of member.submember
    it('GET /rest/v2/json-data/{xid}/{path}', function() {
        const params = {
            path: 'objectKey', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v2/json-data/${params.xid}/${params.path}`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.isTrue(response.data.jsonData.prop1);
            assert.isFalse(response.data.jsonData.prop2);
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Replace JSON Data - {path} is the path to data with dots data.member.submember
    it('POST /rest/v2/json-data/{xid}/{path}', function() {
        const requestBody = {
            prop1: 1,
            prop2: 2
        };
        const params = {
            data: requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['user'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            path: 'objectKey', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['user'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/v2/json-data/${params.xid}/${params.path}`,
                params: {
                    editPermission: params.editPermission,
                    name: params.name,
                    publicData: params.publicData,
                    readPermission: params.readPermission
                },
                data: requestBody
            });
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData.prop1, 1);
            assert.strictEqual(response.data.jsonData.prop2, 2);
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Append JSON Data to existing - {path} is the path to data with dots data.member.submember
    it('PUT /rest/v2/json-data/{xid}/{path}', function() {
        const requestBody = {
            prop3: 123
        };
        const params = {
            data: requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            path: 'objectKey', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v2/json-data/${params.xid}/${params.path}`,
                params: {
                    editPermission: params.editPermission,
                    name: params.name,
                    publicData: params.publicData,
                    readPermission: params.readPermission
                },
                data: requestBody
            });
        }).then(response => {
            // Created
            assert.strictEqual(response.status, 201);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.isTrue(response.data.jsonData.prop1);
            assert.isFalse(response.data.jsonData.prop2);
            assert.strictEqual(response.data.jsonData.prop3, 123);
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Partially Delete JSON Data - {path} is the path to data with dots data.member.submember
    it('DELETE /rest/v2/json-data/{xid}/{path}', function() {
        const params = {
            path: 'objectKey', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'DELETE',
                path: `/rest/v2/json-data/${params.xid}/${params.path}`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.doesNotHaveAnyKeys(response.data.jsonData, ['objectKey'], 'data.jsonData');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
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
            // END MODEL: JsonData
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

});
