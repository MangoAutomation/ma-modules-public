# Node.js Mango API Client

Mango Automation REST API client for Node.js written in ES6.

## Usage
**Run "npm install mango-client" first**

```
const MangoClient = require('@infinite-automation/mango-client');

const client = new MangoClient({
    protocol: 'https',
    host: 'localhost',
    port: 8443,
    rejectUnauthorized: false
});

const DataPoint = client.DataPoint;
const User = client.User;

User.login('admin', 'admin').then(data => {
    console.log(`Logged in as '${data.username}'.`);
    return DataPoint.getValue('internal_mango_num_data_points');
}).then(data => {
    console.log(`There are ${data.value} data points.`);

    // you can perform any arbitrary rest request like this
    return client.restRequest({
        path: '/rest/v1/data-points/internal_mango_num_data_points',
        method: 'GET',
        //data: {object}
    });
}).then(response => {
    console.log(`The data point's name is '${response.data.name}'`);
});
```
