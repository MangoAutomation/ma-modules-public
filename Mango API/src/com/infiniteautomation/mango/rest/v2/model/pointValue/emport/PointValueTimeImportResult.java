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
    private int total;
    private ProcessResult result;

    public PointValueTimeImportResult(String xid, int total, ProcessResult result) {
        super();
        this.xid = xid;
        this.total = total;
        this.result = result;
    }
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public ProcessResult getResult() {
        return result;
    }
    public void setResult(ProcessResult result) {
        this.result = result;
    } 
    
    
}
