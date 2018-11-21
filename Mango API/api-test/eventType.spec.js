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

const config = require('@infinite-automation/mango-client/test/setup');
const uuidV4 = require('uuid/v4');

describe('Event types v2', function() {
    before('Login', config.login);
    
    it('Query event types', () => {
        
        return client.restRequest({
            path: `/rest/v2/event-types?type.eventType=DATA_SOURCE`,
            method: 'GET',
            data: global.addressMailingList
        }).then(response => {
            for(var i=0; i<response.data.items.length; i++){
                console.log('Level: ' + response.data.items[i].alarmLevel);
                console.log('Description: ' + response.data.items[i].description);
                console.log(response.data.items[i].type);
            }           
        });
    });
});
