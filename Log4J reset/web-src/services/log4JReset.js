/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

Log4JResetFactory.$inject = ['maRestResource'];
function Log4JResetFactory(RestResource) {
    
    const baseUrl = '/rest/v2/actions';
    const xidPrefix = 'LOG4JRST_';

    const defaultProperties = {
        action: 'TEST_DEBUG'
    };


    class Log4JResetResource extends RestResource {

        constructor(properties) {
            super(constructor);
        }

        static get defaultProperties() {
            return defaultProperties;
        }

        static get baseUrl() {
            return baseUrl;
        }
        
        static get xidPrefix() {
            return xidPrefix;
        }

        start(opts = {}) {
            let url, method;
            url = `${baseUrl}/trigger/log4JUtil`;
            method = 'PUT';
            
            return this.constructor.http({
                url,
                method,
                data: this,
            }, opts).then(response => {
                return response.data;
            });
        }

        getStatus(resourceId) {
            let url, method;
            url = `${baseUrl}/status/${resourceId}`;
            method = 'GET';
            
            return this.constructor.http({
                url,
                method,
                data: this,
            }).then(response => {
                console.log(response);
                return this;
            });
        }

        static getSubscription() {
            const subscription = {
                sequenceNumber: 0,
                messageType: 'REQUEST',
                requestType: 'SUBSCRIPTION',
                showResultWhenIncomplete: true,
                showResultWhenComplete: true,
                anyStatus: true,
                resourceTypes: []
            };
            if (this.resourceType) {
                subscription.resourceTypes.push(this.resourceType);
            }
            return subscription;
        }

        cancel(opts = {}) {
            const originalId = this.getOriginalId();
            
            return this.constructor.http({
                url: baseUrl + '/status/' + angular.$$encodeUriSegment(originalId),
                method: 'DELETE',
                data: {
                    status: 'CANCELLED'
                },
                params: opts.params
            }, opts).then(response => {
                this.itemUpdated(response.data);
                this.initialize('update');
                this.constructor.notify('update', this, originalId);
                return this;
            });
        }

    }
    
    return Log4JResetResource;
}

export default Log4JResetFactory;