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

const testHelper = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const {createClient, assertValidationErrors, uuid, login} = testHelper;

const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Serial data source', function() {
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
            saved.commPortId  = '/dev/null2';
            saved.baudRate = 9600;
            saved.flowControlIn= 'RTSCTS';
            saved.flowControlOut= 'RTSCTS';
            saved.dataBits= 'DATA_BITS_6';
            saved.stopBits= 'STOP_BITS_1_5';
            saved.parity= 'EVEN';
            saved.readTimeout = 5002;
            saved.retries = 53;
            saved.useTerminator= true;
            saved.messageTerminator= ';;';
            saved.messageRegex= '.*test';
            saved.pointIdentifierIndex= 21;
            saved.hex = false;
            saved.logIO = false;
            saved.maxMessageSize= 50;
            saved.ioLogFileSizeMBytes= 1;
            saved.maxHistoricalIOLogs= 3;
            
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
                saved.pointLocator.valueIndex = 1;
                saved.pointLocator.valueRegex = '.*test';
                saved.pointLocator.pointIdentifier = 'id2';
                
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
                dataType: 'NUMERIC',
                settable: true,
                valueIndex: 0,
                valueRegex: '.*',
                pointIdentifier: 'id',
                modelType: 'PL.SERIAL'
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
                     eventType: 'POINT_WRITE_EXCEPTION',
                     level: 'INFORMATION'
                 },{
                     eventType: 'POINT_READ_PATTERN_MISMATCH_EVENT',
                     level: 'INFORMATION'
                 }
            ],
            editPermission: [],
            commPortId : '/dev/null',
            baudRate : 9600,
            flowControlIn: 'NONE',
            flowControlOut: 'NONE',
            dataBits: 'DATA_BITS_8',
            stopBits: 'STOP_BITS_1',
            parity: 'NONE',
            readTimeout : 500,
            retries : 5,
            useTerminator: false,
            messageTerminator: ';',
            messageRegex: '.*',
            pointIdentifierIndex: 1,
            hex: true,
            logIO: true,
            maxMessageSize: 512,
            ioLogFileSizeMBytes: 512,
            maxHistoricalIOLogs: 2,
            modelType: 'SERIAL'
        });
    }
    
    function assertDataSourceAttributes(saved, local) {
        assert.strictEqual(saved.commPortId, local.commPortId);
        assert.strictEqual(saved.baudRate, local.baudRate);
        assert.strictEqual(saved.flowControlIn, local.flowControlIn);
        assert.strictEqual(saved.flowControlOut, local.flowControlOut);
        assert.strictEqual(saved.dataBits, local.dataBits);
        assert.strictEqual(saved.stopBits, local.stopBits);
        assert.strictEqual(saved.parity, local.parity);
        assert.strictEqual(saved.readTimeout, local.readTimeout);
        assert.strictEqual(saved.retries, local.retries);
        assert.strictEqual(saved.useTerminator, local.useTerminator);
        assert.strictEqual(saved.messageTerminator, local.messageTerminator);
        assert.strictEqual(saved.messageRegex, local.messageRegex);
        assert.strictEqual(saved.pointIdentifierIndex, local.pointIdentifierIndex);
        assert.strictEqual(saved.hex, local.hex);
        assert.strictEqual(saved.logIO, local.logIO);        
        assert.strictEqual(saved.maxMessageSize, local.maxMessageSize);
        assert.strictEqual(saved.ioLogFileSizeMBytes, local.ioLogFileSizeMBytes);
        assert.strictEqual(saved.maxHistoricalIOLogs, local.maxHistoricalIOLogs);
        
        assert.strictEqual(saved.modelType, local.modelType);
    }
    
    function assertPointLocator(saved, local) {
        assert.strictEqual(saved.dataType, local.dataType);
        assert.strictEqual(saved.settable, local.settable);
        assert.strictEqual(saved.pointIdentifier, local.pointIdentifier);
        assert.strictEqual(saved.valueRegex, local.valueRegex);
        assert.strictEqual(saved.valueIndex, local.valueIndex);
    }
});
