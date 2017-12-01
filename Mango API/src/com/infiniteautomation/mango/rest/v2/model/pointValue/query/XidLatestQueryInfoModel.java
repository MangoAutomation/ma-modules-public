/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;

/**
 *
 * @author Terry Packer
 */
public class XidLatestQueryInfoModel extends XidQueryInfoModel{

    protected ZonedDateTime before;
    protected Integer limit;
    protected PointValueTimeCacheControl useCache;
    
    public XidLatestQueryInfoModel() {
        
    }
    
    public XidLatestQueryInfoModel(String[] xids, boolean useRendered, String dateTimeFormat, String timezone,
            ZonedDateTime before, Integer limit,
            PointValueTimeCacheControl useCache, Double simplifyTolerance, Integer simplifyTarget) {
        super(xids, useRendered, dateTimeFormat, timezone, simplifyTolerance, simplifyTarget);
        this.before = before;
        this.limit = limit;
        this.useCache = useCache;
    }
    /**
     * @return the before
     */
    public ZonedDateTime getBefore() {
        return before;
    }
    /**
     * @param before the before to set
     */
    public void setBefore(ZonedDateTime before) {
        this.before = before;
    }
    /**
     * @return the limit
     */
    public Integer getLimit() {
        return limit;
    }
    /**
     * @param limit the limit to set
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    /**
     * @return the useCache
     */
    public PointValueTimeCacheControl getUseCache() {
        return useCache;
    }
    /**
     * @param useCache the useCache to set
     */
    public void setUseCache(PointValueTimeCacheControl useCache) {
        this.useCache = useCache;
    }
    
    /**
     * 
     * @param host
     * @param port
     * @param multiplePoints
     * @param singleArray
     * @return
     * @throws ValidationFailedRestException
     */
    public  LatestQueryInfo createLatestQueryInfo(String host, int port, boolean multiplePointsPerArray, boolean singleArray) throws ValidationFailedRestException {
        return new LatestQueryInfo(host, port, before, dateTimeFormat, timezone, limit, useRendered, multiplePointsPerArray, singleArray, useCache, simplifyTolerance, simplifyTarget);
    }
}
