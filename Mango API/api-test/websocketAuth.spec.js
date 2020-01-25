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

const {createClient, login, uuid, noop, defer, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const User = client.User;
const Role = client.Role;

const noCookieConfig = {
    enableCookies: false
};

const SESSION_DESTROYED = 4101;
const USER_UPDATED = 4102;
const USER_AUTH_TOKENS_REVOKED = 4103;
const USER_AUTH_TOKEN_EXPIRED = 4104;
//const AUTH_TOKENS_REVOKED = 4105;

describe('Websocket authentication', function() {
    before('Login', function() { return login.call(this, client); });
    
    before('Create a test user', function() {
        this.clients = {};
        
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: [],
            password: this.testUserPassword
        });
    });
    
    after('Delete the test user', function() {
        return this.testUser.delete().catch(noop);
    });
    
    before('Create a client that uses session authentication', function() {
        this.clients.session = createClient();
    });

    before('Create a client that uses basic authentication', function() {
        this.clients.basic = createClient(noCookieConfig);
        this.clients.basic.setBasicAuthentication(this.testUser.username, this.testUserPassword);
    });
    
    before('Create a client that uses JWT authentication', function() {
        this.clients.token = createClient(noCookieConfig);
    });

    beforeEach('Reset the test user', function() {
        this.testUserPassword = uuid();
        this.clients.basic.setBasicAuthentication(this.testUser.username, this.testUserPassword);
        
        this.testUser.password = this.testUserPassword;
        this.testUser.disabled = false;
        this.testUser.permissions = [];
        return this.testUser.save();
    });
    
    beforeEach('Ensure session client is logged in', function() {
        this.clients.session.resetXsrfCookie();
        return this.clients.session.User.login(this.testUser.username, this.testUserPassword);
    });
    
    beforeEach('Reset the auth token', function() {
        return client.restRequest({
            path: '/rest/v2/auth-tokens/create',
            method: 'POST',
            data: {
                username: this.testUser.username
            }
        }).then(response => {
            this.clients.token.setBearerAuthentication(response.data.token);
        });
    });

    const testWebSocketsUsingClient = function(client) {
        const socketOpen = defer();
        const gotResponse = defer();
        const sequenceNumber = Math.floor(Math.random() * 10000);

        const ws = client.openWebSocket({
            path: '/rest/v2/websocket/temporary-resources'
        });

        ws.on('open', () => {
            socketOpen.resolve();
        });
        
        ws.on('error', error => {
            const msg = new Error(`WebSocket error, error: ${error}`);
            socketOpen.reject(msg);
            gotResponse.reject(msg);
            ws.close();
        });
        
        ws.on('close', (code, reason) => {
            const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
            socketOpen.reject(msg);
            gotResponse.reject(msg);
        });

        ws.on('message', msgStr => {
            assert.isString(msgStr);
            const msg = JSON.parse(msgStr);
            if (msg.messageType === 'RESPONSE' && msg.sequenceNumber === sequenceNumber) {
                gotResponse.resolve();
                ws.close();
            }
        });

        return socketOpen.promise.then(() => {
            const send = defer();
            ws.send(JSON.stringify({
                messageType: 'REQUEST',
                requestType: 'SUBSCRIPTION',
                sequenceNumber
            }), error => {
                if (error != null) {
                    send.reject(error);
                } else {
                    send.resolve();
                }
            });
            return send.promise;
        }).then(() => gotResponse.promise);
    };
    
    const testWebSocketTermination = function(client, closeAction, checkResponse) {
        this.timeout(5000);
        
        const socketOpen = defer();
        const socketClose = defer();

        const openTimeout = setTimeout(() => {
            socketOpen.reject('Opening websocket timeout');
        }, 2000);
        
        const ws = client.openWebSocket({
            path: '/rest/v2/websocket/temporary-resources'
        });

        ws.on('open', () => {
            socketOpen.resolve(ws);
            clearTimeout(openTimeout);
        });
        
        ws.on('error', error => {
            const msg = new Error(`WebSocket error, error: ${error}`);
            socketOpen.reject(msg);
            ws.close();
        });
        
        ws.on('close', (code, reason) => {
            const msg = new Error(`WebSocket closed, code: ${code}, reason: ${reason}`);
            socketOpen.reject(msg);
            socketClose.resolve({code, reason});
        });

        return socketOpen.promise
            .then(ws => delay(500).then(() => ws))
            .then(closeAction)
            .then(() => socketClose.promise)
            .then(checkResponse);
    };

    ['session', 'basic', 'token'].forEach(clientName => {
        it(`Can use ${clientName} authentication with websockets`, function() {
            return testWebSocketsUsingClient.call(this, this.clients[clientName]);
        });
        
        it(`Terminates ${clientName} authentication websockets when user is deleted`, function() {
            return testWebSocketTermination.call(this, this.clients[clientName], () => {
                return this.testUser.delete();
            }, ({code, reason}) => {
                assert.strictEqual(code, USER_UPDATED);
            }).finally(() => {
                this.testUser.id = null;
                delete this.testUser.originalId;
            });
        });
        
        it(`Terminates ${clientName} authentication websockets when user is disabled`, function() {
            return testWebSocketTermination.call(this, this.clients[clientName], () => {
                this.testUser.disabled = true;
                return this.testUser.save();
            }, ({code, reason}) => {
                assert.strictEqual(code, USER_UPDATED);
            });
        });
        
        if (clientName !== 'token') {
            it(`Terminates ${clientName} authentication websockets when user's password is changed`, function() {
                return testWebSocketTermination.call(this, this.clients[clientName], () => {
                    this.testUser.password = 'xyz12345678';
                    return this.testUser.save();
                }, ({code, reason}) => {
                    assert.strictEqual(code, USER_UPDATED);
                });
            });
        }
        
        it(`Terminates ${clientName} authentication websockets when user's permissions are changed`, function() {
            return testWebSocketTermination.call(this, this.clients[clientName], () => {
                const role = new Role({
                    xid: uuid(),
                    name: 'websocket auth test role'
                });
                return role.save().then(newRole => {
                    this.testUser.permissions = [newRole.xid];
                    return this.testUser.save();
                });
            }, ({code, reason}) => {
                assert.strictEqual(code, USER_UPDATED);
            });
        });
    });

    it('Terminates session authentication websockets when user logs out', function() {
        return testWebSocketTermination.call(this, this.clients.session, () => {
            return this.clients.session.User.logout();
        }, ({code, reason}) => {
            assert.strictEqual(code, SESSION_DESTROYED);
        });
    });

    it('Terminates token authentication websockets when token expires', function() {
        this.timeout(5000);
        
        return client.restRequest({
            path: '/rest/v2/auth-tokens/create',
            method: 'POST',
            data: {
                username: this.testUser.username,
                expiry: new Date(Date.now() + 2000) 
            }
        }).then(response => {
            this.clients.token.setBearerAuthentication(response.data.token);
            
            return testWebSocketTermination.call(this, this.clients.token, () => {
                // noop
            }, ({code, reason}) => {
                assert.strictEqual(code, USER_AUTH_TOKEN_EXPIRED);
            });
        });
    });

    it('Terminates token authentication websockets when token is revoked', function() {
        return testWebSocketTermination.call(this, this.clients.token, () => {
            return client.restRequest({
                path: `/rest/v2/auth-tokens/revoke/${encodeURIComponent(this.testUser.username)}`,
                method: 'POST'
            });
        }, ({code, reason}) => {
            assert.strictEqual(code, USER_AUTH_TOKENS_REVOKED);
        });
    });

    it('Does not terminate token authentication websockets when password is changed', function() {
        this.timeout(5000);
        
        return testWebSocketTermination.call(this, this.clients.token, (ws) => {
            this.testUser.password = 'xyz12345678';
            return this.testUser.save().then(() => {
                return delay(1000);
            }).then(() => {
                ws.close(1000);
            });
        }, ({code, reason}) => {
            assert.strictEqual(code, 1000);
        });
    });

});
