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

const {createClient, login, uuid, config, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const User = client.User;

const jwtUrl = '/rest/v2/auth-tokens';

describe('JSON Web Token authentication', function() {
    before('Login', function() { return login.call(this, client); });
    
    before('Create a test user', function() {
        const username = uuid();
        this.testUserPassword = uuid();
        this.testUser = new User({
            username,
            email: `${username}@example.com`,
            name: `${username}`,
            permissions: '',
            password: this.testUserPassword
        });
        return this.testUser.save();
    });
    
    after('Delete the test user', function() {
        return this.testUser.delete();
    });
    
    before('Helper functions', function() {
        this.createToken = function(body = {}, clt = client) {
            return clt.restRequest({
                path: `${jwtUrl}/create`,
                method: 'POST',
                data: body
            }).then(response => {
                return response.data.token;
            });
        };
        
        this.noCookieConfig = {
            enableCookies: false
        };
        
        this.parseClaims = function(token) {
            const parts = token.split('.');
            assert.strictEqual(parts.length, 3);
            
            const claimsStr = Buffer.from(parts[1], 'base64').toString();
            const claims = JSON.parse(claimsStr);
            
            assert.isObject(claims);
            return claims;
        };
    });

    it('Can create and use an authentication token with REST', function() {
        return this.createToken().then(token => {
            //console.log(Buffer.from(token.split('.')[1], 'base64').toString());
            
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
        });
    });
    
    it('Can create an authentication token for another user', function() {
        return this.createToken({username: this.testUser.username}).then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
        });
    });
    
    it('Can\'t create a token using token authentication', function() {
        return this.createToken().then(token => {
            //console.log(Buffer.from(token.split('.')[1], 'base64').toString());
            
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            
            return this.createToken({}, jwtClient);
        }).then(token => {
            throw new Error('Created token using a token authentication');
        }, error => {
            assert.strictEqual(error.status, 403);
        });
    });
    
    it('Can retrieve the public key', function() {
        const publicClient = createClient();
        
        return publicClient.restRequest({
            path: `${jwtUrl}/public-key`,
            method: 'GET'
        }).then(response => {
            assert.strictEqual(response.status, 200);
            assert.isString(response.data);
        });
    });

    it('Can verify a token', function() {
        return this.createToken().then(token => {
            const publicClient = createClient();
            return publicClient.restRequest({
                path: `${jwtUrl}/verify`,
                method: 'GET',
                params: {
                    token
                }
            });
        }).then(response => {
            assert.strictEqual(response.status, 200);
            
            const parsedToken = response.data;
            assert.isObject(parsedToken);
            assert.isObject(parsedToken.header);
            assert.isObject(parsedToken.body);
            assert.notProperty(parsedToken, 'signature');
            assert.strictEqual(parsedToken.header.alg, 'ES512');
            assert.strictEqual(parsedToken.body.sub, config.username);
            assert.strictEqual(parsedToken.body.typ, 'auth');
            assert.isNumber(parsedToken.body.exp);
            assert.isNumber(parsedToken.body.id);
            assert.isNumber(parsedToken.body.v);
        });
    });
    
    it('Admin can edit another user using token authentication', function() {
        return this.createToken().then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.get(this.testUser.username);
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            user.name = 'Joe';
            return user.save();
        }).then(user => {
            assert.strictEqual(user.name, 'Joe');
        });
    });
    
    it('Standard user can\'t edit own user using token authentication', function() {
        return this.createToken({username: this.testUser.username}).then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            user.name = 'Joe';
            
            return user.save().then(user => {
                throw new Error('Edited own user using a token authentication');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });
    
    it('Admin can\'t edit own user using token authentication', function() {
        return this.createToken().then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
            user.name = 'Joe';
            
            return user.save().then(user => {
                throw new Error('Edited own user using a token authentication');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });

    it('Can create an authentication token using basic authentication', function() {
        const basicAuthClient = createClient(this.noCookieConfig);
        basicAuthClient.setBasicAuthentication(this.testUser.username, this.testUserPassword);
        
        return this.createToken({}, basicAuthClient).then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
        });
    });

    it('Expires tokens correctly', function() {
        this.timeout(10000);
        
        const expiry = new Date(Date.now() + 5000);
        
        return this.createToken({expiry}).then(token => {
            this.jwtClient = createClient(this.noCookieConfig);
            this.jwtClient.setBearerAuthentication(token);
            return this.jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
            return delay(6000);
        }).then(() => {
            return this.jwtClient.User.current().then(user => {
                throw new Error('Expired token worked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });

    it('Creates tokens with the correct expiry timestamp', function() {
        const expiry = new Date(Date.now() + Math.random() * 10000);
        
        return this.createToken({expiry}).then(token => {
            const parts = token.split('.');
            assert.strictEqual(parts.length, 3);
            
            const claimsStr = Buffer.from(parts[1], 'base64').toString();
            const claims = JSON.parse(claimsStr);
            
            assert.isObject(claims);
            assert.strictEqual(claims.exp, Math.floor(expiry.getTime() / 1000));
        });
    });

    it('Verifies that a tampered with token is invalid', function() {
        return this.createToken().then(token => {
            const parts = token.split('.');
            assert.strictEqual(parts.length, 3);
            
            const claimsStr = Buffer.from(parts[1], 'base64').toString();
            const claims = JSON.parse(claimsStr);
            
            assert.isObject(claims);
            claims.id++; // modify the id
            
            parts[1] = Buffer.from(JSON.stringify(claims)).toString('base64');
            
            const tamperedToken = parts.join('.');
            assert.notEqual(tamperedToken, token);
            
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(tamperedToken);
            return jwtClient.User.current().then(user => {
                throw new Error('Invalid token worked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });
    
    it('Can reset the public and private keys', function() {
        return client.restRequest({
            path: `${jwtUrl}/public-key`,
            method: 'GET'
        }).then(pk => {
            this.pk = pk;
            
            return client.restRequest({
                path: `${jwtUrl}/reset-keys`,
                method: 'POST'
            });
        }).then(() => {
            return client.restRequest({
                path: `${jwtUrl}/public-key`,
                method: 'GET'
            });
        }).then(newPk => {
            assert.notEqual(newPk, this.pk);
        });
    });

    it('Rejects tokens signed with an old private key', function() {
        return this.createToken().then(token => {
            this.token = token;
            
            return client.restRequest({
                path: `${jwtUrl}/reset-keys`,
                method: 'POST'
            });
        }).then(() => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(this.token);
            
            return jwtClient.User.current().then(user => {
                throw new Error('Invalid token worked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });

    it('Rejects a token with mismatching username and id', function() {
        const createUsers = () => {
            const username1 = uuid();
            const username2 = uuid();
            this.firstUser = new User({
                username: username1,
                email: `${username1}@example.com`,
                name: 'This is a name',
                permissions: '',
                password: uuid()
            });
            this.secondUser = new User({
                username: username2,
                email: `${username2}@example.com`,
                name: 'This is a name',
                permissions: '',
                password: uuid()
            });
            return Promise.all([this.firstUser.save(), this.secondUser.save()]);
        };
        
        const noop = () => null;
        
        const deleteUsers = () => {
            return Promise.all([this.firstUser && this.firstUser.delete().then(null, noop), this.secondUser && this.firstUser.delete().then(null, noop)]);
        };

        return createUsers().then(() => {
            this.firstUsername = this.firstUser.username;
            return this.createToken({username: this.firstUser.username});
        }).then(token => {
            this.token = token;
            return this.firstUser.delete();
        }).then(() => {
            this.secondUser.username = this.firstUsername;
            return this.secondUser.save();
        }).then(() => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(this.token);
            
            return jwtClient.User.current().then(user => {
                throw new Error('Invalid token worked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        }).then(result => {
            return deleteUsers().then(() => result);
        }, error => {
            return deleteUsers().then(() => Promise.reject(error));
        });
    });

    it('Doesn\'t create sessions when using authentication tokens', function() {
        return this.createToken().then(token => {
            const jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            
            return jwtClient.restRequest({
                path: '/rest/v1/users/current'
            });
        }).then(response => {
            assert.notProperty(response.headers, 'set-cookie');
        });
    });
    
    it('Rejects revoked tokens', function() {
        let jwtClient;
        return this.createToken().then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
            
            return client.restRequest({
                path: `${jwtUrl}/revoke`,
                method: 'POST'
            });
        }).then(response => {
            assert.strictEqual(response.status, 204);
            
            return jwtClient.User.current().then(user => {
                throw new Error('Revoked token worked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });

    it('Can revoke other user\'s tokens', function() {
        let jwtClient;

        return this.createToken({username: this.testUser.username}).then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            
            return client.restRequest({
                path: `${jwtUrl}/revoke/${encodeURIComponent(this.testUser.username)}`,
                method: 'POST'
            });
        }).then(response => {
            assert.strictEqual(response.status, 204);
            
            return jwtClient.User.current().then(user => {
                throw new Error('Revoked token worked');
            }, error => {
                assert.strictEqual(error.status, 401);
            });
        });
    });

    it('Non-admins can\'t create authentication tokens for other users', function() {
        const client2 = createClient(config);
        
        return client2.User.login(this.testUser.username, this.testUserPassword).then(user => {
            // use new client (logged in as the test user) to try and create a token for the admin user
            return this.createToken({username: config.username}, client2);
        }).then(() => {
            throw new Error('Shouldnt be able to create token');
        }, error => {
            assert.strictEqual(error.status, 403);
        });
    });
    
    //User Token Tests v2
    //TODO use UserV2 class when available
    it('Can\'t use home url setting endpoint with token', function() {
        let jwtClient;
        return this.createToken({username: this.testUser.username}).then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            return jwtClient.restRequest({
                path: `/rest/v2/users/${user.username}/homepage?url=/anything`,
                method: 'PUT'
            }).then(response => {
                throw new Error('Shouldnt be able to update home url');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });
    
    it('Can\'t use audio mute setting endpoint with token', function() {
        let jwtClient;
        return this.createToken({username: this.testUser.username}).then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            return jwtClient.restRequest({
                path: `/rest/v2/users/${user.username}/mute?mute=true`,
                method: 'PUT'
            }).then(response => {
                throw new Error('Shouldnt be able to update muted');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });
    
    it('Standard user can\'t edit own user using token authentication v2', function() {
        let jwtClient;
        return this.createToken({username: this.testUser.username}).then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            user.name = 'Joe';
            //Fix permissions to be a set (v1 users use a string)
            user.permissions = ['permissions'];
            return jwtClient.restRequest({
                path: `/rest/v2/users/${user.username}`,
                method: 'PUT',
                data: user
            }).then(user => {
                throw new Error('Edited own user using a token authentication');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });
    
    it('Admin can\'t edit own user using token authentication v2', function() {
        let jwtClient;
        return this.createToken().then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
            user.name = 'Joe';
            //Fix permissions to be a set (v1 users use a string)
            user.permissions = ['superamdin'];
            return jwtClient.restRequest({
                path: `/rest/v2/users/${user.username}`,
                method: 'PUT',
                data: user
            }).then(user => {
                throw new Error('Edited own user using a token authentication');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });
    
    it('Standard user can\'t patch self using token authentication v2', function() {
        let jwtClient;
        return this.createToken({username: this.testUser.username}).then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
            return jwtClient.restRequest({
                path: `/rest/v2/users/${user.username}`,
                method: 'PATCH',
                data: {
                    name: 'Joe'
                }
            }).then(user => {
                throw new Error('Patching own user using a token authentication');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });
    
    it('Admin can\'t edit own user using token authentication v2', function() {
        let jwtClient;
        return this.createToken().then(token => {
            jwtClient = createClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
            return jwtClient.restRequest({
                path: `/rest/v2/users/${user.username}`,
                method: 'PATCH',
                data: {
                    name: 'Joe'
                }
            }).then(user => {
                throw new Error('Edited own user using a token authentication');
            }, error => {
                assert.strictEqual(error.status, 403);
            });
        });
    });

});
