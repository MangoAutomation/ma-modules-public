/**
 * Copyright 2019 Infinite Automation Systems Inc.
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

const {createClient, login} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const SystemSettings = client.SystemSetting;

describe('System settings service', function() {
    before('Login', function() { return login.call(this, client); });
    
    it('Cannot change password min length to less than 1', function() {
        return SystemSettings.setValue('password.rule.lengthMin', 0, 'INTEGER').then(response => {
            throw new Error('Should not have changed min length');
        }, error => {
            assert.strictEqual(error.status, 422);
        });
    });
    it('Cannot change password max length to less than 1', function() {
        return SystemSettings.setValue('password.rule.lengthMax', 0, 'INTEGER').then(response => {
            throw new Error('Should not have changed max length');
        }, error => {
            assert.strictEqual(error.status, 422);
        });
    });
    
    it('Cannot change password min length to more than 255', function() {
        return SystemSettings.setValue('password.rule.lengthMin', 256, 'INTEGER').then(response => {
            throw new Error('Should not have changed min length');
        }, error => {
            assert.strictEqual(error.status, 422);
        });
    });
    it('Cannot change password max length to more than 256', function() {
        return SystemSettings.setValue('password.rule.lengthMax', 256, 'INTEGER').then(response => {
            throw new Error('Should not have changed max length');
        }, error => {
            assert.strictEqual(error.status, 422);
        });
    });
    
});
