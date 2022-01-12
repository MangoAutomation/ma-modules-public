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

const fs = require('fs');
const {createClient, login, delay, noop, config} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const MangoObject = client.MangoObject;
const DataSource = client.DataSource;

class Publisher extends MangoObject {
    static get baseUrl() {
        return '/rest/latest/publishers-without-points';
    }
}

const stressTestConfig = Object.assign({
    duration: 60000,
    numPoints: 100,
    resultsFile: null,
    heapDumpFile: null
}, config.stressTest);

describe('Stress test', function() {
    before('Login', function() { return login.call(this, client); });

    const waitUntilComplete = (response) => {
        if (response.data.status === 'SUCCESS') {
            return response;
        } else if (['TIMED_OUT', 'CANCELLED', 'ERROR'].includes(response.data.status)) {
            return Promise.reject(new Error(`Temporary resource error ${response.data.status}`));
        } else {
            return delay(1000).then(() => {
                // response.headers.location is not set when you do a GET, only on first POST
                let url;
                switch (response.data.resourceType) {
                    case 'BULK_DATA_POINT': url = '/rest/latest/data-points/bulk/'; break;
                    case 'BULK_PUBLISHED_POINT': url = '/rest/latest/published-points/bulk/'; break;
                    default: throw new Error('Unknown type ' + response.data.resourceType);
                }
                url += encodeURIComponent(response.data.id);

                return client.restRequest({
                    path: url,
                    method: 'GET'
                }).then(waitUntilComplete);
            });
        }
    };
    
    before('Create the data sources, data points, and publishers', function() {
        this.timeout(30000 + stressTestConfig.numPoints * 100);
        
        this.virtual = new DataSource({
            name: 'Stress test virtual',
            enabled: false,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 1, type: 'SECONDS' }
        });
        
        this.meta = new DataSource({
            name: 'Stress test meta',
            enabled: false,
            modelType: 'META'
        });
        
        this.persistent = new DataSource({
            name: 'Stress test persistent',
            enabled: true,
            modelType: 'PERSISTENT',
            acceptPointUpdates: true,
            authorizationKey: 'xMzo5X@OPM70IlV$',
            extraTags: {},
            keySize: 256,
            logCount: 1,
            logLevel: 'NONE',
            logSize: 1,
            logToConsole: false,
            newPointReadPermissions: [],
            newPointSetPermissions: [],
            overridePermissionsUpdates: false,
            pathPrefix: [],
            port: 6413,
            saveRealtimeData: false,
            sharedKey: '8bc2a2e13ebeb1f4b5dda7a12e245a3f48027445778d17e16361db96f91d856d',
            socketTimeout: 30000,
            useCompression: true,
            useCrc: true
        });
        
        this.internal = new DataSource({
            name: 'Stress test internal',
            enabled: true,
            modelType: 'INTERNAL',
            quantize: false,
            useCron: false,
            createPointsPattern: null,
            pollPeriod: {
              periods: 10,
              type: 'SECONDS'
            },
            purgeSettings: {
              override: false,
              frequency: {
                periods: 1,
                type: 'YEARS'
              }
            },
            editPermission: [],
        });
        
        this.publisher = new Publisher({
            name: 'Stress test publisher',
            enabled: false,
            modelType: 'PERSISTENT',
            publishType: 'ALL',
            cacheWarningSize: 1000,
            cacheDiscardSize: 10000,
            sendSnapshot: false,
            snapshotSendPeriod: {
                periods: 5,
                type: 'MINUTES'
            },
            publishAttributeChanges: true,
            host: 'localhost',
            port: 6413,
            socketTimeout: 30000,
            connectionCheckPeriod: 60000,
            authorizationKey: 'xMzo5X@OPM70IlV$',
            xidPrefix: 'ST_',
            syncPattern: '* 0/2 * * * ?',
            syncRealTime: true,
            syncPointHierarchy: false,
            historyCutoffPeriod: {
                periods: 2,
                type: 'SECONDS'
            },
            syncResponseTimeout: 1200000,
            parallelSyncTasks: 3,
            maxPointValuesToSend: 5000,
            useCompression: true,
            logLevel: 'LOG_LEVEL_NONE',
            logSize: 1,
            logCount: 1,
            useCrc: true,
            reconnectSyncs: false,
            syncRequestRetryAttempts: 3,
            syncMinimumOverlap: 500,
            pathPrefix: [],
            extraTags: {},
            overridePermissionTransmission: false,
            overrideReadPermission: '',
            overrideSetPermission: '',
            preventUnsyncedDataPurge: true,
            sharedKey: '8bc2a2e13ebeb1f4b5dda7a12e245a3f48027445778d17e16361db96f91d856d',
            keySize: 256,
            allowSettable: false
        });

        return Promise.all([this.virtual.save(), this.meta.save(), this.persistent.save(), this.internal.save()]).then(() => {
            // Create internal data points
            const points = [
                {
                    enabled: true,
                    name: 'Used memory',
                    dataSourceXid: this.internal.xid,
                    pointLocator: {
                        modelType: 'PL.INTERNAL',
                        dataType: 'NUMERIC',
                        settable: false,
                        monitorId: 'java.lang.Runtime.usedMemory'
                    },
                    loggingProperties: {loggingType: 'ALL'}
                }
            ];
            
            return client.restRequest({
                path: '/rest/latest/data-points/bulk',
                method: 'POST',
                data: {
                    action: 'CREATE',
                    requests: points.map(pt => ({body: pt}))
                }
            }).then(waitUntilComplete).then(response => {
                assert.isFalse(response.data.result.hasError);
                this.internalXids = response.data.result.responses.map(r => r.body.xid);
            });
        }).then(() => {
            // Create virtual data points
            const points = Array(stressTestConfig.numPoints).fill().map((e, i) => ({
                enabled: true,
                name: `Stress test virtual ${('' + i).padStart(3, '0')}`,
                dataSourceXid: this.virtual.xid,
                pointLocator: {
                    startValue : '50',
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    changeType : 'BROWNIAN',
                    settable: false,
                    max: 100,
                    maxChange: 0.1,
                    min: 0
                },
                loggingProperties: {
                    loggingType: 'ALL'
                }
            }));
            
            return client.restRequest({
                path: '/rest/latest/data-points/bulk',
                method: 'POST',
                data: {
                    action: 'CREATE',
                    requests: points.map(pt => ({body: pt}))
                }
            }).then(waitUntilComplete).then(response => {
                assert.isFalse(response.data.result.hasError);
                this.virtualXids = response.data.result.responses.map(r => r.body.xid);
            });
        }).then(() => {
            // Create meta data points
            const points = Array(stressTestConfig.numPoints).fill().map((e, i) => ({
                enabled: true,
                name: `Stress test meta ${('' + i).padStart(3, '0')}`,
                dataSourceXid: this.meta.xid,
                pointLocator: {
                    modelType: 'PL.META',
                    context: [
                        {
                            xid: this.virtualXids[i],
                            variableName: 'src',
                            contextUpdate: true
                        }
                    ],
                    script: 'return src.value * 10 - 100;',
                    scriptPermissions: [],
                    executionDelaySeconds: 0,
                    logCount: 5,
                    logSize: 1,
                    logLevel: 'NONE',
                    scriptEngine: 'JAVASCRIPT',
                    dataType: 'NUMERIC',
                    updateEvent: 'NONE',
                    variableName: 'my',
                    settable: false,
                    contextUpdateEvent: 'CONTEXT_UPDATE',
                    updateCronPattern: '',
                    relinquishable: false
                },
                loggingProperties: {
                    loggingType: 'INTERVAL',
                    intervalLoggingType: 'AVERAGE',
                    intervalLoggingPeriod: {periods: 1, type: 'MINUTES'}
                }
            }));
            
            return client.restRequest({
                path: '/rest/latest/data-points/bulk',
                method: 'POST',
                data: {
                    action: 'CREATE',
                    requests: points.map(pt => ({body: pt}))
                }
            }).then(waitUntilComplete).then(response => {
                assert.isFalse(response.data.result.hasError);
                this.metaXids = response.data.result.responses.map(r => r.body.xid);
            });
        }).then(() => {
            // Save the publisher
            return this.publisher.save();
        }).then(publisher => {
            // Add meta points to publisher
            const publisherPoints = this.metaXids.map((xid, i) => ({
                publisherXid: publisher.xid,
                dataPointXid: xid,
                modelType: 'PERSISTENT',
                enabled: true,
                name: `Point ${i}`
            }));

            return client.restRequest({
                path: '/rest/latest/published-points/bulk',
                method: 'POST',
                data: {
                    action: 'CREATE',
                    requests: publisherPoints.map(pp => ({body: pp}))
                }
            }).then(waitUntilComplete).then(response => {
                assert.isFalse(response.data.result.hasError);
            });
        });
    });
    
    after('Delete the data sources, data points, and publishers', function() {
        this.timeout(30000);
        
        return this.publisher.delete().catch(noop).then(() => {
            return this.persistent.delete().catch(noop);
        }).then(() => {
            return this.meta.delete().catch(noop);
        }).then(() => {
            return this.virtual.delete().catch(noop);
        }).then(() => {
            return this.internal.delete().catch(noop);
        });
    });
    
    it('Runs the stress test successfully', function() {
        // allow an extra 100 ms per point for data source startup
        this.timeout(30000 + stressTestConfig.numPoints * 100 + stressTestConfig.duration);
        
        const results = {};
        
        this.virtual.enabled = true;
        return this.virtual.save().then(() => {
            this.meta.enabled = true;
            return this.meta.save();
        }).then(() => {
            this.publisher.enabled = true;
            return this.publisher.save();
        }).then(() => {
            return delay(stressTestConfig.duration);
        }).then(() => {
            if (stressTestConfig.heapDumpFilename) {
                return client.restRequest({
                    path: '/rest/latest/testing/heap-dump',
                    method: 'POST',
                    params: {filename: stressTestConfig.heapDumpFile, readable: true}
                }).then(response => {
                    results.heapDumpFile = response.data;
                });
            }
        }).then(() => {
            return client.restRequest({
                path: '/rest/latest/testing/jvm-info',
            });
        }).then(response => {
            results.jvmInfo = response.data;

            if (stressTestConfig.resultsFile) {
                fs.writeFileSync(stressTestConfig.resultsFile, JSON.stringify(results, null, 4));
            }
        });
    });
});
