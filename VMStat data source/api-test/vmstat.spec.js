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

const testHelper = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const {createClient, assertValidationErrors, uuid, login} = testHelper;

const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('VMStat data source', function() {
    before('Login', function() { return login.call(this, client); });
     
    it('Create data source', () => {
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

    it('Update data source', () => {
        const ds = newDataSource();
        const local = Object.assign({}, ds);
        return ds.save().then(saved => {
            testHelper.assertDataSource(saved, local, assertDataSourceAttributes);
            //Make changes
            saved.name = uuid();
            saved.outputScale = 'LOWER_K';
            saved.pollSeconds = 4;
            
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
            return ds.delete();
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
                saved.pointLocator.attribute = 'PROCS_R';
                
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
            return ds.delete();
        });
    });
    
    function newDataPoint(dsXid) {
        return new DataPoint({
            dataSourceXid: dsXid,
            pointLocator: { 
                attribute: 'MEMORY_FREE',
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.VMSTAT'
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
                     eventType: 'PARSE_EXCEPTION',
                     level: 'INFORMATION'
                 }
            ],
            editPermission: [],
            pollSeconds: 1,
            outputScale: 'LOWER_K',
            modelType: 'VMSTAT'
        });
    }
    
    function assertDataSourceAttributes(saved, local) {
        assert.strictEqual(saved.pollSeconds, local.pollSeconds);
        assert.strictEqual(saved.outputScale, local.outputScale);
    }
    
    function assertPointLocator(saved, local) {
        assert.strictEqual(saved.dataType, local.dataType);
        assert.strictEqual(saved.settable, local.settable);
        assert.strictEqual(saved.attribute, local.attribute);
    }
});
