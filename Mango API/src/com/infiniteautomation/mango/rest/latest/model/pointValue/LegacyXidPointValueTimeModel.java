/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue;

/**
 * Left here for legacy purposes in the point value rest controller.
 *
 * To import values consider using the PointValueModificationRestController
 * with the XidPointValueTimeModel.  The difference being
 * the time format.
 *
 * @author Terry Packer
 */
@Deprecated
public class LegacyXidPointValueTimeModel extends LegacyPointValueTimeModel {

    private String xid;

    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
}
