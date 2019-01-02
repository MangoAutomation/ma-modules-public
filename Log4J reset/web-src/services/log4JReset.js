/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

Log4JResetFactory.$inject = ['maTemporaryRestResource'];
function Log4JResetFactory(TemporaryRestResource) {
    
    const baseUrl = '/rest/v2/system-actions/log4JUtil';
    const xidPrefix = 'LOG4JRST_';

    const defaultProperties = {
        action: 'TEST_DEBUG'
    };

    class Log4JResetResource extends TemporaryRestResource {

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

        static getSubscription() {
            const subscription = {
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

        getStatus(resourceId) {
            let url, method;
            url = `${baseUrl}/status/${resourceId}`;
            method = 'GET';
            
            return this.constructor.http({
                url,
                method,
            }).then(response => {
                return this;
            });
        }

        get() {
            let url = `/rest/v2/system-actions/status/${this.id}`;
            let method = 'GET';

            return this.constructor.http({
                url,
                method
            }).then(response => {
                console.log(response);
            }); 
        }

        // save(opts = {}) {
        //     let url, method;
        //     url = `${baseUrl}/log4JUtil`;
        //     method = 'POST';
            
        //     return this.constructor.http({
        //         url,
        //         method,
        //         data: this,
        //     }).then(response => {
        //         console.log(response);
        //         this.itemUpdated(response.data, opts.responseType);
        //         this.initialize('create');
        //         this.constructor.notify('create', this, this.xid);
        //         return this;
        //     });
        // }

        reset() {
            this.action = 'RESET';

            return this.start();
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