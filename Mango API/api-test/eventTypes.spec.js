/**
 * Copyright 2018 Infinite Automation Systems Inc.
 * http://infiniteautomation.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const DataSource = client.DataSource;
const DataPoint = client.DataPoint;

describe('Event types', function() {
    before('Login', function() { return login.call(this, client); });
    
    //Setup Data Point With Event Detector
    
    it('Query all event types', function () {
        return client.restRequest({
            path: `/rest/latest/event-types`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            assert.isAbove(response.data.items.length, 0);
            for(var i=0; i<response.data.items.length; i++){
                assert.strictEqual(response.data.items[i].type.referenceId1, 0);
                assert.strictEqual(response.data.items[i].type.referenceId2, 0);
                if(response.data.items[i].eventType === 'DATA_POINT'){
                    assert.isNull(response.data.items[i].type.subType);
                    assert.isNull(response.data.items[i].alarmLevel);
                    assert.isTrue(response.data.items[i].supportsReferenceId1);
                    assert.isTrue(response.data.items[i].supportsReferenceId2);
                }else if(response.data.items[i].eventType === 'DATA_SOURCE'){
                    assert.isNull(response.data.items[i].type.subType);
                    assert.isNull(response.data.items[i].type.alarmLevel);
                    assert.isNull(response.data.items[i].alarmLevel);
                    assert.isTrue(response.data.items[i].supportsReferenceId1);
                    assert.isTrue(response.data.items[i].supportsReferenceId2);
                }else if(response.data.items[i].eventType === 'SYSTEM'){
                    assert.isNotNull(response.data.items[i].type.subType);
                    assert.isNotNull(response.data.items[i].alarmLevel);
                    if(response.data.items[i].subType === 'USER_LOGIN')
                        assert.isTrue(response.data.items[i].supportsReferenceId1);
                    else
                        assert.isFalse(response.data.items[i].supportsReferenceId1);
                }
            }             
        });
    });
    
    it('Query for data point event types ref2 always 0', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/DATA_POINT/null`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            assert.isAbove(response.data.items.length, 0);
            for(var i=0; i<response.data.items.length; i++){
                assert.isTrue(response.data.items[i].type.referenceId1 !== 0);
                assert.strictEqual(response.data.items[i].type.referenceId2, 0);
                assert.isNull(response.data.items[i].alarmLevel);
            }           
        });
    });

    it('Query for all possible user login events', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/SYSTEM/USER_LOGIN`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            for(var i=0; i<response.data.items.length; i++){
                assert.isTrue(response.data.items[i].type.referenceId1 !== 0);
                assert.strictEqual(response.data.items[i].type.referenceId2, 0);
            }          
        });
    });
    
    
    it('Query event types for single data point', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/DATA_POINT/null/${this.dp.id}`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            assert.isAbove(response.data.items.length, 0);
            for(var i=0; i<response.data.items.length; i++){
                assert.strictEqual(response.data.items[i].type.referenceId1, this.dp.id);
                assert.isTrue(response.data.items[i].type.referenceId2 !== 0);
                assert.isNotNull(response.data.items[i].alarmLevel);
            }           
        });
    });
    
    it('Query event types for single data source', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/DATA_SOURCE/null/${this.ds.id}`,
            method: 'GET'
        }).then(response => {
            assert.isNumber(response.data.total);
            assert.isAbove(response.data.items.length, 0);
            for(var i=0; i<response.data.items.length; i++){
                assert.strictEqual(response.data.items[i].type.referenceId1, this.ds.id);
                assert.isTrue(response.data.items[i].type.referenceId2 !== 0);
                assert.isNotNull(response.data.items[i].type.alarmLevel);
                assert.isNotNull(response.data.items[i].alarmLevel);
            }           
        });
    });
    
    it('Fails to query data source event subtype', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/DATA_SOURCE/test`,
            method: 'GET'
        }).then(response => {
            assert.fail(response);
        }, error =>{
            assert.strictEqual(error.status, 400);
        });
    });
    
    it('Fails to query system event using reference 1', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/SYSTEM/EMAIL_SEND_FAILURE`,
            method: 'GET'
        }).then(response => {
            assert.fail(response);
        }, error =>{
            assert.strictEqual(error.status, 500);
        });
    });
    
    it('System event with missing subtype should 404', function () {
        return client.restRequest({
            path: `/rest/latest/event-types/SYSTEM/NOTHING`,
            method: 'GET'
        }).then(response => {
            assert.fail(response);
        }, error =>{
            assert.strictEqual(error.status, 404);
        });
    });
    
    beforeEach('Create data source and points', function() {

        this.ds = new DataSource({
            name: 'Mango client test',
            enabled: false,
            modelType: 'VIRTUAL',
            pollPeriod: { periods: 5, type: 'SECONDS' },
            purgeSettings: { override: false, frequency: { periods: 1, type: 'YEARS' } },
            alarmLevels: { POLL_ABORTED: 'URGENT' },
            editPermission: null
        });
        
        this.dp = new DataPoint({
            name: 'Virtual Test Point 1',
            pointLocator : {
                startValue : "true",
                modelType : "PL.VIRTUAL",
                dataType : "BINARY",
                settable : true,
                changeType : "ALTERNATE_BOOLEAN",
            }
        });
        
        this.numDp = new DataPoint({
            name: 'Virtual Test Point 2',
            pointLocator : {
                startValue : "0",
                modelType : "PL.VIRTUAL",
                dataType : "NUMERIC",
                settable : true,
                changeType : "NO_CHANGE",
            }
        });
        
        this.mulDp = new DataPoint({
            name: 'Virtual Test Point 3',
            pointLocator : {
                startValue : "3",
                modelType : "PL.VIRTUAL",
                dataType : "MULTISTATE",
                settable : true,
                changeType : "NO_CHANGE",
            }
        });
        
        this.alphaDp = new DataPoint({
            name: 'Virtual Test Point 4',
            pointLocator : {
                startValue : "",
                modelType : "PL.VIRTUAL",
                dataType : "ALPHANUMERIC",
                settable : true,
                changeType : "NO_CHANGE",
            }
        });
        this.binaryPed1 = {
                name : "When true.",
                duration: {
                    periods: 10,
                    type: "SECONDS"
                },
                alarmLevel : "NONE",
                rtnApplicable : true,
                state: true,
                sourceTypeName : "DATA_POINT",
                detectorType : "BINARY_STATE",
            };
        this.binaryPed2 = {
                name : "When true again",
                duration: {
                    periods: 10,
                    type: "SECONDS"
                },
                alarmLevel : "NONE",
                rtnApplicable : true,
                state: true,
                sourceTypeName : "DATA_POINT",
                detectorType : "BINARY_STATE",
            };
        this.numPed1 = {
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
      return this.ds.save().then((savedDs) => {
          assert.strictEqual(savedDs, this.ds);
          assert.equal(savedDs.name, 'Mango client test');
          assert.isNumber(savedDs.id);
          this.ds.xid = savedDs.xid;
          this.ds.id = savedDs.id;

          let promises = [];
          
          this.dp.dataSourceXid = this.ds.xid;
          promises.push(this.dp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 1');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.dp.xid = savedDp.xid;
            this.dp.id = savedDp.id; //Save the ID for later
            this.binaryPed1.sourceId = this.dp.id;
            this.binaryPed2.sourceId = this.dp.id;
          }));

          this.numDp.dataSourceXid = this.ds.xid;
          promises.push(this.numDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 2');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.numDp.xid = savedDp.xid;
            this.numDp.id = savedDp.id; //Save the ID for later
            this.numPed1.sourceId = this.numDp.id;
          }));

          this.mulDp.dataSourceXid = this.ds.xid;
          promises.push(this.mulDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 3');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.mulDp.xid = savedDp.xid;
            this.mulDp.id = savedDp.id; //Save the ID for later
          }));
          
          this.alphaDp.dataSourceXid = this.ds.xid;
          promises.push(this.alphaDp.save().then((savedDp) => {
            assert.equal(savedDp.name, 'Virtual Test Point 4');
            assert.equal(savedDp.enabled, false);
            assert.isNumber(savedDp.id);
            this.alphaDp.xid = savedDp.xid;
            this.alphaDp.id = savedDp.id; //Save the ID for later
          }));

          return Promise.all(promises);
      });
    });
    
    beforeEach('Create event detectors', function(){
        let promises = [];
        promises.push(client.restRequest({
            path: '/rest/latest/event-detectors',
            method: 'POST',
            data: this.binaryPed1
        }).then(response => {
            this.binaryPed1 = response.data;
        }));
        promises.push(client.restRequest({
            path: '/rest/latest/event-detectors',
            method: 'POST',
            data: this.binaryPed2
        }).then(response => {
            this.binaryPed2 = response.data;
        }));
        promises.push(client.restRequest({
            path: '/rest/latest/event-detectors',
            method: 'POST',
            data: this.numPed1
        }).then(response => {
            this.numPed1 = response.data;
        }));
        return Promise.all(promises);
    });
    
    afterEach('Delete event detectors', function(){
        let promises = [];
        promises.push(client.restRequest({
            path: `/rest/latest/event-detectors/${this.binaryPed1.xid}`,
            method: 'DELETE',
        }).then(response =>{
            
        }));
        promises.push(client.restRequest({
            path: `/rest/latest/event-detectors/${this.binaryPed2.xid}`,
            method: 'DELETE',
        }).then(response =>{
            
        }));
        promises.push(client.restRequest({
            path: `/rest/latest/event-detectors/${this.numPed1.xid}`,
            method: 'DELETE',
        }).then(response =>{
            
        }));
        return Promise.all(promises);
    });
    //Clean up when done
    afterEach('Deletes the new virtual data source and its points to clean up', function() {
        return DataSource.delete(this.ds.xid);
    });
});
