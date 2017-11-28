/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class XidTimeRangeQueryModel extends XidQueryInfoModel{

    protected ZonedDateTime from;
    protected ZonedDateTime to;
    protected Integer limit;
    protected boolean bookend; //Do we want virtual values at the to/from time if they don't already exist?
    protected PointValueTimeCacheControl useCache;

    public XidTimeRangeQueryModel() {

    }
    
    /**
     * @param xids
     * @param useRendered
     * @param dateTimeFormat
     * @param from
     * @param timezone
     * @param limit
     * @param useCache
     */
    public XidTimeRangeQueryModel(String[] xids, boolean useRendered, String dateTimeFormat,
            String timezone, ZonedDateTime from, ZonedDateTime to, Integer limit,
            boolean bookend, PointValueTimeCacheControl useCache) {
        super(xids, useRendered, dateTimeFormat, timezone);
        this.from = from;
        this.to = to;
        this.limit = limit;
        this.bookend = bookend;
        this.useCache = useCache;
    }
    
    /**
     * @return the from
     */
    public ZonedDateTime getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(ZonedDateTime from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public ZonedDateTime getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(ZonedDateTime to) {
        this.to = to;
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
     * @return the bookend
     */
    public boolean isBookend() {
        return bookend;
    }

    /**
     * @param bookend the bookend to set
     */
    public void setBookend(boolean bookend) {
        this.bookend = bookend;
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
     * Create a time range query info object for use in the query
     * @param host
     * @param port
     * @param multiplePointsPerArray
     * @param singleArray
     * @param rollup
     * @return
     * @throws ValidationFailedRestException
     */
    public ZonedDateTimeRangeQueryInfo createZonedDateTimeRangeQueryInfo(String host, int port, boolean multiplePointsPerArray,
            boolean singleArray) throws ValidationFailedRestException {
        return new ZonedDateTimeRangeQueryInfo(host, port, from, to, dateTimeFormat, timezone,
                RollupEnum.NONE, null, limit, bookend, useRendered, multiplePointsPerArray, singleArray, useCache);
    };
    
}
