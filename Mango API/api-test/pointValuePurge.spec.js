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

describe('Point value purge', function() {    
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

    const insertionDelay = 1000;

    //Generate 20 minutes of data
    const numSamples = 60*20;
    const pollPeriod = 1000; //in ms
    const endTime = new Date().getTime();
    const startTime = endTime - (numSamples * pollPeriod);
    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    const testPointXid3 = uuid();

    const pointValues1 = generateSamples(testPointXid1, startTime, numSamples, pollPeriod);
    const pointValues2 = generateSamples(testPointXid2, startTime, numSamples, pollPeriod);
    const pointValues3 = generateSamples(testPointXid3, startTime, numSamples, pollPeriod);

    before('Login', function() { return login.call(this, client); });

    it('Purges data for a single data point by time period', function(){
        this.timeout(5000);
        let resourceId;
        let cutoffTime = new Date().getTime() - 10*60*1000;
        return client.restRequest({
            path: '/rest/v2/point-values/purge',
            method: 'POST',
            data: {
                xids: [testPointXid1],
                duration: {
                    periods: 10,
                    type: 'MINUTES'
                }
            }
        }).then(response => {
            assert.strictEqual(response.status, 201);
            assert.isString(response.data.id);
            assert.isString(response.data.status);

            resourceId = response.data.id;
            
            return delay(2000).then(() => {
                return client.restRequest({
                    path: response.headers.location
                });
            });
        }).then(response => {
            assert.strictEqual(response.data.status, 'SUCCESS');
            assert.strictEqual(response.data.result.successfullyPurged[0], testPointXid1);
            assert.strictEqual(response.data.result.noEditPermission.length, 0);
            assert.strictEqual(response.data.result.notFound.length, 0);

            //Then query to make sure no data exists
            return client.pointValues.forTimePeriod({
                xid: testPointXid1,
                from: startTime,
                to: cutoffTime
            }).then(result => {
                assert.strictEqual(typeof result, 'undefined');
            });
        });
    });
    
    it('Purges data for a data source by time range', function(){
        this.timeout(5000);
        let resourceId;
        let cutoffTime = new Date().getTime() - 10*60*1000;
        return client.restRequest({
            path: '/rest/v2/point-values/purge',
            method: 'POST',
            data: {
                dataSourceXid: this.ds.xid,
                useTimeRange: true,
                timeRange: {
                    from: startTime,
                    to: cutoffTime
                }
            }
        }).then(response => {
            assert.strictEqual(response.status, 201);
            assert.isString(response.data.id);
            assert.isString(response.data.status);

            resourceId = response.data.id;
            
            return delay(2000).then(() => {
                return client.restRequest({
                    path: response.headers.location
                });
            });
        }).then(response => {
            assert.strictEqual(response.data.status, 'SUCCESS');
            assert.strictEqual(response.data.result.successfullyPurged.length, 3);
            assert.strictEqual(response.data.result.noEditPermission.length, 0);
            assert.strictEqual(response.data.result.notFound.length, 0);

            //Then query to make sure no data exists
            return client.pointValues.forTimePeriod({
                xids: [testPointXid1, testPointXid2, testPointXid3],
                from: startTime,
                to: cutoffTime
            }).then(result => {
                assert.deepEqual(result, {});
            });
        });
    });

    beforeEach('Create a virtual data source, points, and insert values', function() {
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
            this.testPoint3 = newDataPoint(testPointXid3, this.ds.xid);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save(), this.testPoint3.save()]);
        }).then(() => {
            const valuesToInsert = pointValues1.concat(pointValues2).concat(pointValues3);
            return client.pointValues.insert(valuesToInsert);
        }).then(() =>{
            //Ensure we set the intra shard purge setting
            //mangoNoSql.intraShardPurge
            return client.restRequest({
                path: '/rest/v2/system-settings/mangoNoSql.intraShardPurge?type=BOOLEAN',
                method: 'PUT',
                data: true
            }).then(response => {
               //Assert it was set? 
            });
        }).then(() => delay(insertionDelay));
    });
    
    afterEach('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });

});