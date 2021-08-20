/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

const {createClient, uuid, noop, config} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');

/*
NOTE: This file is not named .spec.js on purpose, we dont want to run this on the nightly build.
Results for 500k data points (3.5m tags), on MySQL 5.7
Tags were generated using https://github.com/MangoAutomation/script-examples/blob/main/create-tag-config.js

New tag query implementation, left join on dataPointTags per tag -
  Tag query performance
    With admin
      √ Query on 1 tags (6900ms)
      √ Query on 2 tags (1387ms)
      √ Query on 3 tags (2057ms)
      √ Query on 5 tags (1916ms)
      √ Query on 10 tags (1996ms)
    With non-admin
      √ Query on 1 tags (12531ms)
      √ Query on 2 tags (2323ms)
      √ Query on 3 tags (1913ms)
      √ Query on 5 tags (1933ms)
      √ Query on 10 tags (1971ms)

Previous tag query implementation, case when/group by data point id -
  Tag query performance
    With admin
      √ Query on 1 tags (8797ms)
      √ Query on 2 tags (6156ms)
      √ Query on 3 tags (6937ms)
      √ Query on 5 tags (8579ms)
      √ Query on 10 tags (13470ms)
    With non-admin
      √ Query on 1 tags (11780ms)
      √ Query on 2 tags (6431ms)
      √ Query on 3 tags (7010ms)
      √ Query on 5 tags (8713ms)
      √ Query on 10 tags (13098ms)
*/

describe('Tag query performance', function () {

    before('Create admin client', function () {
        this.clients = {};
        this.clients.admin = createClient();
        return this.clients.admin.User.login(config.username, config.password);
    });

    before('Create non-admin user', function () {
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new this.clients.admin.User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            roles: [],
            password: this.testUserPassword
        });
        return this.testUser.save();
    });

    before('Create non-admin client', function () {
        this.clients['non-admin'] = createClient();
        return this.clients['non-admin'].User.login(this.testUser.username, this.testUserPassword);
    });

    after('Delete the test user', function () {
        return this.testUser.delete().catch(noop);
    });

    const tagKeys = Array(10).fill(null).map((v, i) => `key_${i}`);
    const limit = 10;
    const iterationsPerTest = 1;
    const clients = ['admin', 'non-admin'];
    const numberOfTags = [1, 2, 3, 5, 10];
    const randomKeysAndValues = false;
    const queryNullTag = false;
    const queryMissingValue = false;

    clients.forEach(clientName => {
        describe(`With ${clientName}`, function () {
            numberOfTags.forEach(numTags => {
                it(`Query on ${numTags} tags`, function () {
                    this.timeout(120 * 1000);
                    const client = this.clients[clientName];
                    return Array(iterationsPerTest).fill(null).reduce((promise) => {
                        let keys = tagKeys;
                        if (randomKeysAndValues) {
                            keys = tagKeys.slice().sort(() => Math.random() - 0.5);
                        }
                        const params = keys.slice(0, numTags).reduce((p, key, i, array) => {
                            if (queryNullTag && i === (array.length - 1)) {
                                p[`tags.${key}`] = 'null';
                            } else if (queryMissingValue && i === (array.length - 1)) {
                                p[`tags.${key}`] = 'xyz';
                            } else {
                                const value = randomKeysAndValues ? Math.round(Math.random() * 10) : 0;
                                p[`tags.${key}`] = `${key}_value_${value}`;
                            }
                            return p;
                        }, {});
                        const query = new URLSearchParams(params);
                        // console.log(query.toString());
                        return promise.then(() => client.DataPoint.query(`${query.toString()}&limit(${limit})`));
                    }, Promise.resolve());
                });
            });
        });
    });
});
