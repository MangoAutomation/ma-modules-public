/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class ValueModel {
    /**
     * Can hold a formatted timestamp (String) or an epoch ms (long)
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
