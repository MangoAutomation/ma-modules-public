/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
*/

MbusDataSourceFactory.$inject = ['maTemporaryRestResource', '$http'];
function MbusDataSourceFactory(TemporaryRestResource, $http) {
    
    const baseUrl = '/rest/v2/mbus-data-sources';

    class MbusDataSourceResource extends TemporaryRestResource {

        static get baseUrl() {
            return baseUrl;
        }
        
        static getSubscription() {
            const subscription = {
                sequenceNumber: 0,
                requestType: 'SUBSCRIPTION',
                messageType: 'REQUEST',
                showResultWhenIncomplete: true,
                showResultWhenComplete: true,
                anyStatus: true,
                resourceTypes: ['MBUS']
            };
            if (this.resourceType) {
                subscription.resourceTypes.push(this.resourceType);
            }
            return subscription;
        }

        static getScans() {
            let url, method;
            url = `${baseUrl}/scan`;
            method = 'GET';
            
            return $http({
                url,
                method,
            }).then(response => {
                return response.data;
            });
        }

        scan(data) {
            let url, method;
            url = `${baseUrl}/scan`;
            method = 'POST';
            
            return $http({
                url,
                method,
                data: data
            }).then(response => {
                return response.data;
            });
        }

        reset() {
            this.action = 'RESET';

            return this.test();
        }

        cancel(id) {
            return $http({
                url: baseUrl + '/scan/' + id,
                method: 'POST',
                data: {
                    status: 'CANCELLED'
                },
                params: {
                    remove: true
                }
            }).then(response => {
                return response.data;
            });
        }

    }
    
    return MbusDataSourceResource;
}

export default MbusDataSourceFactory;