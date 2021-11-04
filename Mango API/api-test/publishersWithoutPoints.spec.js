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

describe('Publishers without point service', function() {
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
            return client.restRequest({
                path: '/rest/latest/publishers-without-points',
                method: 'POST',
                data: this.pub
            }).then(response => {
                assert.isNotNull(response.data);
            });

            //return this.pub.save();
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
        return client.restRequest({
            path: `/rest/latest/publishers-without-points/${this.pub.xid}`,
            method: 'DELETE'
        }).then((response) => {
          return client.restRequest({
            path: `/rest/latest/publishers-without-points/${this.pub.xid}`,
            method: 'GET'
          });
        }).then((response) => {
            throw new Error('Should not have found publisher');
        }).catch((response) => {
            if(typeof response.response === 'undefined')
                throw response;
            assert.equal(response.response.statusCode, 404);
        }).then(() => {
            return Promise.all([this.ds.delete()]);
        });
    });


    it('Query the publishers without points', function() {
        let testPublisherXid = this.pub.xid;
        return client.restRequest({
            path: `/rest/latest/publishers-without-points`,
            method: 'GET'
        }).then(response => {
            assert.isArray(response.data.items);
            let array1 = response.data.items;
            let found = false;
            //look for the publisher in the array it should be there.
            array1.forEach(element => {if(element.xid === testPublisherXid){
                found = true;
                assert.strictEqual(element.xid, testPublisherXid);
            }});
            assert.isTrue(found);
        });
    });

    it('Get the publisher without points by xid', function() {
        let testPublisherXid = this.pub.xid;
        return client.restRequest({
            path: `/rest/latest/publishers-without-points/${testPublisherXid}`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.data.xid, testPublisherXid);
        });
    });

    it('Modify the publisher without points by xid', function() {
        let testPublisherXid = this.pub.xid;
        let newName = 'New publisher name';
        let newXid = uuid();
        let publishAttributeChanges = true;
        //TODO modify every property of PublisherVO
        return client.restRequest({
            path: `/rest/latest/publishers-without-points/${testPublisherXid}`,
            method: 'GET'
        }).then((response) => {
            let testPublisher = response.data;
            testPublisher.name = newName;
            testPublisher.xid = newXid;
            testPublisher.publishAttributeChanges = publishAttributeChanges;
            return client.restRequest({
                path: `/rest/latest/publishers-without-points/${testPublisherXid}`,
                method: 'PUT',
                data: testPublisher
            });
        }).then((response) => {
            return client.restRequest({
                path: `/rest/latest/publishers-without-points/${newXid}`,
                method: 'GET'
            }).then((response) => {
                assert.strictEqual(response.data.enabled, this.pub.enabled);
                assert.strictEqual(response.data.name, newName);
                assert.strictEqual(response.data.xid, newXid);
                assert.strictEqual(response.data.publishAttributeChanges, publishAttributeChanges);
                //We know the XID changed successfully so set the publisher xid so we can delete it after
                this.pub.xid = response.data.xid;
            });
        });
    });

    it('Disables the publisher without points by xid', function() {
        let testPublisherXid = this.pub.xid;
        return client.restRequest({
            path: `/rest/latest/publishers-without-points/${testPublisherXid}`,
            method: 'GET'
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/publishers-without-points/enable-disable/${response.data.xid}?enabled=false&restart=true`,
                method: 'PUT'
            });
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/publishers-without-points/${testPublisherXid}`,
                method: 'GET'
            }).then(response => {
                assert.isFalse(response.data.enabled);
            });
        });
    });
});


