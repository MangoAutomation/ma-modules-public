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

const path = require('path');

const config = {
    username: 'admin',
    password: 'admin',
    loginRetries: 0,
    loginRetryDelay: 5000
};

try {
    const configFileOptions = require(path.resolve(process.env.CONFIG_FILE || 'config.json'));
    Object.assign(config, configFileOptions);
} catch (e) {
}

global.chai = require('chai');
global.assert = chai.assert;
global.expect = chai.expect;

const MangoClient = require('../src/mangoClient');
global.client = new MangoClient(config);
global.DataSource = client.DataSource;
global.DataPoint = client.DataPoint;
global.User = client.User;
global.pointValues = client.pointValues;

config.login = function() {
    this.timeout(config.loginRetries * config.loginRetryDelay + 5000);
    return User.login(config.username, config.password, config.loginRetries, config.loginRetryDelay);
};

config.delay = function (time) {
    return new Promise((resolve) => {
        setTimeout(resolve, time);
    });
};

config.defer = function() {
    const deferred = {};
    deferred.promise = new Promise((resolve, reject) => {
        deferred.resolve = resolve;
        deferred.reject = reject;
    });
    return deferred;
};

config.noop = function noop() {};

module.exports = config;
