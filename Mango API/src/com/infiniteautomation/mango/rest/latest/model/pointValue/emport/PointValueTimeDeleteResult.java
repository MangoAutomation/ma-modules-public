/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.emport;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class PointValueTimeDeleteResult {

    private String xid;
    private int totalDeleted;
    private int totalSkipped;
    private ProcessResult result;

    public PointValueTimeDeleteResult(String xid, int totalDeleted, int totalSkipped, ProcessResult result) {
        this.xid = xid;
        this.totalDeleted = totalDeleted;
        this.totalSkipped = totalSkipped;
        this.result = result;
    }
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
    public int getTotalDeleted() {
        return totalDeleted;
    }
    public void setTotalDeleted(int totalDeleted) {
        this.totalDeleted = totalDeleted;
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
