/**
 * Copyright 2020 Infinite Automation Systems Inc.
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

const {createClient, login, uuid, delay} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');;
const client = createClient();
const User = client.User;
const csvParser = require('csv-parser');
const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const Readable = require('stream').Readable;
const path = require('path');
const fs = require('fs');

describe('User csv operations ', function() {

    //Delay after upload before requesting result
    const uploadDelayMs = 1000;
    
    const newUser = (username) => {
        return new User({
            name: 'name',
            username: username,
            password: uuid(),
            email: `${username}@example.com`,
            phone: '808-888-8888',
            disabled: false,
            locale: '',
            homeUrl: 'www.google.com',
            receiveAlarmEmails: 'IGNORE',
            receiveOwnAuditEvents: false,
            muted: false,
            permissions: ['user'],
            sessionExpirationOverride: true,
            sessionExpirationPeriod: {
                periods: 1,
                type: 'SECONDS'
            },
            organization: 'Infinite Automation Systems',
            organizationalRole: uuid(),
            data: {
                stringField: 'some random string',
                numberField: 123,
                booleanField: true
            }
        });
    };
    
    function userCsvFactory(client) {
        return class UserCsv {
            get baseUrl() {
                return '/rest/latest/users';
            }
            
            download(query) {
                return client.restRequest({
                    path: `${this.baseUrl}?` + query,
                    method: 'GET'
                }).then(response => {
                    return new Promise((resolve, reject) => {
                        const result = [];
                        const s = new Readable();
                        s.push(response.data);
                        s.push(null);
                        s.pipe(csvParser())
                         .on('headers', function(headers){result.push(headers);})
                         .on('data', function (data){
                             result.push(data);
                         })
                         .on('end', function() {
                             resolve(result);              
                         })
                         .on('error', reject);
                    });
                });
            }
            
            uploadCsvFile(csvFileName) {
                return client.restRequest({
                    path: `${this.baseUrl}/bulk`,
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/csv;charset=UTF-8'
                    },
                    data: fs.readFileSync(csvFileName)
                }).then(response => {
                    return response.headers.location;
                });
            }
            
            uploadCsvData(csvData) {
                return client.restRequest({
                    path: `${this.baseUrl}/bulk`,
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/csv;charset=UTF-8'
                    },
                    data: csvData
                }).then(response => {
                    return response.headers.location;
                });
            }
            
            getUploadStatus(location, delayMs) {
                return delay(delayMs).then(()=>{
                    return client.restRequest({
                        path: location
                    }).then(response => {
                        return response.data.result;
                     });                    
                });
            }
        };
    }
    
    before('Login', function() {
        return login.call(this, client).then((...args) => {
            this.csvClient = createClient({
                defaultHeaders: {
                    Accept : 'text/csv'
                }
            });
            
            //Override to return strings
            const restRequest = this.csvClient.restRequest;
            this.csvClient.restRequest = function(optionsArg) {
                optionsArg.dataType = 'string';
                return restRequest.apply(this, arguments);
            };
           
            const UserCsv = userCsvFactory(this.csvClient);
            this.csvClient.userCsv = new UserCsv();
            // copy the session cookie to the csv client
            Object.assign(this.csvClient.cookies, client.cookies);
        });
    });
    
    beforeEach('Create 3 users', function() {
        this.users = [];
        this.users.push(newUser(uuid()));
        this.users.push(newUser(uuid()));
        this.users.push(newUser(uuid()));
        
        //So we can order on email
        this.users[0].email = 'AAA' + this.users[0].email;
        this.users[1].email = 'BBB' + this.users[1].email;
        this.users[2].email = 'CCC' + this.users[2].email;

        return Promise.all([this.users[0].save(), this.users[1].save(), this.users[2].save()]);
    });
    
    afterEach('Delete the created users', function() {
        return Promise.all([this.users[0].delete(), this.users[1].delete(), this.users[2].delete()]);
    });

    it('Can download csv file for users', function() {
        return this.csvClient.userCsv.download(`in(username,${this.users[0].username},${this.users[1].username})&sort(email)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalUsername');
            assert.strictEqual(headers[3], 'username');
            assert.strictEqual(headers[4], 'password');
            assert.strictEqual(headers[5], 'email');

            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].id, String(this.users[0].id));
            assert.strictEqual(result[1].id, String(this.users[1].id));
            assert.strictEqual(result[0].originalUsername, this.users[0].username);
            assert.strictEqual(result[1].originalUsername, this.users[1].username);
        });
    });
    
    it('Can update both users via csv upload file', function() {
        this.timeout(5000);
        return this.csvClient.userCsv.download(`in(username,${this.users[0].username},${this.users[1].username})&sort(email)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalUsername');
            assert.strictEqual(headers[3], 'username');
            assert.strictEqual(headers[4], 'password');
            assert.strictEqual(headers[5], 'email');
            
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].id, String(this.users[0].id));
            assert.strictEqual(result[1].id, String(this.users[1].id));
            assert.strictEqual(result[0].originalUsername, this.users[0].username);
            assert.strictEqual(result[1].originalUsername, this.users[1].username);
            
            //Modify the organizationalRole swapping them
            result[0].organizationalRole = this.users[1].organizationalRole;
            result[1].organizationalRole = this.users[0].organizationalRole;
            
            //Create the csv data to POST
            let csvHeaders = [];
            for(var i=0; i<headers.length; i++){
                csvHeaders.push({
                    id: headers[i],
                    title: headers[i]
                });
            }
            
            let filename = 'users.csv';
            const csvWriter = createCsvWriter({
                path: filename,
                header: csvHeaders
            });
            
            const uploadFileName = path.resolve(filename);
            return csvWriter.writeRecords(result).then(() => {
                return this.csvClient.userCsv.uploadCsvFile(uploadFileName).then(location => {
                    return delay(uploadDelayMs).then(() => {
                        return client.restRequest({
                            path: location
                        }).then(response => {
                            assert.strictEqual(response.data.result.hasError, false);
                            assert.strictEqual(response.data.result.responses[0].username, this.users[0].username);
                            assert.strictEqual(response.data.result.responses[1].username, this.users[1].username);
                            //Confirm the change
                            assert.strictEqual(response.data.result.responses[0].body.organizationalRole, this.users[1].organizationalRole);
                            assert.strictEqual(response.data.result.responses[1].body.organizationalRole, this.users[0].organizationalRole);
                        });                    
                    });
                });
            }).finally(() => {
                //Delete the file
                fs.unlinkSync(uploadFileName);
            });
        });
    });
    
    it('Can modify username of existing user', function() {
        this.timeout(5000);
        return this.csvClient.userCsv.download(`in(username,${this.users[0].username},${this.users[1].username})&sort(email)`).then(result => {
            assert.isArray(result);
            const headers = result.shift();
            //assert.strictEqual(headers[0], "''");
            assert.strictEqual(headers[1], 'action');
            assert.strictEqual(headers[2], 'originalUsername');
            assert.strictEqual(headers[3], 'username');
            assert.strictEqual(headers[4], 'password');
            assert.strictEqual(headers[5], 'email');
            
            assert.strictEqual(result.length, 2);
            assert.strictEqual(result[0].id, String(this.users[0].id));
            assert.strictEqual(result[1].id, String(this.users[1].id));
            assert.strictEqual(result[0].originalUsername, this.users[0].username);
            assert.strictEqual(result[1].originalUsername, this.users[1].username);
            
            const user0Username = uuid();
            const user1Username = uuid();
            result[0].username = user0Username;
            result[1].username = user1Username;
            ;
            //Create the csv data to POST
            let csvHeaders = [];
            for(var i=0; i<headers.length; i++){
                csvHeaders.push({
                    id: headers[i],
                    title: headers[i]
                });
            }
            let filename = 'users.csv';
            const csvWriter = createCsvWriter({
                path: filename,
                header: csvHeaders
            });
            const uploadFileName = path.resolve(filename);
            return csvWriter.writeRecords(result).then(() => {
                return this.csvClient.userCsv.uploadCsvFile(uploadFileName).then(location => {
                    return delay(uploadDelayMs).then(() => {
                        return client.restRequest({
                            path: location
                        }).then(response => {
                            assert.strictEqual(response.data.result.hasError, false);
                            assert.strictEqual(response.data.result.responses[0].username, this.users[0].username);
                            assert.strictEqual(response.data.result.responses[1].username, this.users[1].username);
                            
                            //Confirm the change
                            assert.strictEqual(response.data.result.responses[0].body.username, user0Username);
                            assert.strictEqual(response.data.result.responses[1].body.username, user1Username);
                            
                            //Reset them so they will get deleted
                            this.users[0].originalId = response.data.result.responses[0].body.username;
                            this.users[1].originalId = response.data.result.responses[1].body.username;
                        });                    
                    });
                });
            }).finally(() => {
                //Delete the file
                fs.unlinkSync(uploadFileName);
            });
        });
    });
    
});
