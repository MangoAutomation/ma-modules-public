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

const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const querystring = require('querystring');
const uuidV4 = require('uuid/v4');
const FormData = require('form-data');
const WebSocket = require('ws');

const DataSourceFactory = require('./dataSource');
const DataPointFactory = require('./dataPoint');
const UserFactory = require('./user');
const MangoObjectFactory = require('./mangoObject');
const pointValuesFactory = require('./pointValue.js');

class MangoClient {
    constructor(options) {
        options = options || {};

        const enableCookies = options.enableCookies == null || options.enableCookies;

        if (enableCookies) {
            this.cookies = {};
            this.resetXsrfCookie();
        }
        
        this.defaultHeaders = options.defaultHeaders || {};

        this.options = {
            protocol: options.protocol || 'http',
            host: options.host || 'localhost',
            port: options.port || (options.protocol === 'https' ? 8443 : 8080),
            rejectUnauthorized: options.rejectUnauthorized == null ? true : !!options.rejectUnauthorized,
            keepAlive: options.keepAlive != null ? !!options.keepAlive : true
        };

        if (options.agent) {
            this.agent = options.agent;
        } else {
            const agentOpts = {
                host: this.options.host,
                port: this.options.port,
                rejectUnauthorized: this.options.rejectUnauthorized,
                keepAlive: this.options.keepAlive
            };

            if (this.options.protocol === 'https') {
                this.agent = new https.Agent(agentOpts);
            } else {
                this.agent = new http.Agent(agentOpts);
            }
        }

        this.MangoObject = MangoObjectFactory(this);
        this.DataSource = DataSourceFactory(this);
        this.DataPoint = DataPointFactory(this);
        this.User = UserFactory(this);
        const PointValues = pointValuesFactory(this);
        this.pointValues = new PointValues();
    }
    
    resetXsrfCookie() {
        this.cookies['XSRF-TOKEN'] = uuidV4();
    }

    setBearerAuthentication(token) {
        this.defaultHeaders.Authorization = `Bearer ${token}`;
    }
    
    setBasicAuthentication(username, password) {
        const encoded = new Buffer(`${username}:${password}`).toString('base64');
        this.defaultHeaders.Authorization = `Basic ${encoded}`;
    }
    
    encodeParams(params) {
        const keys = Object.keys(params);
        if (keys.length) {
            const encodedParams = {};
            keys.forEach(key => {
               const value = params[key];
               if (value instanceof Date) {
                   encodedParams[key] = value.toISOString();
               } else {
                   encodedParams[key] = value;
               }
            });
            return '?' + querystring.stringify(encodedParams);
        }
        return '';
    }
    
    addCookiesToHeaders(headers) {
        if (!this.cookies) return;
        
        if (this.cookies['XSRF-TOKEN']) {
            headers['X-XSRF-TOKEN'] = this.cookies['XSRF-TOKEN'];
        }
        
        const requestCookies = [];
        Object.keys(this.cookies).forEach(name => {
            const value = encodeURIComponent(this.cookies[name]);
            if (value != null) {
                requestCookies.push(`${name}=${value}`);
            }
        });
        if (requestCookies.length) {
            headers.Cookie = requestCookies.join('; ');
        }
    }

    restRequest(optionsArg) {
        let requestPromise = new Promise((resolve, reject) => {
            let hostname = optionsArg.hostname;
            if (!hostname) {
                hostname = this.options.host;
                if (!(this.options.protocol === 'http' && this.options.port !== 80 || this.options.protocol === 'https' && this.options.port !== 443)) {
                    hostname += ':' + this.options.port;
                }
            }
            
            const options = {
                hostname,
                path : optionsArg.path,
                agent: this.agent,
                method : optionsArg.method || 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            };

            if (optionsArg.params) {
                options.path += this.encodeParams(optionsArg.params);
            }

            let bodyData;
            let formData;
            if (optionsArg.data) {
                const contentType = optionsArg.headers && (optionsArg.headers['Content-Type'] || optionsArg.headers['content-type']);
                
                if (optionsArg.data instanceof Buffer) {
                    bodyData = optionsArg.data;
                } else if (contentType == null || contentType == 'application/json' || contentType == 'application/json;charset=utf-8') {
                    bodyData = Buffer.from(JSON.stringify(optionsArg.data));
                    options.headers['Content-Type'] = 'application/json;charset=utf-8';
                } else if (typeof optionsArg.data === 'string') {
                    bodyData = Buffer.from(optionsArg.data);
                } else {
                    throw new Error('Unknown data type');
                }

                options.headers['Content-Length'] = Buffer.byteLength(bodyData);
            } else if (optionsArg.uploadFiles) {
                formData = new FormData();
                optionsArg.uploadFiles.forEach(fileName => {
                    formData.append(path.basename(fileName), fs.createReadStream(fileName));
                });
                options.headers['Content-Type'] = 'multipart/form-data; boundary=' + formData.getBoundary();
            }
            
            this.addCookiesToHeaders(options.headers);
            Object.assign(options.headers, this.defaultHeaders, optionsArg.headers);

            const requestMethod = this.agent.protocol === 'https:' ? https.request : http.request;
            const request = requestMethod(options, response => {
                const responseData = {
                    status: response.statusCode,
                    data: null,
                    headers: response.headers
                };

                if (this.cookies) {
                    const setCookieHeaders = response.headers['set-cookie'];
                    if (setCookieHeaders) {
                        setCookieHeaders.map(parseCookie).forEach(cookie => {
                            if (cookie['Max-Age'] === '0') {
                                delete this.cookies[cookie.name];
                            } else {
                                this.cookies[cookie.name] = cookie.value;
                            }
                        });
                    }
                }

                const chunks = [];
                if (optionsArg.writeToFile) {
                    const fileOutputStream = fs.createWriteStream(optionsArg.writeToFile);
                    response.pipe(fileOutputStream);
                } else {
                    response.on('data', chunk => chunks.push(chunk));
                }

                response.on('end', () => {
                    if (chunks.length) {
                        const fullBody = Buffer.concat(chunks);

                        if (optionsArg.dataType === 'buffer') {
                            responseData.data = fullBody;
                        } else {
                            const stringBody = fullBody.toString('utf8');

                            if (optionsArg.dataType === 'string') {
                                responseData.data = stringBody;
                            } else {
                                try {
                                    responseData.data = JSON.parse(stringBody);
                                } catch(e) {
                                    reject(e);
                                }
                            }
                        }
                    }

                    if (response.statusCode < 400) {
                        resolve(responseData);
                    } else {
                        const e = new Error(`Mango HTTP error - ${response.statusCode} ${response.statusMessage}`);
                        e.status = response.statusCode;
                        e.headers = response.headers;
                        e.response = response;
                        e.data = responseData.data;
                        reject(e);
                    }
                });
            });

            request.on('error', error => reject(error));

            if (formData) {
                formData.pipe(request);
            } else {
                if (bodyData) {
                    request.write(bodyData);
                }
                request.end();
            }
        });

        if (optionsArg.retries > 0) {
            optionsArg.retries--;
            requestPromise = requestPromise.catch((error) => {
                return delay(optionsArg.retryDelay || 5000).then(this.restRequest.bind(this, optionsArg));
            });
        }

        return requestPromise;

        function delay(time) {
            return new Promise((resolve) => {
                setTimeout(resolve, time);
            });
        }
        
        function parseCookie(cookieHeader) {
            const cookieParts = cookieHeader.split(/\s*;\s*/);
            const cookieObject = {};
            cookieParts.forEach((part, i) => {
                const keyValue = part.split('=');
                if (keyValue.length <= 0) return;

                if (i === 0) {
                    cookieObject.name = keyValue[0];
                    const matches = /^"(.*)"$/.exec(keyValue[1]);
                    cookieObject.value = decodeURIComponent(matches ? matches[1] : keyValue[1]);
                } else {
                    cookieObject[keyValue[0]] = keyValue[1];
                }
            });
            return cookieObject;
        }
    }
    
    openWebSocket(optionsArg) {
        const options = Object.assign({}, {
            agent: this.agent,
            rejectUnauthorized: this.options.rejectUnauthorized,
            headers: {}
        }, optionsArg);

        Object.assign(options.headers, this.defaultHeaders, optionsArg.headers);
        this.addCookiesToHeaders(options.headers);
        
        const wsProtocol = this.options.protocol === 'https' ? 'wss' : 'ws';
        let wsUrl = `${wsProtocol}://${this.options.host}:${this.options.port}`;
        
        if (optionsArg.path) {
            wsUrl += optionsArg.path;
        }
        
        if (optionsArg.params) {
            wsUrl += this.encodeParams(optionsArg.params);
        }
        
        return new WebSocket(wsUrl, optionsArg.protocols, options);
    }
}

module.exports = MangoClient;
