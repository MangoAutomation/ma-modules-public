/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

/**
 * @author Jared Wiltshire
 */
public class AllStatisticsModel {

    long count;
    ValueModel first;
    ValueModel last;
    ValueModel start;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public ValueModel getFirst() {
        return first;
    }

    public void setFirst(ValueModel first) {
        this.first = first;
    }

    public ValueModel getLast() {
        return last;
    }

    public void setLast(ValueModel last) {
        this.last = last;
    }

    public ValueModel getStart() {
        return start;
    }

    public void setStart(ValueModel start) {
        this.start = start;
    }

}
