/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class PurgeDataSourceValuesModel extends AbstractPurgeValuesModel {

    private String xid;
    
    @Override
    protected void validateImpl(ProcessResult result) {
        if(StringUtils.isEmpty(xid))
            result.addContextualMessage("xid", "validate.required");
    }

    /**
     * @return the xid
     */
    public String getXid() {
        return xid;
    }
    /**
     * @param xid the xid to set
     */
    public void setXid(String xid) {
        this.xid = xid;
    }
}
