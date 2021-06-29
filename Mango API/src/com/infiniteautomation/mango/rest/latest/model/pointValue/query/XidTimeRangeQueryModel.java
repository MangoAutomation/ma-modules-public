/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class XidTimeRangeQueryModel extends XidQueryInfoModel{

    protected ZonedDateTime from;
    protected ZonedDateTime to;
    protected boolean bookend; //Do we want virtual values at the to/from time if they don't already exist?
    protected PointValueTimeCacheControl useCache;

    public XidTimeRangeQueryModel() {

    }

    public XidTimeRangeQueryModel(String[] xids, String dateTimeFormat,
            String timezone, ZonedDateTime from, ZonedDateTime to, Integer limit,
            boolean bookend, PointValueTimeCacheControl useCache, Double simplifyTolerance,
            Integer simplifyTarget, PointValueField[] fields) {
        super(xids, dateTimeFormat, timezone, limit,
                simplifyTolerance, simplifyTarget, fields);
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
     * @param multiplePointsPerArray
     * @param singleArray
     * @param rollup
     * @return
     * @throws ValidationFailedRestException
     */
    public ZonedDateTimeRangeQueryInfo createZonedDateTimeRangeQueryInfo(boolean multiplePointsPerArray,
            boolean singleArray) throws ValidationFailedRestException {
        return new ZonedDateTimeRangeQueryInfo(from, to, dateTimeFormat, timezone,
                RollupEnum.NONE, null, limit, bookend, multiplePointsPerArray,
                singleArray, useCache, simplifyTolerance, simplifyTarget, false, fields);
    };

}
