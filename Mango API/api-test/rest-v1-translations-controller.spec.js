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

// Mango REST V1 API - Translations
describe('translations-controller', function() {
    before('Login', function() { return login.call(this, client); });

    // Get all translations - Kitchen sink of translations
    it('GET /rest/v2/translations', function() {
        const params = {
            browser: false, // in = query, description = browser, required = false, type = boolean, default = false, enum = 
            language: 'en-US', // in = query, description = Language for translations, required = false, type = string, default = , enum = 
            server: false // in = query, description = server, required = false, type = boolean, default = false, enum = 
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v2/translations`,
                params: {
                    browser: params.browser,
                    language: params.language,
                    server: params.server
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: TranslationsModel
            assert.isObject(response.data, 'data');
            assert.isString(response.data.locale, 'data.locale');
            assert.strictEqual(response.data.locale, params.language, 'data.locale');
            assert.isNull(response.data.namespaces, 'data.namespaces');
            assert.isObject(response.data.translations, 'data.translations');
            assert.include(Object.keys(response.data.translations), params.language);
            Object.values(response.data.translations).forEach((item, index) => {
                assert.isObject(item, 'data.translations[]');
                Object.values(item).forEach((item2, index2) => {
                    assert.isString(item2, 'data.translations[][]');
                });
            });
            // END MODEL: TranslationsModel
        });
    });

    // Clear the translation cache - Translations will be reloaded from .properties files upon next translation request
    it('POST /rest/v2/translations/clear-cache', function() {
        const params = {
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'POST',
                path: `/rest/v2/translations/clear-cache`,
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
        });
    });

    // Get translations for public namespaces - Namespace must be base , ie public not public.messages. Returns sub-namespaces too. For > 1 use comma common,public
    it('GET /rest/v2/translations/public/{namespaces}', function() {
        const params = {
            browser: false, // in = query, description = browser, required = false, type = boolean, default = false, enum = 
            language: 'en-US', // in = query, description = Language for translation (must have language pack installed), required = false, type = string, default = , enum = 
            namespaces: 'login', // in = path, description = Message Namespaces, simmilar to java package structure, required = false, type = string, default = , enum = 
            server: false // in = query, description = Use server language for translation, required = false, type = boolean, default = false, enum = 
        };
        
        return Promise.resolve().then(() => {
            const publicClient = createClient();
            
            return publicClient.restRequest({
                method: 'GET',
                path: `/rest/v2/translations/public/${params.namespaces}`,
                params: {
                    browser: params.browser,
                    language: params.language,
                    server: params.server
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: TranslationsModel
            assert.isObject(response.data, 'data');
            assert.isString(response.data.locale, 'data.locale');
            assert.strictEqual(response.data.locale, params.language, 'data.locale');
            assert.isArray(response.data.namespaces, 'data.namespaces');
            response.data.namespaces.forEach((item, index) => {
                assert.isString(item, 'data.namespaces[]');
            });
            assert.include(response.data.namespaces, params.namespaces);
            assert.isObject(response.data.translations, 'data.translations');
            assert.include(Object.keys(response.data.translations), params.language);
            Object.values(response.data.translations).forEach((item, index) => {
                assert.isObject(item, 'data.translations[]');
                Object.values(item).forEach((item2, index2) => {
                    assert.isString(item2, 'data.translations[][]');
                });
            });
            // END MODEL: TranslationsModel
        });
    });

    // Get translations based on namespaces - Namespace must be base namespace, ie common not common.messages. Returns sub-namespaces too.  For > 1 use comma common,public
    it('GET /rest/v2/translations/{namespaces}', function() {
        const params = {
            browser: false, // in = query, description = browser, required = false, type = boolean, default = false, enum = 
            language: 'en-US', // in = query, description = Language for translation (must have language pack installed), required = false, type = string, default = , enum = 
            namespaces: 'pointEdit', // in = path, description = Message Namespaces, simmilar to java package structure, required = false, type = string, default = , enum = 
            server: false // in = query, description = Use server language for translation, required = false, type = boolean, default = false, enum = 
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'GET',
                path: `/rest/v2/translations/${params.namespaces}`,
                params: {
                    browser: params.browser,
                    language: params.language,
                    server: params.server
                }
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: TranslationsModel
            assert.isObject(response.data, 'data');
            assert.isString(response.data.locale, 'data.locale');
            assert.strictEqual(response.data.locale, params.language, 'data.locale');
            assert.isArray(response.data.namespaces, 'data.namespaces');
            response.data.namespaces.forEach((item, index) => {
                assert.isString(item, 'data.namespaces[]');
            });
            assert.include(response.data.namespaces, params.namespaces);
            assert.isObject(response.data.translations, 'data.translations');
            assert.include(Object.keys(response.data.translations), params.language);
            Object.values(response.data.translations).forEach((item, index) => {
                assert.isObject(item, 'data.translations[]');
                Object.values(item).forEach((item2, index2) => {
                    assert.isString(item2, 'data.translations[][]');
                });
            });
            // END MODEL: TranslationsModel
        });
    });

});
