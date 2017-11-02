package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

public class PointValuesRequestModel {
    private String[] xids;
    private boolean useRendered = false;
    private boolean unitConversion = false;
    private int limit = 100;
    private boolean useCache = true;
    
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
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public boolean isUseCache() {
        return useCache;
    }
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
}
