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
 */

const config = require('@infinite-automation/mango-client/test/setup');
const uuidV4 = require('uuid/v4');
const path = require('path');
const fs = require('fs');

describe('Event detectors CSV', () => {
    before('Login', config.login);

    const newDataPoint = (xid, dsXid) => {
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
            rollup: 'AVERAGE'
        });
    };
    
    const highLimitDetector = function() {
        return {
            name : "When true.",
            alarmLevel : 'URGENT',
            duration : {
                periods: 10,
                type: 'SECONDS'
            },
            limit: 10.0,
            resetLimit: 9.0,
            useResetLimit: true,
            notHigher: false,
            detectorType : "HIGH_LIMIT",
        };
    };
    
    const testPointXid1 = uuidV4();
    const testPointXid2 = uuidV4();
    
    before('Create a virtual data source and points', function() {

        this.ds = new DataSource({
            xid: uuidV4(),
            name: 'Mango client test',
            enabled: true,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'HOURS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });
        
        return this.ds.save().then((savedDs) => {
            assert.strictEqual(savedDs.name, 'Mango client test');
            assert.isNumber(savedDs.id);
        }).then(() => {
            this.testPoint1 = newDataPoint(testPointXid1, this.ds.xid);
            this.testPoint2 = newDataPoint(testPointXid2, this.ds.xid);
            return Promise.all([this.testPoint1.save(), this.testPoint2.save()]);
        }).then(() => {
            this.ed1 = highLimitDetector();
            this.ed1.sourceId = this.testPoint1.id;
            return client.restRequest({
                path: '/rest/v2/full-event-detectors',
                method: 'POST',
                data: this.ed1
            }).then(response => {
                this.ed1.xid = response.data.xid;
                this.ed1.id = response.data.id;
            });
        });
    });

    after('Deletes the new virtual data source and its points', function() {
        return this.ds.delete();
    });
    
//    after('Deletes the event detector', function() {
//        return client.restRequest({
//            path: `/rest/v2/full-event-detectors/${this.ed1.xid}`,
//            method: 'DELETE'
//        });
//    });
    
    it('Can download csv file for event detectors', function() {
        this.timeout(5000);
         
        return client.restRequest({
            path: `/rest/v2/full-event-detectors?limit(100)`,
            method: 'GET',
            headers: {
                'Accept': 'text/csv'
            },
            writeToFile: 'eventDetectors.csv'
        }).then(() => config.delay(1000)).then(response => {
            const uploadFileName = path.resolve('eventDetectors.csv');
            console.log(uploadFileName);
           
        });
    });
});
