/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointField;

/**
 *
 * @author Terry Packer
 */
public abstract class XidQueryInfoModel {

    protected String[] xids;
    protected boolean useRendered;
    protected String dateTimeFormat;
    protected String timezone;
    protected Integer limit;
    protected Double simplifyTolerance;
    protected Integer simplifyTarget;
    protected DataPointField[] extraFields;
    
    /**
     * 
     */
    public XidQueryInfoModel() {
        // TODO Auto-generated constructor stub
    }
    /**
     * @param xids
     * @param useRendered
     * @param dateTimeFormat
     * @param timezone
     */
    public XidQueryInfoModel(String[] xids, boolean useRendered, String dateTimeFormat,
            String timezone, Integer limit, Double simplifyTolerance, Integer simplifyTarget, 
            DataPointField[] extraFields) {
        super();
        this.xids = xids;
        this.useRendered = useRendered;
        this.dateTimeFormat = dateTimeFormat;
        this.timezone = timezone;
        this.limit = limit;
        this.simplifyTolerance = simplifyTolerance;
        this.simplifyTarget = simplifyTarget;
        this.extraFields = extraFields;
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
     * @return the useRendered
     */
    public boolean isUseRendered() {
        return useRendered;
    }
    /**
     * @param useRendered the useRendered to set
     */
    public void setUseRendered(boolean useRendered) {
        this.useRendered = useRendered;
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
    public DataPointField[] getExtraFields() {
        return extraFields;
    }
    /**
     * @param extraFields the extraFields to set
     */
    public void setExtraFields(DataPointField[] extraFields) {
        this.extraFields = extraFields;
    }
    
}
