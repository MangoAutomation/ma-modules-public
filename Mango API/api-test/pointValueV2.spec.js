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

const config = require('@infinite-automation/mango-client/test/setup');
const uuidV4 = require('uuid/v4');
const moment = require('moment-timezone');

describe('Point values v2', function() {
    before('Login', config.login);

    const newDataPoint = (xid, dsXid) => {
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
            }
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

    const insertionDelay = 1000;

    const numSamples = 100;
    const pollPeriod = 1000; //in ms
    const endTime = new Date().getTime();
    const startTime = endTime - (numSamples * pollPeriod);
    const testPointXid1 = uuidV4();
    const testPointXid2 = uuidV4();

    const pointValues1 = generateSamples(testPointXid1, startTime, numSamples, pollPeriod);
    const pointValues2 = generateSamples(testPointXid2, startTime, numSamples, pollPeriod);

    before('Create a virtual data source, points, and insert values', function() {
        this.timeout(insertionDelay * 2);

        this.ds = new DataSource({
            xid: uuidV4(),
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
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid);
            this.testPoint2 = newDataPoint(testPointXid2, this.ds.xid);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save()]);
        }).then(() => {
            const valuesToInsert = pointValues1.concat(pointValues2);
            return client.pointValues.insert(valuesToInsert);
        }).then(() => config.delay(insertionDelay));
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

    it('Does GET requests');
});
