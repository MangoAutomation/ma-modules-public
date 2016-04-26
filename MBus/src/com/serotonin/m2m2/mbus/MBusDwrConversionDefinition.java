/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.mbus.dwr.MBusDataBlockBean;
import com.serotonin.m2m2.mbus.dwr.MBusDeviceBean;
import com.serotonin.m2m2.mbus.dwr.MBusResponseFrameBean;
import com.serotonin.m2m2.module.DwrConversionDefinition;

public class MBusDwrConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(MBusConnectionType.class, "enum");
        addConversion(MBusDeviceBean.class);
        addConversion(MBusResponseFrameBean.class);
        addConversion(MBusDataBlockBean.class);
    }
}
