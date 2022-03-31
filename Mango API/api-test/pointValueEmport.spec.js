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
 * 
 * This test suit is designed to help develop a streaming api for point values but will not run every night 
 *  as it is for performance testing.
 * 
 */

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;
const path = require('path');
const fs = require('fs');
const tmp = require('tmp');

describe('Point value emport tests', function() {
    before('Login', function() { return login.call(this, client); });

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
            },
            rollup: 'AVERAGE'
        });
    };

    const numSamples = 100;
    const pollPeriod = 1; //in ms
    const endTime = new Date().getTime();
    const isoTo = new Date(endTime).toISOString();
    const startTime = endTime - (numSamples * pollPeriod);
    const isoFrom = new Date(startTime).toISOString();
    
    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    
    const pointValues1 = generateSamples(testPointXid1, endTime - 1000*60*60*24, 24*60, 60000);
    const pointValues2 = generateSamples(testPointXid2, endTime - 1000*60*60*24, 24*60, 60000);
    
    const insertionDelay = 1000;
    
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
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid);
            this.testPoint2 = newDataPoint(testPointXid2, this.ds.xid);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save()]);
        }).then(() => {
            const valuesToInsert = pointValues1.concat(pointValues2);
            return client.pointValues.insert(valuesToInsert);
        }).then(() => delay(insertionDelay));
    });

    after('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });
    
    it('Can upload a CSV file for 2 points in a CSV file with 1 value column', function() {
        this.timeout(5000);
         
        return client.restRequest({
            path: `/rest/latest/point-values/multiple-arrays/time-period/AVERAGE`,
            method: 'POST',
            headers: {
                'Accept': 'text/csv',
                'Accept-Encoding': 'gzip, deflate'
            },
            data: {
                dateTimeFormat: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                fields: ["TIMESTAMP", "VALUE", "XID"],
                from: `${isoFrom}`,
                to: `${isoTo}`,
                timePeriod: {
                    periods: 1,
                    type: 'MILLISECONDS'
                },
                xids: [`${testPointXid1}`,`${testPointXid2}`],
            },
            writeToFile: 'pointValues.csv'
        }).then(() => delay(1000)).then(response => {
            const uploadFileName = path.resolve('pointValues.csv');
            return client.restRequest({
                path: `/rest/latest/point-value-modification/import`,
                method: 'POST',
                headers: {
                    'Content-Type': 'text/csv;charset=UTF-8'
                },
                data: fs.readFileSync(uploadFileName)
            }).then(response => {
                assert.strictEqual(response.data.length, 2);

                const testPoint1Result = response.data.find(r => r.xid === testPointXid1);
                assert.strictEqual(testPoint1Result.xid, testPointXid1);
                assert.strictEqual(testPoint1Result.totalQueued, 100);

                const testPoint2Result = response.data.find(r => r.xid === testPointXid2);
                assert.strictEqual(testPoint2Result.xid, testPointXid2);
                assert.strictEqual(testPoint2Result.totalQueued, 100);
            });
        });
    });
    
    it('Can upload a CSV file as text', function() {
        return client.restRequest({
            path: `/rest/latest/point-value-modification/import`,
            method: 'POST',
            headers: {
                'Content-Type': 'text/csv;charset=UTF-8'
            },
            data: '"timestamp","value","xid"\n"2019-07-30T14:44:20.007-10:00",0.20330415737793572,' + testPointXid1
        }).then(response => {
            assert.strictEqual(response.data.length, 1);
            assert.strictEqual(response.data[0].xid, testPointXid1);
            assert.strictEqual(response.data[0].totalQueued, 1);
        });
    });

    it('Can upload a large CSV file', function() {
        this.timeout(60000);

        const csvFile = tmp.fileSync({postfix: '.csv'});

        fs.appendFileSync(csvFile.fd, 'timestamp,value,xid\n');

        // results in a file about 66 MiB in size
        const numSamples = 1e6;
        const period = 100;
        const endDate = new Date();
        const startDate = new Date(endDate - numSamples * period);

        for (let i = 0; i < numSamples; i++) {
            const timestamp = startDate + i * period;
            const row = [new Date(timestamp).toISOString(), '1.0', testPointXid1];
            fs.appendFileSync(csvFile.fd, row.join(',') + '\n');
        }

        // console.log('File size (MiB): ' + fs.statSync(csvFile.name).size / Math.pow(1024, 2));

        // TODO no facility to upload a file directly, use buffer for now
        const buffer = fs.readFileSync(csvFile.name);

        return client.restRequest({
            path: `/rest/latest/point-value-modification/import`,
            method: 'POST',
            headers: {
                'Content-Type': 'text/csv;charset=UTF-8'
            },
            data: buffer
        }).then(response => {
            assert.strictEqual(response.data.length, 1);
            assert.strictEqual(response.data[0].xid, testPointXid1);
            assert.strictEqual(response.data[0].totalQueued, numSamples);
        });
    });
    
    it('Fails to upload an invalid CSV file', function() {
        return client.restRequest({
            path: `/rest/latest/point-value-modification/import`,
            method: 'POST',
            headers: {
                'Content-Type': 'text/csv;charset=UTF-8'
            },
            data: '"timestamp","value","name"\n"2019-07-30T14:44:20.007-10:00",0.20330415737793572,' + testPointXid1
        }).then(response => {
            assert.fail('should not succeed');
        }, error => {
            assert.strictEqual(error.status, 500);
        });
    });
    
    it('Can upload a JSON file for 2 points', function() {
        this.timeout(5000);
        //We need to ensure the data comes back as an array with value,xid,timestamp 
        //but we don't have an endpoint for multiple points that does that AFAIK yet.
        return client.restRequest({
            path: `/rest/latest/point-value-modification/import`,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            data: [ {
                timestamp : "2019-07-24T07:23:09.360-10:00",
                xid : testPointXid1,
                value : 99.9
              }, {
                timestamp : "2019-07-24T07:23:09.360-10:00",
                xid : testPointXid2,
                value : 1.0020763454149213,
              }]
        }).then(response => {
            assert.strictEqual(response.data.length, 2);
            assert.strictEqual(response.data[0].xid, testPointXid1);
            assert.strictEqual(response.data[0].totalQueued, 1);
            assert.strictEqual(response.data[1].xid, testPointXid2);
            assert.strictEqual(response.data[1].totalQueued, 1);
        });
    });
});