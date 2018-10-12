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

const config = require('@infinite-automation/mango-client/test/setup');

describe('Data source service', () => {
    before('Login', config.login);

    it('Gets the internal data source', () => {
        return DataSource.get('internal_mango_monitoring_ds').then(ds => {
            assert.equal(ds.xid, 'internal_mango_monitoring_ds');
            assert.equal(ds.name, 'Mango Internal');
            assert.isTrue(ds.enabled);
        });
    });

    it('Creates a new virtual data source', () => {
        const ds = new DataSource({
            xid: 'mango_client_test',
            name: 'Mango client test',
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

    it('Modifies the new virtual data source', () => {
        return DataSource.get('mango_client_test').then(ds => {
            assert.equal(ds.xid, 'mango_client_test');
            assert.equal(ds.name, 'Mango client test');
            ds.name = 'xyz';
            return ds.save().then((savedDs) => {
                assert.equal(savedDs.name, 'xyz');
            });
        });
    });

    it('Deletes the new virtual data source', () => {
        return DataSource.delete('mango_client_test');
    });

    it('Lists all data sources', () => {
        return DataSource.list().then((dsList) => {
            assert.isArray(dsList);
            assert.isNumber(dsList.total);
            assert.equal(dsList.length, dsList.total);
        });
    });

    it('Queries for the internal data source', () => {
        return DataSource.query('xid=internal_mango_monitoring_ds').then((dsList) => {
            assert.isArray(dsList);
            assert.isNumber(dsList.total);
            assert.equal(dsList.length, dsList.total);
            assert.equal(dsList.length, 1);
            assert.equal(dsList[0].name, 'Mango Internal');
        });
    });

});
