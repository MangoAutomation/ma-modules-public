/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.emport;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeImportResult {

    private String xid;
    private int totalQueued;
    private int totalSkipped;
    private ProcessResult result;

    public PointValueTimeImportResult(String xid, int totalQueued, int totalSkipped, ProcessResult result) {
        super();
        this.xid = xid;
        this.totalQueued = totalQueued;
        this.totalSkipped = totalSkipped;
        this.result = result;
    }
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
    public int getTotalQueued() {
        return totalQueued;
    }
    public void setTotalQueued(int totalQueued) {
        this.totalQueued = totalQueued;
    }
    public int getTotalSkipped() {
        return totalSkipped;
    }
    public void setTotalSkipped(int totalSkipped) {
        this.totalSkipped = totalSkipped;
    }
    public ProcessResult getResult() {
        return result;
    }
    public void setResult(ProcessResult result) {
        this.result = result;
    } 
    
    
}
