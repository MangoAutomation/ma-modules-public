/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.query;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;

/**
 *
 * @author Terry Packer
 */
public abstract class XidQueryInfoModel {

    protected String[] xids;
    protected String dateTimeFormat;
    protected String timezone;
    protected Integer limit;
    protected Double simplifyTolerance;
    protected Integer simplifyTarget;
    protected PointValueField[] fields;
    
    public XidQueryInfoModel() { }

    /**
     */
    public XidQueryInfoModel(String[] xids, String dateTimeFormat,
            String timezone, Integer limit, Double simplifyTolerance, Integer simplifyTarget, 
            PointValueField[] fields) {
        super();
        this.xids = xids;
        this.dateTimeFormat = dateTimeFormat;
        this.timezone = timezone;
        this.limit = limit;
        this.simplifyTolerance = simplifyTolerance;
        this.simplifyTarget = simplifyTarget;
        this.fields = fields;
    }
    /**
     * @return the xids
     */
    public String[] getXids() {
        return xids;
    }
    /**
     * @param xids the xids to set
     */
    public void setXids(String[] xids) {
        this.xids = xids;
    }

    /**
     * @return the dateTimeFormat
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }
    /**
     * @param dateTimeFormat the dateTimeFormat to set
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }
    /**
     * @return the timezone
     */
    public String getTimezone() {
        return timezone;
    }
    /**
     * @param timezone the timezone to set
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    /**
     * @return the limit
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    /**
     * @return the simplifyTolerance
     */
    public Double getSimplifyTolerance() {
        return simplifyTolerance;
    }
    /**
     * @param simplifyTolerance the simplifyTolerance to set
     */
    public void setSimplifyTolerance(Double simplifyTolerance) {
        this.simplifyTolerance = simplifyTolerance;
    }
    /**
     * @return the simplifyTarget
     */
    public Integer getSimplifyTarget() {
        return simplifyTarget;
    }
    /**
     * @param simplifyTarget the simplifyTarget to set
     */
    public void setSimplifyTarget(Integer simplifyTarget) {
        this.simplifyTarget = simplifyTarget;
    }
    /**
     * @return the extraFields
     */
    public PointValueField[] getFields() {
        return fields;
    }
    /**
     * @param fields the extraFields to set
     */
    public void setFields(PointValueField[] fields) {
        this.fields = fields;
    }
    
}
