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

    const testSetup = function() {
        return client.restRequest({
            method: 'POST',
            path: `/rest/v1/json-data/${this.params.xid}`,
            params: {
                editPermission: this.params.editPermission || [],
                name: this.params.name || 'test data',
                publicData: this.params.publicData,
                readPermission: this.params.readPermission || []
            },
            data: this.requestBody || {}
        });
    };
    
    const testTeardown = function() {
        // common test teardown, e.g. delete a VO object
        return client.restRequest({
            method: 'DELETE',
            path: `/rest/v1/json-data/${this.params.xid}`,
        });
    };
    
    // List all available xids - Shows any xids that you have read permissions for
    it('GET /rest/v1/json-data', function() {
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/json-data`,
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
    it('GET /rest/v1/json-data/public/{xid}', function() {
        this.requestBody = {
            key1: 'value1',
            key2: 'value2'
        };
        
        this.params = {
            xid: uuid(), // in = path, description = XID, required = true, type = string, default = , enum = 
            publicData: true
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            const publicClient = createClient();
            return publicClient.restRequest({
                method: 'GET',
                path: `/rest/v1/json-data/public/${this.params.xid}`,
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
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Get JSON Data - Returns only the data
    it('GET /rest/v1/json-data/{xid}', function() {
        this.requestBody = {
            key1: 'value1',
            key2: 'value2'
        };
        this.params = {
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/json-data/${this.params.xid}`,
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
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Create/replace JSON Data - 
    it('POST /rest/v1/json-data/{xid}', function() {
        this.requestBody = {
            key1: 'value1'
        };
        this.params = {
            data: this.requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/v1/json-data/${this.params.xid}`,
                params: {
                    editPermission: this.params.editPermission,
                    name: this.params.name,
                    publicData: this.params.publicData,
                    readPermission: this.params.readPermission
                },
                data: this.requestBody
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
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Append JSON Data to existing - 
    it('PUT /rest/v1/json-data/{xid}', function() {
        this.requestBody = {
            key1: 'value1'
        };
        this.params = {
            data: this.requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v1/json-data/${this.params.xid}`,
                params: {
                    editPermission: this.params.editPermission,
                    name: this.params.name,
                    publicData: this.params.publicData,
                    readPermission: this.params.readPermission
                },
                data: {
                    key2: 'value2'
                }
            });
        }).then(response => {
            // OK
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
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });
    
    // Append JSON Data to existing - 
    it('PUT /rest/v1/json-data/{xid} using array', function() {
        this.requestBody = [0];
        this.params = {
            data: this.requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v1/json-data/${this.params.xid}`,
                params: {
                    editPermission: this.params.editPermission,
                    name: this.params.name,
                    publicData: this.params.publicData,
                    readPermission: this.params.readPermission
                },
                data: 1
            });
        }).then(response => {
            // OK
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
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Fully Delete JSON Data - 
    it('DELETE /rest/v1/json-data/{xid}', function() {
        this.requestBody = {
            key1: 'value1'
        };
        this.params = {
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'DELETE',
                path: `/rest/v1/json-data/${this.params.xid}`,
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
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Get JSON Data using a path - To get a sub component of the data use a path of member.submember
    it('GET /rest/v1/json-data/{xid}/{path}', function() {
        this.requestBody = {
            property1: {
                testString: '1234'
            }
        };
        this.params = {
            path: 'property1', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v1/json-data/${this.params.xid}/${this.params.path}`,
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
            assert.strictEqual(response.data.jsonData.testString, '1234', 'data.jsonData');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Replace JSON Data - {path} is the path to data with dots data.member.submember
    it('POST /rest/v1/json-data/{xid}/{path}', function() {
        this.requestBody = {
            property1: {}
        };
        
        this.params = {
            data: this.requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            path: 'property1', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/v1/json-data/${this.params.xid}/${this.params.path}`,
                params: {
                    editPermission: this.params.editPermission,
                    name: this.params.name,
                    publicData: this.params.publicData,
                    readPermission: this.params.readPermission
                },
                data: {newValue: 1}
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
            assert.strictEqual(response.data.jsonData.newValue, 1, 'data.jsonData.property1.newValue');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Append JSON Data to existing - {path} is the path to data with dots data.member.submember
    it('PUT /rest/v1/json-data/{xid}/{path}', function() {
        this.requestBody = {
            property1: {
                key1: 'value1'
            }
        };
        
        this.params = {
            data: this.requestBody, // in = body, description = Data to save, required = false, type = , default = , enum = 
            editPermission: ['string'], // in = query, description = Edit Permissions, required = false, type = array, default = , enum = 
            name: 'string', // in = query, description = Name, required = true, type = string, default = , enum = 
            path: 'property1', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            publicData: false, // in = query, description = Is public?, required = true, type = boolean, default = false, enum = 
            readPermission: ['string'], // in = query, description = Read Permissions, required = false, type = array, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v1/json-data/${this.params.xid}/${this.params.path}`,
                params: {
                    editPermission: this.params.editPermission,
                    name: this.params.name,
                    publicData: this.params.publicData,
                    readPermission: this.params.readPermission
                },
                data: {
                    key2: 'value2'
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 201);
            // MODEL: JsonData
            // DESCRIPTION: Json Data Model
            assert.isObject(response.data, 'data');
            assert.isString(response.data.editPermission, 'data.editPermission');
            // DESCRIPTION: ID of object in database
            assert.isNumber(response.data.id, 'data.id');
            // MODEL: ArbitraryJsonData
            assert.isObject(response.data.jsonData, 'data.jsonData');
            assert.strictEqual(response.data.jsonData.key1, 'value1', 'data.jsonData.key1');
            assert.strictEqual(response.data.jsonData.key2, 'value2', 'data.jsonData.key2');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

    // Partially Delete JSON Data - {path} is the path to data with dots data.member.submember
    it('DELETE /rest/v1/json-data/{xid}/{path}', function() {
        this.requestBody = {
            property1: {}
        };
        
        this.params = {
            path: 'property1', // in = path, description = Data path using dots as separator, required = true, type = string, default = , enum = 
            xid: uuid() // in = path, description = XID, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this);
        }).then(() => {
            return client.restRequest({
                method: 'DELETE',
                path: `/rest/v1/json-data/${this.params.xid}/${this.params.path}`,
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
            assert.strictEqual(Object.keys(response.data.jsonData).length, 0, 'data.jsonData key length');
            // END MODEL: ArbitraryJsonData
            // DESCRIPTION: Model Type Definition
            assert.isString(response.data.modelType, 'data.modelType');
            // DESCRIPTION: Name of object
            assert.isString(response.data.name, 'data.name');
            assert.isBoolean(response.data.publicData, 'data.publicData');
            assert.isString(response.data.readPermission, 'data.readPermission');
            // DESCRIPTION: Messages for validation of data
            assert.isArray(response.data.validationMessages, 'data.validationMessages');
            //assert.isAbove(response.data.validationMessages.length, 0, 'data.validationMessages');
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
            return testTeardown.call(this).catch(noop);
        });
    });

});
