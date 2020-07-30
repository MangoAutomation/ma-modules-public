/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.util.List;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class PurgeDataPointValuesModel extends AbstractPurgeValuesModel {

    private List<String> xids;
    private String dataSourceXid;

    @Override
    protected void validateImpl(ProcessResult result) {
        if((dataSourceXid == null || dataSourceXid.isEmpty()) && (xids == null || xids.isEmpty()))
            result.addContextualMessage("xids", "validate.required");
    }

    /**
     * @return the xids
     */
    public List<String> getXids() {
        return xids;
    }

    /**
     * @param xids the xids to set
     */
    public void setXids(List<String> xids) {
        this.xids = xids;
    }

    public String getDataSourceXid() {
        return dataSourceXid;
    }

    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
    }

}
