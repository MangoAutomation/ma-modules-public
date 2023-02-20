/*
 *  Copyright (C) 2023 RadixIot LLC. All rights reserved.
 */

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('JSON configuration template controller', function() {

    // create a context object to replace global which was previously used throughout this suite
    const testContext = {};

    before('Login', function() { return login.call(this, client); });

    // List device names -
    it('GET /rest/latest/template/import', function() {
        return client.restRequest({
            method: 'GET',
            path: '/rest/latest/template/import?filePath=script-examples/mustache/data.csv&templateName=datapoints.mustache'
        }).then(response => {
            // OK
            assert.strictEqual(response.status, 200);
            assert.isAbove(response.data.length, 0, 'data');
        });
    });
});
