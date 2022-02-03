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

const path = require('path');
const testHelper = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const {createClient, assertValidationErrors, uuid, login} = testHelper;

const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('ASCII file data source', function() {
    before('Login', function() { return login.call(this, client); });

    it('Create data source', function() {
        const ds = newDataSource();
        const local = Object.assign({}, ds);
        return ds.save().then(saved => {
            testHelper.assertDataSource(saved, local, assertDataSourceAttributes);
        }, error => {
            assertValidationErrors([''], error);
        }).finally(() => {
            return ds.delete();
        });
    });

    it('Update data source', function() {
        const ds = newDataSource();
        const local = Object.assign({}, ds);
        return ds.save().then(saved => {
            testHelper.assertDataSource(saved, local, assertDataSourceAttributes);
            //Make changes
            saved.name = uuid();
            saved.quantize = false;
            saved.pollPeriod= {
                periods: 3,
                type: 'MINUTES'
            };
            
            saved.filePath = path.resolve('/user/local/test2');
            
            const localUpdate = Object.assign({}, saved);
            return saved.save().then(updated => {
                testHelper.assertDataSource(updated, localUpdate, assertDataSourceAttributes); 
            }, error => {
                assertValidationErrors([''], error);
            });
        }, error => {
            assertValidationErrors([''], error);
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Delete data source', function() {
        const ds = newDataSource();
        return ds.save().then(saved => {
            testHelper.assertDataSource(ds, saved, assertDataSourceAttributes);
            return ds.delete().then(() => {
               return DataSource.get(saved.xid).then(notFound => {
                   assert.fail('Should not have found ds ' + notFound.xid);
               }, error => {
                   assert.strictEqual(404, error.status);
               }); 
            });
        });
    });
    
    it('Create data point', function() {
        const ds = newDataSource();
        return ds.save().then(saved => {
            testHelper.assertDataSource(ds, saved, assertDataSourceAttributes);
            const dp = newDataPoint(ds.xid);
            return dp.save().then(saved => {
                testHelper.assertDataPoint(saved, dp, assertPointLocator);
            }, error => {
                assertValidationErrors([''], error);
            });
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Update data point', function() {
        const ds = newDataSource();
        return ds.save().then(saved => {
            testHelper.assertDataSource(ds, saved, assertDataSourceAttributes);
            const dp = newDataPoint(ds.xid);
            const local = Object.assign({}, dp);
            return dp.save().then(saved => {
                testHelper.assertDataPoint(saved, local, assertPointLocator);

                saved.pointLocator.pointIdentifier = 'id2';
                saved.pointLocator.pointIdentifierIndex = 2;
                saved.pointLocator.valueIndex = 3;
                saved.pointLocator.valueRegex = '(test.*)';
                saved.pointLocator.hasTimestamp = false;
                saved.pointLocator.timestampIndex = 2;
                saved.pointLocator.timestampFormat = 'yyyy-dd-MM';
                
                const localUpdate = Object.assign({}, saved);
                return saved.save().then(updated => {
                    return DataPoint.get(updated.xid).then(found => {
                        testHelper.assertDataPoint(found, localUpdate, assertPointLocator);
                    });
                });
            });
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Delete data point', function() {
        const ds = newDataSource();
        return ds.save().then(saved => {
            testHelper.assertDataSource(ds, saved, assertDataSourceAttributes);
            const dp = newDataPoint(ds.xid);
            return dp.save().then(saved => {
                testHelper.assertDataPoint(saved, dp, assertPointLocator);
                return saved.delete().then(() => {
                    return DataPoint.get(saved.xid).then(notFound => {
                        assert.fail('Should not have found point ' + notFound.xid);
                    }, error => {
                        assert.strictEqual(404, error.status);
                    }); 
                });
            });
        }).finally(() => {
            return ds.delete();
        });
    });
    
    function newDataPoint(dsXid) {
        return new DataPoint({
            dataSourceXid: dsXid,
            pointLocator: { 
                settable: false,
                dataType: 'ALPHANUMERIC',
                pointIdentifier: 'id1',
                pointIdentifierIndex: 1,
                valueIndex: 2,
                valueRegex: '(.*)',
                hasTimestamp: true,
                timestampIndex: 1,
                timestampFormat: 'yyyy-dd',
                modelType: 'PL.ASCII_FILE'
            }
        });
    }
    function newDataSource() {
        return new DataSource({
            eventAlarmLevels: [
                 {
                     eventType: 'DATA_SOURCE_EXCEPTION',
                     level: 'INFORMATION'
                 },{
                     eventType: 'POINT_READ_EXCEPTION',
                     level: 'INFORMATION'
                 },{
                     eventType: 'POINT_READ_PATTERN_MISMATCH_EVENT',
                     level: 'INFORMATION'
                 },{
                     eventType: 'POLL_ABORTED',
                     level: 'INFORMATION'
                 }
            ],
            editPermission: [],
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            quantize: true,
            useCron: false,
            filePath: path.resolve('/user/local/test'),
            modelType: 'ASCII FILE'
        });
    }
    
    function assertDataSourceAttributes(saved, local) {
        assert.strictEqual(saved.pollPeriod.periods, local.pollPeriod.periods);
        assert.strictEqual(saved.pollPeriod.type, local.pollPeriod.type);
        assert.strictEqual(saved.quantize, local.quantize);
        assert.strictEqual(saved.useCron, local.useCron);

        assert.strictEqual(saved.filePath, local.filePath);
        
        assert.strictEqual(saved.modelType, local.modelType);
    }
    
    function assertPointLocator(saved, local) {
        assert.strictEqual(saved.dataType, local.dataType);
        assert.strictEqual(saved.settable, local.settable);
        
        assert.strictEqual(saved.pointIdentifier, local.pointIdentifier);
        assert.strictEqual(saved.pointIdentifierIndex, local.pointIdentifierIndex);
        assert.strictEqual(saved.valueIndex, local.valueIndex);
        assert.strictEqual(saved.valueRegex, local.valueRegex);
        assert.strictEqual(saved.hasTimestamp, local.hasTimestamp);
        assert.strictEqual(saved.timestampIndex, local.timestampIndex);
        assert.strictEqual(saved.timestampFormat, local.timestampFormat);
        
        assert.strictEqual(saved.modelType, local.modelType);
    }
});
