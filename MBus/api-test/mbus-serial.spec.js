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

const testHelper = require('@infinite-automation/mango-client/test/testHelper');
const {createClient, uuid, assertValidationErrors, login} = testHelper;

const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('MBus serial data source', function() {
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

            saved.connection = {
                bitPerSecond : 9600,
                responseTimeoutOffset : 55,
                commPortId : '/dev/null',
                modelType : "mbusSerial"
              };
            
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
                
                //TODO Add more fields to test
                saved.pointLocator.address = 3;
                saved.pointLocator.storageNumber = 4;
                
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
                modelType: 'PL.MBUS',
                settable: false,
                dataType: 'NUMERIC',
                addressing: 'primary',
                address:1,
                dbIndex:0,
                difCode:"16 Bit Integer",
                effectiveSiPrefix:"",
                exponent:0,
                functionField:"Instantaneous value",
                identNumber:0,
                manufacturer:"ADF",
                medium:"Other",
                responseFrame:"default",
                siPrefix:"",
                storageNumber:0,
                subUnit:0,
                tariff:0,
                unitOfMeasurement:"V",
                version:2,
                vifLabel:"Voltage",
                vifType:"extention FD",
                vifeLabels:[],
                vifeTypes:[]
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
                },
                {
                    eventType: 'DATA_SOURCE_EXCEPTION',
                    level: 'INFORMATION',
                },
                {
                    eventType: 'POINT_READ_EXCEPTION',
                    level: 'INFORMATION',
                },
                {
                    eventType: 'POINT_WRITE_EXCEPTION',
                    level: 'INFORMATION',
                }
            ],
            connection : {
                bitPerSecond : 2400,
                responseTimeoutOffset : 50,
                commPortId : "/dev/ttys000",
                modelType : "mbusSerial"
              },
            modelType: 'MBUS'
        });
    }
    
    function assertDataSourceAttributes(saved, local) {
        assert.strictEqual(saved.pollPeriod.periods, local.pollPeriod.periods);
        assert.strictEqual(saved.pollPeriod.type, local.pollPeriod.type);
        assert.strictEqual(saved.quantize, local.quantize);
        assert.strictEqual(saved.useCron, local.useCron);
        
        assert.strictEqual(saved.connection.bitPerSecond, local.connection.bitPerSecond);
        assert.strictEqual(saved.connection.responseTimeoutOffset, local.connection.responseTimeoutOffset);
        assert.strictEqual(saved.connection.commPortId, local.connection.commPortId);
        
    }
    
    function assertPointLocator(saved, local) {
        assert.strictEqual(saved.dataTypeId, local.dataTypeId);
        assert.strictEqual(saved.settable, local.settable);
        
        
        assert.strictEqual(saved.addressing, local.addressing);
        assert.strictEqual(saved.address, local.address);
        assert.strictEqual(saved.dbIndex, local.dbIndex);
        assert.strictEqual(saved.difCode, local.difCode);
        assert.strictEqual(saved.effectiveSiPrefix, local.effectiveSiPrefix);
        assert.strictEqual(saved.exponent, local.exponent);
        assert.strictEqual(saved.functionField, local.functionField);
        assert.strictEqual(saved.identNumber, local.identNumber);
        assert.strictEqual(saved.manufacturer, local.manufacturer);
        assert.strictEqual(saved.medium, local.medium);
        assert.strictEqual(saved.responseFrame, local.responseFrame);
        assert.strictEqual(saved.siPrefix, local.siPrefix);
        assert.strictEqual(saved.storageNumber, local.storageNumber);
        assert.strictEqual(saved.subUnit, local.subUnit);
        assert.strictEqual(saved.tariff, local.tariff);
        assert.strictEqual(saved.unitOfMeasurement, local.unitOfMeasurement);
        assert.strictEqual(saved.version, local.version);
        assert.strictEqual(saved.vifLabel, local.vifLabel);
        assert.strictEqual(saved.vifType, local.vifType);
        assert.strictEqual(saved.vifeLabels.length, local.vifeLabels.length);
        assert.strictEqual(saved.vifeTypes.length, local.vifeTypes.length);
        assert.strictEqual(saved.settable, local.settable);
        assert.strictEqual(saved.settable, local.settable);
    }
});
