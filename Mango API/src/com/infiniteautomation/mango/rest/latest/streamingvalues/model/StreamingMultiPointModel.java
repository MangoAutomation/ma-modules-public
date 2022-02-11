/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class StreamingMultiPointModel {
    final long exactTimestamp;
    final Object formattedTimestamp;
    final Map<String, StreamingPointValueTimeModel> pointValues = new LinkedHashMap<>();

    public StreamingMultiPointModel(long exactTimestamp, Object formattedTimestamp) {
        this.exactTimestamp = exactTimestamp;
        this.formattedTimestamp = formattedTimestamp;
    }

    public Object getTimestamp() {
        return formattedTimestamp;
    }

    @JsonIgnore
    public long getExactTimestamp() {
        return exactTimestamp;
    }

    @JsonAnyGetter
    public Map<String, StreamingPointValueTimeModel> getPointValues() {
        return pointValues;
    }

    public void addPointValue(String xid, StreamingPointValueTimeModel pointValue) {
        this.pointValues.put(xid, pointValue);
    }
}
