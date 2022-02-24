/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

/**
 * Information that can optionally be returned
 * in point value query results.
 * @author Terry Packer
 */
public enum PointValueField {

    VALUE("value"),
    TIMESTAMP("timestamp"),
    ANNOTATION("annotation"),
    CACHED("cached"),
    BOOKEND("bookend"),
    RENDERED("rendered"),
    RAW("raw"), //unconverted value if the point has a rendered unit

    XID("xid"),
    NAME("name"),
    DEVICE_NAME("deviceName"),
    DATA_SOURCE_NAME("dataSourceName");

    private final String fieldName;

    PointValueField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

}
