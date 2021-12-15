/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import java.time.ZonedDateTime;

import com.infiniteautomation.mango.rest.latest.exception.ValidationFailedRestException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriod;


/**
 *
 * @author Terry Packer
 */
public class XidRollupTimeRangeQueryModel extends XidQueryInfoModel{

    protected ZonedDateTime from;
    protected ZonedDateTime to;
    protected TimePeriod timePeriod;
    protected boolean truncate;

    public XidRollupTimeRangeQueryModel() {

    }

    public XidRollupTimeRangeQueryModel(String[] xids, String dateTimeFormat,
            String timezone, Integer limit, ZonedDateTime from, ZonedDateTime to,
            TimePeriod timePeriod, boolean truncate, PointValueField[] fields) {
        super(xids, dateTimeFormat, timezone, limit, null, null, fields);
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
     * @return the truncate
     */
    public boolean isTruncate() {
        return truncate;
    }

    /**
     * @param truncate the truncate to set
     */
    public void setTruncate(boolean truncate) {
        this.truncate = truncate;
    }

    /**
     *
     */
    public ZonedDateTimeRangeQueryInfo createZonedDateTimeRangeQueryInfo(boolean multiplePointsPerArray,
            boolean singleArray, RollupEnum rollup) throws ValidationFailedRestException {
        return new ZonedDateTimeRangeQueryInfo(from, to, dateTimeFormat, timezone,
                rollup, timePeriod, limit, true, multiplePointsPerArray, singleArray,
                PointValueTimeCacheControl.NONE, null, null, truncate, fields);
    };
}
