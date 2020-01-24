/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.PollingDataSourceDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import net.sf.mbus4j.MBusUtils;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.TcpIpConnection;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;
import net.sf.mbus4j.dataframes.datablocks.vif.VifFB;
import net.sf.mbus4j.dataframes.datablocks.vif.VifFD;
import net.sf.mbus4j.dataframes.datablocks.vif.VifPrimary;
import net.sf.mbus4j.dataframes.datablocks.vif.VifTypes;

public class MBusDataSourceDefinition extends PollingDataSourceDefinition<MBusDataSourceVO> {

    public static final String DATA_SOURCE_TYPE = "MBUS";

    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.mbus";
    }

    @Override
    protected MBusDataSourceVO createDataSourceVO() {
        return new MBusDataSourceVO();
    }

    @Override
    public void validate(ProcessResult response, MBusDataSourceVO vo, PermissionHolder holder) {
        super.validate(response, vo, holder);
        if (vo.getConnection() == null) {
            response.addContextualMessage("connection", "validate.required");
        }else{
            //Validate the connections pieces
            if (vo.getConnection() instanceof TcpIpConnection) {
                TcpIpConnection cnxn =  ((TcpIpConnection) vo.getConnection());
                if(StringUtils.isEmpty(cnxn.getHost()))
                    response.addContextualMessage("ipAddressOrHostname", "validate.required");
                if(cnxn.getPort() < 1)
                    response.addContextualMessage("tcpPort", "validate.greaterThanZero");
            } else if (vo.getConnection() instanceof SerialPortConnection) {
                SerialPortConnection cnxn = ((SerialPortConnection) vo.getConnection());
                if(StringUtils.isEmpty(cnxn.getPortName())){
                    response.addContextualMessage("commPortId", "validate.required");
                }
            }
        }
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO dsvo,
            PermissionHolder user) {
        if (!(dsvo instanceof MBusDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");
        MBusPointLocatorVO pl = dpvo.getPointLocator();

        switch (pl.getAddressing()) {
            case PRIMARY:
                if ((pl.getAddress() & 0xFF) > (MBusUtils.LAST_REGULAR_PRIMARY_ADDRESS & 0xFF)) {
                    response.addContextualMessage("address", "validate.invalidValue");
                }
                break;
            case SECONDARY:
                if ((pl.getAddress() == MBusUtils.BROADCAST_NO_ANSWER_PRIMARY_ADDRESS)
                        || (pl.getAddress() == MBusUtils.BROADCAST_WITH_ANSWER_PRIMARY_ADDRESS)) {
                    response.addContextualMessage("address", "validate.invalidValue");
                }
                break;
        }

        if (pl.getSubUnit() < 0) {
            response.addContextualMessage("subUnit", "validate.required");
        }

        if (pl.getTariff() < 0) {
            response.addContextualMessage("tariff", "validate.required");
        }

        if (pl.getStorageNumber() < 0) {
            response.addContextualMessage("storageNumber", "validate.required");
        }

        try{
            VifTypes t = VifTypes.fromLabel(pl.getVifType());
            switch(t) {
                case PRIMARY:
                    try{
                        VifPrimary.assemble(pl.getVifLabel(), pl.unitOfMeasurement(), pl.siPrefix(), pl.getExponent());
                    }catch(Exception e) {
                        response.addContextualMessage("vifLabel", "validate.invalidValue");
                    }
                    break;
                case FB_EXTENTION:
                    try{
                        VifFB.assemble(pl.getVifLabel(), pl.unitOfMeasurement(), pl.siPrefix(), pl.getExponent());
                    }catch(Exception e) {
                        response.addContextualMessage("vifLabel", "validate.invalidValue");
                    }
                    break;
                case FD_EXTENTION:
                    try{
                        VifFD.assemble(pl.getVifLabel(), pl.unitOfMeasurement(), pl.siPrefix(), pl.getExponent());
                    }catch(Exception e) {
                        response.addContextualMessage("vifLabel", "validate.invalidValue");
                    }
                    break;
                case ASCII:
                case MANUFACTURER_SPECIFIC:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown vifType: " + t);
            }


        }catch(Exception e) {
            response.addContextualMessage("vifType", "validate.invalidValue");
        }

        if (pl.getVifeLabels().length > 0) {
            if (pl.getVifeLabels().length != pl.getVifeTypes().length) {
                response.addContextualMessage("vifeLabels", "mbus.validate.vifeLengthsInvalid");
                response.addContextualMessage("vifeTypes", "mbus.validate.vifeLengthsInvalid");
            }
            for (int i = 0; i < pl.getVifeLabels().length; i++) {
                try {
                    DataBlock.getVife(pl.getVifeTypes()[i], pl.getVifeLabels()[i]);
                } catch (IllegalArgumentException ex) {
                    response.addContextualMessage("vifeTypes[" + i + "]", "validate.invalidValue");
                    response.addContextualMessage("vifeLabels[" + i + "]", "validate.invalidValue");
                }
            }
        }
        if ((pl.getResponseFrame() == null) || (pl.getResponseFrame().length() == 0)) {
            response.addContextualMessage("responseFrame", "validate.required");
        }
        if (((pl.getVersion() & 0xFF) < 0) || ((pl.getVersion() & 0xFF) > 0xFF)) {
            response.addContextualMessage("version", "validate.required");
        }
        if (pl.getIdentNumber() < 0) {
            response.addContextualMessage("identNumber", "validate.required");
        }
        if ((pl.getManufacturer() == null) || (pl.getManufacturer().length() != 3)) {
            response.addContextualMessage("manufacturer", "validate.required");
        }

    }
}
