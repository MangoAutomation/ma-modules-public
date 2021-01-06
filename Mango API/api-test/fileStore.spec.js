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

const {createClient, login} = require('@infinite-automation/mango-module-tools/test-helper/testHelper');
const client = createClient();
const fs = require('fs');
const tmp = require('tmp');
const crypto = require('crypto');
const path = require('path');

describe('Test File Store endpoints', function() {
    before('Login', function() { return login.call(this, client); });
    this.timeout(5000);

    const checkContentDisposition = (response) => {
        const contentDisposition = response.headers['content-disposition'].split(/\s*;\s*/);
        assert.include(contentDisposition, type);
    };

    it('Uploads a random binary file to default store', () => {
        const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': 'application/octet-stream'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'application/octet-stream;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(response.headers['cache-control'], 'max-age=0');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Uploads a random .png file to default store, tests download=false', function() {
        const uploadFile = tmp.fileSync({postfix: '.png'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                },
                params: {
                    download: false
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'image/png;charset=utf-8');
                checkContentDisposition(response, 'inline');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Downloads an unknown file extension with application/octet-stream MIME type', function() {
        const uploadFile = tmp.fileSync({postfix: '.mango-test'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'application/octet-stream;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Downloads an unknown file extension with application/octet-stream MIME type even if Accept header is set', function() {
        const uploadFile = tmp.fileSync({postfix: '.mango-test'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': 'mango/test-mime'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'application/octet-stream;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Downloads the correct MIME type when using wildcard Accept header', function() {
        const uploadFile = tmp.fileSync({postfix: '.js'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'application/javascript;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Downloads the correct MIME type when using application/* Accept header', function() {
        const uploadFile = tmp.fileSync({postfix: '.css'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': 'application/*;q=0.5'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'text/css;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Returns 406 Not Acceptable when Accept header does not match the file\'s MIME type', function() {
        const uploadFile = tmp.fileSync({postfix: '.txt'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/node-client-test/debug',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/node-client-test/debug/${percentEncodedFilename}`,
                method: 'GET',
                headers: {
                    'Accept': 'application/javascript'
                },
                dataType: 'buffer'
            }).then(response => {
                throw new Error('Returned successful response', response.status);
            }, error => {
                assert.strictEqual(error.response.statusCode, 406);
            });
        });
    });

    it('Returns 404 Not Found when file is not found in file store', function() {
        return client.restRequest({
            path: '/rest/latest/file-stores/default/xyz.12345',
            method: 'GET',
            headers: {
                'Accept': '*/*'
            },
            dataType: 'buffer'
        }).then(response => {
            throw new Error('Returned successful response', response.status);
        }, error => {
            assert.strictEqual(error.response.statusCode, 404);
        });
    });

    it('Uploads and downloads files with UTF file-names', function() {
        const uploadFile = tmp.fileSync({prefix: '\u2665-', postfix: '.txt'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/utf',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/utf/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'text/plain;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Uploads and downloads files with spaces in the filename', function() {
        const uploadFile = tmp.fileSync({prefix: 'space ', postfix: '.txt'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'text/plain;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Can\'t create files below the store base path using ".."', function() {
    	const uploadFile = tmp.fileSync({prefix: 'evil', postfix: '.exe'});
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/../',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
        	throw new Error('Returned successful response', response.status);
        }, error => {
            uploadFile.removeCallback();
            assert.strictEqual(error.response.statusCode, 500);
        });
    });

    it('Can\'t get files below the store base path using ".."', function() {
        return client.restRequest({
            path: '/rest/latest/file-stores/default/../../LICENSE',
            method: 'GET',
            headers: {
                'Accept': '*/*'
            },
            dataType: 'buffer',
            params: {
            	download: false
            }
        }).then(response => {
            throw new Error('Returned successful response', response.status);
        }, error => {
            assert.strictEqual(error.response.statusCode, 500);
        });
    });

    // this is important as the MappingJackson2HttpMessageConverter will try (and fail) to serialize a JSON file resource if it runs first
    it('Can download a .json file', function() {
    	const uploadFile = tmp.fileSync({postfix: '.json'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': 'application/json'
                }
            }).then(response => {
                assert.strictEqual(response.headers['content-type'], 'application/json;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Won\'t overwrite existing files', function() {
    	const uploadFile = tmp.fileSync({postfix: 'noext'});
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            assert.strictEqual(response.data[0].filename, fileBaseName);

            return client.restRequest({
                path: '/rest/latest/file-stores/default/',
                method: 'POST',
                uploadFiles: [uploadFile.name]
            }).then(response => {
                uploadFile.removeCallback();
                assert.strictEqual(response.data[0].filename, `${fileBaseName}_001`);
            });
        });
    });

    it('Uploads multiple files at once', function() {
    	const uploadFile1 = tmp.fileSync();
    	const uploadFile2 = tmp.fileSync();
        const fileBaseName1 = path.basename(uploadFile1.name);
        const fileBaseName2 = path.basename(uploadFile2.name);
        const randomBytes1 = crypto.randomBytes(1024);
        const randomBytes2 = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile1.name, randomBytes1);
        fs.writeFileSync(uploadFile2.name, randomBytes2);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile1.name, uploadFile2.name]
        }).then(response => {
        	uploadFile1.removeCallback();
        	uploadFile2.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName1);
            assert.strictEqual(response.data[1].filename, fileBaseName2);
        });
    });

    it('Uploads a file to a folder with spaces and UTF characters', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        const folderName = 'love \u2665';
        const url = encodeURI(`/rest/latest/file-stores/default/${folderName}/`);

        return client.restRequest({
            path: url,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const filePath = url + encodeURI(response.data[0].filename);
            return client.restRequest({
                path: filePath,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });

    it('Can delete a file from the filestore', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);
        let percentEncodedFilename;

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now delete it
            percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${percentEncodedFilename}`,
                method: 'DELETE'
            });
        }).then(response => {
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
            	throw new Error('Returned successful response', response.status);
            }, error => {
            	assert.strictEqual(error.response.statusCode, 404);
            });
        });
    });

    it('Can recursively delete a folder from the filestore', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        const dirName = path.basename(tmp.tmpNameSync({prefix: 'd', postfix: 'd'}));
        const url = `/rest/latest/file-stores/default/${dirName}/`;

        return client.restRequest({
            path: url,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            return client.restRequest({
                path: url,
                method: 'DELETE',
                params: {
                	recursive: true
                }
            });
        }).then(response => {
            return client.restRequest({
                path: url,
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            }).then(response => {
            	throw new Error('Returned successful response', response.status);
            }, error => {
            	assert.strictEqual(error.response.statusCode, 404);
            });
        });
    });

    it('Won\'t delete a folder if it has files in it without the recursive option', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        const dirName = path.basename(tmp.tmpNameSync({prefix: 'd', postfix: 'd'}));
        const url = `/rest/latest/file-stores/default/${dirName}/`;

        return client.restRequest({
            path: url,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            return client.restRequest({
                path: url,
                method: 'DELETE'
            });
        }).then(response => {
        	throw new Error('Returned successful response', response.status);
        }, error => {
        	assert.strictEqual(error.response.statusCode, 500);
        });
    });

    it('Won\'t allow uploading large files', function() {
        this.timeout(60000);
        const uploadFile = tmp.fileSync();

        return client.restRequest({
            path: '/rest/latest/testing/upload-limit',
            method: 'GET'
        }).then(response => {
            const uploadLimit = response.data;

            const randomBytes = crypto.randomBytes(uploadLimit + 1);
            fs.writeFileSync(uploadFile.name, randomBytes);

            return client.restRequest({
                path: '/rest/latest/file-stores/default/',
                method: 'POST',
                uploadFiles: [uploadFile.name]
            });
        }).then(response => {
            uploadFile.removeCallback();
            throw new Error('Returned successful response', response.status);
        }, error => {
            uploadFile.removeCallback();
        	assert.strictEqual(error.response.statusCode, 500);
        });
    });

    it('Can create folders', () => {
    	const dirName = path.basename(tmp.tmpNameSync({prefix: 'd', postfix: 'd'}));
        return client.restRequest({
            path: '/rest/latest/file-stores/default/' + dirName,
            method: 'POST'
        }).then(response => {
        	assert.strictEqual(response.data.filename, dirName);
        	assert.isTrue(response.data.directory);
        });
    });

    it('Can overwrite files', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        let randomBytes = crypto.randomBytes(1024);
        fs.writeFileSync(uploadFile.name, randomBytes);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
            assert.strictEqual(response.data[0].filename, fileBaseName);

            randomBytes = crypto.randomBytes(2048);
            fs.writeFileSync(uploadFile.name, randomBytes);

            return client.restRequest({
                path: '/rest/latest/file-stores/default/',
                method: 'POST',
                uploadFiles: [uploadFile.name],
                params: {
                	overwrite: true
                }
            });
        }).then(response => {
        	uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            }).then(response => {
                assert.strictEqual(Buffer.compare(randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        }).then(null, error => {
        	uploadFile.removeCallback();
        	throw error;
        });
    });

    it('Can create empty files', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            assert.strictEqual(response.data[0].filename, fileBaseName);

            // file uploaded OK, now download it and compare
            const percentEncodedFilename = encodeURI(response.data[0].filename);
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${percentEncodedFilename}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
            assert.strictEqual(response.headers['content-length'], '0');
        });
    });

    it('Can move files', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);

        return client.restRequest({
            path: '/rest/latest/file-stores/default/movefiles/',
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/movefiles/${fileBaseName}`,
                method: 'POST',
                params: {
                	moveTo: '..'
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileBaseName}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        });
    });

    it('Can move folders', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const dirName = path.basename(tmp.tmpNameSync({prefix: 'd', postfix: 'd'}));

        return client.restRequest({
            path: `/rest/latest/file-stores/default/movefiles/${dirName}/`,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/movefiles/${dirName}`,
                method: 'POST',
                params: {
                	moveTo: '..'
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	return client.restRequest({
                path: `/rest/latest/file-stores/default/${dirName}/${fileBaseName}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        });
    });

    it('Can rename files', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const fileName2 = path.basename(tmp.tmpNameSync());

        return client.restRequest({
            path: `/rest/latest/file-stores/default/`,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileBaseName}`,
                method: 'POST',
                params: {
                	moveTo: fileName2
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileName2}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        });
    });

    it('Can rename folders', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const dirName = path.basename(tmp.tmpNameSync({prefix: 'd', postfix: 'd'}));
        const dirName2 = path.basename(tmp.tmpNameSync({prefix: 'd', postfix: 'd'}));

        return client.restRequest({
            path: `/rest/latest/file-stores/default/movefiles/${dirName}/`,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/movefiles/${dirName}`,
                method: 'POST',
                params: {
                	moveTo: dirName2
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	return client.restRequest({
                path: `/rest/latest/file-stores/default/movefiles/${dirName2}/${fileBaseName}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        });
    });

    it('Can parse Unicode moveTo parameters correctly', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const fileName2 = path.basename(tmp.tmpNameSync({prefix: '\u2665-', postfix: '.txt'}));
        const fileName2Encoded = encodeURIComponent(fileName2);

        return client.restRequest({
            path: `/rest/latest/file-stores/default/`,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileBaseName}`,
                method: 'POST',
                params: {
                	moveTo: fileName2
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileName2Encoded}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        });
    });

    it('Can parse moveTo parameters with a space correctly', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);
        const fileName2 = path.basename(tmp.tmpNameSync({prefix: 'test ', postfix: '.txt'}));
        const fileName2Encoded = encodeURIComponent(fileName2);

        return client.restRequest({
            path: `/rest/latest/file-stores/default/`,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileBaseName}`,
                method: 'POST',
                params: {
                	moveTo: fileName2
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileName2Encoded}`,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*'
                }
            });
        });
    });

    it('Can\'t move a file out of the file store root', function() {
    	const uploadFile = tmp.fileSync();
        const fileBaseName = path.basename(uploadFile.name);

        return client.restRequest({
            path: `/rest/latest/file-stores/default/`,
            method: 'POST',
            uploadFiles: [uploadFile.name]
        }).then(response => {
        	uploadFile.removeCallback();
            return client.restRequest({
                path: `/rest/latest/file-stores/default/${fileBaseName}`,
                method: 'POST',
                params: {
                	moveTo: encodeURIComponent('..')
                }
            });
        }, error => {
        	uploadFile.removeCallback();
        	throw error;
        }).then(response => {
        	throw new Error('Returned successful response ' + response.status);
        }, error => {
        	assert.strictEqual(error.response.statusCode, 500);
        });
    });

    it('Can\'t move the file store root', function() {
        return client.restRequest({
            path: `/rest/latest/file-stores/default`,
            method: 'POST',
            params: {
                moveTo: 'moved_default'
            }
        }).then(response => {
            throw new Error('Returned successful response ' + response.status);
        }, error => {
            assert.strictEqual(error.response.statusCode, 500);
        });
    });

    it('Can\'t delete the file store root', function() {
        return client.restRequest({
            path: `/rest/latest/file-stores/default`,
            method: 'DELETE',
            params: {
                recursive: true
            }
        }).then(response => {
            assert.fail('Returned successful response ' + response.status);
        }, error => {
            assert.strictEqual(error.response.statusCode, 500);
        });
    });

    describe('with partial range requests', function() {
        beforeEach('upload a random .mp3 file', function() {
            const uploadFile = tmp.fileSync({postfix: '.mp3'});
            this.randomBytes = crypto.randomBytes(1024);
            fs.writeFileSync(uploadFile.name, this.randomBytes);

            this.fileName = path.basename(uploadFile.name);
            this.encodedFileName = encodeURI(this.fileName);
            this.path = `/rest/latest/file-stores/default/node-client-test/debug/${this.encodedFileName}`;

            return client.restRequest({
                path: '/rest/latest/file-stores/default/node-client-test/debug',
                method: 'POST',
                uploadFiles: [uploadFile.name]
            }).then(response => {
                uploadFile.removeCallback();
                assert.strictEqual(response.data[0].filename, this.fileName);
            });
        });

        afterEach('cleanup file', function() {
            return client.restRequest({
                path: this.path,
                method: 'DELETE'
            }).then(null, error => null);
        });

        it('for bytes=0-', function() {
            return client.restRequest({
                path: this.path,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*',
                    'Range': 'bytes=0-'
                }
            }).then(response => {
                assert.strictEqual(response.status, 206); // partial
                assert.strictEqual(response.headers['content-type'], 'audio/mpeg;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(response.headers['content-range'], 'bytes 0-1023/1024');
                assert.strictEqual(response.headers['content-length'], '1024');
                assert.strictEqual(Buffer.compare(this.randomBytes, response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });

        it('for bytes=0-100', function() {
            return client.restRequest({
                path: this.path,
                method: 'GET',
                dataType: 'buffer',
                headers: {
                    'Accept': '*/*',
                    'Range': 'bytes=0-99'
                }
            }).then(response => {
                assert.strictEqual(response.status, 206); // partial
                assert.strictEqual(response.headers['content-type'], 'audio/mpeg;charset=utf-8');
                checkContentDisposition(response, 'attachment');
                assert.strictEqual(response.headers['content-range'], 'bytes 0-99/1024');
                assert.strictEqual(response.headers['content-length'], '100');
                assert.strictEqual(Buffer.compare(this.randomBytes.slice(0, 100), response.data), 0,
                    'downloaded file does not match the uploaded file');
            });
        });
    });
});
