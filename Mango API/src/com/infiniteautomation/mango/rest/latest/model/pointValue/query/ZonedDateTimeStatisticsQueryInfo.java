/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All
 *            rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;


/**
 *
 * @author Terry Packer
 */
public class ZonedDateTimeStatisticsQueryInfo extends ZonedDateTimeRangeQueryInfo {


    public ZonedDateTimeStatisticsQueryInfo(ZonedDateTime from, ZonedDateTime to,
            String dateTimeFormat, String timezone, PointValueTimeCacheControl useCache,
            PointValueField[] fields) {
        super(from, to, dateTimeFormat, timezone, RollupEnum.ALL, null, null, true, false, false, useCache,
                null, null, false, fields);
    }

}
