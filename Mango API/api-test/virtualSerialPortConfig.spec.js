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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();

describe('Virtual serial port config tests', function(){
    before('Login', function() { return login.call(this, client); });
    this.timeout(20000);

    it('Creates a virtual serial socket bridge', () => {
      const ssb = {
          xid : "VSP_client",
          portName : "virtual-client",
          address : "localhost",
          port : 9000,
          timeout : 0,
          portType : "SERIAL_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports',
          method: 'POST',
          data: ssb
      }).then(response => {
        assert.equal(response.data.portName, ssb.portName);
      });
    });

    it('Creates a virtual serial server socket bridge', () => {
      const ssb = {
          xid : "VSP_server",
          portName : "virtual-server",
         port : 9000,
         bufferSize : 1024,
         timeout : 0,
         portType : "SERIAL_SERVER_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports',
          method: 'POST',
          data: ssb
      }).then(response => {
        assert.equal(response.data.portName, ssb.portName);
      });
    });

    it('Updates a virtual serial socket bridge', () => {
      const ssb = {
          xid : "VSP_client",
          portName : "virtual-client",
          address : "192.168.0.1",
          port : 9000,
          timeout : 0,
          portType : "SERIAL_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports/VSP_client',
          method: 'PUT',
          data: ssb
      }).then(response => {
        assert.equal(response.data.address, ssb.address);
      });
    });

    it('Updates a virtual serial server socket bridge', () => {
      const ssb = {
          xid : "VSP_server",
          portName : "virtual-server",
         port : 9012,
         bufferSize : 1024,
         timeout : 0,
         portType : "SERIAL_SERVER_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports/VSP_server',
          method: 'PUT',
          data: ssb
      }).then(response => {
        assert.equal(response.data.port, ssb.port);
      });
    });

    //Get both
    it('Gets a virtual serial socket bridge', () => {
      const ssb = {
          xid : "VSP_client",
          portName : "virtual-client",
          address : "192.168.0.1",
          port : 9000,
          timeout : 0,
          portType : "SERIAL_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports/VSP_client',
          method: 'GET'
      }).then(response => {
        assert.equal(response.data.address, ssb.address);
      });
    });

    it('Gets a virtual serial server socket bridge', () => {
      const ssb = {
          xid : "VSP_server",
          portName : "virtual-server",
         port : 9012,
         bufferSize : 1024,
         timeout : 0,
         portType : "SERIAL_SERVER_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports/VSP_server',
          method: 'GET',
      }).then(response => {
        assert.equal(response.data.port, ssb.port);
      });
    });

    it('Deletes a virtual serial socket bridge', () => {
      const ssb = {
          xid : "VSP_client",
          portName : "virtual-client",
          address : "192.168.0.1",
          port : 9000,
          timeout : 0,
          portType : "SERIAL_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports/VSP_client',
          method: 'DELETE',
          data: {}
      }).then(response => {
        assert.equal(response.data.address, ssb.address);
      });
    });

    it('Deletes a virtual serial server socket bridge', () => {
      const ssb = {
          xid : "VSP_server",
          portName : "virtual-server",
         port : 9012,
         bufferSize : 1024,
         timeout : 0,
         portType : "SERIAL_SERVER_SOCKET_BRIDGE"
        };
      return client.restRequest({
          path: '/rest/latest/virtual-serial-ports/VSP_server',
          method: 'DELETE',
          data: {}
      }).then(response => {
        assert.equal(response.data.port, ssb.port);
      });
    });
});
