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
const uuidV4 = require('uuid/v4');
const MangoClient = require('@infinite-automation/mango-client');

const jwtUrl = '/rest/v2/auth-tokens';

describe('JSON Web Token authentication', function() {
    before('Login', config.login);
    
    before('Create a test user', function() {
        const username = uuidV4();
        this.testUserPassword = uuidV4();
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
        
        this.noCookieConfig = Object.assign({
            enableCookies: false
        }, config);
        
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
            
            const jwtClient = new MangoClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
        });
    });
    
    it('Can create an authentication token for another user', function() {
        return this.createToken({username: this.testUser.username}).then(token => {
            const jwtClient = new MangoClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
        });
    });
    
    it('Can\'t create a token using token authentication', function() {
        return this.createToken().then(token => {
            //console.log(Buffer.from(token.split('.')[1], 'base64').toString());
            
            const jwtClient = new MangoClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            
            return this.createToken({}, jwtClient);
        }).then(token => {
            throw new Error('Created token using a token authentication');
        }, error => {
            assert.strictEqual(error.status, 403);
        });
    });
    
    it('Can retrieve the public key', function() {
        const publicClient = new MangoClient(config);
        
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
            const publicClient = new MangoClient(config);
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
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            const jwtClient = new MangoClient(this.noCookieConfig);
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
        const basicAuthClient = new MangoClient(this.noCookieConfig);
        basicAuthClient.setBasicAuthentication(this.testUser.username, this.testUserPassword);
        
        return this.createToken({}, basicAuthClient).then(token => {
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            this.jwtClient = new MangoClient(this.noCookieConfig);
            this.jwtClient.setBearerAuthentication(token);
            return this.jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, config.username);
            return config.delay(6000);
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
            
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            const username1 = uuidV4();
            const username2 = uuidV4();
            this.firstUser = new User({
                username: username1,
                email: `${username1}@example.com`,
                name: 'This is a name',
                permissions: '',
                password: uuidV4()
            });
            this.secondUser = new User({
                username: username2,
                email: `${username2}@example.com`,
                name: 'This is a name',
                permissions: '',
                password: uuidV4()
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
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            const jwtClient = new MangoClient(this.noCookieConfig);
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
            jwtClient = new MangoClient(this.noCookieConfig);
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
            jwtClient = new MangoClient(this.noCookieConfig);
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
        const client2 = new MangoClient(config);
        
        return client2.User.login(this.testUser.username, this.testUserPassword).then(user => {
            // use new client (logged in as the test user) to try and create a token for the admin user
            return this.createToken({username: config.username}, client2);
        }).then(() => {
            throw new Error('Shouldnt be able to create token');
        }, error => {
            assert.strictEqual(error.status, 403);
        });
    });
    
    //User Token Tests
    it.skip('Can\'t use home url setting endpoint with token', function() {
        return this.createToken({username: this.testUser.username}).then(token => {
            const jwtClient = new MangoClient(this.noCookieConfig);
            jwtClient.setBearerAuthentication(token);
            return jwtClient.User.current();
        }).then(user => {
            assert.strictEqual(user.username, this.testUser.username);
        });
    });
    it.skip('Can\'t Patch self using token');
    it.skip('Can\'t Update self using token');

    it.skip('Can\'t use audio mute setting endpoint using token');
    
    it.skip('Can\'t Update self using token', function() {
        return this.createToken().then(token => {
            this.jwtClient = new MangoClient(this.noCookieConfig);
            this.jwtClient.setBearerAuthentication(token);
            return this.jwtClient.User.current();
        }).then(user => {
            this.testUser.name = 'Not possible';
            return this.jwtClient.restRequest({
                path: `/rest/v2/users/${this.testUser.username}`,
                method: 'PUT',
                data: this.testUser
            }).then(response => {
                assert.equal(response.data.mute, !this.testUser.mute);
            });
        });
    });
});
