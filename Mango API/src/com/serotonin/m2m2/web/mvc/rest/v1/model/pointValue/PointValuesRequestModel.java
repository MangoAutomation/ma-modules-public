/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

public class PointValuesRequestModel {
    
    protected String[] xids;
    protected boolean useRendered = false;
    protected boolean unitConversion = false;
    protected Integer limit;
    protected boolean useCache = true;
    protected String dateTimeFormat;
    protected String timezone;
    
    public PointValuesRequestModel() {
        limit = 100;
    }
    
    public String[] getXids() {
        return xids;
    }
    public void setXids(String[] xids) {
        this.xids = xids;
    }
    public boolean isUseRendered() {
        return useRendered;
    }
    public void setUseRendered(boolean useRendered) {
        this.useRendered = useRendered;
    }
    public boolean isUnitConversion() {
        return unitConversion;
    }
    public void setUnitConversion(boolean unitConversion) {
        this.unitConversion = unitConversion;
    }
    public Integer getLimit() {
        return limit;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    public boolean isUseCache() {
        return useCache;
    }
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
