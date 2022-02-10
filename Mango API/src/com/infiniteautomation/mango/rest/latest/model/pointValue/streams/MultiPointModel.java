/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class MultiPointModel {
    final long exactTimestamp;
    final Object formattedTimestamp;
    final Map<String, StreamPointValueTimeModel> pointValues = new LinkedHashMap<>();

    public MultiPointModel(long exactTimestamp, Object formattedTimestamp) {
        this.exactTimestamp = exactTimestamp;
        this.formattedTimestamp = formattedTimestamp;
    }

    public Object getTimestamp() {
        return formattedTimestamp;
    }

    @JsonAnyGetter
    public Map<String, StreamPointValueTimeModel> getPointValues() {
        return pointValues;
    }

    public void putPointValue(String xid, StreamPointValueTimeModel pointValue) {
        this.pointValues.put(xid, pointValue);
    }
}
