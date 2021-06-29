/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;

/**
 *
 * @author Terry Packer
 */
public class XidStatisticsQueryInfoModel extends XidTimeRangeQueryModel {

    public XidStatisticsQueryInfoModel() {
        
    }
    
    public XidStatisticsQueryInfoModel(String[] xids, String dateTimeFormat,
            String timezone, ZonedDateTime from, ZonedDateTime to, Integer limit,
            boolean bookend, PointValueTimeCacheControl useCache, Double simplifyTolerance, 
            Integer simplifyTarget, PointValueField[] fields) {
        super(xids, dateTimeFormat, timezone, from, to, null, true,
                useCache, null, null, fields);
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.latest.model.pointValue.query.XidTimeRangeQueryModel#isBookend()
     */
    @Override
    public boolean isBookend() {
        return true;
    }
    
}
