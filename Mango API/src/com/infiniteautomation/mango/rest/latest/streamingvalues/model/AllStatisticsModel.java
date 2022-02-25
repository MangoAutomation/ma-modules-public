/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Used for data points with ALPHANUMERIC data type.
 *
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class AllStatisticsModel {
    /**
     * Can hold a formatted timestamp (String) or an epoch ms (long)
     */
    Object timestamp;

    long count;
    ValueTimeModel first;
    ValueTimeModel last;
    ValueTimeModel start;

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public ValueTimeModel getFirst() {
        return first;
    }

    public void setFirst(ValueTimeModel first) {
        this.first = first;
    }

    public ValueTimeModel getLast() {
        return last;
    }

    public void setLast(ValueTimeModel last) {
        this.last = last;
    }

    public ValueTimeModel getStart() {
        return start;
    }

    public void setStart(ValueTimeModel start) {
        this.start = start;
    }

}
