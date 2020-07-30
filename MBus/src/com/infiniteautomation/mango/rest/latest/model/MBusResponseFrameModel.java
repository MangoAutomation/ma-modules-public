/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.mbus4j.dataframes.Frame;
import net.sf.mbus4j.dataframes.UserDataResponse;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;
import net.sf.mbus4j.dataframes.datablocks.vif.VifAscii;
import net.sf.mbus4j.dataframes.datablocks.vif.VifPrimary;
import net.sf.mbus4j.dataframes.datablocks.vif.Vife;

/**
 * @author Terry Packer
 *
 */
public class MBusResponseFrameModel {

    private String name;
    private List<MBusDataBlockModel> dataBlocks;
    
    public MBusResponseFrameModel(String name, Frame frame) {
        this.name = name;
        if (frame instanceof UserDataResponse) {
            this.dataBlocks = new ArrayList<>();
            UserDataResponse rf = (UserDataResponse) frame;
            for (int i = 0; i < rf.getDataBlockCount(); i++) {
                this.dataBlocks.add(new MBusDataBlockModel(rf.getDataBlock(i)));
            }
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MBusDataBlockModel> getDataBlocks() {
        return dataBlocks;
    }

    public void setDataBlocks(List<MBusDataBlockModel> dataBlocks) {
        this.dataBlocks = dataBlocks;
    }

    public static class MBusDataBlockModel {

        private String name;
        private short subUnit;
        private String dataField;
        private String functionField;
        private long storageNumber;
        private int tariff;
        private String siPrefix;
        private String unitOfMeasurement;
        private String vifType;
        private String vifLabel;
        private Integer exponent;
        private Map<String,String> vifeTypes;
        
        private boolean highlight;
        private Map<String, String> parameters;
        private String value;

        
        public MBusDataBlockModel(DataBlock dataBlock) {
            this.name = dataBlock.getParamDescr();
            this.subUnit = dataBlock.getSubUnit();
            this.dataField = dataBlock.getDataFieldCode().getLabel();
            this.functionField = dataBlock.getFunctionField().getLabel();
            this.storageNumber = dataBlock.getStorageNumber();
            this.tariff = dataBlock.getTariff();
            this.siPrefix = dataBlock.getSiPrefix().getLabel();
            this.unitOfMeasurement = dataBlock.getUnitOfMeasurement() != null ? dataBlock.getUnitOfMeasurement().getLabel(): null;
            this.vifType = dataBlock.getVif().getVifType().getLabel();
            this.vifLabel = dataBlock.getVif().getLabel();
            this.exponent = dataBlock.getExponent();
            this.vifeTypes = new HashMap<>();
            if (dataBlock.getVifes() != null)
                for (Vife vife : dataBlock.getVifes())
                    this.vifeTypes.put(vife.getLabel(), vife.getVifeType().getLabel());
            
            this.highlight = (dataBlock.getStorageNumber() == 0)
                    && (dataBlock.getVif() != VifPrimary.FABRICATION_NO)
                    && (dataBlock.getVif() != VifPrimary.TIMEPOINT_DATE)
                    && (dataBlock.getVif() != VifPrimary.TIMEPOINT_TIME_AND_DATE)
                    && (!(dataBlock.getVif() instanceof VifAscii))
                    && (dataBlock.getVif() != VifPrimary.BUS_ADDRESS);
            
            this.parameters = new HashMap<>();
            String[] splitted = dataBlock.toString().split("\n");
            for (String line : splitted) {
                if (line.startsWith("value")) {
                    //skip
                } else {
                    String[] subLine = line.split(" = ");
                    parameters.put(subLine[0], subLine[1]);
                }
            }
            this.value = dataBlock.getValueAsString();
        }

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public boolean isHighlight() {
            return highlight;
        }
        
        public void setHighlight(boolean highlight) {
            this.highlight = highlight;
        }
        
        public Map<String, String> getParameters() {
            return parameters;
        }
        
        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }

        public short getSubUnit() {
            return subUnit;
        }

        public void setSubUnit(short subUnit) {
            this.subUnit = subUnit;
        }

        public String getDataField() {
            return dataField;
        }

        public void setDataField(String dataField) {
            this.dataField = dataField;
        }

        public String getFunctionField() {
            return functionField;
        }

        public void setFunctionField(String functionField) {
            this.functionField = functionField;
        }

        public long getStorageNumber() {
            return storageNumber;
        }

        public void setStorageNumber(long storageNumber) {
            this.storageNumber = storageNumber;
        }

        public int getTariff() {
            return tariff;
        }

        public void setTariff(int tariff) {
            this.tariff = tariff;
        }

        public String getSiPrefix() {
            return siPrefix;
        }

        public void setSiPrefix(String siPrefix) {
            this.siPrefix = siPrefix;
        }

        public String getUnitOfMeasurement() {
            return unitOfMeasurement;
        }

        public void setUnitOfMeasurement(String unitOfMeasurement) {
            this.unitOfMeasurement = unitOfMeasurement;
        }

        public String getVifType() {
            return vifType;
        }

        public void setVifType(String vifType) {
            this.vifType = vifType;
        }

        public String getVifLabel() {
            return vifLabel;
        }

        public void setVifLabel(String vifLabel) {
            this.vifLabel = vifLabel;
        }

        public Integer getExponent() {
            return exponent;
        }

        public void setExponent(Integer exponent) {
            this.exponent = exponent;
        }

        public Map<String, String> getVifeTypes() {
            return vifeTypes;
        }

        public void setVifeTypes(Map<String, String> vifeTypes) {
            this.vifeTypes = vifeTypes;
        }
    }
}
