/**
 * Copyright 2019 Infinite Automation Systems Inc.
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
 * 
 * This test suit is designed to help develop a streaming api for point values but will not run every night 
 *  as it is for performance testing.
 * 
 */

const config = require('@infinite-automation/mango-client/test/setup');

describe.skip('Point value streaming tests', function() {

    before('Login', config.login);

    before('Create data source and point', function() {
        global.ds = new DataSource({
            name: 'Mango client test',
            enabled: false,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });

        return global.ds.save().then((savedDs) => {
            assert.strictEqual(savedDs, global.ds);
            assert.equal(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
            global.ds.xid = savedDs.xid;
            global.ds.id = savedDs.id;

            global.dp = new DataPoint({
                  name : 'Virtual Test Point 1',
                  enabled : false,
                  dataSourceXid : global.ds.xid,
                  modelType : 'DATA_POINT',
                  pointLocator : {
                    startValue : 0,
                    modelType : 'PL.VIRTUAL',
                    dataType : 'NUMERIC',
                    settable : true,
                    changeType : 'NO_CHANGE'
                  }
            });

            return global.dp.save().then((savedDp) => {
                assert.equal(savedDp.name, 'Virtual Test Point 1');
                assert.equal(savedDp.enabled, false);
                assert.isNumber(savedDp.id);
                global.dp.id = savedDp.id;
                global.dp.xid = savedDp.xid;
            }, (error) => {
                if(error.status === 422){
                    var msg = 'Validation Failed: \n';
                    for(var m in error.data.result.messages)
                        msg += error.data.result.messages[m].property + '-->' + error.data.result.messages[m].message;
                    assert.fail(msg);
                }else{
                    assert.fail(error);
                }
            });
        });
    });
    //TODO ALL Statistics make a large memory difference
    
    it('Can make a MILLISECOND rollup request for a large file', function() {
        this.timeout(50000000);
         
        const fileSizeMB = 500;
        const numSamples = 20 * 1024 * fileSizeMB;
        const pollPeriod = 1; //in ms
        const endTime = new Date().getTime();
        const startTime = endTime - (numSamples * pollPeriod);
        const isoFrom = new Date(startTime).toISOString();
        
        return client.restRequest({
            path: `/rest/v2/point-values/time-period/${global.dp.xid}/FIRST?from=${isoFrom}&timePeriodType=MILLISECONDS&timePeriods=1`,
            method: 'GET',
            writeToFile: 'pointValues.json'
        }).then(response => {
            console.log(response);
        });
        
    });
    
    after('Deletes the new virtual data source and its points', () => {
        return DataSource.delete(global.ds.xid);
    });
});