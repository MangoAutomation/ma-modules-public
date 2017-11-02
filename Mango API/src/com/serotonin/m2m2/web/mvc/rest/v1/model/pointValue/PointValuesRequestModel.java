package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

public class PointValuesRequestModel {
    String[] xids;
    boolean useRendered = false;
    boolean unitConversion = false;
    Integer limit;
    boolean useCache = true;
    
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
}
