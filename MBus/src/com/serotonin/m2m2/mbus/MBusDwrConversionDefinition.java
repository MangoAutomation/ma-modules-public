/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.mbus.dwr.MBusDataBlockBean;
import com.serotonin.m2m2.mbus.dwr.MBusDeviceBean;
import com.serotonin.m2m2.mbus.dwr.MBusResponseFrameBean;
import com.serotonin.m2m2.module.DwrConversionDefinition;
import net.sf.mbus4j.Connection;
import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.TcpIpConnection;
import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.dataframes.datablocks.vif.SiPrefix;

public class MBusDwrConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(MBusConnectionType.class, "enum");
        addConversion(MBusDeviceBean.class);
        addConversion(MBusResponseFrameBean.class);
        addConversion(MBusDataBlockBean.class);
        addConversion(PrimaryAddressingSearch.class);
        addConversion(SecondaryAddressingSearch.class);
        addConversion(MBusSearchByAddressing.class);
        addConversion(MBusMedium.class, "enum");
        addConversion(SiPrefix.class, "enum");
        addConversion(MBusAddressing.class, "enum");
        
        addConversionWithInclusions(Connection.class, "bitPerSecond,responseTimeOutOffset");
        addConversionWithInclusions(SerialPortConnection.class, "bitPerSecond,responseTimeOutOffset,portName");
        addConversionWithInclusions(TcpIpConnection.class, "bitPerSecond,responseTimeOutOffset,host,port");
    }

}
