/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;

/**
 * <p>Model for a point value or an aggregated value (statistic) with a timestamp.</p>
 *
 * <p>Holds the value (can be a {@link com.serotonin.m2m2.rt.dataImage.types.DataValue}, or a primitive),
 * a raw value (i.e. the unconverted value for NUMERIC points), and the rendered value from the point's text renderer.</p>
 *
 * <p>The fields returned in the model can be specified via REST parameters, with the default being
 * {@link PointValueField#TIMESTAMP} and {@link PointValueField#VALUE}.</p>
 *
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class ValueTimeModel {
    /**
     * Can hold a formatted timestamp (String) or an epoch ms (long).
     */
    Object timestamp;
    Object value;
    Object raw;
    String rendered;

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }

    public String getRendered() {
        return rendered;
    }

    public void setRendered(String rendered) {
        this.rendered = rendered;
    }
}
