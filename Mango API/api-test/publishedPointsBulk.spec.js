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

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;
const Publisher = client.Publisher;

describe('Published point bulk operations', function() {
    before('Login', function() { return login.call(this, client); });

    const newPublishedPoint = (xid, pubXid, dataPointXid) => {
            return {
                xid: xid,
                name: 'Published point name',
                enabled: false,
                publisherXid: pubXid,
                dataPointXid: dataPointXid,
                modelType: 'MOCK'
            };
        };

    const newDataPoint = (xid, dsXid) => {
        return new DataPoint({
            xid : xid,
            deviceName : "_",
            name : "Mock Test Point 1",
            enabled : false,
            loggingProperties : {
                tolerance : 0.0,
                discardExtremeValues : false,
                discardLowLimit : -1.7976931348623157E308,
                discardHighLimit : 1.7976931348623157E308,
                loggingType : "ON_CHANGE",
                intervalLoggingType: "INSTANT",
                intervalLoggingPeriod : {
                    periods : 15,
                    type : "MINUTES"
                },
                overrideIntervalLoggingSamples : false,
                intervalLoggingSampleWindowSize : 0,
                cacheSize : 1
            },
            textRenderer : {
                zeroLabel : "zero",
                zeroColour : "blue",
                oneLabel : "one",
                oneColour : "black",
                type : "textRendererBinary"
            },
            dataSourceXid : dsXid,
            useIntegralUnit : false,
            useRenderedUnit : false,
            readPermission : '',
            setPermission : 'superadmin',
            chartColour : "",
            rollup : "NONE",
            plotType : "STEP",
            purgeOverride : false,
            purgePeriod : {
                periods : 1,
                type : "YEARS"
            },
            unit : "",
            integralUnit : "s",
            renderedUnit : "",
            modelType : "DATA_POINT",
            pointLocator : {
                modelType : "PL.MOCK",
                dataType : "BINARY",
                settable : true
            }
        });
    };

    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    const testPublishedPointXid1 = uuid();
    const testPublishedPointXid2 = uuid();

    beforeEach('Create a mock data source, points, publisher and points', function() {
        this.ds = new DataSource({
            xid: uuid(),
            name: 'Mango client test',
            enabled: true,
            modelType: 'MOCK',
            pollPeriod: { periods: 5, type: 'HOURS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        this.pub = new Publisher({
            enabled: true,
            modelType: 'MOCK'
        });

        return this.ds.save().then((savedDs) => {
            assert.strictEqual(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
        }).then(() => {
            return this.pub.save();
        }).then(() => {
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid);
            this.testPoint2 = newDataPoint(testPointXid2, this.ds.xid);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save()]);
        }).then(() => {
            this.testPublishedPoint1 = newPublishedPoint(testPublishedPointXid1, this.pub.xid, testPointXid1);
            return client.restRequest({
                path: '/rest/latest/published-points',
                method: 'POST',
                data: this.testPublishedPoint1
            }).then(response => {
                this.testPublishedPoint1 = response.data;
            });
        }).then(() => {
            this.testPublishedPoint2 = newPublishedPoint(testPublishedPointXid2, this.pub.xid, testPointXid2);
            return client.restRequest({
                path: '/rest/latest/published-points',
                method: 'POST',
                data: this.testPublishedPoint2
            }).then(response => {
                this.testPublishedPoint2 = response.data;
            });
        });
    });

    afterEach('Deletes the new mock data source and its points and the published points', function() {
        return Promise.all([this.ds.delete(), this.pub.delete()]);
    });

    it('Can bulk create published points', function() {
        this.timeout(5000);

        const publishedPointOne = newPublishedPoint(uuid(), this.pub.xid, testPointXid1);
        const publishedPointTwo = newPublishedPoint(uuid(), this.pub.xid, testPointXid2);

        return client.restRequest({
            path: `/rest/latest/published-points/bulk`,
            method: 'POST',
            data: {
                action: 'CREATE',
                body: null, //defaults?
                requests: [
                    {
                        action: 'CREATE',
                        xid: null,
                        body: publishedPointOne
                    },
                    {
                        action: 'CREATE',
                        xid: null,
                        body: publishedPointTwo
                    }
                ]
            }
        }).then(response => {
            assert.strictEqual(response.status, 201);
            assert.isString(response.data.id);
            assert.isString(response.data.status);
            assert.notStrictEqual(response.data.status, 'TIMED_OUT');
            assert.notStrictEqual(response.data.status, 'CANCELLED');
            assert.notStrictEqual(response.data.status, 'ERROR');

            return delay(500).then(() => {
                return client.restRequest({
                    path: response.headers.location
                });
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.strictEqual(response.data.status, 'SUCCESS');
            assert.strictEqual(response.data.position, 2);
            assert.strictEqual(response.data.maximum, 2);
            assert.strictEqual(response.data.progress, 100);
            assert.isNumber(response.data.expiration);

            const results = response.data.result.responses;
            assert.strictEqual(response.data.result.hasError, false);
            assert.isArray(results);
            assert.strictEqual(results.length, 2);

            assert.strictEqual(results[0].httpStatus, 200);
            assert.strictEqual(results[0].body.name, publishedPointOne.name);
            assert.strictEqual(results[0].body.xid, publishedPointOne.xid);
            assert.strictEqual(results[1].httpStatus, 200);
            assert.strictEqual(results[1].body.name, publishedPointTwo.name);
            assert.strictEqual(results[1].body.xid, publishedPointTwo.xid);
        });
    });

    it('Can bulk update published points', function() {
        this.timeout(5000);
        
        this.testPublishedPoint1.name = 'Point 1 New Name';
        this.testPublishedPoint2.name = 'Point 2 New Name';
        
        return client.restRequest({
            path: `/rest/latest/published-points/bulk`,
            method: 'POST',
            data: {
                action: 'UPDATE',
                body: null, //defaults?
                requests: [
                    {
                        action: 'UPDATE',
                        xid: this.testPublishedPoint1.xid,
                        body: this.testPublishedPoint1
                    },
                    {
                        action: 'UPDATE',
                        xid: this.testPublishedPoint2.xid,
                        body: this.testPublishedPoint2
                    }
                ]
            }
        }).then(response => {
            assert.strictEqual(response.status, 201);
            assert.isString(response.data.id);
            assert.isString(response.data.status);
            assert.notStrictEqual(response.data.status, 'TIMED_OUT');
            assert.notStrictEqual(response.data.status, 'CANCELLED');
            assert.notStrictEqual(response.data.status, 'ERROR');
            
            return delay(500).then(() => {
                return client.restRequest({
                    path: response.headers.location
                });
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.strictEqual(response.data.status, 'SUCCESS');
            assert.strictEqual(response.data.position, 2);
            assert.strictEqual(response.data.maximum, 2);
            assert.strictEqual(response.data.progress, 100);
            assert.isNumber(response.data.expiration);
            
            const results = response.data.result.responses;
            assert.strictEqual(response.data.result.hasError, false);
            assert.isArray(results);
            assert.strictEqual(results.length, 2);

            assert.strictEqual(results[0].httpStatus, 200);
            assert.strictEqual(results[0].body.name, this.testPublishedPoint1.name);
            assert.strictEqual(results[1].httpStatus, 200);
            assert.strictEqual(results[1].body.name, this.testPublishedPoint2.name);
        });
    });
});
