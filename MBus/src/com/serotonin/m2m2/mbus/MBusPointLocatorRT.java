/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;
import net.sf.mbus4j.dataframes.datablocks.dif.DataFieldCode;
import net.sf.mbus4j.dataframes.datablocks.dif.FunctionField;
import net.sf.mbus4j.dataframes.datablocks.vif.Vife;
import net.sf.mbus4j.master.ValueRequestPointLocator;

import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class MBusPointLocatorRT extends PointLocatorRT {
    private final MBusPointLocatorVO vo;

    public MBusPointLocatorRT(MBusPointLocatorVO vo) {
        this.vo = vo;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    /**
     * @return the vo
     */
    public MBusPointLocatorVO getVo() {
        return vo;
    }

    public ValueRequestPointLocator<DataPointRT> createValueRequestPointLocator(DataPointRT point) {
        ValueRequestPointLocator<DataPointRT> result = new ValueRequestPointLocator<DataPointRT>();
        result.setAddressing(MBusAddressing.fromLabel(vo.getAddressing()));
        result.setAddress(vo.getAddress());
        result.setDeviceUnit(vo.getDeviceUnit());
        result.setDifCode(DataFieldCode.fromLabel(vo.getDifCode()));
        result.setFunctionField(FunctionField.fromLabel(vo.getFunctionField()));
        result.setIdentnumber(vo.getIdentNumber());
        result.setManufacturer(vo.getManufacturer());
        result.setMedium(MBusMedium.fromLabel(vo.getMedium()));
        result.setReference(point);
        result.setResponseFrameName(vo.getResponseFrame());
        result.setStorageNumber(vo.getStorageNumber());
        result.setTariff(vo.getTariff());
        result.setVersion(vo.getVersion());
        result.setVif(DataBlock.getVif(vo.getVifType(), vo.getVifLabel(), vo.getUnitOfMeasurement(), vo.getSiPrefix(),
                vo.getExponent()));
        if (vo.getVifeLabels().length == 0) {
            result.setVifes(DataBlock.EMPTY_VIFE);
        }
        else {
            Vife[] vifes = new Vife[vo.getVifeLabels().length];
            for (int i = 0; i < vo.getVifeLabels().length; i++) {
                vifes[i] = DataBlock.getVife(vo.getVifeTypes()[i], vo.getVifeLabels()[i]);
            }
            result.setVifes(vifes);
        }
        return result;
    }
}
