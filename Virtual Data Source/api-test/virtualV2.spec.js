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

const {createClient, login, uuid} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Virtual data source', function() {
    before('Login', function() { return login.call(this, client); });
    
    const newDataPoint = (xid, dsXid, locator) => {
        return new DataPoint({
            xid: xid,
            name: uuid(),
            deviceName: uuid(),
            enabled: false,
            loggingProperties: {
                tolerance : 0.0,
                discardExtremeValues : false,
                discardLowLimit : -1.7976931348623157E308,
                discardHighLimit : 1.7976931348623157E308,
                loggingType : 'ON_CHANGE',
                intervalLoggingType: 'INSTANT',
                intervalLoggingPeriod : {
                    periods : 15,
                    type : 'MINUTES'
                },
                overrideIntervalLoggingSamples : false,
                intervalLoggingSampleWindowSize : 0,
                cacheSize : 1 
            },
            textRenderer: {
                useUnitAsSuffix: false,
                suffix: 'test',
                type: 'textRendererPlain'
            },
            dataSourceXid : dsXid,
            useIntegralUnit : false,
            useRenderedUnit : false,
            readPermission : '',
            setPermission : 'superadmin',
            chartColour : 'black',
            rollup : 'NONE',
            plotType : 'STEP',
            purgeOverride : false,
            purgePeriod : {
                periods : 1,
                type : 'YEARS'
            },
            unit : 'V',
            integralUnit : 'v*s',
            renderedUnit : '',
            pointLocator: locator,
            modelType: 'DATA_POINT'
        });
    };
    
    const newDataSource = (xid) => {
        return new DataSource({
            id: -1,
            xid: xid,
            name: uuid(),
            enabled: false,
            eventAlarmLevels: [
                {
                    eventType: 'POLL_ABORTED',
                    duplicateHandling: 'IGNORE',
                    level: 'INFORMATION',
                    description: 'Poll aborted'
                 }
            ],
            purgeSettings: {
                override: true,
                frequency: {
                    periods: 7,
                    type: 'DAYS'
                }
            },
            editPermission: ['superadmin', 'user'],
            pollPeriod: {
                periods: 5,
                type: 'SECONDS'
            },
            quantize: true,
            useCron: false,
            cronPattern: '',
            polling: true,
            modelType: 'VIRTUAL'            
        });
    };
     
    it('Create data source', () => {
        const ds = newDataSource(uuid());
        return ds.save().then(saved => {
            assertDataSource(ds, saved);
        }).finally(() => {
            ds.delete();
        });
    });

    it('Update data source', () => {
        const ds = newDataSource(uuid());
        return ds.save().then(saved => {
            assertDataSource(ds, saved);
            //Make changes
            saved.name = uuid();
            saved.polling = true;
            return saved.save().then(updated => {
               assertDataSource(saved, updated); 
            });
        }).finally(() => {
            ds.delete();
        });
    });
    
    it('Delete data source', () => {
        const ds = newDataSource(uuid());
        return ds.save().then(saved => {
            assertDataSource(ds, saved);
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
        const ds = newDataSource(uuid());
        return ds.save().then(saved => {
            assertDataSource(ds, saved);
            const dp = newDataPoint(uuid(), ds.xid, {
                startValue: 'false',
                changeType: 'NO_CHANGE',
                dataType: 'BINARY',
                settable: true,
                modelType: 'PL.VIRTUAL'
            });
            return dp.save().then(saved => {
                assertDataPoint(saved, dp);
            });
        }).finally(() => {
            ds.delete();
        });
    });
    
    it('Update data point', () => {
        const ds = newDataSource(uuid());
        return ds.save().then(saved => {
            assertDataSource(ds, saved);
            const dp = newDataPoint(uuid(), ds.xid, {
                startValue: 'false',
                changeType: 'NO_CHANGE',
                dataType: 'BINARY',
                settable: true,
                modelType: 'PL.VIRTUAL'
            });
            return dp.save().then(saved => {
                assertDataPoint(saved, dp);
                saved.pointLocator.startValue = 'true';
                saved.pointLocator.changeType = 'ALTERNATE_BOOLEAN';
                return saved.save().then(updated => {
                    assertDataPoint(updated, saved);
                });
            });
        }).finally(() => {
            ds.delete();
        });
    });
    
    it('Delete data point', () => {
        const ds = newDataSource(uuid());
        return ds.save().then(saved => {
            assertDataSource(ds, saved);
            const dp = newDataPoint(uuid(), ds.xid, {
                startValue: 'false',
                changeType: 'NO_CHANGE',
                dataType: 'BINARY',
                settable: true,
                modelType: 'PL.VIRTUAL'
            });
            return dp.save().then(saved => {
                assertDataPoint(saved, dp);
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
    
    function assertDataSource(saved, local) {
        assert.isNumber(saved.id);
        assert.strictEqual(saved.xid, local.xid);
        assert.strictEqual(saved.name, local.name);
        assert.strictEqual(saved.enabled, local.enabled);
        assert.strictEqual(saved.polling, local.polling);
        assert.strictEqual(saved.pollPeriod.periods, local.pollPeriod.periods);
        assert.strictEqual(saved.pollPeriod.type, local.pollPeriod.type);
        assertPermissions(saved.editPermission, local.editPermission);
        assertAlarmLevels(saved.eventAlarmLevels, local.eventAlarmLevels);
    }
    
    function assertDataPoint(saved, local) {
        assert.isNumber(saved.id);
        assert.strictEqual(saved.xid, local.xid);
        assert.strictEqual(saved.name, local.name);
        assert.strictEqual(saved.deviceName, local.deviceName);
        assert.strictEqual(saved.enabled, local.enabled);
        assertLoggingProperties(saved.loggingProperties, local.loggingProperties);
        assertTextRenderer(saved.textRenderer, local.textRenderer);
        assert.strictEqual(saved.dataSourceXid, local.dataSourceXid);
        assert.strictEqual(saved.useIntegralUnit, local.useIntegralUnit);
        assert.strictEqual(saved.useRenderedUnit, local.useRenderedUnit);
        assertPermissions(safeSplitPermission(saved.readPermission), safeSplitPermission(local.readPermission));
        assertPermissions(safeSplitPermission(saved.setPermission), safeSplitPermission(local.setPermission));
        assert.strictEqual(saved.chartColour, local.chartColour);
        assert.strictEqual(saved.rollup, local.rollup);
        assert.strictEqual(saved.plotType, local.plotType);
        assert.strictEqual(saved.purgeOverride, local.purgeOverride);
        assert.strictEqual(saved.purgePeriod.periods, local.purgePeriod.periods);
        assert.strictEqual(saved.purgePeriod.type, local.purgePeriod.type);
        assert.strictEqual(saved.unit, local.unit);
        assert.strictEqual(saved.integralUnit, local.integralUnit);
        assert.strictEqual(saved.renderedUnit, local.renderedUnit);
        assertPointLocator(saved.pointLocator, local.pointLocator);
    }
    
    function assertPointLocator(saved, local) {
        assert.strictEqual(saved.changeType, local.changeType);
        switch(saved.changeType) {
            case 'ALTERNATE_BOOLEAN':
                assert.strictEqual(saved.startValue, local.startValue);
            break;
            case 'NO_CHANGE':
                assert.strictEqual(saved.startValue, local.startValue);
            break;
        }
        assert.strictEqual(saved.dataSourceXid, local.dataSourceXid);
        assert.strictEqual(saved.dataSourceXid, local.dataSourceXid);
        assert.strictEqual(saved.dataSourceXid, local.dataSourceXid);
        assert.strictEqual(saved.dataSourceXid, local.dataSourceXid);
    }
    
    function assertTextRenderer(saved, local) {
        assert.strictEqual(saved.type, local.type);
        switch(local.type) {
            case 'textRendererBinary':
                assert.strictEqual(saved.zeroLabel, local.zeroLabel);
                assert.strictEqual(saved.zeroColour, local.zeroColour);
                assert.strictEqual(saved.oneLabel, local.oneLabel);
                assert.strictEqual(saved.oneColour, local.oneColour);
            break;
            case 'textRendererPlain':
                assert.strictEqual(saved.suffix, local.suffix);
                assert.strictEqual(saved.useUnitAsSuffix, local.useUnitAsSuffix);
            break;
        }
    }
    
    function assertLoggingProperties(saved, local) {
        assert.strictEqual(saved.loggingType, local.loggingType);
        assert.strictEqual(saved.tolerance, local.tolerance);
        
        assert.strictEqual(saved.discardExtremeValues, local.discardExtremeValues);
        if(saved.discardExtremeValues === true) {
            assert.strictEqual(saved.discardLowLimit, local.discardLowLimit);
            assert.strictEqual(saved.discardHighLimit, local.discardHighLimit);
        }
        assert.strictEqual(saved.cacheSize, local.cacheSize);
        
        switch(saved.loggingType) {
        case 'ON_CHANGE':
            break;
        case 'ALL':
            break;
        case 'NONE':
            break;
        case 'ON_TS_CHANGE':
            break;
        case 'INTERVAL':
            assert.strictEqual(saved.intervalLoggingType, local.intervalLoggingType);
        case 'ON_CHANGE_INTERVAL':
            assert.strictEqual(saved.intervalLoggingPeriod.periods, local.intervalLoggingPeriod.periods);
            assert.strictEqual(saved.intervalLoggingPeriod,periodType, local.intervalLoggingPeriod.periodType);
            assert.strictEqual(saved.overrideIntervalLoggingSamples, local.overrideIntervalLoggingSamples);
            if(saved.overrideIntervalLoggingSamples === true) {
                assert.strictEqual(saved.intervalLoggingSampleWindowSize, local.intervalLoggingSampleWindowSize);
            }
            break;
        }
    }
    
    function safeSplitPermission(permissionString) {
        if(permissionString != null) {
            return permissionString.split(',');
        }else{
            return [];
        }
    }
    
    function assertPermissions(saved, stored) {
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            assert.include(saved, stored[i], stored[i] + ' was not found in permissions')
        }
    }
    
    function assertAlarmLevels(saved, stored){
        var assertedEventTypes = [];
        assert.strictEqual(saved.length, stored.length);
        for(var i=0; i<stored.length; i++){
            var found = false;
            for(var j=0; j<saved.length; j++){
                if(stored[i].eventType === saved[j].eventType){
                    found = true;
                    assert.strictEqual(saved.level, stored.level);
                    assertedEventTypes.push(saved[i].eventType)
                    break;
                }
            }
            if(found === false)
                assert.fail('Did not find event type: ' + stored[i].eventType);
        }
    }

});
