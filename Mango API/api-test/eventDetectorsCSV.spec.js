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

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;
const csvParser = require('csv-parser');
const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const Readable = require('stream').Readable;
const path = require('path');
const fs = require('fs');

function eventDetectorsCsvFactory(client) {
    return class EventDetectorsCsv {
        get baseUrl() {
            return '/rest/latest/event-detectors';
        }
        
        download(query) {
            return client.restRequest({
                path: `${this.baseUrl}?` + query,
                method: 'GET'
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
        
        uploadCsvFile(csvFileName) {
            return client.restRequest({
                path: `${this.baseUrl}/bulk`,
                method: 'POST',
                headers: {
                    'Content-Type': 'text/csv;charset=UTF-8'
                },
                data: fs.readFileSync(csvFileName)
            }).then(response => {
                return response.headers.location;
            });
        }
        
        uploadCsvData(csvData) {
            return client.restRequest({
                path: `${this.baseUrl}/bulk`,
                method: 'POST',
                headers: {
                    'Content-Type': 'text/csv;charset=UTF-8'
                },
                data: csvData
            }).then(response => {
                return response.headers.location;
            });
        }
        
        getUploadStatus(location, delayMs) {
            return delay(delayMs).then(()=>{
                return client.restRequest({
                    path: location
                }).then(response => {
                    return response.data.result;
                 });                    
            });
        }
    };
}

describe('Event detectors CSV format', function() {
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
           
            const EventDetectorsCsv = eventDetectorsCsvFactory(this.csvClient);
            this.csvClient.eventDetectorsCsv = new EventDetectorsCsv();
            // copy the session cookie to the csv client
            Object.assign(this.csvClient.cookies, client.cookies);
        });
    });

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
            tags: {
                tag1: 'value1',
                tag2: 'value2'
            },
            rollup: 'AVERAGE'
        });
    };
    
    //Delay after upload before requesting result
    const uploadDelayMs = 1000;
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
    
    beforeEach('Create a virtual data source and points', function() {

        this.testPointXid1 = uuid();
        this.testPointXid2 = uuid();
        this.testDetectorXid1 = 'AAA' + uuid();
        this.testDetectorXid2 = 'ZZZ' + uuid();
        
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
            this.testPoint1 = newDataPoint(this.testPointXid1, this.ds.xid);
            this.testPoint2 = newDataPoint(this.testPointXid2, this.ds.xid);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save()]);
        }).then(() => {
            this.ed1 = highLimitDetector(this.testDetectorXid1, this.testPoint1.id);
            this.ed2 = highLimitDetector(this.testDetectorXid2, this.testPoint2.id);
            return Promise.all([
                client.restRequest({
                    path: '/rest/latest/event-detectors',
                    method: 'POST',
                    data: this.ed1
                }).then(response => {
                    this.ed1.id = response.data.id;
                }),
                client.restRequest({
                    path: '/rest/latest/event-detectors',
                    method: 'POST',
                    data: this.ed2
                }).then(response => {
                    this.ed2.id = response.data.id;
                })
           ]);
        });
    });

    afterEach('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });
    
    it('Can download csv file for both event detectors', function() {
        return this.csvClient.eventDetectorsCsv.download(`in(xid,${this.ed1.xid},${this.ed2.xid})&sort(xid)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalXid');
            assert.strictEqual(headers[3], 'sourceId');
            assert.strictEqual(headers[4], 'readPermission');
            assert.strictEqual(headers[5], 'editPermission');
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].sourceId, String(this.ed1.sourceId));
            assert.strictEqual(result[1].sourceId, String(this.ed2.sourceId));
            assert.strictEqual(result[0].originalXid, String(this.ed1.xid));
            assert.strictEqual(result[1].originalXid, String(this.ed2.xid));
        });
    });
    
    it('Can update both event detectors source point via csv upload file', function() {
        this.timeout(5000);
        return this.csvClient.eventDetectorsCsv.download(`in(xid,${this.ed1.xid},${this.ed2.xid})&sort(xid)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalXid');
            assert.strictEqual(headers[3], 'sourceId');
            assert.strictEqual(headers[4], 'readPermission');
            assert.strictEqual(headers[5], 'editPermission');
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].sourceId, String(this.ed1.sourceId));
            assert.strictEqual(result[1].sourceId, String(this.ed2.sourceId));
            assert.strictEqual(result[0].originalXid, String(this.ed1.xid));
            assert.strictEqual(result[1].originalXid, String(this.ed2.xid));
            
            //Modify the source points by swapping them
            result[0].sourceId = String(this.ed2.sourceId);
            result[1].sourceId = String (this.ed1.sourceId);
            //Create the csv data to POST
            let csvHeaders = [];
            for(var i=0; i<headers.length; i++){
                csvHeaders.push({
                    id: headers[i],
                    title: headers[i]
                });
            }
            let filename = 'eventDetectors.csv';
            const csvWriter = createCsvWriter({
                path: filename,
                header: csvHeaders
            });
            const uploadFileName = path.resolve(filename);
            return csvWriter.writeRecords(result).then(() => {
                return this.csvClient.eventDetectorsCsv.uploadCsvFile(uploadFileName).then(location => {
                    return delay(uploadDelayMs).then(() => {
                        return client.restRequest({
                            path: location
                        }).then(response => {
                            assert.strictEqual(response.data.result.hasError, false);
                            assert.strictEqual(response.data.result.responses[0].xid, this.ed1.xid);
                            assert.strictEqual(response.data.result.responses[1].xid, this.ed2.xid);
                            //Confirm the change
                            assert.strictEqual(response.data.result.responses[0].body.sourceId, this.ed2.sourceId);
                            assert.strictEqual(response.data.result.responses[1].body.sourceId, this.ed1.sourceId);
                        });                    
                    });
                });
            }).finally(() => {
                //Delete the file
                fs.unlinkSync(uploadFileName);
            });
        });
    });
    
    it('Can modify xid of existing event detector', function() {
        this.timeout(5000);
        return this.csvClient.eventDetectorsCsv.download(`in(xid,${this.ed1.xid},${this.ed2.xid})&sort(xid)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalXid');
            assert.strictEqual(headers[3], 'sourceId');
            assert.strictEqual(headers[4], 'readPermission');
            assert.strictEqual(headers[5], 'editPermission');
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].sourceId, String(this.ed1.sourceId));
            assert.strictEqual(result[1].sourceId, String(this.ed2.sourceId));
            assert.strictEqual(result[0].originalXid, String(this.ed1.xid));
            assert.strictEqual(result[1].originalXid, String(this.ed2.xid));
            
            //Modify the source points by swapping them
            const ed1Xid = uuid();
            const ed2Xid = uuid();
            result[0].xid = ed1Xid;
            result[1].xid = ed2Xid;
            //Create the csv data to POST
            let csvHeaders = [];
            for(var i=0; i<headers.length; i++){
                csvHeaders.push({
                    id: headers[i],
                    title: headers[i]
                });
            }
            let filename = 'eventDetectors.csv';
            const csvWriter = createCsvWriter({
                path: filename,
                header: csvHeaders
            });
            const uploadFileName = path.resolve(filename);
            return csvWriter.writeRecords(result).then(() => {
                return this.csvClient.eventDetectorsCsv.uploadCsvFile(uploadFileName).then(location => {
                    return delay(uploadDelayMs).then(() => {
                        return client.restRequest({
                            path: location
                        }).then(response => {
                            assert.strictEqual(response.data.result.hasError, false);
                            assert.strictEqual(response.data.result.responses[0].xid, this.ed1.xid);
                            assert.strictEqual(response.data.result.responses[1].xid, this.ed2.xid);
                            //Confirm the change
                            assert.strictEqual(response.data.result.responses[0].body.xid, ed1Xid);
                            assert.strictEqual(response.data.result.responses[1].body.xid, ed2Xid);
                        });                    
                    });
                });
            }).finally(() => {
                //Delete the file
                fs.unlinkSync(uploadFileName);
            });
        });
    });
    
    it('Fails to update an event detector that does not exist', function() {
        this.timeout(5000);
        this.timeout(5000);
        return this.csvClient.eventDetectorsCsv.download(`in(xid,${this.ed1.xid},${this.ed2.xid})&sort(xid)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalXid');
            assert.strictEqual(headers[3], 'sourceId');
            assert.strictEqual(headers[4], 'readPermission');
            assert.strictEqual(headers[5], 'editPermission');
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].sourceId, String(this.ed1.sourceId));
            assert.strictEqual(result[1].sourceId, String(this.ed2.sourceId));
            assert.strictEqual(result[0].originalXid, String(this.ed1.xid));
            assert.strictEqual(result[1].originalXid, String(this.ed2.xid));
            
            //Modify the source points by swapping them
            result[0].originalXid = 'IDONTEXIST';
            //Create the csv data to POST
            let csvHeaders = [];
            for(var i=0; i<headers.length; i++){
                csvHeaders.push({
                    id: headers[i],
                    title: headers[i]
                });
            }
            let filename = 'eventDetectors.csv';
            const csvWriter = createCsvWriter({
                path: filename,
                header: csvHeaders
            });
            const uploadFileName = path.resolve(filename);
            return csvWriter.writeRecords(result).then(() => {
                return this.csvClient.eventDetectorsCsv.uploadCsvFile(uploadFileName).then(location => {
                    return delay(uploadDelayMs).then(() => {
                        return client.restRequest({
                            path: location
                        }).then(response => {
                            assert.strictEqual(response.data.result.hasError, true);
                            assert.strictEqual(response.data.result.responses[0].xid, 'IDONTEXIST');
                            assert.strictEqual(response.data.result.responses[0].httpStatus, 404);
                            
                            assert.strictEqual(response.data.result.responses[1].xid, this.ed2.xid);
                            assert.strictEqual(response.data.result.responses[1].error, null);
                        });                    
                    });
                });
            }).finally(() => {
                //Delete the file
                fs.unlinkSync(uploadFileName);
            });
        });
    });
});
