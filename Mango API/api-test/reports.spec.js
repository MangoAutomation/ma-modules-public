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

describe.skip('Test Report Endpoints', function() {
    before('Login', config.login);

    //TODO Create a Report first to get the XID to use
    // then un-skip the test, we currently don't have a Reports REST controller


    it('Create Report event handler', () => {

      return client.restRequest({
          path: '/rest/v1/event-handlers',
          method: 'POST',
          data: {
              xid : "EVTH_REPORT_TEST",
              name : null,
              alias : "Testing reports",
              disabled : false,
              activeReportId: 1,
              inactiveReportId: -1,
              eventType : {
                refId1 : 0,
                duplicateHandling : "ALLOW",
                typeName : "SYSTEM",
                systemEventType : "USER_LOGIN",
                rateLimited : false
              },

              handlerType : "REPORT"
            }
      }).then(response => {
        var savedHandler = response.data;
        assert.equal(savedHandler.xid, 'EVTH_REPORT_TEST');
        assert.equal(savedHandler.alias, 'Testing reports');
        assert.equal(savedHandler.activeReportId, 1);
        assert.equal(savedHandler.inactiveReportId, -1);
        assert.equal(savedHandler.eventType.duplicateHandling, "ALLOW");
        assert.isNumber(savedHandler.id);
      });
    });

    it('Delete Report event handler', () => {
      return client.restRequest({
          path: '/rest/v1/event-handlers/EVTH_REPORT_TEST',
          method: 'DELETE',
          data: {}
      }).then(response => {
          assert.equal(response.data.xid, 'EVTH_REPORT_TEST');
          assert.equal(response.data.alias, 'Testing reports');
          assert.equal(response.data.activeReportId, 1);
          assert.equal(response.data.inactiveReportId, -1);
          assert.equal(response.data.eventType.duplicateHandling, "ALLOW");
          assert.isNumber(response.data.id);
      });
    });
});
