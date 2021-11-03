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

const {createClient, login, defer, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;
const Publisher = client.Publisher;

describe('Published point service', function() {
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

    it('Enables the published point', function() {
        return client.restRequest({
            path: `/rest/latest/published-points/${testPublishedPointXid2}`,
            method: 'GET'
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/published-points/enable-disable/${testPublishedPointXid2}?enabled=true&restart=false`,
                method: 'PUT'
            });
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/published-points/${testPublishedPointXid2}`,
                method: 'GET'
            }).then(response => {
                assert.isTrue(response.data.enabled);
            });
        });
    });

    it('Restarts the published point', function() {
        return client.restRequest({
            path: `/rest/latest/published-points/${testPublishedPointXid2}`,
            method: 'GET'
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/published-points/enable-disable/${testPublishedPointXid2}?enabled=true&restart=true`,
                method: 'PUT'
            });
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/published-points/${testPublishedPointXid2}`,
                method: 'GET'
            }).then(response => {
                assert.isTrue(response.data.enabled);
            });
        });
    });

    it('Disables the published point', function() {
        return client.restRequest({
            path: `/rest/latest/published-points/${testPublishedPointXid2}`,
            method: 'GET'
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/published-points/enable-disable/${testPublishedPointXid2}?enabled=false&restart=true`,
                method: 'PUT'
            });
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/published-points/${testPublishedPointXid2}`,
                method: 'GET'
            }).then(response => {
                assert.isFalse(response.data.enabled);
            });
        });
    });

    it('Gets websocket notifications for published point create', function() {
        this.timeout(5000);
        
        let ws;
        const socketOpenDeferred = defer();
        const gotMessageDeferred = defer();
        const pubXid = this.pub.xid;
        const ppName = uuid();
        const newXid = uuid();
        const testPoint3Xid = uuid();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/published-points'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                console.log('Got error', error);
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                gotMessageDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                gotMessageDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                const msg = JSON.parse(msgStr);
                if (msg.payload.action === 'add' && msg.payload.object.name === ppName) {
                    gotMessageDeferred.resolve(msg);
                }
            });

            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start
            return socketOpenDeferred.promise.then(() => delay(1000));
        }).then(() => {
            const testPoint3 = newDataPoint(testPoint3Xid, this.ds.xid);
            return testPoint3.save();
        }).then((testPoint3) => {
            // WebSocket is open and raring to go
            const pp = newPublishedPoint(newXid, pubXid, testPoint3Xid);
            pp.name = ppName;

            return client.restRequest({
                path: '/rest/latest/published-points',
                method: 'POST',
                data: pp
            });
        }).then(() => {
            // point was saved, wait for the WebSocket message
            return gotMessageDeferred.promise;
        }).then(msg => {
            assert.strictEqual(msg.payload.object.name, ppName);
            assert.strictEqual(msg.payload.object.xid, newXid);
            assert.strictEqual(msg.payload.object.publisherXid, pubXid);
            assert.strictEqual(msg.payload.object.dataPointXid, testPoint3Xid);
            assert.strictEqual(msg.payload.object.modelType, 'MOCK');
        }).finally(() => {
            if (ws) {
                // Close websocket 
                ws.close();
            }
        });
    });

});
