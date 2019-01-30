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

function dataPointFactory(client) {
    const MangoObject = client.MangoObject;

    return class DataPoint extends MangoObject {
        static get baseUrl() {
            return '/rest/v2/data-points';
        }

        static getValue(xid) {
            return this.getValues(xid, 1).then(data => {
                return data[0];
            });
        }

        static getValues(xid, number) {
            return client.restRequest({
                path: '/rest/v1/point-values/' + encodeURIComponent(xid) + '/latest',
                params: {
                    limit: number
                }
            }).then(response => {
                return response.data;
            });
        }
        
        static setEnabled(xid, enabled, restart) {
            let encodedXid = encodeURIComponent(xid);
            
            return client.restRequest({
                path: `${this.baseUrl}/enable-disable/${encodedXid}`,
                method: 'PUT',
                params: {
                    enabled,
                    restart
                }
            }).then(response => {
                return enabled;
            });
        }
        
        static getTags(xid) {
            let encodedXid = encodeURIComponent(xid);
            
            return client.restRequest({
                path: `/rest/v2/data-point-tags/point/${encodedXid}`,
                method: 'GET'
            }).then(response => {
                return response.data;
            });
        }
        
        static setTags(xid, tags) {
            let encodedXid = encodeURIComponent(xid);
            
            return client.restRequest({
                path: `/rest/v2/data-point-tags/point/${encodedXid}`,
                method: 'POST',
                data: tags
            }).then(response => {
                return response.data;
            });
        }
        
        static addTags(xid, tags) {
            let encodedXid = encodeURIComponent(xid);
            
            return client.restRequest({
                path: `/rest/v2/data-point-tags/point/${encodedXid}`,
                method: 'PUT',
                data: tags
            }).then(response => {
                return response.data;
            });
        }

        getValue() {
            return this.constructor.getValue(this.xid);
        }

        getValues(number) {
            return this.constructor.getValues(this.xid, number);
        }
        
        setEnabled(...args) {
            return this.constructor.setEnabled(this.xid, ...args).then(enabled => {
                this.enabled = enabled;
                return this;
            });
        }
        
        getTags() {
            return this.constructor.getTags(this.xid).then(tags => {
                this.tags = tags;
                return this;
            });
        }
        
        setTags(tags) {
            return this.constructor.setTags(this.xid, tags).then(tags => {
                this.tags = tags;
                return this;
            });
        }
        
        addTags(tags) {
            return this.constructor.addTags(this.xid, tags).then(tags => {
                this.tags = tags;
                return this;
            });
        }
    };
}

module.exports = dataPointFactory;
