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

const {createClient, login, uuid} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataPoint = client.DataPoint;
const DataSource = client.DataSource;

describe('Data source service', function() {
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
            simplifyTolerance: simplifyValue
        });
    };
    
    beforeEach('Creates a new virtual data source', function() {
        this.ds = new DataSource({
            xid: uuid(),
            name: uuid(),
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return this.ds.save().then((savedDs) => {
            assert.strictEqual(savedDs, this.ds);
            assert.equal(savedDs.xid, this.ds.xid);
            assert.equal(savedDs.name, this.ds.name);
            assert.isNumber(savedDs.id);
            this.ds.id = savedDs.id;
        });
    });
    
    afterEach('Deletes the virtual data source and its points', function() {
        return this.ds.delete();
    });
    
    it('Gets the data source', function() {
        return DataSource.get(this.ds.xid).then(ds => {
            assert.equal(ds.xid, this.ds.xid);
            assert.equal(ds.name, this.ds.name);
            assert.equal(ds.enabled, this.ds.enabled);
        });
    });

    it('Modifies a virtual data source', function() {
        return DataSource.get(this.ds.xid).then(ds => {
            assert.equal(ds.xid, this.ds.xid);
            assert.equal(ds.name, this.ds.name);
            ds.name = 'xyz';
            return ds.save().then((savedDs) => {
                assert.equal(savedDs.name, 'xyz');
            });
        });
    });



    it('Lists all data sources', function() {
        return DataSource.list().then((dsList) => {
            assert.isArray(dsList);
            assert.isNumber(dsList.total);
            assert.equal(dsList.length, dsList.total);
        });
    });

    it('Queries for the data source', function() {
        return DataSource.query(`xid=${this.ds.xid}`).then((dsList) => {
            assert.isArray(dsList);
            assert.isNumber(dsList.total);
            assert.equal(dsList.length, dsList.total);
            assert.equal(dsList.length, 1);
            assert.equal(dsList[0].name, this.ds.name);
        });
    });
    
    it('Copies a data source and points', function() {

        this.testPoint1 = newDataPoint(uuid(), this.ds.xid, 'FIRST', 'NONE', 0);
        this.testPoint2 = newDataPoint(uuid(), this.ds.xid, 'FIRST', 'NONE', 0);
        this.testPoint3 = newDataPoint(uuid(), this.ds.xid, 'COUNT', 'TOLERANCE', 10.0);
        this.testPoint4 = newDataPoint(uuid(), this.ds.xid, 'COUNT', 'NONE', 0);
        return Promise.all([this.testPoint1.save(),
            this.testPoint2.save(),
            this.testPoint3.save(),
            this.testPoint4.save()]).then(() => {
                return client.restRequest({
                    path: `/rest/v2/data-sources/copy/${this.ds.xid}`,
                    params: {
                        copyName: this.ds.name + '-copy',
                        copyXid: this.ds.xid + '-copy',
                        copyDeviceName: 'Mango client copy device name',
                        copyPoints: true,
                        enabled: false
                    },
                    method: 'PUT'
                }).then(response => {
                   assert.strictEqual(response.data.xid, this.ds.xid + '-copy');
                   assert.strictEqual(response.data.name, this.ds.name + '-copy');
                   assert.strictEqual(response.data.enabled, false);
                   return  client.restRequest({
                       path: `/rest/v2/data-points?dataSourceXid=${this.ds.xid}-copy`,
                       method: 'GET'
                   }).then(response => {
                       assert.strictEqual(response.data.total, 4);
                       response.data.items.forEach((dp, i) => {
                           assert.strictEqual(dp.deviceName, 'Mango client copy device name');
                       });
                   });
                });
            });
    });
});
