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
const fs = require('fs');
const csvParser = require('csv-parser');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Events tests', function(){
    before('Login', function() { return login.call(this, client); });

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
            simplifyTolerance: simplifyValue,
            tags: {
                tag1: 'tag1Value',
                tag2: 'tag2Value',
                tag3: 'tag3Value'
            }
        });
    };

    const raiseDelay = 1000; //Delay to raise alarm
    const testPointXid1 = uuid();

    beforeEach('Create a virtual data source, points, raise event', function() {
        this.timeout(raiseDelay * 200);

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
            this.ds.id = savedDs.id;
        }).then(() => {
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid, 'FIRST', 'NONE', 0);
            return this.testPoint1.save().then((savedDp) =>{
               this.testPoint1.id = savedDp.id;
            });
        }).then(() => {
            return client.restRequest({
                path: '/rest/latest/testing/raise-event',
                method: 'POST',
                data: {
                    event: {
                        eventType: 'DATA_POINT',
                        dataSourceId: this.ds.id,
                        referenceId1: this.testPoint1.id,
                        duplicateHandling: 'ALLOW'
                    },
                    context: {},
                    level: 'INFORMATION',
                    message: 'Dummy Point event'
                }
            });
        }).then(() => delay(raiseDelay));
    });

    afterEach('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });

    it('Gets websocket notifications for raised events', function() {
        this.timeout(5000);

        let ws;
        const subscription = {
            actions: ['RAISED'],
            levels: ['NONE'],
            sendActiveSummary: true,
            sendUnacknowledgedSummary: true,
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION'
        };

        const socketOpenDeferred = defer();
        const gotAlarmSummaries = defer();
        const gotEventDeferred = defer();

        const testId = uuid();
        return Promise.resolve().then(function() {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/events'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });

            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                gotEventDeferred.reject(msg);
                gotAlarmSummaries.reject(msg);
            });

            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                gotEventDeferred.reject(msg);
                gotAlarmSummaries.reject(msg);
            });

            ws.on('message', msgStr => {
                const msg = JSON.parse(msgStr);
                if(msg.messageType === 'RESPONSE') {
                    assert.strictEqual(msg.sequenceNumber, 0);
                    assert.property(msg, 'payload');
                    assert.strictEqual(msg.payload.activeSummary.length, 8);
                    assert.strictEqual(msg.payload.unacknowledgedSummary.length, 8);
                    gotAlarmSummaries.resolve();
                }else if(msg.messageType === 'NOTIFICATION' && msg.payload.message === 'test id ' + testId) {
                    assert.strictEqual(msg.notificationType, 'RAISED');
                    assert.property(msg.payload, 'eventType');
                    assert.strictEqual(msg.payload.eventType.eventType, 'SYSTEM');
                    assert.strictEqual(msg.payload.eventType.subType, 'Test event');
                    assert.strictEqual(msg.payload.alarmLevel, 'NONE');
                    gotEventDeferred.resolve();
                }else if(msg.status === 'ERROR') {
                    gotAlarmSummaries.reject();
                    gotEventDeferred.reject(new Error(msg.payload.message));
                }
            });
            return socketOpenDeferred.promise;
        }).then(function() {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotAlarmSummaries.promise).then(function() {
            return client.restRequest({
                path: '/rest/latest/testing/raise-event',
                method: 'POST',
                data: {
                    event: {
                        eventType: 'SYSTEM',
                        subType: 'Test event'
                    },
                    level: 'NONE',
                    message: 'test id ' + testId
                }
            });
        }).then(() => gotEventDeferred.promise).then(function(r) {
            ws.close();
            return r;
        }, function(e) {
            ws.close();
            return Promise.reject(e);
        });
    });

    it('Can get data point totals via websocket', function() {
        this.timeout(5000);

        let ws;
        const subscription = {
            actions: ['RAISED'],
            levels: ['NONE','INFORMATION'],
            sendActiveSummary: true,
            sendUnacknowledgedSummary: true,
            messageType: 'REQUEST',
            requestType: 'SUBSCRIPTION'
        };

        const socketOpenDeferred = defer();
        const gotAlarmSummaries = defer();
        const gotEventQueryResult = defer();

        return Promise.resolve().then(function() {
            ws = client.openWebSocket({
                path: '/rest/latest/websocket/events'
            });

            ws.on('open', () => {
                socketOpenDeferred.resolve();
            });

            ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                gotEventQueryResult.reject(msg);
                gotAlarmSummaries.reject(msg);
            });

            ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                gotEventQueryResult.reject(msg);
                gotAlarmSummaries.reject(msg);
            });

            ws.on('message', msgStr => {
                assert.isString(msgStr);
                const msg = JSON.parse(msgStr);
                if(msg.messageType === 'RESPONSE' && msg.sequenceNumber === 0) {
                    assert.property(msg, 'payload');
                    assert.strictEqual(msg.payload.activeSummary.length, 8);
                    assert.strictEqual(msg.payload.unacknowledgedSummary.length, 8);
                    gotAlarmSummaries.resolve();
                }else if(msg.messageType === 'RESPONSE' && msg.sequenceNumber === 1) {
                    assert.property(msg, 'payload');
                    assert.isArray(msg.payload);
                    assert.strictEqual(msg.payload.length, 1);
                    assert.strictEqual(msg.payload[0].xid, testPointXid1);
                    assert.strictEqual(msg.payload[0].counts.INFORMATION, 1);

                    gotEventQueryResult.resolve();
                }else if(msg.status === 'ERROR') {
                    gotAlarmSummaries.reject();
                    gotEventQueryResult.reject(new Error(msg.payload.message));
                }
            });
            return socketOpenDeferred.promise;
        }).then(function() {
            const send = defer();
            ws.send(JSON.stringify(subscription), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotAlarmSummaries.promise).then(function() {
            const send = defer();
            ws.send(JSON.stringify({
                messageType: 'REQUEST',
                requestType: 'DATA_POINT_SUMMARY',
                dataPointXids: [testPointXid1],
                sequenceNumber: 1
            }), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotEventQueryResult.promise).then(function(r) {
            ws.close();
            return r;
        }, function(e) {
            ws.close();
            return Promise.reject(e);
        });
    });

    it('Can query for data point events', function() {
        return client.restRequest({
            path: `/rest/latest/events?eq(eventType,DATA_POINT)&eq(referenceId1,${this.testPoint1.id})&sort(-activeTimestamp)&limit(15,0)`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.items.length, 1);
            assert.isAtLeast(response.data.total, 1);
        });
    });

    it('Can export events csv format', function() {
        const fileName = 'events.csv';
        return client.restRequest({
            path: `/rest/latest/events?eq(eventType,DATA_POINT)&eq(referenceId1,${this.testPoint1.id})&sort(-activeTimestamp)&limit(15,0)&format=csv2`,
            method: 'GET',
            writeToFile: fileName
        }).then(response => {
            assert.strictEqual(response.status, 200);

            return extractCsvData(fileName).then(data => {
                assert.strictEqual(data.length, 1);
                const columns = Object.keys(data[0]);
                assert.strictEqual(columns.length, 90);
            });
        });
    });

    it('Can export events reduced csv format with data point tags', function() {
        return client.restRequest({
            method: 'POST',
            path: '/rest/latest/system-settings',
            data: { 'eventsExport.tags': 'tag1,tag2' }
        }).then(response => {
            assert.strictEqual(response.status, 200);

            const fileName = 'events_reduced_with_tags.csv';
            return client.restRequest({
                path: `/rest/latest/events/reduced?eq(eventType,DATA_POINT)&eq(referenceId1,${this.testPoint1.id})&sort(-activeTimestamp)&limit(15,0)&format=csv2`,
                method: 'GET',
                writeToFile: fileName
            }).then(response => {
                assert.strictEqual(response.status, 200);

                return extractCsvData(fileName).then(data => {
                    assert.strictEqual(data.length, 1);
                    const columns = Object.keys(data[0]);
                    const tagColumns = ['dataPointTags/tag1', 'dataPointTags/tag2' ];
                    assert.strictEqual(columns.length, baseCsvColumns.length + tagColumns.length);
                    [...tagColumns, ...baseCsvColumns].every(item => {
                        assert.equal(columns.includes(item), true, `column: ${item} is missing on CSV export`);
                    });
                });
            });
        });
    });

    it('Can export events reduced csv format', function() {
        return client.restRequest({
            method: 'POST',
            path: '/rest/latest/system-settings',
            data: { 'eventsExport.tags': null }
        }).then(response => {
            assert.strictEqual(response.status, 200);

            const fileName = 'events_reduced.csv';
            return client.restRequest({
                path: `/rest/latest/events/reduced?eq(eventType,DATA_POINT)&eq(referenceId1,${this.testPoint1.id})&sort(-activeTimestamp)&limit(15,0)&format=csv2`,
                method: 'GET',
                writeToFile: fileName
            }).then(response => {
                assert.strictEqual(response.status, 200);

                return extractCsvData(fileName).then(data => {
                    assert.strictEqual(data.length, 1);
                    const columns = Object.keys(data[0]);
                    assert.strictEqual(columns.length, baseCsvColumns.length);
                    baseCsvColumns.every(item => {
                        assert.equal(columns.includes(item), true, `column: ${item} is missing on CSV export`);
                    });
                });
            });
        });
    });

    const baseCsvColumns = [
        '﻿"',
        'id',
        'activeTimestamp',
        'acknowledgedByUserId',
        'acknowledgedByUsername',
        'acknowledgedTimestamp',
        'alternateAckSource',
        'rtnApplicable',
        'rtnTimestamp',
        'rtnCause',
        'alarmLevel',
        'message',
        'rtnMessage',
        'activeDate',
        'acknowledgedDate',
        'rtnDate',
        'active',
        'acknowledged',
        'dataPointTags'
    ];

    const extractCsvData = (fileName) => {
        const result = [];
        return new Promise((resolve, reject) => {
            fs.createReadStream(fileName)
                .pipe(csvParser())
                .on('data', (row) => {
                    result.push(row);
                })
                .on('end', () => {
                    resolve(result);
                })
                .on('error', reject);
        });
    }

});
