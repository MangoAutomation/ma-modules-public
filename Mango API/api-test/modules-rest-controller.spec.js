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

const {createClient, login, uuid, noop} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

// Mango REST V1 API - Access Module Definitions
describe('modules-rest-controller', function() {
    before('Login', function() { return login.call(this, client); });

    const testSetup = function(params, requestBody) {
        return Promise.resolve();
    };
    
    const testTeardown = function(params) {
        return Promise.resolve();
    };
    
    // AngularJS Modules - Publicly Available Angular JS Modules
    it('GET /rest/latest/modules/angularjs-modules/public', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/angularjs-modules/public`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: AngularJSModuleDefinitionGroupModel
            assert.isObject(response.data, 'data');
            assert.isArray(response.data.modules, 'data.modules');
            response.data.modules.forEach((item, index) => {
                // MODEL: ModuleInfo
                assert.isObject(item, 'data.modules[]');
                assert.isString(item.name, 'data.modules[].name');
                assert.isString(item.url, 'data.modules[].url');
                assert.isString(item.version, 'data.modules[].version');
                // END MODEL: ModuleInfo
            });
            assert.isArray(response.data.urls, 'data.urls');
            response.data.urls.forEach((item, index) => {
                assert.isString(item, 'data.urls[]');
            });
            // END MODEL: AngularJSModuleDefinitionGroupModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get Core Module - For checking current licensing and version
    it('GET /rest/latest/modules/core', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/core`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: ModuleModel
            assert.isObject(response.data, 'data');
            if (response.data.dependencies != null) {
                assert.isString(response.data.dependencies, 'data.dependencies');
            }
            assert.isString(response.data.description, 'data.description');
            assert.isString(response.data.licenseType, 'data.licenseType');
            if (response.data.longDescription != null) {
                assert.isString(response.data.longDescription, 'data.longDescription');
            }
            assert.isBoolean(response.data.markedForDeletion, 'data.markedForDeletion');
            assert.isString(response.data.name, 'data.name');
            assert.isString(response.data.normalVersion, 'data.normalVersion');
            assert.isBoolean(response.data.signed, 'data.signed');
            assert.isBoolean(response.data.unloaded, 'data.unloaded');
            assert.isString(response.data.vendor, 'data.vendor');
            assert.isString(response.data.vendorUrl, 'data.vendorUrl');
            assert.isString(response.data.version, 'data.version');
            // END MODEL: ModuleModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Set Marked For Deletion state of Module - Marking a module for deletion will un-install it upon restart
    it.skip('PUT /rest/latest/modules/deletion-state', function() {
        const requestBody =
        { // title: ModuleModel
            dependencies: 'string',
            description: 'string',
            licenseType: 'string',
            longDescription: 'string',
            markedForDeletion: false,
            name: 'string',
            normalVersion: 'string',
            signed: false,
            unloaded: false,
            vendor: 'string',
            vendorUrl: 'string',
            version: 'string'
        };
        const params = {
            delete: false, // in = query, description = Deletion State, required = true, type = boolean, default = false, enum = 
            model: requestBody // in = body, description = Module model, required = false, type = , default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/latest/modules/deletion-state`,
                params: {
                    delete: params.delete
                },
                data: requestBody
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: ModuleModel
            assert.isObject(response.data, 'data');
            assert.isString(response.data.dependencies, 'data.dependencies');
            assert.isString(response.data.description, 'data.description');
            assert.isString(response.data.licenseType, 'data.licenseType');
            assert.isString(response.data.longDescription, 'data.longDescription');
            assert.isBoolean(response.data.markedForDeletion, 'data.markedForDeletion');
            assert.isString(response.data.name, 'data.name');
            assert.isString(response.data.normalVersion, 'data.normalVersion');
            assert.isBoolean(response.data.signed, 'data.signed');
            assert.isBoolean(response.data.unloaded, 'data.unloaded');
            assert.isString(response.data.vendor, 'data.vendor');
            assert.isString(response.data.vendorUrl, 'data.vendorUrl');
            assert.isString(response.data.version, 'data.version');
            // END MODEL: ModuleModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Set Marked For Deletion state of Module - Marking a module for deletion will un-install it upon restart
    it.skip('PUT /rest/latest/modules/deletion-state/{moduleName}', function() {
        const params = {
            delete: false, // in = query, description = Deletion State, required = true, type = boolean, default = false, enum = 
            moduleName: 'string' // in = path, description = moduleName, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/latest/modules/deletion-state/${params.moduleName}`,
                params: {
                    delete: params.delete
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: ModuleModel
            assert.isObject(response.data, 'data');
            assert.isString(response.data.dependencies, 'data.dependencies');
            assert.isString(response.data.description, 'data.description');
            assert.isString(response.data.licenseType, 'data.licenseType');
            assert.isString(response.data.longDescription, 'data.longDescription');
            assert.isBoolean(response.data.markedForDeletion, 'data.markedForDeletion');
            assert.isString(response.data.name, 'data.name');
            assert.isString(response.data.normalVersion, 'data.normalVersion');
            assert.isBoolean(response.data.signed, 'data.signed');
            assert.isBoolean(response.data.unloaded, 'data.unloaded');
            assert.isString(response.data.vendor, 'data.vendor');
            assert.isString(response.data.vendorUrl, 'data.vendorUrl');
            assert.isString(response.data.version, 'data.version');
            // END MODEL: ModuleModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Download your license from the store - Admin Only
    it.skip('PUT /rest/latest/modules/download-license', function() {
        const requestBody =
        { // title: CredentialsModel
            password: 'string',
            username: 'string'
        };
        const params = {
            model: requestBody, // in = body, description = User Credentials, required = true, type = , default = , enum = 
            retries: 0 // in = query, description = Connection retries, required = false, type = integer, default = 0, enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/latest/modules/download-license`,
                params: {
                    retries: params.retries
                },
                data: requestBody
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // List Current Installed Modules - List all installed
    it('GET /rest/latest/modules/list', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/list`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isArray(response.data, 'data');
            response.data.forEach((item, index) => {
                // MODEL: ModuleModel
                assert.isObject(item, 'data[]');
                if (response.data.dependencies != null) {
                    assert.isString(item.dependencies, 'data[].dependencies');
                }
                assert.isString(item.description, 'data[].description');
                if (response.data.licenseType != null) {
                    assert.isString(item.licenseType, 'data[].licenseType');
                }
                if (response.data.longDescription != null) {
                    assert.isString(item.longDescription, 'data[].longDescription');
                }
                assert.isBoolean(item.markedForDeletion, 'data[].markedForDeletion');
                assert.isString(item.name, 'data[].name');
                assert.isString(item.normalVersion, 'data[].normalVersion');
                assert.isBoolean(item.signed, 'data[].signed');
                assert.isBoolean(item.unloaded, 'data[].unloaded');
                assert.isString(item.vendor, 'data[].vendor');
                assert.isString(item.vendorUrl, 'data[].vendorUrl');
                assert.isString(item.version, 'data[].version');
                // END MODEL: ModuleModel
            });
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // List Current Missing Module Dependencies - List all installed
    it('GET /rest/latest/modules/list-missing-dependencies', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/list-missing-dependencies`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isObject(response.data, 'data');
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get the update license payload, to make requests to store - Admin Only
    it('GET /rest/latest/modules/update-license-payload', function() {
        const params = {
            download: true // in = query, description = Set content disposition to attachment, required = false, type = boolean, default = true, enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/update-license-payload`,
                params: {
                    download: params.download
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: UpdateLicensePayloadModel
            assert.isObject(response.data, 'data');
            assert.isNumber(response.data.currentVersionState, 'data.currentVersionState');
            assert.isString(response.data.description, 'data.description');
            assert.isString(response.data.distributor, 'data.distributor');
            assert.isString(response.data.guid, 'data.guid');
            assert.isObject(response.data.modules, 'data.modules');
            assert.isString(response.data.storeUrl, 'data.storeUrl');
            assert.isNumber(response.data.upgradeVersionState, 'data.upgradeVersionState');
            // END MODEL: UpdateLicensePayloadModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Download Upgrades and optionally backup and restart - Use Modules web socket to track progress
    it.skip('POST /rest/latest/modules/upgrade', function() {
        const requestBody =
        { // title: ModuleUpgradesModel
            newInstalls: [
                { // title: ModuleUpgradeModel
                    dependencies: 'string',
                    dependencyVersions: { // title: undefined
                    },
                    description: 'string',
                    licenseType: 'string',
                    longDescription: 'string',
                    markedForDeletion: false,
                    name: 'string',
                    newVersion: 'string',
                    normalVersion: 'string',
                    releaseNotes: 'string',
                    signed: false,
                    unloaded: false,
                    vendor: 'string',
                    vendorUrl: 'string',
                    version: 'string'
                }
            ],
            upgrades: [
                { // title: ModuleUpgradeModel
                    dependencies: 'string',
                    dependencyVersions: { // title: undefined
                    },
                    description: 'string',
                    licenseType: 'string',
                    longDescription: 'string',
                    markedForDeletion: false,
                    name: 'string',
                    newVersion: 'string',
                    normalVersion: 'string',
                    releaseNotes: 'string',
                    signed: false,
                    unloaded: false,
                    vendor: 'string',
                    vendorUrl: 'string',
                    version: 'string'
                }
            ]
        };
        const params = {
            backup: false, // in = query, description = Perform Backup first, required = false, type = boolean, default = false, enum = 
            model: requestBody, // in = body, description = Desired Upgrades, required = true, type = , default = , enum = 
            restart: false // in = query, description = Restart when completed, required = false, type = boolean, default = false, enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/latest/modules/upgrade`,
                params: {
                    backup: params.backup,
                    restart: params.restart
                },
                data: requestBody
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Cancel Download of Upgrades - 
    it.skip('PUT /rest/latest/modules/upgrade', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/latest/modules/upgrade`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get Current Upgrade Task Status - 
    it.skip('GET /rest/latest/modules/upgrade-status', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/upgrade-status`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: UpgradeStatusModel
            assert.isObject(response.data, 'data');
            assert.isBoolean(response.data.cancelled, 'data.cancelled');
            assert.isString(response.data.error, 'data.error');
            assert.isBoolean(response.data.finished, 'data.finished');
            assert.isArray(response.data.results, 'data.results');
            response.data.results.forEach((item, index) => {
                // MODEL: ModuleModel
                assert.isObject(item, 'data.results[]');
                assert.isString(item.dependencies, 'data.results[].dependencies');
                assert.isString(item.description, 'data.results[].description');
                assert.isString(item.licenseType, 'data.results[].licenseType');
                assert.isString(item.longDescription, 'data.results[].longDescription');
                assert.isBoolean(item.markedForDeletion, 'data.results[].markedForDeletion');
                assert.isString(item.name, 'data.results[].name');
                assert.isString(item.normalVersion, 'data.results[].normalVersion');
                assert.isBoolean(item.signed, 'data.results[].signed');
                assert.isBoolean(item.unloaded, 'data.results[].unloaded');
                assert.isString(item.vendor, 'data.results[].vendor');
                assert.isString(item.vendorUrl, 'data.results[].vendorUrl');
                assert.isString(item.version, 'data.results[].version');
                // END MODEL: ModuleModel
            });
            assert.isBoolean(response.data.running, 'data.running');
            assert.isString(response.data.stage, 'data.stage');
            assert.isBoolean(response.data.willRestart, 'data.willRestart');
            // END MODEL: UpgradeStatusModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Get Available Upgrades - Check the store for Upgrades
    it.skip('GET /rest/latest/modules/upgrades-available', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/latest/modules/upgrades-available`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: ModuleUpgradesModel
            assert.isObject(response.data, 'data');
            assert.isArray(response.data.newInstalls, 'data.newInstalls');
            response.data.newInstalls.forEach((item, index) => {
                // MODEL: ModuleUpgradeModel
                assert.isObject(item, 'data.newInstalls[]');
                assert.isString(item.dependencies, 'data.newInstalls[].dependencies');
                assert.isObject(item.dependencyVersions, 'data.newInstalls[].dependencyVersions');
                assert.isString(item.description, 'data.newInstalls[].description');
                assert.isString(item.licenseType, 'data.newInstalls[].licenseType');
                assert.isString(item.longDescription, 'data.newInstalls[].longDescription');
                assert.isBoolean(item.markedForDeletion, 'data.newInstalls[].markedForDeletion');
                assert.isString(item.name, 'data.newInstalls[].name');
                assert.isString(item.newVersion, 'data.newInstalls[].newVersion');
                assert.isString(item.normalVersion, 'data.newInstalls[].normalVersion');
                assert.isString(item.releaseNotes, 'data.newInstalls[].releaseNotes');
                assert.isBoolean(item.signed, 'data.newInstalls[].signed');
                assert.isBoolean(item.unloaded, 'data.newInstalls[].unloaded');
                assert.isString(item.vendor, 'data.newInstalls[].vendor');
                assert.isString(item.vendorUrl, 'data.newInstalls[].vendorUrl');
                assert.isString(item.version, 'data.newInstalls[].version');
                // END MODEL: ModuleUpgradeModel
            });
            assert.isArray(response.data.upgrades, 'data.upgrades');
            response.data.upgrades.forEach((item, index) => {
                // MODEL: ModuleUpgradeModel
                assert.isObject(item, 'data.upgrades[]');
                assert.isString(item.dependencies, 'data.upgrades[].dependencies');
                assert.isObject(item.dependencyVersions, 'data.upgrades[].dependencyVersions');
                assert.isString(item.description, 'data.upgrades[].description');
                assert.isString(item.licenseType, 'data.upgrades[].licenseType');
                assert.isString(item.longDescription, 'data.upgrades[].longDescription');
                assert.isBoolean(item.markedForDeletion, 'data.upgrades[].markedForDeletion');
                assert.isString(item.name, 'data.upgrades[].name');
                assert.isString(item.newVersion, 'data.upgrades[].newVersion');
                assert.isString(item.normalVersion, 'data.upgrades[].normalVersion');
                assert.isString(item.releaseNotes, 'data.upgrades[].releaseNotes');
                assert.isBoolean(item.signed, 'data.upgrades[].signed');
                assert.isBoolean(item.unloaded, 'data.upgrades[].unloaded');
                assert.isString(item.vendor, 'data.upgrades[].vendor');
                assert.isString(item.vendorUrl, 'data.upgrades[].vendorUrl');
                assert.isString(item.version, 'data.upgrades[].version');
                // END MODEL: ModuleUpgradeModel
            });
            // END MODEL: ModuleUpgradesModel
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

    // Upload upgrade zip bundle, to be installed on restart - The bundle can be downloaded from the Mango Store
    it.skip('POST /rest/latest/modules/upload-upgrades', function() {
        const params = {
            backup: false, // in = query, description = Perform Backup first, required = false, type = boolean, default = false, enum = 
            multipartRequest: undefined, // in = formData, description = multipartRequest, required = false, type = file, default = , enum = 
            restart: false // in = query, description = Restart after upload completes, required = false, type = boolean, default = false, enum = 
        };
        
        return Promise.resolve().then(() => {
            return testSetup.call(this, params);
        }).then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/latest/modules/upload-upgrades`,
                params: {
                    backup: params.backup,
                    restart: params.restart
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            if (response.data != null && this.test.expectedResult != null) {
                assert.deepEqual(response.data, this.test.expectedResult);
            }
        }).finally(() => {
            return testTeardown.call(this, params).catch(noop);
        });
    });

});
