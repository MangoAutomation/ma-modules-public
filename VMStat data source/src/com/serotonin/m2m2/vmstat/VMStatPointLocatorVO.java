/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vmstat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

/**
 * @author Matthew Lohbihler
 */
public class VMStatPointLocatorVO extends AbstractPointLocatorVO<VMStatPointLocatorVO> implements JsonSerializable {
    public interface Attributes {
        int PROCS_R = 1;
        int PROCS_B = 2;
        int MEMORY_SWPD = 3;
        int MEMORY_FREE = 4;
        int MEMORY_BUFF = 5;
        int MEMORY_CACHE = 6;
        int SWAP_SI = 7;
        int SWAP_SO = 8;
        int IO_BI = 9;
        int IO_BO = 10;
        int SYSTEM_IN = 11;
        int SYSTEM_CS = 12;
        int CPU_US = 13;
        int CPU_SY = 14;
        int CPU_ID = 15;
        int CPU_WA = 16;
        int CPU_ST = 17;
    }

    public static ExportCodes ATTRIBUTE_CODES = new ExportCodes();
    static {
        ATTRIBUTE_CODES.addElement(Attributes.PROCS_R, "PROCS_R", "dsEdit.vmstat.attr.procsR");
        ATTRIBUTE_CODES.addElement(Attributes.PROCS_B, "PROCS_B", "dsEdit.vmstat.attr.procsB");
        ATTRIBUTE_CODES.addElement(Attributes.MEMORY_SWPD, "MEMORY_SWPD", "dsEdit.vmstat.attr.memorySwpd");
        ATTRIBUTE_CODES.addElement(Attributes.MEMORY_FREE, "MEMORY_FREE", "dsEdit.vmstat.attr.memoryFree");
        ATTRIBUTE_CODES.addElement(Attributes.MEMORY_BUFF, "MEMORY_BUFF", "dsEdit.vmstat.attr.memoryBuff");
        ATTRIBUTE_CODES.addElement(Attributes.MEMORY_CACHE, "MEMORY_CACHE", "dsEdit.vmstat.attr.memoryCache");
        ATTRIBUTE_CODES.addElement(Attributes.SWAP_SI, "SWAP_SI", "dsEdit.vmstat.attr.swapSi");
        ATTRIBUTE_CODES.addElement(Attributes.SWAP_SO, "SWAP_SO", "dsEdit.vmstat.attr.swapSo");
        ATTRIBUTE_CODES.addElement(Attributes.IO_BI, "IO_BI", "dsEdit.vmstat.attr.ioBi");
        ATTRIBUTE_CODES.addElement(Attributes.IO_BO, "IO_BO", "dsEdit.vmstat.attr.ioBo");
        ATTRIBUTE_CODES.addElement(Attributes.SYSTEM_IN, "SYSTEM_IN", "dsEdit.vmstat.attr.systemIn");
        ATTRIBUTE_CODES.addElement(Attributes.SYSTEM_CS, "SYSTEM_CS", "dsEdit.vmstat.attr.systemCs");
        ATTRIBUTE_CODES.addElement(Attributes.CPU_US, "CPU_US", "dsEdit.vmstat.attr.cpuUs");
        ATTRIBUTE_CODES.addElement(Attributes.CPU_SY, "CPU_SY", "dsEdit.vmstat.attr.cpuSy");
        ATTRIBUTE_CODES.addElement(Attributes.CPU_ID, "CPU_ID", "dsEdit.vmstat.attr.cpuId");
        ATTRIBUTE_CODES.addElement(Attributes.CPU_WA, "CPU_WA", "dsEdit.vmstat.attr.cpuWa");
        ATTRIBUTE_CODES.addElement(Attributes.CPU_ST, "CPU_ST", "dsEdit.vmstat.attr.cpuSt");
    };

    private int attributeId = Attributes.CPU_ID;

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public VMStatPointLocatorRT createRuntime() {
        return new VMStatPointLocatorRT(this);
    }

    @Override
    public TranslatableMessage getConfigurationDescription() {
        if (ATTRIBUTE_CODES.isValidId(attributeId))
            return new TranslatableMessage(ATTRIBUTE_CODES.getKey(attributeId));
        return new TranslatableMessage("common.unknown");
    }

    @Override
    public int getDataTypeId() {
        return DataTypes.NUMERIC;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#validate(com.serotonin.m2m2.i18n.ProcessResult, com.serotonin.m2m2.vo.DataPointVO, com.serotonin.m2m2.vo.dataSource.DataSourceVO)
     */
    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO<?> dsvo) {
        if (!(dsvo instanceof VMStatDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");     
        if (!ATTRIBUTE_CODES.isValidId(attributeId))
            response.addContextualMessage("attributeId", "validate.invalidValue");
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(attributeId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1)
            attributeId = in.readInt();
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("attributeId", ATTRIBUTE_CODES.getCode(attributeId));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("attributeId");
        if (text == null)
            throw new TranslatableJsonException("emport.error.missing", "attributeId", ATTRIBUTE_CODES.getCodeList());
        attributeId = ATTRIBUTE_CODES.getId(text);
        if (!ATTRIBUTE_CODES.isValidId(attributeId))
            throw new TranslatableJsonException("emport.error.invalid", "attributeId", text,
                    ATTRIBUTE_CODES.getCodeList());
    }
}
