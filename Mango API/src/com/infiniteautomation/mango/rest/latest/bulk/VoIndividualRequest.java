/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.bulk;

/**
 * @author Jared Wiltshire
 */
public class VoIndividualRequest<B> extends IndividualRequest<VoAction, B> {
    private String xid;

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }
}
