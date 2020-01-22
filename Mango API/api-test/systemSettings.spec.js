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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
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
    
    it('Can set password uppercase requirement', function() {
        let minLength,maxLength,existingValue;
        return SystemSettings.getValue('password.rule.lengthMin', 'INTEGER').then(response =>{
            minLength = response;
            return SystemSettings.getValue('password.rule.lengthMax', 'INTEGER').then(response =>{
                maxLength = response;
                return SystemSettings.getValue('password.rule.upperCaseCount', 'INTEGER').then(response =>{
                    existingValue = response;
                    return SystemSettings.setValue('password.rule.upperCaseCount', minLength + 1, 'INTEGER').then(response => {
                        assert.strictEqual(minLength + 1, response);
                    }).finally(() =>{
                        return SystemSettings.setValue('password.rule.upperCaseCount', existingValue, 'INTEGER');
                    });
                });
            });
        });
    });
});
