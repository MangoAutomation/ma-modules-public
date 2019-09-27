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
 */

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;

describe('Event detector bulk operations', () => {
    before('Login', login.bind(this, client));

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
    
    const highLimitDetector = (xid, sourceId) => {
        return {
            xid: xid,
            sourceId: sourceId,
            name : "When true.",
            alarmLevel : 'URGENT',
            duration : {
                periods: 10,
                type: 'SECONDS'
            },
            limit: 10.0,
            resetLimit: 9.0,
            useResetLimit: true,
            notHigher: false,
            detectorType : "HIGH_LIMIT",
        };
    };
    
    const testPointXid1 = uuid();
    const testPointXid2 = uuid();
    const testDetectorXid1 = uuid();
    const testDetectorXid2 = uuid();
    
    before('Create a virtual data source and points', function() {

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
            this.ed1 = highLimitDetector(testDetectorXid1, this.testPoint1.id);
            this.ed2 = highLimitDetector(testDetectorXid2, this.testPoint2.id);
            return Promise.all([
                client.restRequest({
                    path: '/rest/v2/full-event-detectors',
                    method: 'POST',
                    data: this.ed1
                }).then(response => {
                    this.ed1.id = response.data.id;
                }),
                client.restRequest({
                    path: '/rest/v2/full-event-detectors',
                    method: 'POST',
                    data: this.ed2
                }).then(response => {
                    this.ed2.id = response.data.id;
                })
           ]);
        });
    });

    after('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });
    
    it('Can bulk update event detectors', function() {
        this.timeout(5000);
        
        this.ed1.name = 'new name for ed1';
        this.ed2.name = 'new name for ed2';
        return client.restRequest({
            path: `/rest/v2/full-event-detectors/bulk`,
            method: 'POST',
            data: {
                action: 'UPDATE',
                body: null, //defaults?
                requests: [
                    {
                        action: 'UPDATE',
                        xid: this.ed1.xid,
                        body: this.ed1
                    },
                    {
                        action: 'UPDATE',
                        xid: this.ed2.xid,
                        body: this.ed2
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
            assert.strictEqual(results[0].body.name, this.ed1.name);
            assert.strictEqual(results[1].httpStatus, 200);
            assert.strictEqual(results[1].body.name, this.ed2.name);
        });
    });
});
