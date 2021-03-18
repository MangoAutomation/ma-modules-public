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

const {createClient, login, defer, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Data point service', function() {
    before('Login', function() { return login.call(this, client); });

    const newDataPoint = (xid, dsXid) => {
        return new DataPoint({
            xid : xid,
            deviceName : "_",
            name : "Virtual Test Point 1",
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
                startValue : "true",
                modelType : "PL.VIRTUAL",
                dataType : "BINARY",
                settable : true,
                changeType : "ALTERNATE_BOOLEAN",
                relinquishable : false
            }
        });
    };
    
    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    
    beforeEach('Create a virtual data source, points', function() {
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
        });
    });

    afterEach('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });
    
    it('Enables the data point', function() {
        return DataPoint.get(testPointXid1).then(dataPoint => {
            return dataPoint.setEnabled(true);
        }).then(dataPoint => {
            assert.isTrue(dataPoint.enabled);
        });
    });
    
    it('Restarts the data point', function() {
        return DataPoint.get(testPointXid1).then(dataPoint => {
            return dataPoint.setEnabled(true, true);
        }).then(dataPoint => {
            assert.isTrue(dataPoint.enabled);
        });
    });
    
    it('Disables the data point', function() {
        return DataPoint.get(testPointXid1).then(dataPoint => {
            return dataPoint.setEnabled(false);
        }).then(dataPoint => {
            assert.isFalse(dataPoint.enabled);
        });
    });
    
    it('Can change the data point name', function() {
        return DataPoint.get(testPointXid1).then(dataPoint => {
            dataPoint.name = 'Changed name';
            return dataPoint.save();
        }).then(dataPoint => {
            assert.strictEqual(dataPoint.name, 'Changed name');
        });
    });

    it('Lists all data points', function() {
        return DataPoint.list().then((dpList) => {
            assert.isArray(dpList);
            assert.isNumber(dpList.total);
            assert.isAtLeast(dpList.total, dpList.length);
        });
    });

    it('Queries for the new data point', function() {
        return DataPoint.query(`xid=${testPointXid1}`).then((dpList) => {
            assert.isArray(dpList);
            assert.isNumber(dpList.total);
            assert.equal(dpList.length, dpList.total);
            assert.equal(dpList.length, 1);
            assert.equal(dpList[0].name, this.testPoint1.name);
        });
    });
    
    it('Can create data points using minimal JSON', function() {
        const dp = new DataPoint({
            name: 'Node mango client test 2',
            dataSourceXid : this.ds.xid,
            pointLocator : {
                startValue : '0',
                modelType : 'PL.VIRTUAL',
                dataType : 'NUMERIC',
                settable : false,
                changeType : 'BROWNIAN',
                max: 100,
                maxChange: 0.01,
                min: 0
            }
        });

        return dp.save().then((savedDp) => {
            assert.isString(savedDp.xid);
            assert.equal(savedDp.name, 'Node mango client test 2');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
        });
    });
        
    it('Gets websocket notifications for data point create', function() {
        this.timeout(5000);
        
        let ws;
        const socketOpenDeferred = defer();
        const gotMessageDeferred = defer();
        const dsXid = this.ds.xid;
        const dpName = uuid();
        
        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/data-points'
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
                if (msg.payload.action === 'add' && msg.payload.object.name === dpName) {
                    gotMessageDeferred.resolve(msg);
                }
            });

            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start
            return socketOpenDeferred.promise.then(() => delay(1000));
        }).then(() => {
            // WebSocket is open and raring to go
            const dp = new DataPoint({
                name: dpName,
                dataSourceXid : dsXid,
                pointLocator : {
                    startValue : '0',
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    settable : false,
                    changeType : 'BROWNIAN',
                    max: 100,
                    maxChange: 0.01,
                    min: 0
                }
            });
            
            return dp.save();
        }).then(() => {
            // data point was saved, wait for the WebSocket message
            return gotMessageDeferred.promise;
        }).then(msg => {
            assert.strictEqual(msg.payload.object.name, dpName);
            assert.strictEqual(msg.payload.object.pointLocator.dataType, 'NUMERIC');
            assert.strictEqual(msg.payload.object.pointLocator.modelType, 'PL.VIRTUAL');
        }).finally(() => {
            if (ws) {
                // Close websocket 
                ws.close();
            }
        });
    });

});
