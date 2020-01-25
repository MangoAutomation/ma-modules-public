/**
 * Copyright 2017 Infinite Automation Systems Inc.
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

const testHelper = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const {createClient, uuid, assertValidationErrors, login} = testHelper;

const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Virtual data source', function() {
    before('Login', function() { return login.call(this, client); });
     
    it('Create data source', () => {
        const ds = newDataSource();
        const local = Object.assign({}, ds);
        return ds.save().then(saved => {
            testHelper.assertDataSource(saved, local, assertDataSourceAttributes);
        }, error => {
            assertValidationErrors([''], error);
        }).finally(() => {
            ds.delete();
        });
    });

    it('Update data source', () => {
        const ds = newDataSource();
        const local = Object.assign({}, ds);
        return ds.save().then(saved => {
            testHelper.assertDataSource(saved, local, assertDataSourceAttributes);
            //Make changes
            saved.name = uuid();
            saved.polling = true;
            const localUpdate = Object.assign({}, saved);
            return saved.save().then(updated => {
                testHelper.assertDataSource(updated, localUpdate, assertDataSourceAttributes); 
            });
        }, error => {
            assertValidationErrors([''], error);
        }).finally(() => {
            ds.delete();
        });
    });
    
    it('Delete data source', () => {
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
    
    it('Create data point', () => {
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
            ds.delete();
        });
    });
    
    it('Update data point', () => {
        const ds = newDataSource();
        return ds.save().then(saved => {
            testHelper.assertDataSource(ds, saved, assertDataSourceAttributes);
            const dp = newDataPoint(ds.xid);
            const local = Object.assign({}, dp);
            return dp.save().then(saved => {
                testHelper.assertDataPoint(saved, local, assertPointLocator);
                saved.pointLocator.startValue = 'true';
                saved.pointLocator.changeType = 'ALTERNATE_BOOLEAN';
                
                const localUpdate = Object.assign({}, saved);
                return saved.save().then(updated => {
                    return DataPoint.get(updated.xid).then(found => {
                        testHelper.assertDataPoint(found, localUpdate, assertPointLocator);
                    });
                });
            });
        }).finally(() => {
            ds.delete();
        });
    });
    
    it('Delete data point', () => {
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
            ds.delete();
        });
    });
    
    function newDataPoint(dsXid) {
        return new DataPoint({
            dataSourceXid: dsXid,
            pointLocator: {
                startValue: 'false',
                changeType: 'NO_CHANGE',
                dataType: 'BINARY',
                settable: true,
                modelType: 'PL.VIRTUAL'
            }
        });
    }
    function newDataSource() {
        return new DataSource({
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            quantize: true,
            useCron: false,
            editPermission: [],
            eventAlarmLevels: [
                {
                    eventType: 'POLL_ABORTED',
                    level: 'INFORMATION',
                 }
            ],
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            quantize: false,
            useCron: false,
            polling: false,
            modelType: 'VIRTUAL'
        });
    }
    
    function assertDataSourceAttributes(saved, local) {
        assert.strictEqual(saved.polling, local.polling);
        assert.strictEqual(saved.pollPeriod.periods, local.pollPeriod.periods);
        assert.strictEqual(saved.pollPeriod.type, local.pollPeriod.type);
        assert.strictEqual(saved.quantize, local.quantize);
        assert.strictEqual(saved.useCron, local.useCron);
    }
    
    function assertPointLocator(saved, local) {
        assert.strictEqual(saved.dataType, local.dataType);
        
        assert.strictEqual(saved.changeType, local.changeType);
        switch(saved.changeType) {
            case 'ALTERNATE_BOOLEAN':
                assert.strictEqual(saved.startValue, local.startValue);
            break;
            case 'NO_CHANGE':
                assert.strictEqual(saved.startValue, local.startValue);
            break;
        }
    }
});
