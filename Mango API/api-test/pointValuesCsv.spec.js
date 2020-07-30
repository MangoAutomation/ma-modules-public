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
const csvParser = require('csv-parser');
const Readable = require('stream').Readable;

function pointValuesCsvFactory(client) {

    return class PointValueCsv {
        get baseUrl() {
            return '/rest/latest/point-values';
        }
        
        toRequestBody(options) {
            const requestBody = Object.assign({}, options);
            if (requestBody.xid) {
                requestBody.xids = [requestBody.xid];
                delete requestBody.xid;
            }
            
            if (requestBody.from) {
                let from = moment(requestBody.from);
                if (requestBody.timezone) {
                    from = from.tz(requestBody.timezone);
                }
                requestBody.from = from.toISOString();
            }
            
            if (requestBody.to) {
                let to = moment(requestBody.to);
                if (requestBody.timezone) {
                    to = to.tz(requestBody.timezone);
                }
                requestBody.to = to.toISOString();
            }

            return requestBody;
        }
        
        latest(options) {
            const requestBody = this.toRequestBody(options);
            
            return client.restRequest({
                path: `${this.baseUrl}/multiple-arrays/latest`,
                method: 'POST',
                data: requestBody
            }).then(response => {
                return new Promise((resolve, reject) => {
                    const result = [];
                    const s = new Readable();
                    s.push(response.data);
                    s.push(null);
                    s.pipe(csvParser())
                     .on('headers', function(headers){result.push(headers);})
                     .on('data', function (data){
                         result.push(data);
                     })
                     .on('end', function() {
                         resolve(result);              
                     })
                     .on('error', reject);
                });
            });
        }
        
        latestAsSingleArray(options) {
            if (options.xid) {
                return this.latest(options);
            }
            const requestBody = this.toRequestBody(options);

            return client.restRequest({
                path: `${this.baseUrl}/single-array/latest`,
                method: 'POST',
                data: requestBody
            }).then(response => {
                return new Promise((resolve, reject) => {
                    const result = [];
                    const s = new Readable();
                    s.push(response.data);
                    s.push(null);
                    s.pipe(csvParser())
                     .on('headers', function(headers){result.push(headers);})
                     .on('data', function (data){
                         result.push(data);
                     })
                     .on('end', function() {
                         resolve(result);              
                     })
                     .on('error', reject);
                });
            });
        }

        forTimePeriod(options) {
            const requestBody = this.toRequestBody(options);
            
            let url = `${this.baseUrl}/multiple-arrays/time-period`;
            if (typeof options.rollup === 'string' && options.rollup.toUpperCase() !== 'NONE') {
                url += '/' + encodeURIComponent(options.rollup.toUpperCase());
            }
            
            return client.restRequest({
                path: url,
                method: 'POST',
                data: requestBody
            }).then(response => {
                return new Promise((resolve, reject) => {
                    const result = [];
                    const s = new Readable();
                    s.push(response.data);
                    s.push(null);
                    s.pipe(csvParser())
                     .on('headers', function(headers){result.push(headers);})
                     .on('data', function (data){
                         result.push(data);
                     })
                     .on('end', function() {
                         resolve(result);              
                     })
                     .on('error', reject);
                });
            });
        }
        
        forTimePeriodAsSingleArray(options) {
            if (options.xid) {
                return this.forTimePeriod(options);
            }
            const requestBody = this.toRequestBody(options);
            
            let url = `${this.baseUrl}/single-array/time-period`;
            if (typeof options.rollup === 'string' && options.rollup.toUpperCase() !== 'NONE') {
                url += '/' + encodeURIComponent(options.rollup.toUpperCase());
            }

            return client.restRequest({
                path: url,
                method: 'POST',
                data: requestBody
            }).then(response => {
                return new Promise((resolve, reject) => {
                    const result = [];
                    const s = new Readable();
                    s.push(response.data);
                    s.push(null);
                    s.pipe(csvParser())
                     .on('headers', function(headers){result.push(headers);})
                     .on('data', function (data){
                         result.push(data);
                     })
                     .on('end', function() {
                         resolve(result);              
                     })
                     .on('error', reject);
                });
            });
        }

        /*
         * Save point values Array of:
         *  {xid,value,dataType,timestamp,annotation}
         */
        insert(values) {
            return client.restRequest({
                path: this.baseUrl,
                method: 'POST',
                data: values
            }).then(response => {
                return response.data;
            });
        }
        
        /**
         * Delete values >= from and < to
         */
        purge(options) {
            return client.restRequest({
                path: `${this.baseUrl}/${encodeURIComponent(options.xid)}`,
                method: 'DELETE',
                params: this.toRequestBody({
                    to: options.to,
                    from: options.from,
                    timezone: options.timezone
                })
            }).then(response => {
                return response.data;
            });
        }
    };
}

describe('Point values csv', function() {
    before('Login', function() {
        return login.call(this, client).then((...args) => {
            this.csvClient = createClient({
                defaultHeaders: {
                    Accept : 'text/csv'
                }
            });
            
            //Override to return strings
            const restRequest = this.csvClient.restRequest;
            this.csvClient.restRequest = function(optionsArg) {
                optionsArg.dataType = 'string';
                return restRequest.apply(this, arguments);
            };
            const PointValuesCsv = pointValuesCsvFactory(this.csvClient);
            this.csvClient.pointValues = new PointValuesCsv();
            
            // copy the session cookie to the csv client
            Object.assign(this.csvClient.cookies, client.cookies);
        });
    });

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
            assert.strictEqual(Number(responseData[i].timestamp), expectedValue.timestamp);
            assert.strictEqual(Number(responseData[i][valueProperty]), expectedValue.value);
        });
    };

    const insertionDelay = 5000 + 500 * 4; //Delay to insert the values

    const numSamples = 100;
    const pollPeriod = 1000; //in ms
    const endTime = new Date().getTime();
    const startTime = endTime - (numSamples * pollPeriod);
    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    const testPointXid3 = uuid();
    const testPointXid4 = uuid();
    
    const pointValues1 = generateSamples(testPointXid1, startTime, numSamples, pollPeriod);
    const pointValues2 = generateSamples(testPointXid2, startTime, numSamples, pollPeriod);
    const pointValues3 = generateSamples(testPointXid3, startTime, numSamples, pollPeriod);
    const pointValues4 = generateSamples(testPointXid4, startTime, numSamples, pollPeriod);

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
            this.testPoint3 = newDataPoint(testPointXid3, this.ds.xid, 'COUNT', 'TOLERANCE', 5.0);
            this.testPoint4 = newDataPoint(testPointXid4, this.ds.xid, 'COUNT', 'NONE', 0);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save(), this.testPoint3.save(), this.testPoint4.save()]);
        }).then(() => {
            const valuesToInsert = pointValues1.concat(pointValues2.concat(pointValues3.concat(pointValues4)));
            return client.pointValues.insert(valuesToInsert);
        }).then(() => delay(insertionDelay));
    });

    after('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });

    it('Gets latest point values for a data point as csv', function() {
        return this.csvClient.pointValues.latest({
            xid: testPointXid1,
            dataType: 'string'
        }).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            comparePointValues({
                responseData: result.slice().reverse(),
                expectedValues: pointValues1
            });
        });
    });

    it('Gets latest point values for a data point, using cache only as csv', function() {
        return this.csvClient.pointValues.latest({
            xid: testPointXid1,
            useCache: 'CACHE_ONLY',
            fields: ['TIMESTAMP', 'VALUE', 'CACHED']
        }).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'cached');
            assert.strictEqual(result.length, 1); // default cache size is 1
            assert.strictEqual(result[0].cached, 'true');
        });
    });

    it('Gets latest point values for a data point, using cache both as csv', function() {
        return this.csvClient.pointValues.latest({
            xid: testPointXid1,
            useCache: 'BOTH',
            fields: ['TIMESTAMP', 'VALUE', 'CACHED']
        }).then(result => {
            assert.isArray(result);

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'cached');
            
            const reversedResult = result.slice().reverse();

            assert.strictEqual(result[0].cached, 'true'); // default cache size is 1

            comparePointValues({
                responseData: reversedResult,
                expectedValues: pointValues1
            });
        });
    });

    it('Gets latest point values for a data point with a limit of 20 as csv', function() {
        return this.csvClient.pointValues.latest({
            xid: testPointXid1,
            limit: 20
        }).then(result => {
            assert.isArray(result);

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');

            comparePointValues({
                responseData: result.slice().reverse(),
                expectedValues: pointValues1.slice(-20)
            });
        });
    });

    it('Gets latest point values for multiple points as a single array as csv', function() {
        return this.csvClient.pointValues.latestAsSingleArray({
            xids: [testPointXid1, testPointXid2]
        }).then(result => {
            assert.isArray(result);

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
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

    it('Gets latest point values for multiple points as csv', function() {
        return this.csvClient.pointValues.latest({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP']
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');

            assert.strictEqual(result.length, 200);
            
            const point1Result = result.slice(0, 100).reverse();
            const point2Result = result.slice(100, 200).reverse();
            
            //Assert xid
            point1Result.forEach(pv => {
                assert.strictEqual(pv.xid, testPointXid1);
            });
            point2Result.forEach(pv => {
                assert.strictEqual(pv.xid, testPointXid2);
            });
            
            comparePointValues({
                responseData: point1Result,
                expectedValues: pointValues1,
                valueProperty: 'value'
            });
            comparePointValues({
                responseData: point2Result,
                expectedValues: pointValues2,
                valueProperty: 'value'
            });
        });
    });

    it('Queries time period for multiple points as single array as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
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

    it('Queries time period for multiple points as single array with limit 20 as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
            xids: [testPointXid1, testPointXid2],
            from: startTime,
            to: endTime,
            limit: 20
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
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

    it('Queries time period for multiple points as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime,
            to: endTime
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.strictEqual(result.length, 200);
            
            const point1Result = result.slice(0, 100);
            const point2Result = result.slice(100, 200);
            
            //Assert xid
            point1Result.forEach(pv => {
                assert.strictEqual(pv.xid, testPointXid1);
            });
            point2Result.forEach(pv => {
                assert.strictEqual(pv.xid, testPointXid2);
            });
            
            comparePointValues({
                responseData: point1Result,
                expectedValues: pointValues1,
                valueProperty: 'value'
            });
            comparePointValues({
                responseData: point2Result,
                expectedValues: pointValues2,
                valueProperty: 'value'
            });
        });
    });

    it('Queries time period for multiple points with limit 20 as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime,
            to: endTime,
            limit: 20
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.strictEqual(result.length, 40);
            
            const point1Result = result.slice(0, 20);
            const point2Result = result.slice(20, 40);
            
            //Assert xid
            point1Result.forEach(pv => {
                assert.strictEqual(pv.xid, testPointXid1);
            });
            point2Result.forEach(pv => {
                assert.strictEqual(pv.xid, testPointXid2);
            });
            
            comparePointValues({
                responseData: point1Result,
                expectedValues: pointValues1.slice(0, 20),
                valueProperty: 'value'
            });
            comparePointValues({
                responseData: point2Result,
                expectedValues: pointValues2.slice(0, 20),
                valueProperty: 'value'
            });
        });
    });

    it('Queries time period for single point as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });
        });
    });

    it('Queries time period for single point with limit 20 as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            limit: 20
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1.slice(0, 20)
            });
        });
    });

    it('Queries time period with no start bookend, regular end bookend as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime - 10,
            to: endTime + 10,
            bookend: true,
            fields: ['TIMESTAMP', 'VALUE', 'BOOKEND']
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'bookend');
            
            const startBookend = result.shift();
            const endBookend = result.pop();
            assert.strictEqual(startBookend.value, '');
            assert.strictEqual(startBookend.bookend, 'true');
            assert.strictEqual(endBookend.bookend, 'true');
            assert.strictEqual(endBookend.timestamp, String(endTime + 10));
            assert.strictEqual(endBookend.value, String(result[result.length - 1].value));

            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });
        });
    });

    it('Does not return a start bookend when there is a value at from time as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            bookend: true,
            fields: ['TIMESTAMP', 'VALUE', 'BOOKEND']
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'bookend');

            assert.strictEqual(result[0].bookend, '');

            assert.isAbove(Number(result[1].timestamp), Number(result[0].timestamp));

            const endBookend = result.pop();
            assert.strictEqual(endBookend.bookend, 'true');
            assert.strictEqual(endBookend.timestamp, String(endTime));
            assert.strictEqual(endBookend.value, String(result[result.length - 1].value));

            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });
        });
    });

    it('Simplify works for single point, time period and tolerance as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            simplifyTolerance: 10
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.isBelow(result.length, pointValues1.length);
            
            let prevTime = startTime;
            result.forEach(pv => {
                assert.isAtLeast(Number(pv.timestamp), prevTime);
                assert.isBelow(Number(pv.timestamp), endTime);
                prevTime = Number(pv.timestamp);
            });
        });
    });

    it('Simplify works for single point, latest values and tolerance as csv', function() {
        return this.csvClient.pointValues.latest({
            xid: testPointXid1,
            simplifyTolerance: 10
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = endTime;
            result.slice().reverse().forEach(pv => {
                assert.isAtMost(Number(pv.timestamp), prevTime);
                assert.isBelow(Number(pv.timestamp), endTime);
                prevTime = Number(pv.timestamp);
            });
        });
    });

    it('Simplify works for single point, time period and target csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            simplifyTarget: 50
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.isBelow(result.length, pointValues1.length);
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = startTime;
            result.forEach(pv => {
                assert.isAtLeast(Number(pv.timestamp), prevTime);
                assert.isBelow(Number(pv.timestamp), endTime);
                prevTime = Number(pv.timestamp);
            });
        });
    });

    it('Simplify works for single point, latest values and target csv', function() {
        return this.csvClient.pointValues.latest({
            xid: testPointXid1,
            simplifyTarget: 50
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.isBelow(result.length, pointValues1.length);

            let prevTime = endTime;
            result.slice().reverse().forEach(pv => {
                assert.isAtMost(Number(pv.timestamp), prevTime);
                assert.isBelow(Number(pv.timestamp), endTime);
                prevTime = Number(pv.timestamp);
            });
        });
    });

    it('Formats timestamps correctly as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            dateTimeFormat: 'yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX'
        }).then(result => {
            assert.isArray(result);

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.isString(pv.timestamp);
                assert.strictEqual(moment(pv.timestamp).valueOf(), pointValues1[i].timestamp);
            });
        });
    });

    it('Returns rendered values correctly as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xid: testPointXid1,
            from: startTime,
            to: endTime,
            fields: ['TIMESTAMP', 'VALUE', 'RENDERED']
        }).then(result => {
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            
            comparePointValues({
                responseData: result,
                expectedValues: pointValues1
            });

            result.forEach((pv, i) => {
                assert.strictEqual(pv.rendered, '' + pointValues1[i].value.toFixed(2));
            });
        });
    });

    it('Returns the same point values using a FIRST rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the zeros as single array using a COUNT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);

            assert.strictEqual(result.length, 60);
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1], '0');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as single array using a FIRST rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);

            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns nulls as single array using a POINT_DEFAULT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);

            assert.strictEqual(result.length, 60);

            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1], '');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as single array using a POINT_DEFAULT rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);

            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as single array using a NONE rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);

            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as single array using POINT_DEFAULT rollup and Simplify with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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

            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid3);

            assert.isBelow(result.length, pointValues1.length);

            let prevTime = startTime;
            result.forEach(pv => {
                assert.isNumber(Number(pv[testPointXid3]));
                assert.isAtLeast(Number(pv.timestamp), prevTime);
                assert.isBelow(Number(pv.timestamp), endTime);
                prevTime = Number(pv.timestamp);
            });

        });
    });
    
    it('Returns zeros for two points as single array using a COUNT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);

            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1], '0');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                assert.strictEqual(pv[testPointXid2], '0');
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a FIRST rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
            assert.strictEqual(result.length, pointValues1.length);
            
            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
                assert.strictEqual(Number(pv[testPointXid2]), pointValues2[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns null values for two points as single array using a POINT_DEFAULT for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1], '');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                assert.strictEqual(pv[testPointXid2], '');
                prevTime += 1000;
            });
        });
    });
    
    it('Uses correct rollup for two points as single array using a POINT_DEFAULT for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid4);
            
            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv[testPointXid1], '');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                assert.strictEqual(pv[testPointXid4], '0');
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a POINT_DEFAULT rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
            assert.strictEqual(result.length, pointValues1.length);
            
            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
                assert.strictEqual(Number(pv[testPointXid2]), pointValues2[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a NONE rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid2);
            
            assert.strictEqual(result.length, pointValues1.length);
            
            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
                assert.strictEqual(Number(pv[testPointXid2]), pointValues2[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for two points as single array using a POINT_DEFAULT rollup and Simplify as csv', function() {
        return this.csvClient.pointValues.forTimePeriodAsSingleArray({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], testPointXid1);
            assert.strictEqual(headers[2], testPointXid3);
            
            assert.strictEqual(result.length, pointValues1.length);
            
            let testPoint3Count = 0;
            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv[testPointXid1]), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
                if(pv[testPointXid3] !== '')
                    testPoint3Count++;
            });
            assert.isBelow(testPoint3Count, result.length);
        });
    });

    it('Returns zeros as multiple arrays using a COUNT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(result.length, 60);
            
            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '0');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a FIRST rollup with same time period as poll period', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns null values as multiple arrays using a POINT_DEFAULT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            
            assert.strictEqual(result.length, 60);

            let prevTime = startTime - 60000;
            result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '');
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a POINT_DEFAULT rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });

    it('Returns the same point values as multiple arrays using a NONE rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(result.length, pointValues1.length);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values as multiple arrays using a POINT_DEFAULT rollup and Simplify as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            assert.isBelow(result.length, pointValues3.length);
                        
            let prevTime = startTime;
            result.forEach(pv => {
                assert.isString(pv.value);
                assert.isAtLeast(Number(pv.timestamp), prevTime);
                assert.isBelow(Number(pv.timestamp), endTime);
                prevTime = Number(pv.timestamp);
            });
            
        });
    });
    
    it('Returns zeros for 2 points as multiple arrays using a COUNT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime - 60000,
            to: startTime,
            rollup: 'COUNT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');

            assert.strictEqual(result.length, 120);
            
            const point1Result = result.splice(0, 60);
            const point2Result = result.splice(60, 120);

            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '0');
                assert.strictEqual(pv.xid, testPointXid1);
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
            prevTime = startTime - 60000;
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '0');
                assert.strictEqual(pv.xid, testPointXid2);
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a FIRST rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime,
            to: endTime,
            rollup: 'FIRST',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.strictEqual(result.length, 200);
            
            const point1Result = result.splice(0, 100);
            const point2Result = result.splice(100, 200);

            point1Result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(pv.xid, testPointXid1);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
            
            point2Result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues2[i].value);
                assert.strictEqual(pv.xid, testPointXid2);
                assert.strictEqual(Number(pv.timestamp), pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns null values for 2 points as multiple arrays using a POINT_DEFAULT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.strictEqual(result.length, 120);
            
            const point1Result = result.splice(0, 60);
            const point2Result = result.splice(60, 120);
            
            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '');
                assert.strictEqual(pv.xid, testPointXid1);
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '');
                assert.strictEqual(pv.xid, testPointXid2);
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Uses correct rollup for 2 points as multiple arrays using a POINT_DEFAULT rollup for minute before start time as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid4],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime - 60000,
            to: startTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.strictEqual(result.length, 120);
            
            const point1Result = result.splice(0, 60);
            const point2Result = result.splice(60, 120);
            
            let prevTime = startTime - 60000;
            point1Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '');
                assert.strictEqual(pv.xid, testPointXid1);
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(pv.value, '0');
                assert.strictEqual(pv.xid, testPointXid4);
                assert.strictEqual(Number(pv.timestamp), prevTime);
                prevTime += 1000;
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a POINT_DEFAULT rollup with same time period as poll period as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');

            assert.strictEqual(result.length, 200);
            
            const point1Result = result.splice(0, 100);
            const point2Result = result.splice(100, 200);


            point1Result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(pv.xid, testPointXid1);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues2[i].value);
                assert.strictEqual(pv.xid, testPointXid2);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a NONE rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid2],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime,
            to: endTime,
            rollup: 'NONE',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.strictEqual(result.length, 200);
            
            const point1Result = result.splice(0, 100);
            const point2Result = result.splice(100, 200);
            
            point1Result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i].value);
                assert.strictEqual(pv.xid, testPointXid1);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
            });
            point2Result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues2[i].value);
                assert.strictEqual(pv.xid, testPointXid2);
                assert.strictEqual(Number(pv.timestamp), pointValues2[i].timestamp);
            });
        });
    });
    
    it('Returns the same point values for 2 points as multiple arrays using a POINT_DEFAULT rollup and Simplify as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
            xids: [testPointXid1, testPointXid3],
            fields: ['XID', 'VALUE', 'TIMESTAMP'],
            from: startTime,
            to: endTime,
            rollup: 'POINT_DEFAULT',
            timePeriod: {
                periods: 1,
                type: 'SECONDS'
            }
        }).then(result => {
            assert.isArray(result);
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'xid');
            assert.strictEqual(headers[1], 'value');
            assert.strictEqual(headers[2], 'timestamp');
            
            assert.isAbove(result.length, pointValues1.length);

            let point3Count = 0;
            result.forEach((pv, i) => {
                if(i < 100){
                    assert.strictEqual(Number(pv.value), pointValues1[i].value);
                    assert.strictEqual(pv.xid, testPointXid1);
                    assert.strictEqual(Number(pv.timestamp), pointValues1[i].timestamp);
                }else{
                    assert.strictEqual(pv.xid, testPointXid3);
                    if(pv.value !== '')
                        point3Count++;  
                }
            });
            assert.isBelow(point3Count, result.length);
        });
    });
    
    it('Returns the correct number of point values when downsampling using a rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            
            assert.strictEqual(result.length, pointValues1.length / 5);

            result.forEach((pv, i) => {
                assert.strictEqual(Number(pv.value), pointValues1[i * 5].value);
                assert.strictEqual(Number(pv.timestamp), pointValues1[i * 5].timestamp);
            });
        });
    });

    it('Can truncate to the nearest minute when doing a rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');
            
            // should always have 3 samples as we have 100 point values with 1 second period
            // first value is start of first minute and last value will be expanded to start of 3rd minute
            assert.isAtLeast(result.length, 2);
            assert.isAtMost(result.length, 3); //Expanding a time period across midnight will result in 3 days

            assert.strictEqual(Number(result[0].value), pointValues1[0].value);
            assert.strictEqual(moment(Number(result[0].timestamp)).toISOString(),
                    moment(pointValues1[0].timestamp).startOf('minute').toISOString());
        });
    });

    it('Can truncate to the start of the day using the correct timezone when doing a rollup as csv', function() {
        return this.csvClient.pointValues.forTimePeriod({
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
            
            const headers = result.shift();
            assert.strictEqual(headers[0], 'timestamp');
            assert.strictEqual(headers[1], 'value');

            // depending on when we run the test the point values might fall across two days, but will be almost always length 1
            assert.isAtLeast(result.length, 1);
            assert.isAtMost(result.length, 2); //Expanding a time period across midnight will result in 3 days

            assert.strictEqual(Number(result[0].value), pointValues1[0].value);
            assert.strictEqual(moment.tz(Number(result[0].timestamp), 'Australia/Sydney').format(),
                    moment.tz(pointValues1[0].timestamp, 'Australia/Sydney').startOf('day').format());
        });
    });

});
