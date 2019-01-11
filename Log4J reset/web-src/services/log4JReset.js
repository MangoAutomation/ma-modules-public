/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

Log4JResetFactory.$inject = ['maTemporaryRestResource', '$http'];
function Log4JResetFactory(TemporaryRestResource, $http) {
    
    const baseUrl = '/rest/v2/system-actions/log4JUtil';
    const xidPrefix = 'LOG4JRST_';

    const defaultProperties = {
        action: 'TEST_DEBUG'
    };

    class Log4JResetResource extends TemporaryRestResource {

        static get defaultProperties() {
            return defaultProperties;
        }

        static get baseUrl() {
            return baseUrl;
        }
        
        static get xidPrefix() {
            return xidPrefix;
        }

        static getSubscription() {
            const subscription = {
                sequenceNumber: 0,
                requestType: 'SUBSCRIPTION',
                messageType: 'REQUEST',
                showResultWhenIncomplete: true,
                showResultWhenComplete: true,
                anyStatus: true,
                resourceTypes: ['log4JUtil']
            };
            if (this.resourceType) {
                subscription.resourceTypes.push(this.resourceType);
            }
            return subscription;
        }

        test() {
            let url, method;
            url = `${baseUrl}`;
            method = 'POST';
            
            return $http({
                url,
                method,
                data: this
            }).then(response => {
                this.itemUpdated(response.data, null);
                if (this.constructor.notifyUpdateOnGet) {
                    this.constructor.notify('update', this, this.originalId);
                }
                return this;
            });
        }

        get() {
            let url = `/rest/v2/system-actions/status/${this.id}`;
            let method = 'GET';

            return $http({
                url,
                method
            }).then(response => {
                this.itemUpdated(response.data, null);
                if (this.constructor.notifyUpdateOnGet) {
                    this.constructor.notify('update', this, this.originalId);
                }
                return this;
            }); 
        }

        reset() {
            this.action = 'RESET';

            return this.test();
        }

        cancel(opts = {}) {
            const originalId = this.getOriginalId();
            
            return $http({
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