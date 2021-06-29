/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;

/**
 *
 * @author Terry Packer
 */
public class XidLatestQueryInfoModel extends XidQueryInfoModel{

    protected ZonedDateTime before;
    protected PointValueTimeCacheControl useCache;
    
    public XidLatestQueryInfoModel() {
        
    }
    
    public XidLatestQueryInfoModel(String[] xids, String dateTimeFormat, String timezone,
            ZonedDateTime before, Integer limit,
            PointValueTimeCacheControl useCache, Double simplifyTolerance, Integer simplifyTarget,
            PointValueField[] extraFields) {
        super(xids, dateTimeFormat, timezone, limit, simplifyTolerance, simplifyTarget, extraFields);
        this.before = before;
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
     * @param multiplePoints
     * @param singleArray
     * @return
     * @throws ValidationFailedRestException
     */
    public  LatestQueryInfo createLatestQueryInfo(boolean multiplePointsPerArray, boolean singleArray) throws ValidationFailedRestException {
        return new LatestQueryInfo(before, dateTimeFormat, timezone, limit, multiplePointsPerArray, singleArray, 
                useCache, simplifyTolerance, simplifyTarget, fields);
    }
}
