/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.util.List;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class PurgeDataPointValuesModel extends AbstractPurgeValuesModel {
    
    private List<String> xids;
        
    @Override
    protected void validateImpl(ProcessResult result) {
        if(xids == null || xids.size() == 0)
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
    
}
