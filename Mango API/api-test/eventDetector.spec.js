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

const {createClient, assertValidationErrors, login, defer, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const EventDetector = client.EventDetector;
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Event detector service', function() {
    this.timeout(5000);
    
    before('Login', function() { return login.call(this, client); });
    
    it('Delete a binary state event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'BINARY_STATE');
                return ed.save().then(savedEd => {
                    return savedEd.delete().then(deletedEd => {
                        return EventDetector.get(deletedEd.xid).then(ed => {
                            assert.fail('Should not have found detector ' + ed.xid);
                        }, error => {
                            assert.strictEqual(error.status, 404);
                        });
                    });
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Can query for a binary state event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'BINARY_STATE');
                return ed.save().then(savedEd => {
                    return EventDetector.query(`xid=${savedEd.xid}`).then(result => {
                        assert.strictEqual(1, result.total);
                        assert.strictEqual(savedEd.xid, result[0].xid);
                    });
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a binary state event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'BINARY_STATE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.state, savedEd.state);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a binary state event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'BINARY_STATE');
                ed.duration = null;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a no update event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'NO_UPDATE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a no update event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'BINARY_STATE');
                ed.duration = null;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });

    it('Creates a no change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'NO_CHANGE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a no change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'NO_CHANGE');
                ed.duration = null;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a state change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'STATE_CHANGE_COUNT');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.changeCount, savedEd.changeCount);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a state change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'BINARY',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'STATE_CHANGE_COUNT');
                ed.duration = null;
                ed.changeCount = -1;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['changeCount', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a alphanumeric regex state change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'ALPHANUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'ALPHANUMERIC_REGEX_STATE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.state, savedEd.state);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create an alphanumeric regex state change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'ALPHANUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'ALPHANUMERIC_REGEX_STATE');
                ed.duration = null;
                ed.state = null;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['state', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });

    it('Creates a analog change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'ANALOG_CHANGE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.checkIncrease, savedEd.checkIncrease);
                    assert.strictEqual(ed.checkDecrease, savedEd.checkDecrease);                   
                    assert.strictEqual(ed.limit, savedEd.limit);                    
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create an analog change event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'ANALOG_CHANGE');
                ed.duration = null;
                ed.checkIncrease = false;
                ed.checkDecrease = false;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['checkDecrease', 'checkIncrease', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a high limit event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'HIGH_LIMIT', client);
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.resetLimit, savedEd.resetLimit);
                    assert.strictEqual(ed.useResetLimit, savedEd.useResetLimit);                   
                    assert.strictEqual(ed.notHigher, savedEd.notHigher);
                    assert.strictEqual(ed.limit, savedEd.limit);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a high limit event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'HIGH_LIMIT');
                ed.duration = null;
                ed.useResetLimit = true;
                ed.notHigher = true;
                ed.resetLimit = 4;
                ed.limit = 4;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['resetLimit', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a low limit event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'LOW_LIMIT');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.resetLimit, savedEd.resetLimit);
                    assert.strictEqual(ed.useResetLimit, savedEd.useResetLimit);                   
                    assert.strictEqual(ed.notLower, savedEd.notLower);
                    assert.strictEqual(ed.limit, savedEd.limit);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a low limit event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'LOW_LIMIT');
                ed.duration = null;
                ed.useResetLimit = true;
                ed.notLower = true;
                ed.resetLimit = 4;
                ed.limit = 4;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['resetLimit', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a range event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'RANGE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.high, savedEd.high);
                    assert.strictEqual(ed.low, savedEd.low);                   
                    assert.strictEqual(ed.withinRange, savedEd.withinRange);
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a range event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'RANGE');
                ed.duration = null;
                ed.high = 10;
                ed.low = 11;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['high', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a negative cusum event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'NEGATIVE_CUSUM');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.limit, savedEd.limit);
                    assert.strictEqual(ed.weight, savedEd.weight);                   
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a negative cusum event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'NEGATIVE_CUSUM');
                ed.duration = null;
                ed.limit = "NaN";
                ed.weight = "NaN";
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['limit', 'weight', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a positive cusum event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'POSITIVE_CUSUM');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.limit, savedEd.limit);
                    assert.strictEqual(ed.weight, savedEd.weight);                   
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a positive cusum event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'POSITIVE_CUSUM');
                ed.duration = null;
                ed.limit = "NaN";
                ed.weight = "NaN";
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['limit', 'weight', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a smoothness event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'SMOOTHNESS');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.limit, savedEd.limit);
                    assert.strictEqual(ed.boxcar, savedEd.boxcar);                   
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a smoothness event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'NUMERIC',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'SMOOTHNESS');
                ed.duration = null;
                ed.limit = "NaN";
                ed.boxcar = "NaN";
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['limit', 'boxcar', 'duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Creates a multistate state event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'MULTISTATE',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'MULTISTATE_STATE');
                return ed.save().then(savedEd => {
                    assert.isNumber(savedEd.id);
                    assert.strictEqual(ed.xid, savedEd.xid);
                    assert.strictEqual(ed.name, savedEd.name);
                    assert.strictEqual(ed.state, savedEd.state);    
                    assert.strictEqual(ed.duration.periods, savedEd.duration.periods);
                    assert.strictEqual(ed.duration.periodType, savedEd.duration.periodType);
                    assert.strictEqual(ed.alarmLevel, savedEd.alarmLevel);
                    assert.strictEqual(ed.detectorSourceType, savedEd.detectorSourceType);
                    assert.strictEqual(ed.sourceId, savedEd.sourceId);
                    assert.strictEqual(ed.detectorType, savedEd.detectorType);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
    it('Fails to create a multistate state event detector', () => {
        const ds = new DataSource({ modelType: 'MOCK' });
        return ds.save().then(savedDs => {
            ds.id = savedDs.id;
            const dp = new DataPoint({
                dataSourceXid: savedDs.xid,
                pointLocator : {
                dataType: 'MULTISTATE',
                settable: false,
                modelType: 'PL.MOCK'
                    }
            });
            return dp.save().then(savedDp => {
                const ed = EventDetector.createEventDetector(savedDp.id, 'MULTISTATE_STATE');
                ed.duration = null;
                return ed.save().then(savedEd => {
                    assert.fail('Should not have saved detector ' + savedEd.xid);
                }, error => {
                    assertValidationErrors(['duration','durationType'], error);
                });
            })
        }).finally(() => {
            return ds.delete();
        });
    });
    
  //Tests for websockets
    it('Gets websocket notifications for event detectors', function() {
      this.timeout(5000);

      const socketOpenDeferred = defer();
      const gotAddEventDeferred = defer();
      const gotUpdateEventDeferred = defer();
      const gotDeleteEventDeferred = defer();
      
      //Create the event detector for add message
      const ds = new DataSource({ modelType: 'MOCK' });
      let ed,originalXid;
      
      return Promise.resolve().then(() => {
          const ws = client.openWebSocket({
              path: '/rest/latest//websocket/event-detectors'
          });

          ws.on('open', () => {
              socketOpenDeferred.resolve();
          });

          ws.on('error', error => {
              const msg = new Error(`WebSocket error, error: ${error}`);
              socketOpenDeferred.reject(msg);
              gotAddEventDeferred.reject(msg);
              gotUpdateEventDeferred.reject(msg);
              gotDeleteEventDeferred.reject(msg);
          });

          ws.on('close', (code, reason) => {
              const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
              socketOpenDeferred.reject(msg);
              gotAddEventDeferred.reject(msg);
              gotUpdateEventDeferred.reject(msg);
              gotDeleteEventDeferred.reject(msg);
          });

          ws.on('message', msgStr => {
              assert.isString(msgStr);

              const msg = JSON.parse(msgStr);
              //console.log(msg.payload);
              assert.strictEqual(msg.status, 'OK');
              if(msg.payload.action === 'add'){
                assert.strictEqual(msg.payload.object.xid, ed.xid);
                assert.strictEqual(msg.payload.originalXid, null);
                gotAddEventDeferred.resolve();
              }else if(msg.payload.action === 'update'){
                assert.strictEqual(msg.payload.object.xid, ed.xid);
                assert.strictEqual(msg.payload.object.name, ed.name);
                assert.strictEqual(msg.payload.originalXid, originalXid);
                gotUpdateEventDeferred.resolve();
              }else if(msg.payload.action === 'delete'){
                assert.strictEqual(msg.payload.object.xid, ed.xid);
                assert.strictEqual(msg.payload.originalXid, null);
                ws.close();
                gotDeleteEventDeferred.resolve();
              }
          });

          return socketOpenDeferred.promise;
        }).then(() => delay(500)).then(() => {
            return ds.save().then(savedDs => {
                ds.id = savedDs.id;
                const dp = new DataPoint({
                    dataSourceXid: savedDs.xid,
                    pointLocator : {
                    dataType: 'BINARY',
                    settable: false,
                    modelType: 'PL.MOCK'
                        }
                });
                return dp.save().then(savedDp => {
                    ed = EventDetector.createEventDetector(savedDp.id, 'BINARY_STATE');
                    return ed.save().then(savedEd => {
                        originalXid = savedEd.xid;
                    });
                })
            });
        }).then(() => gotAddEventDeferred.promise).then(()=>{
          ed.name = 'new name';
          ed.xid = ed.xid + '_UPDATE';
          return ed.save();
        }).then(() => gotUpdateEventDeferred.promise).then(()=>{
          return ed.delete();
        }).then(() => gotDeleteEventDeferred.promise);
    });
    
});
