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

const {createClient, login} = require('@infinite-automation/mango-client/test/testHelper');
const client = createClient();

describe('Background processing settings', function() {
    before('Login', function() { return login.call(this, client); });

    it('Sets high priority pool settings', () => {
        return client.restRequest({
            path: '/rest/v1/background-processing/high-priority-thread-pool-settings',
            method: 'PUT',
            data: {
                corePoolSize: 3,
                maximumPoolSize: 31
            }
        }).then(response => {
          //console.log(response.data);
        });
    });

    it('Gets high priority pool settings', () => {
        return client.restRequest({
            path: '/rest/v1/background-processing/high-priority-thread-pool-settings',
            method: 'GET'
        }).then(response => {
            assert.equal(response.data.corePoolSize, 3);
            assert.equal(response.data.maximumPoolSize, 31);
        });
    });

    it('Sets medium priority pool settings', () => {
        return client.restRequest({
            path: '/rest/v1/background-processing/medium-priority-thread-pool-settings',
            method: 'PUT',
            data: {
                corePoolSize: 4
            }
        }).then(response => {
          //console.log(response.data);
        });
    });

    it('Gets medium priority pool settings', () => {
        return client.restRequest({
            path: '/rest/v1/background-processing/medium-priority-thread-pool-settings',
            method: 'GET'
        }).then(response => {
          assert.equal(response.data.corePoolSize, 4);
        });
    });

    it('Sets low priority pool settings', () => {
        return client.restRequest({
            path: '/rest/v1/background-processing/low-priority-thread-pool-settings',
            method: 'PUT',
            data: {
                corePoolSize: 2
            }
        }).then(response => {
          //console.log(response.data);
        });
    });

    it('Gets low priority pool settings', () => {
        return client.restRequest({
            path: '/rest/v1/background-processing/low-priority-thread-pool-settings',
            method: 'GET'
        }).then(response => {
          assert.equal(response.data.corePoolSize, 2);
        });
    });


});
