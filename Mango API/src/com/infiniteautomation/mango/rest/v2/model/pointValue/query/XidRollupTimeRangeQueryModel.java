/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.v2.exception.ValidationFailedRestException;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;

/**
 *
 * @author Terry Packer
 */
public class XidRollupTimeRangeQueryModel extends XidQueryInfoModel{
    
    protected ZonedDateTime from;
    protected ZonedDateTime to;
    protected TimePeriod timePeriod;
   
    public XidRollupTimeRangeQueryModel() {

    }
    
    public XidRollupTimeRangeQueryModel(String[] xids, boolean useRendered, String dateTimeFormat,
            String timezone, ZonedDateTime from, ZonedDateTime to, TimePeriod timePeriod) {
        super(xids, useRendered, dateTimeFormat, timezone);
        this.from = from;
        this.to = to;
        this.timePeriod = timePeriod;
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
     * @return the timePeriod
     */
    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    /**
     * @param timePeriod the timePeriod to set
     */
    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    /**
     * 
     * @param host
     * @param port
     * @param multiplePointsPerArray
     * @param singleArray
     * @param rollup
     * @return
     * @throws ValidationFailedRestException
     */
    public ZonedDateTimeRangeQueryInfo createZonedDateTimeRangeQueryInfo(String host, int port, boolean multiplePointsPerArray,
            boolean singleArray, RollupEnum rollup) throws ValidationFailedRestException {
        return new ZonedDateTimeRangeQueryInfo(host, port, from, to, dateTimeFormat, timezone,
                rollup, timePeriod, null, true, useRendered, multiplePointsPerArray, singleArray, PointValueTimeCacheControl.NONE);
    };
}
