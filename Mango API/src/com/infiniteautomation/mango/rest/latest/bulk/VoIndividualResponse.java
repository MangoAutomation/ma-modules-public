/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.bulk;

/**
 * @author Jared Wiltshire
 */
public class VoIndividualResponse<B> extends RestExceptionIndividualResponse<VoAction, B> {
    String xid;

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }
}
