/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;
import net.sf.mbus4j.dataframes.datablocks.dif.DataFieldCode;
import net.sf.mbus4j.dataframes.datablocks.dif.FunctionField;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;
import com.serotonin.util.SerializationHelper;

public class MBusPointLocatorVO extends AbstractPointLocatorVO {

    public final static String[] EMPTY_STRING_ARRAY = new String[0];
    /**
     * The address of the device.
     */
    @JsonProperty
    private byte address;
    @JsonProperty
    private String difCode = DataFieldCode._8_DIGIT_BCD.getLabel();
    @JsonProperty
    private String functionField;
    @JsonProperty
    private int deviceUnit;
    @JsonProperty
    private int tariff;
    @JsonProperty
    private long storageNumber;
    @JsonProperty
    private String vifType;
    @JsonProperty
    private String vifLabel;
    @JsonProperty
    private String unitOfMeasurement;
    @JsonProperty
    private String siPrefix;
    @JsonProperty
    private Integer exponent;
    @JsonProperty
    private String[] vifeLabels = EMPTY_STRING_ARRAY;
    @JsonProperty
    private String[] vifeTypes = EMPTY_STRING_ARRAY;
    @JsonProperty
    private String medium;
    @JsonProperty
    private String responseFrame;
    @JsonProperty
    private byte version;
    @JsonProperty
    private int identNumber;
    @JsonProperty
    private String manufacturer;
    @JsonProperty
    private String addressing;

    @Override
    public int getDataTypeId() {
        switch (DataFieldCode.fromLabel(difCode)) {
        case _12_DIGIT_BCD:
        case _16_BIT_INTEGER:
        case _24_BIT_INTEGER:
        case _2_DIGIT_BCD:
        case _32_BIT_INTEGER:
        case _32_BIT_REAL:
        case _48_BIT_INTEGER:
        case _4_DIGIT_BCD:
        case _64_BIT_INTEGER:
        case _6_DIGIT_BCD:
        case _8_BIT_INTEGER:
        case _8_DIGIT_BCD:
            return DataTypes.NUMERIC;
        default:
            return DataTypes.UNKNOWN;
        }
    }

    @Override
    public TranslatableMessage getConfigurationDescription() {
        return new TranslatableMessage("common.default", address + " " + manufacturer);
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new MBusPointLocatorRT(this);
    }

    @Override
    public void validate(ProcessResult response) {
//        if ((address < MBusUtils.FIRST_REGULAR_PRIMARY_ADDRESS)
//                || (address > MBusUtils.LAST_REGULAR_PRIMARY_ADDRESS)) {
//            response.addContextualMessage("addressHex", "validate.required");
//        }
        try {
            DataFieldCode.fromLabel(difCode);
        }
        catch (IllegalArgumentException ex) {
            response.addContextualMessage("difCode", "validate.required");
        }

        try {
            FunctionField.fromLabel(functionField);
        }
        catch (IllegalArgumentException ex) {
            response.addContextualMessage("functionField", "validate.required");
        }

        if (deviceUnit < 0) {
            response.addContextualMessage("deviceUnit", "validate.required");
        }

        if (tariff < 0) {
            response.addContextualMessage("tariff", "validate.required");
        }

        if (storageNumber < 0) {
            response.addContextualMessage("storageNumber", "validate.required");
        }

        
        if(StringUtils.isEmpty(vifType)){
        	response.addContextualMessage("vifType", "validate.required");
        }
        if(StringUtils.isEmpty(vifLabel)){
        	response.addContextualMessage("vifLabel", "validate.required");
        }        
//        if(StringUtils.isEmpty(unitOfMeasurement)){
//        	response.addContextualMessage("unitOfMeasurement", "validate.required");
//        }
//        if(StringUtils.isEmpty(siPrefix)){
//        	response.addContextualMessage("siPrefix", "validate.required");
//        }        
        try {
            DataBlock.getVif(vifType, vifLabel, unitOfMeasurement, siPrefix, exponent);
        }
        catch (IllegalArgumentException ex) {
            response.addMessage(new TranslatableMessage("mbus.validate.vifInvalid", ex.getMessage()));
        }

        if (vifeLabels.length > 0) {
            if (vifeLabels.length != vifeTypes.length) {
                response.addMessage(new TranslatableMessage("mbus.validate.vifeLengthsInvalid"));
            }
            for (int i = 0; i < vifeLabels.length; i++) {
                try {
                    DataBlock.getVife(vifeTypes[i], vifeLabels[i]);
                }
                catch (IllegalArgumentException ex) {
                   response.addMessage(new TranslatableMessage("mbus.validate.vifeInvalid", ex.getMessage()));
                }
            }
        }
        try {
            MBusMedium.fromLabel(medium);
        }
        catch (IllegalArgumentException ex) {
            response.addContextualMessage("medium", "validate.required");
        }
        if ((responseFrame == null) || (responseFrame.length() == 0)) {
            response.addContextualMessage("responseFrame", "validate.required");
        }
        if ((version < 0) || (version > 0xFF)) {
            response.addContextualMessage("version", "validate.required");
        }
        if (identNumber < 0) {
            response.addContextualMessage("id", "validate.required");
        }
        if ((manufacturer == null) || (manufacturer.length() != 3)) {
            response.addContextualMessage("manufacturer", "validate.required");
        }
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int serialVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(serialVersion);
        SerializationHelper.writeSafeUTF(out, addressing);

        out.writeByte(address);
        out.writeByte(version);
        out.writeInt(identNumber);
        SerializationHelper.writeSafeUTF(out, manufacturer);
        SerializationHelper.writeSafeUTF(out, medium);

        SerializationHelper.writeSafeUTF(out, responseFrame);

        SerializationHelper.writeSafeUTF(out, difCode);
        SerializationHelper.writeSafeUTF(out, functionField);
        out.writeInt(deviceUnit);
        out.writeInt(tariff);
        out.writeLong(storageNumber);
        SerializationHelper.writeSafeUTF(out, vifType);
        SerializationHelper.writeSafeUTF(out, vifLabel);
        SerializationHelper.writeSafeUTF(out, unitOfMeasurement);
        SerializationHelper.writeSafeUTF(out, siPrefix);
        out.writeObject(exponent);
        out.writeInt(vifeLabels.length);
        for (int i = 0; i < vifeLabels.length; i++) {
            SerializationHelper.writeSafeUTF(out, vifeTypes[i]);
            SerializationHelper.writeSafeUTF(out, vifeLabels[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            addressing = SerializationHelper.readSafeUTF(in);

            address = in.readByte();
            version = in.readByte();
            identNumber = in.readInt();
            manufacturer = SerializationHelper.readSafeUTF(in);
            medium = SerializationHelper.readSafeUTF(in);

            responseFrame = SerializationHelper.readSafeUTF(in);

            difCode = SerializationHelper.readSafeUTF(in);
            functionField = SerializationHelper.readSafeUTF(in);
            deviceUnit = in.readInt();
            tariff = in.readInt();
            storageNumber = in.readLong();
            vifType = SerializationHelper.readSafeUTF(in);
            vifLabel = SerializationHelper.readSafeUTF(in);
            unitOfMeasurement = SerializationHelper.readSafeUTF(in);
            siPrefix = SerializationHelper.readSafeUTF(in);
            exponent = (Integer) in.readObject();
            final int vifeLength = in.readInt();
            if (vifeLength == 0) {
                vifeLabels = EMPTY_STRING_ARRAY;
                vifeTypes = EMPTY_STRING_ARRAY;
            }
            else {
                vifeLabels = new String[vifeLength];
                vifeTypes = new String[vifeLength];
                for (int i = 0; i < vifeLength; i++) {
                    vifeTypes[i] = SerializationHelper.readSafeUTF(in);
                    vifeLabels[i] = SerializationHelper.readSafeUTF(in);
                }
            }
        }
    }

    /**
     * @return the address
     */
    public byte getAddress() {
        return address;
    }

    /**
     * @return the address
     */
    public String getAddressHex() {
        return String.format("0x%02x", address);
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddress(byte address) {
        this.address = address;
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddressHex(String address) {
        this.address = (byte) Integer.parseInt(address.substring(2), 16);
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setIdentNumber(int identNumber) {
        this.identNumber = identNumber;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setVersionHex(String version) {
        this.version = (byte) Integer.parseInt(version.substring(2), 16);
    }

    public void setResponseFrame(String responseFrame) {
        this.responseFrame = responseFrame;
    }

    /**
     * @return the deviceUnit
     */
    public int getDeviceUnit() {
        return deviceUnit;
    }

    /**
     * @param deviceUnit
     *            the deviceUnit to set
     */
    public void setDeviceUnit(int deviceUnit) {
        this.deviceUnit = deviceUnit;
    }

    /**
     * @return the tariff
     */
    public int getTariff() {
        return tariff;
    }

    /**
     * @param tariff
     *            the tariff to set
     */
    public void setTariff(int tariff) {
        this.tariff = tariff;
    }

    /**
     * @return the storageNumber
     */
    public long getStorageNumber() {
        return storageNumber;
    }

    /**
     * @param storageNumber
     *            the storageNumber to set
     */
    public void setStorageNumber(long storageNumber) {
        this.storageNumber = storageNumber;
    }

    /**
     * @return the responseFrame
     */
    public String getResponseFrame() {
        return responseFrame;
    }

    /**
     * @return the version
     */
    public byte getVersion() {
        return version;
    }

    /**
     * @return the version
     */
    public String getVersionHex() {
        return String.format("0x%02x", version);
    }

    /**
     * @return the id
     */
    public int getIdentNumber() {
        return identNumber;
    }

    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * @return the difCode
     */
    public String getDifCode() {
        return difCode;
    }

    /**
     * @param difCode
     *            the difCode to set
     */
    public void setDifCode(String difCode) {
        this.difCode = difCode;
    }

    /**
     * @return the functionField
     */
    public String getFunctionField() {
        return functionField;
    }

    /**
     * @param functionField
     *            the functionField to set
     */
    public void setFunctionField(String functionField) {
        this.functionField = functionField;
    }

    /**
     * @return the vifLabel
     */
    public String getVifLabel() {
        return vifLabel;
    }

    /**
     * @param vifLabel
     *            the vifLabel to set
     */
    public void setVifLabel(String vifLabel) {
        this.vifLabel = vifLabel;
    }

    /**
     * @return the unitOfMeasurement
     */
    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    /**
     * @param unitOfMeasurement
     *            the unitOfMeasurement to set
     */
    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    /**
     * @return the siPrefix
     */
    public String getSiPrefix() {
        return siPrefix;
    }

    /**
     * @param siPrefix
     *            the siPrefix to set
     */
    public void setSiPrefix(String siPrefix) {
        this.siPrefix = siPrefix;
    }

    /**
     * @return the exponent
     */
    public Integer getExponent() {
        return exponent;
    }

    /**
     * @param exponent
     *            the exponent to set
     */
    public void setExponent(Integer exponent) {
        this.exponent = exponent;
    }

    /**
     * @return the vifeLabel
     */
    public String[] getVifeLabels() {
        return vifeLabels;
    }

    /**
     * @param vifeLabel
     *            the vifeLabel to set
     */
    public void setVifeLabels(String[] vifeLabel) {
        vifeLabels = vifeLabel;
    }

    /**
     * @return the medium
     */
    public String getMedium() {
        return medium;
    }

    /**
     * @param medium
     *            the medium to set
     */
    public void setMedium(String medium) {
        this.medium = medium;
        System.out.println("MEDIUM: " + this.medium);
    }

    /**
     * @return the addressing
     */
    public String getAddressing() {
        return addressing;
    }

    /**
     * @param addressing
     *            the addressing to set
     */
    public void setAddressing(String addressing) {
        this.addressing = addressing;
    }

    /**
     * Helper for JSP
     * 
     * @return
     */
    public boolean isPrimaryAddressing() {
        return MBusAddressing.PRIMARY.getLabel().equals(addressing);
    }

    /**
     * Helper for JSP
     * 
     * @return
     */
    public boolean isSecondaryAddressing() {
        return MBusAddressing.SECONDARY.getLabel().equals(addressing);
    }

    /**
     * @return the vifType
     */
    public String getVifType() {
        return vifType;
    }

    /**
     * @param vifType
     *            the vifType to set
     */
    public void setVifType(String vifType) {
        this.vifType = vifType;
    }

    /**
     * @return the vifeTypes
     */
    public String[] getVifeTypes() {
        return vifeTypes;
    }

    /**
     * @param vifeTypes
     *            the vifeTypes to set
     */
    public void setVifeTypes(String[] vifeTypes) {
        this.vifeTypes = vifeTypes;
    }
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#asModel()
	 */
	@Override
	public PointLocatorModel<?> asModel() {
		//TODO Implement when we have a Model
		return new MBusPointLocatorModel(this);
	}
}