/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.util.Date;

import com.infiniteautomation.mango.rest.latest.model.time.TimePeriod;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public abstract class AbstractPurgeValuesModel {

    protected boolean purgeAll;
    protected TimePeriod duration;
    protected boolean useTimeRange;
    protected TimeRange timeRange;
    
    protected Long expiry;
    protected Long timeout;

    
    public void ensureValid() throws ValidationException {
        ProcessResult result = new ProcessResult();
        if(useTimeRange) {
            if(timeRange == null)
                result.addContextualMessage("timeRange", "validate.required");
            else {
                if(timeRange.getFrom() == null)
                    result.addContextualMessage("timeRange.from", "validate.required");
                if(timeRange.getTo() == null)
                    result.addContextualMessage("timeRange.to", "validate.required");
                if(timeRange.getFrom() != null && timeRange.getTo() != null)
                    if(timeRange.getFrom().after(timeRange.getTo()))
                        result.addContextualMessage("timeRange", "rest.validate.timeRange.invalid");
            }
        }else {
            if(duration == null)
                result.addContextualMessage("duration", "validate.required");
        }
        
        validateImpl(result);
        
        result.ensureValid();
    }
    
    /**
     *
     */
    protected abstract void validateImpl(ProcessResult result);

    /**
     * @return the purgeAll
     */
    public boolean isPurgeAll() {
        return purgeAll;
    }


    /**
     * @param purgeAll the purgeAll to set
     */
    public void setPurgeAll(boolean purgeAll) {
        this.purgeAll = purgeAll;
    }


    /**
     * @return the duration
     */
    public TimePeriod getDuration() {
        return duration;
    }


    /**
     * @param duration the duration to set
     */
    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }


    /**
     * @return the useTimeRange
     */
    public boolean isUseTimeRange() {
        return useTimeRange;
    }


    /**
     * @param useTimeRange the useTimeRange to set
     */
    public void setUseTimeRange(boolean useTimeRange) {
        this.useTimeRange = useTimeRange;
    }


    /**
     * @return the timeRange
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }


    /**
     * @param timeRange the timeRange to set
     */
    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }


    /**
     * @return the expiry
     */
    public Long getExpiry() {
        return expiry;
    }


    /**
     * @param expiry the expiry to set
     */
    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }


    /**
     * @return the timeout
     */
    public Long getTimeout() {
        return timeout;
    }


    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }


    public static class TimeRange {
        private Date from;
        private Date to;
        /**
         * @return the from
         */
        public Date getFrom() {
            return from;
        }
        /**
         * @param from the from to set
         */
        public void setFrom(Date from) {
            this.from = from;
        }
        /**
         * @return the to
         */
        public Date getTo() {
            return to;
        }
        /**
         * @param to the to to set
         */
        public void setTo(Date to) {
            this.to = to;
        }
        
    }
    
}
