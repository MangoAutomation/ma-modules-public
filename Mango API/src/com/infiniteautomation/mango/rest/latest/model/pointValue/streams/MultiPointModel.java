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
    private final Object timestamp;

    private final Map<String, StreamPointValueTimeModel> pointValues = new LinkedHashMap<>();

    public MultiPointModel(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    @JsonAnyGetter
    public Map<String, StreamPointValueTimeModel> getPointValues() {
        return pointValues;
    }

    public void putPointValue(String xid, StreamPointValueTimeModel pointValue) {
        this.pointValues.put(xid, pointValue);
    }
}
