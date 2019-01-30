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

function MangoObjectFactory(client) {
    return class MangoObject {
        constructor(options) {
            Object.assign(this, options);
        }

        static get idProperty() {
            return 'xid';
        }

        static get(id) {
            var options = {};
            options[this.idProperty] = id;
            return (new this(options)).get();
        }

        static delete(id) {
            var options = {};
            options[this.idProperty] = id;
            return (new this(options)).delete();
        }

        static copy(existingXid, newXid, newName) {
            var options = {};
            options[this.idProperty] = existingXid;
            return (new this(options)).copy(newXid, newName);
        }

        static list() {
            return this.query();
        }

        static query(queryString) {
            let path = this.baseUrl;
            if (queryString) path += '?' + queryString;

            return client.restRequest({
                path: path
            }).then((response) => {
                if (response.data.length != null) {
                    return response;
                }
                response.data.items.total = response.data.total;
                return response.data.items;
            });
        }

        get() {
            return client.restRequest({
                path: this.constructor.baseUrl + '/' + encodeURIComponent(this[this.constructor.idProperty])
            }).then(response => {
                return this.updateSelf(response);
            });
        }

        save() {
            let method  = 'POST';
            let path = this.constructor.baseUrl;
            if (this.originalId) {
                method = 'PUT';
                path += '/' + encodeURIComponent(this.originalId);
            }

            return client.restRequest({
                path: path,
                method: method,
                data: this
            }).then(response => {
                return this.updateSelf(response);
            });
        }

        delete() {
            return client.restRequest({
                path: this.constructor.baseUrl + '/' + encodeURIComponent(this[this.constructor.idProperty]),
                method: 'DELETE'
            }).then(response => {
                this.updateSelf(response);
                delete this.originalId;
                return this;
            });
        }

        copy(newXid, newName) {
            return client.restRequest({
                path: this.constructor.baseUrl + '/copy/' + encodeURIComponent(this[this.constructor.idProperty]),
                method: 'PUT',
                params: {
                    copyXid: newXid,
                    copyName: newName
                }
            }).then(response => {
                const copy = new this.constructor();
                return copy.updateSelf(response);
            });
        }

        updateSelf(response) {
            Object.assign(this, response.data);
            this.originalId = this[this.constructor.idProperty];
            return this;
        }
    };
}
module.exports = MangoObjectFactory;
