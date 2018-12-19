/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

Log4JResetFactory.$inject = ['maRestResource', '$http', 'maTemporaryRestResource'];
function Log4JResetFactory(RestResource, $http, TemporaryRestResource) {
    
    const baseUrl = '/rest/v2/actions/trigger/log4JUtil';
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
                messageType: 'REQUEST',
                requestType: 'SUBSCRIPTION',
                showResultWhenIncomplete: true,
                showResultWhenComplete: true,
                anyStatus: true,
                resourceTypes: ['NO_SQL_DATA_TRANSFER']
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