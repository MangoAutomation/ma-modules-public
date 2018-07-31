/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All
 *            rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueField;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 *
 * @author Terry Packer
 */
public class ZonedDateTimeStatisticsQueryInfo extends ZonedDateTimeRangeQueryInfo {


    public ZonedDateTimeStatisticsQueryInfo(ZonedDateTime from, ZonedDateTime to,
            String dateTimeFormat, String timezone, PointValueTimeCacheControl useCache, 
            PointValueField[] fields) {
        super(from, to, dateTimeFormat, timezone, RollupEnum.NONE, null, null, true, false, false, useCache,
                null, null, false, fields);
    }

}
