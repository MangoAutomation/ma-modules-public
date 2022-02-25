/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class StreamingPointValueTimeModel {

    final String dataPointXid;
    final long exactTimestamp;

    @JsonUnwrapped
    ValueModel value;

    @JsonUnwrapped
    AllStatisticsModel allStatistics;

    TranslatableMessage annotation;
    Boolean cached;
    Boolean bookend;
    String xid;
    String name;
    String deviceName;
    String dataSourceName;

    /**
     * @param dataPointXid required for grouping by timestamp
     * @param exactTimestamp required for grouping by timestamp
     */
    public StreamingPointValueTimeModel(String dataPointXid, long exactTimestamp) {
        this.dataPointXid = Objects.requireNonNull(dataPointXid);
        this.exactTimestamp = exactTimestamp;
    }

    /**
     * Not part of the model.
     */
    @JsonIgnore
    public String getDataPointXid() {
        return dataPointXid;
    }

    /**
     * Not part of the model.
     */
    @JsonIgnore
    public long getExactTimestamp() {
        return exactTimestamp;
    }

    public ValueModel getValue() {
        return value;
    }

    public void setValue(ValueModel value) {
        this.value = value;
    }

    public AllStatisticsModel getAllStatistics() {
        return allStatistics;
    }

    public void setAllStatistics(AllStatisticsModel allStatistics) {
        this.allStatistics = allStatistics;
    }

    public TranslatableMessage getAnnotation() {
        return annotation;
    }

    public void setAnnotation(TranslatableMessage annotation) {
        this.annotation = annotation;
    }

    public Boolean getCached() {
        return cached;
    }

    public void setCached(Boolean cached) {
        this.cached = cached;
    }

    public Boolean getBookend() {
        return bookend;
    }

    public void setBookend(Boolean bookend) {
        this.bookend = bookend;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
}
