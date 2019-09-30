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

const {createClient, login, defer, delay} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Data point service', function() {
    before('Login', function() { return login.call(this, client); });

    it('Creates a new virtual data source', () => {
        const ds = new DataSource({
            xid: 'mango_client_test',
            name: 'Mango client test',
            deviceName: 'Mango client device name',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return ds.save().then((savedDs) => {
            assert.strictEqual(savedDs, ds);
            assert.equal(savedDs.xid, 'mango_client_test');
            assert.equal(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
        });
    });

    it('Creates a binary virtual data point', () => {
        const dp = new DataPoint({
            xid : "dp_mango_client_test",
            deviceName : "_",
            name : "Virtual Test Point 1",
            enabled : false,
            templateXid : "Binary_Default",
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
            chartRenderer : {
                limit : 10,
                type : "chartRendererTable"
            },
            dataSourceXid : "mango_client_test",
            useIntegralUnit : false,
            useRenderedUnit : false,
            readPermission : "read",
            setPermission : "write",
            chartColour : "",
            rollup : "NONE",
            plotType : "STEP",
            purgeOverride : false,
            purgePeriod : {
                periods : 1,
                type : "YEARS"
            },
            unit : "",
            pointFolderId : 0,
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

        return dp.save().then((savedDp) => {
            assert.equal(savedDp.xid, 'dp_mango_client_test');
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
        });
    });
    
    it('Enables the binary data point', () => {
        return DataPoint.get('dp_mango_client_test').then(dataPoint => {
            return dataPoint.setEnabled(true);
        }).then(dataPoint => {
            assert.isTrue(dataPoint.enabled);
        });
    });
    
    it('Restarts the binary data point', () => {
        return DataPoint.get('dp_mango_client_test').then(dataPoint => {
            return dataPoint.setEnabled(true, true);
        }).then(dataPoint => {
            assert.isTrue(dataPoint.enabled);
        });
    });
    
    it('Disables the binary data point', () => {
        return DataPoint.get('dp_mango_client_test').then(dataPoint => {
            return dataPoint.setEnabled(false);
        }).then(dataPoint => {
            assert.isFalse(dataPoint.enabled);
        });
    });
    
    it('Can change the data point name', () => {
        return DataPoint.get('dp_mango_client_test').then(dataPoint => {
            dataPoint.name = 'Changed name';
            return dataPoint.save();
        }).then(dataPoint => {
            assert.strictEqual(dataPoint.name, 'Changed name');
        });
    });
    
    it('Lists all data points', () => {
        return DataPoint.list().then((dpList) => {
            assert.isArray(dpList);
            assert.isNumber(dpList.total);
            assert.isAtLeast(dpList.total, dpList.length);
        });
    });

    it('Queries for the new data point', () => {
        return DataPoint.query('xid=dp_mango_client_test').then((dpList) => {
            assert.isArray(dpList);
            assert.isNumber(dpList.total);
            assert.equal(dpList.length, dpList.total);
            assert.equal(dpList.length, 1);
            assert.equal(dpList[0].name, 'Changed name');
        });
    });
    
    it('Can create data points using minimal JSON', () => {
        const dp = new DataPoint({
            name: 'Node mango client test 2',
            dataSourceXid : 'mango_client_test',
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
    
    it('Can create data points with a template using minimal JSON', () => {
        const dp = new DataPoint({
            xid: 'dp_mango_client_test_3',
            name: 'Node mango client test 3',
            templateXid : 'Numeric_Default',
            dataSourceXid : 'mango_client_test',
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
            assert.strictEqual(savedDp.xid, 'dp_mango_client_test_3');
            assert.strictEqual(savedDp.name, 'Node mango client test 3');
            assert.strictEqual(savedDp.enabled, false);
            assert.strictEqual(savedDp.templateXid, 'Numeric_Default');
            assert.strictEqual(savedDp.templateName, 'Numeric');
            assert.isNumber(savedDp.id);
        });
    });
    
    it('Can update a data point with a template using minimal JSON', () => {
        const dp = new DataPoint({
            originalId: 'dp_mango_client_test_3',
            name: 'Node mango client test 3 - changed'
        });

        return dp.save().then((savedDp) => {
            assert.strictEqual(savedDp.xid, 'dp_mango_client_test_3');
            assert.strictEqual(savedDp.name, 'Node mango client test 3 - changed');
            assert.strictEqual(savedDp.enabled, false);
            assert.strictEqual(savedDp.templateXid, 'Numeric_Default');
            assert.strictEqual(savedDp.templateName, 'Numeric');
            assert.isNumber(savedDp.id);
        });
    });

    it('Can update a data point and remove its template', function () {
        this.timeout(50000000);
        const dp = new DataPoint({
            originalId: 'dp_mango_client_test_3',
            templateXid: null
        });

        return dp.save().then((savedDp) => {
            assert.strictEqual(savedDp.xid, 'dp_mango_client_test_3');
            assert.strictEqual(savedDp.enabled, false);
            assert.strictEqual(savedDp.templateXid, null);
            assert.notProperty(savedDp, 'templateName');
            assert.isNumber(savedDp.id);
        });
    });

    it('Can update data point read permissions', function () {
        this.timeout(50000000);
        return client.restRequest({
            path: `/rest/v1/data-points/bulk-apply-read-permissions?xid=dp_mango_client_test`,
            method: 'POST',
            data: 'permission1,permission2'
        }).then(response => {
            assert.equal(response.data, 1);
            return DataPoint.get('dp_mango_client_test').then(point => {
                assert.strictEqual(point.readPermission, 'permission1,permission2,read');
            });
        });
    });
    it('Can update data point set permissions', function () {
        this.timeout(50000000);
        return client.restRequest({
            path: `/rest/v1/data-points/bulk-apply-set-permissions?xid=dp_mango_client_test`,
            method: 'POST',
            data: 'permission1,permission2'
        }).then(response => {
            assert.equal(response.data, 1);
            return DataPoint.get('dp_mango_client_test').then(point => {
                assert.strictEqual(point.setPermission, 'permission1,permission2,write');
            });
        });
    });
    
    it('Gets v2 websocket notifications for data point create', function() {
        this.timeout(5000);
        
        let ws;
        const socketOpenDeferred = defer();
        const actionFinishedDeferred = defer();

        return Promise.resolve().then(() => {
            ws = client.openWebSocket({
                path: '/rest/v2/websocket/data-points'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });
            
            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                actionFinishedDeferred.reject(msg);
            });
            
            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                actionFinishedDeferred.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                if(msg.payload.action === 'add'){
                    assert.strictEqual(msg.payload.object.name, 'Node mango client ws');
                    assert.strictEqual(msg.payload.object.pointLocator.dataType, 'NUMERIC');
                    assert.strictEqual(msg.payload.object.pointLocator.modelType, 'PL.VIRTUAL');
                    actionFinishedDeferred.resolve();
                }else{
                    actionFinishedDeferred.reject();
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => delay(1000)).then(() => {
            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start
            const dp = new DataPoint({
                name: 'Node mango client ws',
                dataSourceXid : 'mango_client_test',
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
            return client.restRequest({
                path: '/rest/v2/data-points',
                method: 'POST',
                data: dp
            }).then(response => {
                //TODO Could assert but not really the purpose here
            }, error =>{
                actionFinishedDeferred.reject(error);
            });
            
        }).then(() => actionFinishedDeferred.promise ).then(() => {
          //Close websocket 
            ws.close();
        });
    });
    
    
    it('Deletes the new virtual data source and its points', () => {
        return DataSource.delete('mango_client_test');
    });

});
