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

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;
const moment = require('moment-timezone');

describe('Point values v2', function() {
    before('Login', function() { return login.call(this, client); });
    this.timeout(10000000);
    const newDataPoint = (xid, dsXid, rollupType, simplifyType, simplifyValue) => {
        return new DataPoint({
            xid: xid,
            enabled: true,
            name: 'Point values test',
            deviceName: 'Point values test',
            dataSourceXid : dsXid,
            pointLocator : {
                startValue : '0',
                modelType : 'PL.VIRTUAL',
                dataType : 'NUMERIC',
                changeType : 'NO_CHANGE',
                settable: true
            },
            textRenderer: {
                type: 'textRendererAnalog',
                format: '0.00',
                suffix: '',
                useUnitAsSuffix: false,
                unit: '',
                renderedUnit: ''
            },
            rollup: rollupType,
            simplifyType: simplifyType,
            simplifyTolerance: simplifyValue
        });
    };

    const generateSamples = (xid, startTime, numSamples, pollPeriod) => {
        const pointValues = [];
        let time = startTime;
        let startValue = 0;
        for (let i = 0; i < numSamples; i++) {
            pointValues.push({
                xid: xid,
                value: startValue + (Math.random() * 20 - 10),
                timestamp: time,
                dataType: 'NUMERIC'
            });
            time += pollPeriod;
        }
        return pointValues;
    };

    const comparePointValues = (options) => {
        const valueProperty = options.valueProperty || 'value';
        let responseData = options.responseData;
        let expectedValues = options.expectedValues;

        assert.isArray(responseData);
        assert.strictEqual(responseData.length, expectedValues.length);

        expectedValues.forEach((expectedValue, i) => {
            assert.strictEqual(responseData[i].timestamp, expectedValue.timestamp);

            const value = responseData[i][valueProperty];
            if (typeof value === 'number') {
                assert.strictEqual(value, expectedValue.value);
            } else {
                assert.strictEqual(value.value, expectedValue.value);
            }
        });
    };

    const insertionDelay = 5000 + 500 * 5; //Delay to insert the values

    const numSamples = 100;
    const pollPeriod = 1000; //in ms
    const endTime = new Date().getTime();
    const startTime = endTime - (numSamples * pollPeriod);
    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    const testPointXid3 = uuid();
    const testPointXid4 = uuid();
    const testPointXid5 = uuid();
    
    const pointValues1 = generateSamples(testPointXid1, startTime, numSamples, pollPeriod);
    const pointValues2 = generateSamples(testPointXid2, startTime, numSamples, pollPeriod);
    const pointValues3 = generateSamples(testPointXid3, startTime, numSamples, pollPeriod);
    const pointValues4 = generateSamples(testPointXid4, startTime, numSamples, pollPeriod);
    const pointValues5 = generateSamples(testPointXid5, startTime, numSamples, pollPeriod);

    before('Create a virtual data source, points, and insert values', function() {
        this.timeout(insertionDelay * 2);

        this.ds = new DataSource({
            xid: uuid(),
            name: 'Mango client test',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'HOURS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return this.ds.save().then((savedDs) => {
            assert.strictEqual(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
        }).then(() => {
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid, 'FIRST', 'NONE', 0);
            this.testPoint2 = newDataPoint(testPointXid2, this.ds.xid, 'FIRST', 'NONE', 0);
            this.testPoint3 = newDataPoint(testPointXid3, this.ds.xid, 'COUNT', 'TOLERANCE', 10.0);
            this.testPoint4 = newDataPoint(testPointXid4, this.ds.xid, 'COUNT', 'NONE', 0);
            this.testPoint5 = newDataPoint(testPointXid5, this.ds.xid, 'NONE', 'NONE', 0);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save(), this.testPoint3.save(), this.testPoint4.save(), this.testPoint5.save()]);
        }).then(() => {
            const valuesToInsert = pointValues1.concat(pointValues2.concat(pointValues3.concat(pointValues4.concat(pointValues5))));
            return client.pointValues.insert(valuesToInsert);
        }).then(() => delay(insertionDelay));
    });

    after('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });

    it('Gets latest point values for a data point', function() {
        return client.pointValues.latest({
            xid: testPointXid1
        }).then(result => {
            assert.isArray(result);
            comparePointValues({
                responseData: result.slice().reverse(),
                expectedValues: pointValues1
            });
        });
    });

    it('Gets latest point values for a data point, using cache only', function() {
        return client.pointValues.latest({
            xid: testPointXid1,
            useCache: 'CACHE_ONLY',
            fields: ['TIMESTAMP', 'VALUE', 'CACHED']
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 1); // default cache size is 1
            assert.strictEqual(result[0].cached, true);
        });
    });

    it('Gets latest point values for a data point, using cache both', function() {
        return client.pointValues.latest({
            xid: testPointXid1,
            useCache: 'BOTH',
            fields: ['TIMESTAMP', 'VALUE', 'CACHED']
        }).then(result => {
            assert.isArray(result);
            const reversedResult = result.slice().reverse();

            assert.strictEqual(result[0].cached, true); // default cache size is 1

            comparePointValues({
                responseData: reversedResult,
                expectedValues: pointValues1
            });
        });
    });

    it('Gets latest point values for a data point with a limit of 20', function() {
        return client.pointValues.latest({
            xid: testPointXid1,
            limit: 20
        }).then(result => {
            assert.isArray(result);
            comparePointValues({
                responseData: result.slice().reverse(),
                expectedValues: pointValues1.slice(-20)
            });
        });
    });

    it('Gets latest point values for multiple points as a single array', function() {
        return client.pointValues.latestAsSingleArray({
            xids: [testPointXid1, testPointXid2]
        }).then(result => {
            assert.isArray(result);

            comparePointValues({
                responseData: result.slice().reverse(),
                expectedValues: pointValues1,
                valueProperty: testPointXid1
            });
            comparePointValues({
                responseData: result.slice().reverse(),
                expectedValues: pointValues2,
                valueProperty: testPointXid2
            });
        });
    });

    it('Gets latest point values for multiple points', function() {
        return client.pointValues.latest({
            xids: [testPointXid1, testPointXid2]
        }).then(result => {
            assert.isArray(result[testPointXid1]);
            assert.isArray(result[testPointXid2]);

            comparePointValues({
                responseData: result[testPointXid1].slice().reverse(),
                expectedValues: pointValues1
            });
            comparePointValues({
                responseData: result[testPointXid2].slice().reverse(),
                expectedValues: pointValues2
            });
        });
    });

    it('Queries time period for multiple points as single array', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime
        }).then(result => {
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1,
                valueProperty: testPointXid1
            });
            comparePointValues({
                responseData: result,
                expectedValues: pointValues2,
                valueProperty: testPointXid2
            });
        });
    });

    it('Queries time period for multiple points as single array with limit 20', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            limit: 20
        }).then(result => {
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1.slice(0, 10),
                valueProperty: testPointXid1
            });
            comparePointValues({
                responseData: result,
                expectedValues: pointValues2.slice(0, 10),
                valueProperty: testPointXid2
            });
        });
    });

    it('Queries time period for multiple points', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime
        }).then(result => {
            comparePointValues({
                responseData: result[testPointXid1],
                expectedValues: pointValues1
            });
            comparePointValues({
                responseData: result[testPointXid2],
                expectedValues: pointValues2
            });
        });
    });

    it('Queries time period for multiple points with limit 20', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            limit: 20
        }).then(result => {
            comparePointValues({
                responseData: result[testPointXid1],
                expectedValues: pointValues1.slice(0, 20)
            });
            comparePointValues({
                responseData: result[testPointXid2],
                expectedValues: pointValues2.slice(0, 20)
            });
        });
    });

    it('Queries time period for single point', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime
        }).then(result => {
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });
        });
    });

    it('Queries time period for single point with limit 20', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            limit: 20
        }).then(result => {
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1.slice(0, 20)
            });
        });
    });

    it('Queries time period with no start bookend, regular end bookend', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime - 10,
            to: endTime + 10,
            bookend: true,
            fields: ['TIMESTAMP', 'VALUE', 'BOOKEND']
        }).then(result => {
            assert.isArray(result);
            const startBookend = result.shift();
            const endBookend = result.pop();
            assert.strictEqual(startBookend.value, null);
            assert.isTrue(endBookend.bookend);
            assert.strictEqual(endBookend.timestamp, endTime + 10);
            assert.strictEqual(endBookend.value, result[result.length - 1].value);

            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });
        });
    });

    it('Does not return a start bookend when there is a value at from time', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            bookend: true,
            fields: ['TIMESTAMP', 'VALUE', 'BOOKEND']
        }).then(result => {
            assert.isArray(result);
            assert.notProperty(result[0], 'bookend');

            assert.isAbove(result[1].timestamp, result[0].timestamp);

            const endBookend = result.pop();
            assert.isTrue(endBookend.bookend);
            assert.strictEqual(endBookend.timestamp, endTime);
            assert.strictEqual(endBookend.value, result[result.length - 1].value);

            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });
        });
    });

    it('Simplify of single point adds bookends for periods with no data', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime - 60000,
            to: startTime,
            simplifyTolerance: 10,
            bookend: true,
            fields: ['BOOKEND', 'VALUE', 'TIMESTAMP']
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].bookend, true);
            assert.strictEqual(result[0].value, null);
            assert.strictEqual(result[0].timestamp, startTime - 60000);
            
            assert.strictEqual(result[1].bookend, true);
            assert.strictEqual(result[1].value, null);
            assert.strictEqual(result[1].timestamp, startTime);
        });
    });
    
    it('Simplify works for single point, time period and tolerance', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            simplifyTolerance: 10
        }).then(result => {
            assert.isArray(result);
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = startTime;
            result.forEach(pv => {
                assert.isNumber(pv.value);
                assert.isNumber(pv.timestamp);
                assert.isAtLeast(pv.timestamp, prevTime);
                assert.isBelow(pv.timestamp, endTime);
                prevTime = pv.timestamp;
            });
        });
    });

    it('Simplify works for single point, latest values and tolerance', function() {
        return client.pointValues.latest({
            xid: testPointXid1,
            simplifyTolerance: 10
        }).then(result => {
            assert.isArray(result);
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = endTime;
            result.slice().reverse().forEach(pv => {
                assert.isNumber(pv.value);
                assert.isNumber(pv.timestamp);
                assert.isAtMost(pv.timestamp, prevTime);
                assert.isBelow(pv.timestamp, endTime);
                prevTime = pv.timestamp;
            });
        });
    });

    it('Simplify works for single point, time period and target', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            simplifyTarget: 50
        }).then(result => {
            assert.isArray(result);
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = startTime;
            result.forEach(pv => {
                assert.isNumber(pv.value);
                assert.isNumber(pv.timestamp);
                assert.isAtLeast(pv.timestamp, prevTime);
                assert.isBelow(pv.timestamp, endTime);
                prevTime = pv.timestamp;
            });
        });
    });

    it('Simplify works for single point, latest values and target', function() {
        return client.pointValues.latest({
            xid: testPointXid1,
            simplifyTarget: 50
        }).then(result => {
            assert.isArray(result);
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = endTime;
            result.slice().reverse().forEach(pv => {
                assert.isNumber(pv.value);
                assert.isNumber(pv.timestamp);
                assert.isAtMost(pv.timestamp, prevTime);
                assert.isBelow(pv.timestamp, endTime);
                prevTime = pv.timestamp;
            });
        });
    });

    it('Formats timestamps correctly', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            dateTimeFormat: 'yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX'
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.isString(pv.timestamp);
                assert.strictEqual(moment(pv.timestamp).valueOf(), pointValues1[i].timestamp);
            });
        });
    });

    it('Returns rendered values correctly', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            fields: ['TIMESTAMP', 'VALUE', 'RENDERED']
        }).then(result => {
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });

            result.forEach((pv, i) => {
                assert.strictEqual(pv.rendered, '' + pointValues1[i].value.toFixed(2));
            });
        });
    });

    it('Returns the same point values using a FIRST rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the zeros as single array using a COUNT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1],
            from: startTime - 60000,
            to: startTime,
            rollup: 'COUNT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 60);
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, 0);
                assert.strictEqual(pv[testPointXid1].timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as single array using a FIRST rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1],
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv[testPointXid1].timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns nulls as single array using a POINT_DEFAULT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 60);

            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, null);
                assert.strictEqual(pv[testPointXid1].timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as single array using a POINT_DEFAULT rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv[testPointXid1].timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as single array using a NONE rollup', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1],
            from: startTime,
            to: endTime,
            rollup: 'NONE',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv[testPointXid1].timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as single array using POINT_DEFAULT rollup and Simplify with same time period as poll period', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid3],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);

            assert.isBelow(result.length, pointValues1.length);

            let prevTime = startTime;
            result.forEach(pv => {
                assert.isNumber(pv[testPointXid3].value);
                assert.isNumber(pv[testPointXid3].timestamp);
                assert.isAtLeast(pv[testPointXid3].timestamp, prevTime);
                assert.isBelow(pv[testPointXid3].timestamp, endTime);
                prevTime = pv[testPointXid3].timestamp;
            });

        });
    });
    
    it('Returns zeros for two points as single array using a COUNT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime - 60000,
            to: startTime,
            rollup: 'COUNT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, 0);
                assert.strictEqual(pv.timestamp, prevTime);
                assert.strictEqual(pv[testPointXid2].value, 0);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a FIRST rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);
            
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
                assert.strictEqual(pv[testPointXid2].value, pointValues2[i].value);
                assert.strictEqual(pv.timestamp, pointValues2[i].timestamp);
            });
        });
    });
    
    it('Uses correct rollup for two points as single array using a POINT_DEFAULT for minute before start time', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid4],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                assert.strictEqual(pv[testPointXid4].value, 0);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns null values for two points as single array using a POINT_DEFAULT for minute before start time', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                assert.strictEqual(pv[testPointXid2].value, null);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a POINT_DEFAULT rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);
            
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
                assert.strictEqual(pv[testPointXid2].value, pointValues2[i].value);
                assert.strictEqual(pv.timestamp, pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a NONE rollup', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            rollup: 'NONE',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);
            
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
                assert.strictEqual(pv[testPointXid2].value, pointValues2[i].value);
                assert.strictEqual(pv.timestamp, pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a POINT_DEFAULT rollup and Simplify', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid3],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length);
            
            let testPoint3Count = 0;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1].value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
                if(typeof pv[testPointXid3] !== 'undefined')
                    testPoint3Count++;
            });
            assert.isBelow(testPoint3Count, result.length);
        });
    });

    it('Simplify of multiple points as single array adds bookends for periods with no data', function() {
        return client.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime - 60000,
            to: startTime,
            fields: ['XID', 'BOOKEND', 'VALUE', 'TIMESTAMP'],
            bookend: true
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, 2);
            
            assert.strictEqual(result[0][testPointXid1].bookend, true);
            assert.strictEqual(result[0][testPointXid1].value, null);
            assert.strictEqual(result[0][testPointXid2].bookend, true);
            assert.strictEqual(result[0][testPointXid2].value, null);
            assert.strictEqual(result[0].timestamp, startTime - 60000);
            
            assert.strictEqual(result[1][testPointXid1].bookend, true);
            assert.strictEqual(result[1][testPointXid1].value, null);
            assert.strictEqual(result[1][testPointXid2].bookend, true);
            assert.strictEqual(result[1][testPointXid2].value, null);
            assert.strictEqual(result[1].timestamp, startTime);

        });
    });
    
    it('Simplify of multiple points as multiple arrays adds bookends for periods with no data', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime - 60000,
            to: startTime,
            simplifyTolerance: 10,
            bookend: true,
            fields: ['BOOKEND', 'VALUE', 'TIMESTAMP']
        }).then(result => {
            assert.isObject(result);
            
            const point1Result = result[testPointXid1];
            const point2Result = result[testPointXid2];
            assert.isArray(point1Result);
            assert.isArray(point2Result);
            
            assert.strictEqual(point1Result.length, 2);
            assert.strictEqual(point1Result[0].bookend, true);
            assert.strictEqual(point1Result[0].value, null);
            assert.strictEqual(point1Result[0].timestamp, startTime - 60000);
            
            assert.strictEqual(point1Result[1].bookend, true);
            assert.strictEqual(point1Result[1].value, null);
            assert.strictEqual(point1Result[1].timestamp, startTime);
            
            assert.strictEqual(point2Result.length, 2);
            assert.strictEqual(point2Result[0].bookend, true);
            assert.strictEqual(point2Result[0].value, null);
            assert.strictEqual(point2Result[0].timestamp, startTime - 60000);
            
            assert.strictEqual(point2Result[1].bookend, true);
            assert.strictEqual(point2Result[1].value, null);
            assert.strictEqual(point2Result[1].timestamp, startTime);
        });
    });
    
    it('Returns zeros as multiple arrays using a COUNT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1],
            from: startTime - 60000,
            to: startTime,
            rollup: 'COUNT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            const point1Result = result[testPointXid1];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, 60);

            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, 0);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });

    it('Returns nulls as multiple arrays using a default NONE rollup for minute before start time', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid5],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            const point5Result = result[testPointXid5];
            assert.isArray(point5Result);
            assert.strictEqual(point5Result.length, 60);

            let prevTime = startTime - 60000;
            point5Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a FIRST rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1],
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, pointValues1.length);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a default NONE rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid5],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point5Result = result[testPointXid5];
            assert.isArray(point5Result);
            assert.strictEqual(point5Result.length, pointValues5.length);

            point5Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues5[i].value);
                assert.strictEqual(pv.timestamp, pointValues5[i].timestamp);
            });
        });
    });
    
    it('Returns null values as multiple arrays using a POINT_DEFAULT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, 60);

            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a POINT_DEFAULT rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, pointValues1.length);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a NONE rollup', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1],
            from: startTime,
            to: endTime,
            rollup: 'NONE',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, pointValues1.length);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a POINT_DEFAULT rollup and Simplify', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid3],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point3Result = result[testPointXid3];
            
            assert.isArray(point3Result);
            assert.isBelow(point3Result.length, pointValues3.length);
                        
            let prevTime = startTime;
            point3Result.forEach(pv => {
                assert.isNumber(pv.value);
                assert.isNumber(pv.timestamp);
                assert.isAtLeast(pv.timestamp, prevTime);
                assert.isBelow(pv.timestamp, endTime);
                prevTime = pv.timestamp;
            });
            
        });
    });
    
    it('Returns zeros for 2 points as multiple arrays using a COUNT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime - 60000,
            to: startTime,
            rollup: 'COUNT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            let point2Result = result[testPointXid2];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, 60);
            assert.isArray(point2Result);
            assert.strictEqual(point2Result.length, 60);

            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, 0);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
            prevTime = startTime - 60000;
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, 0);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a FIRST rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            let point2Result = result[testPointXid2];
            assert.isArray(point1Result);
            assert.strictEqual(point1Result.length, pointValues1.length);
            assert.isArray(point2Result);
            assert.strictEqual(point2Result.length, pointValues2.length);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues2[i].value);
                assert.strictEqual(pv.timestamp, pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns null values for 3 points as multiple arrays using a POINT_DEFAULT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2, testPointXid5],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            const point1Result = result[testPointXid1];
            const point2Result = result[testPointXid2];
            const point5Result = result[testPointXid5];
            
            assert.isArray(point1Result);
            assert.isArray(point2Result);
            assert.isArray(point5Result);
            
            assert.strictEqual(point1Result.length, 60);
            assert.strictEqual(point2Result.length, 60);
            assert.strictEqual(point5Result.length, 60);
            
            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
            
            prevTime = startTime - 60000;
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
            
            prevTime = startTime - 60000;
            point5Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Uses correct rollup for 2 points as multiple arrays using a POINT_DEFAULT rollup for minute before start time', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid4],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            let point4Result = result[testPointXid4];
            assert.isArray(point1Result);
            assert.isArray(point4Result);
            assert.strictEqual(point1Result.length, 60);
            assert.strictEqual(point4Result.length, 60);
            
            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, null);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
            prevTime = startTime - 60000;
            point4Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, 0);
                assert.strictEqual(pv.timestamp, prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a POINT_DEFAULT rollup with same time period as poll period', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            let point2Result = result[testPointXid2];
            assert.isArray(point1Result);
            assert.isArray(point2Result);
            assert.strictEqual(point1Result.length, pointValues1.length);
            assert.strictEqual(point2Result.length, pointValues2.length);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues2[i].value);
                assert.strictEqual(pv.timestamp, pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a NONE rollup', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            rollup: 'NONE',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            let point2Result = result[testPointXid2];
            assert.isArray(point1Result);
            assert.isArray(point2Result);
            assert.strictEqual(point1Result.length, pointValues1.length);
            assert.strictEqual(point2Result.length, pointValues2.length);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues2[i].value);
                assert.strictEqual(pv.timestamp, pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a POINT_DEFAULT rollup and Simplify', function() {
        return client.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid3],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isObject(result);
            let point1Result = result[testPointXid1];
            let point3Result = result[testPointXid3];
            
            assert.isArray(point1Result);
            assert.isArray(point3Result);
            assert.strictEqual(point1Result.length, pointValues1.length);
            assert.isBelow(point3Result.length, pointValues3.length);
            
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i].value);
                assert.strictEqual(pv.timestamp, pointValues1[i].timestamp);
            });
            
            let prevTime = startTime;
            point3Result.forEach(pv => {
                assert.isNumber(pv.value);
                assert.isNumber(pv.timestamp);
                assert.isAtLeast(pv.timestamp, prevTime);
                assert.isBelow(pv.timestamp, endTime);
                prevTime = pv.timestamp;
            });
            
        });
    });
    
    it('Returns the correct number of point values when downsampling using a rollup', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 5,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            assert.strictEqual(result.length, pointValues1.length / 5);

            result.forEach((pv, i) => {
                assert.strictEqual(pv.value, pointValues1[i * 5].value);
                assert.strictEqual(pv.timestamp, pointValues1[i * 5].timestamp);
            });
        });
    });

    it('Can truncate to the nearest minute when doing a rollup', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime + 20000,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'MINUTES'
            },
            truncate: true
        }).then(result => {
            assert.isArray(result);
            // should always have 3 samples as we have 100 point values with 1 second period
            // first value is start of first minute and last value will be expanded to start of 3rd minute
            assert.isAtLeast(result.length, 2);
            assert.isAtMost(result.length, 3); //Expanding a time period across midnight will result in 3 days

            assert.strictEqual(result[0].value, pointValues1[0].value);
            assert.strictEqual(moment(result[0].timestamp).toISOString(),
                    moment(pointValues1[0].timestamp).startOf('minute').toISOString());
        });
    });

    it('Can truncate to the start of the day using the correct timezone when doing a rollup', function() {
        return client.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime + 86400000, //If we are truncating then we need to run our query over > 1 day else from==to
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'DAYS'
            },
            truncate: true,
            timezone: 'Australia/Sydney'
        }).then(result => {
            assert.isArray(result);

            // depending on when we run the test the point values might fall across two days, but will be almost always length 1
            assert.isAtLeast(result.length, 1);
            assert.isAtMost(result.length, 2); //Expanding a time period across midnight will result in 3 days

            assert.strictEqual(result[0].value, pointValues1[0].value);
            assert.strictEqual(moment.tz(result[0].timestamp, 'Australia/Sydney').format(),
                    moment.tz(pointValues1[0].timestamp, 'Australia/Sydney').startOf('day').format());
        });
    });
    
    // Update an existing data point's value - Data point must exist and be enabled
    it.skip('PUT /rest/v1/point-values/{xid}', function() {
        const requestBody = {
            annotation: 'test annotation',
            dataType: 'NUMERIC',
            timestamp: 0,
            value: 123
        };
        const params = {
            model: requestBody, // in = body, description = model, required = true, type = , default = , enum = 
            unitConversion: false, // in = query, description = Return converted value using displayed unit, required = false, type = boolean, default = false, enum = 
            xid: uuid() // in = path, description = xid, required = true, type = string, default = , enum = 
        };
        
        return Promise.resolve().then(() => {
            return client.restRequest({
                method: 'PUT',
                path: `/rest/v2/point-values/${params.xid}`,
                params: {
                    unitConversion: params.unitConversion
                },
                data: requestBody
            });
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            // MODEL: PointValueTimeModel
            assert.isObject(response.data, 'data');
            assert.isString(response.data.annotation, 'data.annotation');
            assert.isString(response.data.dataType, 'data.dataType');
            assert.include(["ALPHANUMERIC","BINARY","MULTISTATE","NUMERIC","IMAGE"], response.data.dataType, 'data.dataType');
            assert.isNumber(response.data.timestamp, 'data.timestamp');
            assert.isObject(response.data.value, 'data.value');
            // END MODEL: PointValueTimeModel
        });
    });

});
