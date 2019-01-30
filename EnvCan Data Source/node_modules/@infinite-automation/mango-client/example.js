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

// change to require('@infinite-automation/mango-client') when installed via NPM
const MangoClient = require('./src/mangoClient');

const client = new MangoClient({
    protocol: 'https',
    host: 'localhost',
    port: 8443,
    rejectUnauthorized: false
});

const DataPoint = client.DataPoint;
const User = client.User;

User.login('admin', 'admin').then(data => {
    console.log(`Logged in as '${data.username}'.`);
    return DataPoint.getValue('internal_mango_num_data_points');
}).then(data => {
    console.log(`There are ${data.value} data points.`);

    // you can perform any arbitrary rest request like this
    return client.restRequest({
        path: '/rest/v1/data-points/internal_mango_num_data_points',
        method: 'GET',
        //data: {object}
    });
}).then(response => {
    console.log(`The data point's name is '${response.data.name}'`);
});
