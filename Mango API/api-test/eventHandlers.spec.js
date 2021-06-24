/**
 * Copyright 2018 Infinite Automation Systems Inc.
 * http://infiniteautomation.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

const {
    createClient,
    login,
    defer,
    delay,
    uuid,
    noop
} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;
const EventDetector = client.EventDetector;

describe('Event handlers', function() {

    before('Login', function() {
        return login.call(this, client);
    });

    before('Create data sources', function() {
        this.createPoint = (options) => {
            const pointLocator = Object.assign({
                startValue: '0',
                modelType: 'PL.VIRTUAL',
                dataType: 'BINARY',
                changeType: 'NO_CHANGE',
                settable: true
            }, options.pointLocator);
            delete options.pointLocator;

            return new DataPoint(Object.assign({
                enabled: true,
                deviceName: 'Data point test',
                dataSourceXid: this.ds1.xid,
                pointLocator
            }, options));
        };

        this.createHandler = (options) => {
            return Object.assign({
                xid: `EH_${uuid()}`,
                name: "Test event handler",
                disabled: false,
                targetPointXid: this.dp1.xid,
                activeAction: "STATIC_VALUE",
                inactiveAction: "STATIC_VALUE",
                activeValueToSet: false,
                inactiveValueToSet: true,
                activeScript: '',
                inactiveScript: '',
                scriptContext: [],
                scriptPermissions: [],
                eventTypes: [
                    {
                        eventType: 'DATA_SOURCE',
                        subType: '',
                        referenceId1: this.ds1.id,
                        referenceId2: 1 //Poll Aborted
                    }
                ],
                handlerType: "SET_POINT"
            }, options);
        };

        this.ds1 = new DataSource({
            xid: `DS_${uuid()}`,
            name: 'Event Handler Testing 1',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: {periods: 5, type: 'SECONDS'},
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}},
            alarmLevels: {POLL_ABORTED: 'URGENT'},
            editPermission: null
        });

        this.ds2 = new DataSource({
            xid: `DS_${uuid()}`,
            name: 'Event Handler Testing 2',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: {periods: 5, type: 'SECONDS'},
            purgeSettings: {override: false, frequency: {periods: 1, type: 'YEARS'}},
            alarmLevels: {POLL_ABORTED: 'URGENT'},
            editPermission: null
        });

        return Promise.all([this.ds1.save(), this.ds2.save()]).then(() => {
            this.dp1 = this.createPoint({name: 'test point 1'});
            this.dp2 = this.createPoint({name: 'test point 2'});
            this.targetPoint = this.createPoint({name: 'Target alpha-numeric', pointLocator: {dataType: 'ALPHANUMERIC'}});
            this.detectorPoint = this.createPoint({name: 'Detector test point'});

            return Promise.all([this.dp1.save(), this.dp2.save(), this.targetPoint.save(), this.detectorPoint.save()]);
        });
    });

    after('Delete data sources', function() {
        return Promise.all([this.ds1.delete(), this.ds2.delete()]);
    });

    const verifyHandler = (responseHandler, expectedHandler) => {
        assert.isNumber(responseHandler.id);
        assert.strictEqual(responseHandler.xid, expectedHandler.xid);
        assert.strictEqual(responseHandler.name, expectedHandler.name);
        assert.strictEqual(responseHandler.disabled, expectedHandler.disabled);
        assert.strictEqual(responseHandler.handlerType, expectedHandler.handlerType);

        assert.strictEqual(responseHandler.eventTypes.length, expectedHandler.eventTypes.length);
        for (let i = 0; i < responseHandler.eventTypes.length; i++) {
            assert.strictEqual(responseHandler.eventTypes[i].eventType, expectedHandler.eventTypes[i].eventType);
            assert.strictEqual(responseHandler.eventTypes[i].subType, expectedHandler.eventTypes[i].subType);
            assert.strictEqual(responseHandler.eventTypes[i].referenceId1, expectedHandler.eventTypes[i].referenceId1);
            assert.strictEqual(responseHandler.eventTypes[i].referenceId2, expectedHandler.eventTypes[i].referenceId2);
        }

        if (expectedHandler.handlerType === 'SET_POINT') {
            assert.strictEqual(responseHandler.targetPointXid, expectedHandler.targetPointXid);
            assert.strictEqual(responseHandler.activePointXid, expectedHandler.activePointXid);
            assert.strictEqual(responseHandler.inactivePointXid, expectedHandler.inactivePointXid);
            assert.strictEqual(responseHandler.activeAction, expectedHandler.activeAction);
            assert.strictEqual(responseHandler.inactiveAction, expectedHandler.inactiveAction);
            if (expectedHandler.activeAction === 'STATIC_VALUE') {
                assert.strictEqual(responseHandler.activeValueToSet, expectedHandler.activeValueToSet);
            }
            if (expectedHandler.inactiveAction === 'STATIC_VALUE') {
                assert.strictEqual(responseHandler.inactiveValueToSet, expectedHandler.inactiveValueToSet);
            }

            assert.strictEqual(responseHandler.activeScript, expectedHandler.activeScript);
            assert.strictEqual(responseHandler.inactiveScript, expectedHandler.inactiveScript);
        } else if (expectedHandler.handlerType === 'PROCESS') {
            assert.strictEqual(responseHandler.activeProcessCommand, responseHandler.activeProcessCommand);
            assert.strictEqual(responseHandler.activeProcessTimeout, responseHandler.activeProcessTimeout);
            assert.strictEqual(responseHandler.inactiveProcessCommand, responseHandler.inactiveProcessCommand);
            assert.strictEqual(responseHandler.inactiveProcessTimeout, responseHandler.inactiveProcessTimeout);
        } else if (expectedHandler.handlerType === 'EMAIL') {
            assert.strictEqual(responseHandler.activeRecipients.length, expectedHandler.activeRecipients.length);
            assert.strictEqual(responseHandler.activeRecipients[0].username, expectedHandler.activeRecipients[0].username);
            assert.strictEqual(responseHandler.activeRecipients[1].address, expectedHandler.activeRecipients[1].address);

            assert.strictEqual(responseHandler.sendEscalation, expectedHandler.sendEscalation);
            assert.strictEqual(responseHandler.repeatEscalations, expectedHandler.repeatEscalations);
            assert.strictEqual(responseHandler.escalationDelayType, expectedHandler.escalationDelayType);
            assert.strictEqual(responseHandler.escalationDelay, expectedHandler.escalationDelay);

            assert.strictEqual(responseHandler.escalationRecipients.length, expectedHandler.escalationRecipients.length);
            assert.strictEqual(responseHandler.escalationRecipients[0].username, expectedHandler.escalationRecipients[0].username);
            assert.strictEqual(responseHandler.escalationRecipients[1].address, expectedHandler.escalationRecipients[1].address);

            assert.strictEqual(responseHandler.sendInactive, expectedHandler.sendInactive);
            assert.strictEqual(responseHandler.inactiveOverride, expectedHandler.inactiveOverride);

            assert.strictEqual(responseHandler.inactiveRecipients.length, expectedHandler.inactiveRecipients.length);
            assert.strictEqual(responseHandler.inactiveRecipients[0].username, expectedHandler.inactiveRecipients[0].username);
            assert.strictEqual(responseHandler.inactiveRecipients[1].address, expectedHandler.inactiveRecipients[1].address);

            assert.strictEqual(responseHandler.includeSystemInfo, expectedHandler.includeSystemInfo);
            assert.strictEqual(responseHandler.includeLogfile, expectedHandler.includeLogfile);
            assert.strictEqual(responseHandler.customTemplate, expectedHandler.customTemplate);

            assert.strictEqual(responseHandler.subject, 'INCLUDE_EVENT_MESSAGE');
        }

        if (expectedHandler.handlerType === 'SET_POINT' || expectedHandler.handlerType === 'EMAIL') {
            assert.strictEqual(responseHandler.scriptContext.length, expectedHandler.scriptContext.length);
            for (let i = 0; i < responseHandler.scriptContext.length; i++) {
                assert.strictEqual(responseHandler.scriptContext[i].xid, expectedHandler.scriptContext[i].xid);
                assert.strictEqual(responseHandler.scriptContext[i].variableName, expectedHandler.scriptContext[i].variableName);
            }
            for (let i = 0; i < responseHandler.scriptPermissions.length; i++) {
                assert.include(expectedHandler.scriptPermissions, responseHandler.scriptPermissions[i]);
            }
        }
    };

    const saveHandler = (eventHandler) => {
        return client.restRequest({
            path: '/rest/latest/event-handlers',
            method: 'POST',
            data: eventHandler
        });
    };

    const updateHandler = (eventHandler) => {
        return client.restRequest({
            path: `/rest/latest/event-handlers/${eventHandler.xid}`,
            method: 'PUT',
            data: eventHandler
        });
    };

    const deleteHandler = (eventHandler) => {
        return client.restRequest({
            path: `/rest/latest/event-handlers/${eventHandler.xid}`,
            method: 'DELETE'
        });
    };

    it('Create static set point event handler', function() {
        const eventHandler = this.createHandler();

        return saveHandler(eventHandler).then(response => {
            verifyHandler(response.data, eventHandler);
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Query event handlers lists', function() {
        const eventHandler = this.createHandler();

        return saveHandler(eventHandler).then(() => {
            return client.restRequest({
                path: '/rest/latest/event-handlers',
                params: { xid: eventHandler.xid },
                method: 'GET'
            });
        }).then(response => {
            assert.isArray(response.data.items);
            assert.strictEqual(response.data.items.length, 1);
            assert.strictEqual(response.data.total, 1);
            verifyHandler(response.data.items[0], eventHandler);
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Delete static set point event handler', function() {
        const eventHandler = this.createHandler();

        return saveHandler(eventHandler).then(() => {
            return deleteHandler(eventHandler);
        }).then(response => {
            verifyHandler(response.data, eventHandler);
        });
    });

    it('Create point set point event handler', function() {
        const eventHandler = this.createHandler({
            targetPointXid: this.dp1.xid,
            activePointXid: this.dp2.xid,
            inactivePointXid: this.dp1.xid,
            activeAction: "POINT_VALUE",
            inactiveAction: "POINT_VALUE",
        });

        return saveHandler(eventHandler).then(response => {
            verifyHandler(response.data, eventHandler);
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Test invalid set point event handler', function() {
        const eventHandler = this.createHandler({
            targetPointXid: 'missingTarget',
            activePointXid: 'missingActive',
            inactivePointXid: 'missingInactive',
            activeAction: "POINT_VALUE",
            inactiveAction: "POINT_VALUE",
            scriptContext: [{xid: 'missing', variableName: 'point2'}]
        });
        return saveHandler(eventHandler).then(response => {
            throw new Error('Should not have created set point event handler');
        }, error => {
            assert.strictEqual(error.status, 422);
            assert.strictEqual(error.data.result.messages.length, 4);

            assert.strictEqual(error.data.result.messages[0].property, 'targetPointId');
            assert.strictEqual(error.data.result.messages[1].property, 'activePointId');
            assert.strictEqual(error.data.result.messages[2].property, 'inactivePointId');
            assert.strictEqual(error.data.result.messages[3].property, 'scriptContext[0].id');
        });
    });

    it('Gets websocket notifications for update', function() {
        this.timeout(5000);

        const eventHandler = this.createHandler({
            targetPointXid: this.dp1.xid,
            activePointXid: this.dp2.xid,
            inactivePointXid: this.dp1.xid,
            activeAction: "POINT_VALUE",
            inactiveAction: "POINT_VALUE",
        });
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return saveHandler(eventHandler).then(() => {
            this.ws = client.openWebSocket({
                path: '/rest/latest/websocket/event-handlers'
            });

            this.ws.on('open', () => {
                socketOpenDeferred.resolve();
            });

            this.ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            this.ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            this.ws.on('message', msgStr => {
                try {
                    assert.isString(msgStr);
                    const msg = JSON.parse(msgStr);
                    assert.strictEqual(msg.status, 'OK');
                    assert.strictEqual(msg.payload.action, 'update');
                    assert.strictEqual(msg.payload.object.xid, eventHandler.xid);
                    assert.strictEqual(msg.payload.object.eventTypes.length, 1);
                    assert.strictEqual(msg.payload.object.eventTypes[0].eventType, eventHandler.eventTypes[0].eventType);
                    assert.strictEqual(msg.payload.object.eventTypes[0].subType, eventHandler.eventTypes[0].subType);
                    listUpdatedDeferred.resolve();
                } catch (e) {
                    listUpdatedDeferred.reject(e);
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => {
            const send = defer();
            this.ws.send(JSON.stringify({eventTypes: ['add', 'delete', 'update']}), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => delay(1000)).then(() => {
            //TODO Fix DaoNotificationWebSocketHandler so we can remove this delay, only required for cold start

            eventHandler.name = 'New event handler name';
            eventHandler.eventTypes = [
                {
                    eventType: 'SYSTEM',
                    subType: 'USER_LOGIN',
                    referenceId1: 0,
                    referenceId2: 0
                }
            ];
            return updateHandler(eventHandler).then(response => {
                verifyHandler(response.data, eventHandler);
            });
        }).then(() => listUpdatedDeferred.promise).finally(() => {
            this.ws.close();
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Gets websocket notifications for delete', function() {
        this.timeout(5000);

        const eventHandler = this.createHandler({
            targetPointXid: this.dp1.xid,
            activePointXid: this.dp2.xid,
            inactivePointXid: this.dp1.xid,
            activeAction: "POINT_VALUE",
            inactiveAction: "POINT_VALUE",
        });
        const socketOpenDeferred = defer();
        const listUpdatedDeferred = defer();

        return saveHandler(eventHandler).then(() => {
            this.ws = client.openWebSocket({
                path: '/rest/latest/websocket/event-handlers'
            });

            this.ws.on('open', () => {
                socketOpenDeferred.resolve();
            });

            this.ws.on('error', error => {
                const msg = new Error(`WebSocket error, error: ${error}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            this.ws.on('close', (code, reason) => {
                const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
                socketOpenDeferred.reject(msg);
                listUpdatedDeferred.reject(msg);
            });

            this.ws.on('message', msgStr => {
                try {
                    assert.isString(msgStr);
                    const msg = JSON.parse(msgStr);
                    assert.strictEqual(msg.status, 'OK');
                    assert.strictEqual(msg.payload.action, 'delete');
                    assert.strictEqual(msg.payload.xid, eventHandler.xid);
                    listUpdatedDeferred.resolve();
                } catch (e) {
                    listUpdatedDeferred.reject(e);
                }
            });

            return socketOpenDeferred.promise;
        }).then(() => {
            const send = defer();
            this.ws.send(JSON.stringify({eventTypes: ['add', 'delete', 'update']}), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => delay(1000)).then(() => {
            return deleteHandler(eventHandler).then(response => {
                verifyHandler(response.data, eventHandler);
            });
        }).then(() => listUpdatedDeferred.promise).finally(() => {
            this.ws.close();
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    //Process Event Handler Tests
    it('Create process event handler', function() {
        const eventHandler = this.createHandler({
            disabled: true,
            activeProcessCommand: 'ls',
            activeProcessTimeout: 1000,
            inactiveProcessCommand: 'cd /',
            inactiveProcessTimeout: 1000,
            handlerType: 'PROCESS'
        });
        return saveHandler(eventHandler).then(response => {
            verifyHandler(response.data, eventHandler);
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Patch process event handler', function() {
        const eventHandler = this.createHandler({
            disabled: true,
            activeProcessCommand: 'ls',
            activeProcessTimeout: 1000,
            inactiveProcessCommand: 'cd /',
            inactiveProcessTimeout: 1000,
            handlerType: 'PROCESS'
        });
        return saveHandler(eventHandler).then(() => {
            return client.restRequest({
                path: '/rest/latest/event-handlers/' + eventHandler.xid,
                method: 'PATCH',
                data: {
                    disabled: false
                }
            });
        }).then(response => {
            eventHandler.disabled = false;
            verifyHandler(response.data, eventHandler);
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    //Email Event Handler Tests
    it('Create email event handler', function() {
        const eventHandler = this.createHandler({
            activeRecipients: [
                {username: 'admin', recipientType: 'USER'},
                {address: 'test@test.com', recipientType: 'ADDRESS'}
            ],
            sendEscalation: true,
            repeatEscalations: true,
            escalationDelayType: 'HOURS',
            escalationDelay: 1,
            escalationRecipients: [
                {username: 'admin', recipientType: 'USER'},
                {address: 'test@testingEscalation.com', recipientType: 'ADDRESS'}
            ],
            sendInactive: true,
            inactiveOverride: true,
            inactiveRecipients: [
                {username: 'admin', recipientType: 'USER'},
                {address: 'test@testingInactive.com', recipientType: 'ADDRESS'}
            ],
            includeSystemInfo: true,
            includeLogfile: true,
            customTemplate: '<h2></h2>',
            scriptContext: [
                {xid: this.dp1.xid, variableName: 'point1'},
                {xid: this.dp2.xid, variableName: 'point2'}
            ],
            scriptPermissions: ['superadmin', 'user'],
            script: 'return 0;',
            subject: 'INCLUDE_EVENT_MESSAGE',
            handlerType: "EMAIL"
        });

        return saveHandler(eventHandler).then(response => {
            verifyHandler(response.data, eventHandler);
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Test invalid email event handler', function() {
        const eventHandler = this.createHandler({
            activeRecipients: [
                {username: 'noone', recipientType: 'USER'},
                {address: 'test@test.com', recipientType: 'ADDRESS'}
            ],
            sendEscalation: true,
            repeatEscalations: true,
            escalationDelayType: 'HOURS',
            escalationDelay: 1,
            escalationRecipients: [
                {username: 'noone', recipientType: 'USER'},
                {address: 'test@testingEscalation.com', recipientType: 'ADDRESS'}
            ],
            sendInactive: true,
            inactiveOverride: true,
            inactiveRecipients: [
                {username: 'admin', recipientType: 'USER'},
                {address: 'test@testingInactive.com', recipientType: 'ADDRESS'}
            ],
            includeSystemInfo: true,
            includeLogfile: true,
            customTemplate: '${empty',
            scriptContext: [
                {xid: 'missing', variableName: 'point1'},
                {xid: this.dp2.xid, variableName: 'point2'}
            ],
            scriptPermissions: ['superadmin', 'user'],
            script: 'return 0;',
            handlerType: 'EMAIL'
        });
        return saveHandler(eventHandler).then(response => {
            throw new Error('Should not have created email event handler');
        }, error => {
            assert.strictEqual(error.status, 422);
            assert.strictEqual(error.data.result.messages.length, 5);

            //Missing user
            assert.strictEqual(error.data.result.messages[0].property, 'activeRecipients[0]');
            //Missing user
            assert.strictEqual(error.data.result.messages[1].property, 'escalationRecipients[0]');
            //Invalid FTL
            assert.strictEqual(error.data.result.messages[2].property, 'customTemplate');
            //Missing point
            assert.strictEqual(error.data.result.messages[3].property, 'scriptContext[0].id');
            //Invalid subject
            assert.strictEqual(error.data.result.messages[4].property, 'subject');
        });
    });

    before('Add verifyHandlerTriggered method', function() {
        this.verifyHandlerTriggered = (eventHandler, eventType) => {
            return client.restRequest({
                path: '/rest/latest/testing/raise-event',
                method: 'POST',
                data: {
                    event: eventType,
                    context: {},
                    level: 'INFORMATION',
                    message: 'Test event'
                }
            }).then(() => {
                return client.restRequest({
                    path: `/rest/latest/point-values/latest/${this.targetPoint.xid}`,
                    params: {
                        fields: 'VALUE,TIMESTAMP',
                        limit: 1,
                        useCache: 'CACHE_ONLY'
                    },
                    method: 'GET'
                }).then(response => {
                    assert.strictEqual(response.data[0].value, eventHandler.activeValueToSet);
                    return response.data[0];
                });
            });
        }
    });

    it('Verify handler triggered, specific referenceId1', function() {
        const referenceId1 = Math.round(Math.random() * 10000);

        const eventHandler = this.createHandler({
            targetPointXid: this.targetPoint.xid,
            activeValueToSet: uuid(),
            inactiveAction: 'NONE',
            eventTypes: [
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: referenceId1,
                    referenceId2: 0
                }
            ]
        });

        return saveHandler(eventHandler).then(() => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1,
                referenceId2: 0
            });
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Verify handler triggered, wildcard referenceId1', function() {
        const referenceId1 = Math.round(Math.random() * 10000);

        const eventHandler = this.createHandler({
            targetPointXid: this.targetPoint.xid,
            activeValueToSet: uuid(),
            inactiveAction: 'NONE',
            eventTypes: [
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: 0,
                    referenceId2: 0
                }
            ]
        });

        return saveHandler(eventHandler).then(() => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1,
                referenceId2: 0
            });
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Verify handler triggered, wildcard and specific referenceId1', function() {
        const referenceId1 = Math.round(Math.random() * 10000);

        const eventHandler = this.createHandler({
            targetPointXid: this.targetPoint.xid,
            activeValueToSet: uuid(),
            inactiveAction: 'NONE',
            eventTypes: [
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: referenceId1,
                    referenceId2: 0
                },
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: 0,
                    referenceId2: 0
                }
            ]
        });

        return saveHandler(eventHandler).then(() => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1,
                referenceId2: 0
            });
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Verify handler triggered, trigger twice with specific referenceId1s', function() {
        const referenceId1_1 = Math.round(Math.random() * 10000);
        const referenceId1_2 = Math.round(Math.random() * 10000);

        const eventHandler = this.createHandler({
            targetPointXid: this.targetPoint.xid,
            activeValueToSet: uuid(),
            inactiveAction: 'NONE',
            eventTypes: [
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: referenceId1_1,
                    referenceId2: 0
                },
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: referenceId1_2,
                    referenceId2: 0
                }
            ]
        });

        return saveHandler(eventHandler).then(() => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1_1,
                referenceId2: 0
            })
        }).then(value1 => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1_2,
                referenceId2: 0
            }).then(value2 => {
                assert.isAbove(value2.timestamp, value1.timestamp);
            });
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Verify handler triggered, trigger twice with wildcard referenceId1', function() {
        const referenceId1_1 = Math.round(Math.random() * 10000);
        const referenceId1_2 = Math.round(Math.random() * 10000);

        const eventHandler = this.createHandler({
            targetPointXid: this.targetPoint.xid,
            activeValueToSet: uuid(),
            inactiveAction: 'NONE',
            eventTypes: [
                {
                    eventType: 'SYSTEM',
                    subType: 'XXX_TESTING',
                    referenceId1: 0,
                    referenceId2: 0
                }
            ]
        });

        return saveHandler(eventHandler).then(() => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1_1,
                referenceId2: 0
            })
        }).then(value1 => {
            return this.verifyHandlerTriggered(eventHandler, {
                eventType: 'SYSTEM',
                subType: 'XXX_TESTING',
                referenceId1: referenceId1_2,
                referenceId2: 0
            }).then(value2 => {
                assert.isAbove(value2.timestamp, value1.timestamp);
            });
        }).finally(() => {
            return deleteHandler(eventHandler).catch(noop);
        });
    });

    it('Handler XID is added to detector\'s handlerXids', function() {
        const eventHandler = this.createHandler({
            eventTypes: [
                {
                    eventType: 'DATA_POINT',
                    subType: '',
                    referenceId1: this.detectorPoint.id,
                    referenceId2: 0
                }
            ]
        });

        const detector = EventDetector.createEventDetector(this.detectorPoint.id, 'BINARY_STATE');
        return detector.save().then(savedDetector => {
            assert.strictEqual(savedDetector, detector);
            assert.isArray(detector.handlerXids);
            assert.notInclude(detector.handlerXids, eventHandler.xid);
        }).then(() => {
            return saveHandler(eventHandler);
        }).then(() => {
            return detector.get();
        }).then(() => {
            assert.isArray(detector.handlerXids);
            assert.include(detector.handlerXids, eventHandler.xid);
        }).finally(() => {
            return Promise.all([
                detector.delete().catch(noop),
                deleteHandler(eventHandler).catch(noop)
            ]);
        });
    });

    it('Handler XID only appears once in detector\'s handlerXids', function() {
        const eventHandler = this.createHandler({
            eventTypes: [
                {
                    eventType: 'DATA_POINT',
                    subType: '',
                    referenceId1: this.detectorPoint.id,
                    referenceId2: 0
                },
                {
                    eventType: 'DATA_POINT',
                    subType: '',
                    referenceId1: 0,
                    referenceId2: 0
                }
            ]
        });

        const detector = EventDetector.createEventDetector(this.detectorPoint.id, 'BINARY_STATE');
        return detector.save().then(savedDetector => {
            assert.strictEqual(savedDetector, detector);
            assert.isArray(detector.handlerXids);
            assert.notInclude(detector.handlerXids, eventHandler.xid);
        }).then(() => {
            return saveHandler(eventHandler);
        }).then(() => {
            return detector.get();
        }).then(() => {
            assert.isArray(detector.handlerXids);
            const matching = detector.handlerXids.filter(xid => xid === eventHandler.xid);
            assert.strictEqual(matching.length, 1);
        }).finally(() => {
            return Promise.all([
                detector.delete().catch(noop),
                deleteHandler(eventHandler).catch(noop)
            ]);
        });
    });
});
