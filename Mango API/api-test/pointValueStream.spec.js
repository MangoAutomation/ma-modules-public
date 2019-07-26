/**
 * Copyright 2019 Infinite Automation Systems Inc.
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
 * 
 * This test suit is designed to help develop a streaming api for point values but will not run every night 
 *  as it is for performance testing.
 * 
 */

const config = require('@infinite-automation/mango-client/test/setup');
const uuidV4 = require('uuid/v4');
const path = require('path');
const fs = require('fs');

describe('Point value streaming load tests', function() {
    before('Login', config.login);

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
    
    const logOutput = true;
    const fileSizeMB = 1; //rough estimate per data point
    const numSamples = 20 * 1024 * fileSizeMB;
    const pollPeriod = 1; //in ms
    const endTime = new Date().getTime();
    const isoTo = new Date(endTime).toISOString();
    const startTime = endTime - (numSamples * pollPeriod);
    const isoFrom = new Date(startTime).toISOString();
    
    const testPointXid1 = uuidV4();
    const testPointXid2 = uuidV4();
    
    const pointValues1 = generateSamples(testPointXid1, endTime - 1000*60*60*24, 24*60, 60000);
    const pointValues2 = generateSamples(testPointXid2, endTime - 1000*60*60*24, 24*60, 60000);
    
    const insertionDelay = 1000;
    
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
    
    //TODO ALL Statistics make a large memory difference
    
    it.skip('Can make a MILLISECOND rollup request for 2 points for a large JSON file', function() {
        this.timeout(50000000);

        return client.restRequest({
            path: `/rest/v2/point-values/single-array/time-period/FIRST`,
            method: 'POST',
            data: {
                dateTimeFormat: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                fields: ["TIMESTAMP", "VALUE"],
                from: `${isoFrom}`,
                to: `${isoTo}`,
                timePeriod: {
                    periods: 1,
                    type: 'MILLISECONDS'
                },
                xids: [`${testPointXid1}`,`${testPointXid2}`],
                
            },
            writeToFile: 'pointValues.json'
        }).then(response => {
            if(logOutput === true) {
                console.log(response);
                console.log('Query from: ' + isoFrom);
                console.log('Query to: ' + isoTo);
                console.log('Series1: ' + new Date(pointValues1[0].timestamp).toISOString() + ' to ' + 
                        new Date(pointValues1[pointValues1.length - 1].timestamp).toISOString());
            }
        });
        
    });
    
    it.skip('Can make a MILLISECOND default rollup request for 2 points for a large JSON file', function() {
        this.timeout(50000000);

        return client.restRequest({
            path: `/rest/v2/point-values/single-array/time-period/POINT_DEFAULT`,
            method: 'POST',
            data: {
                dateTimeFormat: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                fields: ["TIMESTAMP", "VALUE"],
                from: `${isoFrom}`,
                to: `${isoTo}`,
                timePeriod: {
                    periods: 1,
                    type: 'MILLISECONDS'
                },
                xids: [`${testPointXid1}`,`${testPointXid2}`],
                
            },
            writeToFile: 'pointValues.json'
        }).then(response => {
            if(logOutput === true) {
                console.log(response);
                console.log('Query from: ' + isoFrom);
                console.log('Query to: ' + isoTo);
                console.log('Series1: ' + new Date(pointValues1[0].timestamp).toISOString() + ' to ' + 
                        new Date(pointValues1[pointValues1.length - 1].timestamp).toISOString());
            }
        });
        
    });
    
    it.skip('Can make a MILLISECOND rollup request for 2 points for a CSV large file', function() {
        this.timeout(50000000);
         
        return client.restRequest({
            path: `/rest/v2/point-values/single-array/time-period/AVERAGE`,
            method: 'POST',
            headers: {
                'Accept': 'text/csv',
                'Accept-Encoding': 'gzip, deflate'
            },
            data: {
                dateTimeFormat: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                fields: ["TIMESTAMP", "VALUE"],
                from: `${isoFrom}`,
                to: `${isoTo}`,
                timePeriod: {
                    periods: 1,
                    type: 'MILLISECONDS'
                },
                xids: [`${testPointXid1}`,`${testPointXid2}`],
            },
            writeToFile: 'pointValues.csv'
        }).then(response => {
            if(logOutput === true) {
                console.log(response);
                console.log('Query from: ' + isoFrom);
                console.log('Query to: ' + isoTo);
                console.log('Series1: ' + new Date(pointValues1[0].timestamp).toISOString() + ' to ' + 
                        new Date(pointValues1[pointValues1.length - 1].timestamp).toISOString());
            }
        });
    });
    
    it.skip('Can make a MILLISECOND default rollup request for 2 points for a CSV large file', function() {
        this.timeout(50000000);
         
        return client.restRequest({
            path: `/rest/v2/point-values/single-array/time-period/POINT_DEFAULT`,
            method: 'POST',
            headers: {
                'Accept': 'text/csv',
                'Accept-Encoding': 'gzip, deflate'
            },
            data: {
                dateTimeFormat: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                fields: ["TIMESTAMP", "VALUE"],
                from: `${isoFrom}`,
                to: `${isoTo}`,
                timePeriod: {
                    periods: 1,
                    type: 'MILLISECONDS'
                },
                xids: [`${testPointXid1}`,`${testPointXid2}`],
            },
            writeToFile: 'pointValues.csv'
        }).then(response => {
            if(logOutput === true) {
                console.log(response);
                console.log('Query from: ' + isoFrom);
                console.log('Query to: ' + isoTo);
                console.log('Series1: ' + new Date(pointValues1[0].timestamp).toISOString() + ' to ' + 
                        new Date(pointValues1[pointValues1.length - 1].timestamp).toISOString());
            }
        });
    });
    
    it('Can upload a large CSV file for 2 points in a CSV file with 1 value column', function() {
        this.timeout(50000000);
         
        return client.restRequest({
            path: `/rest/v2/point-values/multiple-arrays/time-period/AVERAGE`,
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
                xids: ['voltage', 'temperature']
                //xids: [`${testPointXid1}`,`${testPointXid2}`],
            },
            writeToFile: 'pointValues.csv'
        }).then(response => {
            const uploadFileName = path.resolve('pointValues.csv');
            return client.restRequest({
                path: `/rest/v2/point-value-modification/import`,
                method: 'POST',
                headers: {
                    'Content-Type': 'text/csv;charset=UTF-8'
                },
                dataType: 'buffer',
                data: fs.readFileSync(uploadFileName)
                //TODO data: fs.createReadStream(uploadFileName)
            }).then(response => {
                console.log(JSON.parse(response.data.toString('utf8')));
            });
        });
    });
});