/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.util.SerializationHelper;

import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.MBusUtils;
import net.sf.mbus4j.dataframes.MBusMedium;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;
import net.sf.mbus4j.dataframes.datablocks.dif.DataFieldCode;
import net.sf.mbus4j.dataframes.datablocks.dif.FunctionField;
import net.sf.mbus4j.dataframes.datablocks.vif.SiPrefix;
import net.sf.mbus4j.dataframes.datablocks.vif.UnitOfMeasurement;
import net.sf.mbus4j.dataframes.datablocks.vif.VifFB;
import net.sf.mbus4j.dataframes.datablocks.vif.VifFD;
import net.sf.mbus4j.dataframes.datablocks.vif.VifPrimary;
import net.sf.mbus4j.dataframes.datablocks.vif.VifTypes;

public class MBusPointLocatorVO extends AbstractPointLocatorVO<MBusPointLocatorVO> implements JsonSerializable{

//    private static Log LOG = LogFactory.getLog(MBusPointLocatorVO.class);

    public final static String[] EMPTY_STRING_ARRAY = new String[0];
    /**
     * The address of the device.
     */
    @JsonProperty
    private byte address;
    private DataFieldCode difCode = DataFieldCode._12_DIGIT_BCD;
    private FunctionField functionField = FunctionField.INSTANTANEOUS_VALUE;
    @JsonProperty
    private int subUnit;
    @JsonProperty
    private int tariff;
    @JsonProperty
    private long storageNumber;
    @JsonProperty
    private String vifType;
    @JsonProperty
    private String vifLabel;
    private UnitOfMeasurement unitOfMeasurement = UnitOfMeasurement.DIMENSIONLESS;
    private SiPrefix siPrefix = SiPrefix.ONE;
    @JsonProperty
    private Integer exponent;
    @JsonProperty
    private String[] vifeLabels = EMPTY_STRING_ARRAY;
    @JsonProperty
    private String[] vifeTypes = EMPTY_STRING_ARRAY;
    private MBusMedium medium = MBusMedium.OTHER;
    @JsonProperty
    private String responseFrame;
    @JsonProperty
    private byte version;
    @JsonProperty
    private int identNumber;
    @JsonProperty
    private String manufacturer;
    @JsonProperty
    private int dbIndex;
    private SiPrefix effectiveSiPrefix = SiPrefix.ONE;
    private MBusAddressing addressing = MBusAddressing.PRIMARY;

    @Override
    public int getDataTypeId() {
        //Currently if a new DP is created DataPoint.setPointLocator is called and difCode is null
        if (difCode == null) {
            return DataTypes.UNKNOWN;
        }
        switch (difCode) {
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
            case VARIABLE_LENGTH:
                return DataTypes.ALPHANUMERIC;
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
    public MBusPointLocatorRT createRuntime() {
        return new MBusPointLocatorRT(this);
    }

    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO<?> dsvo) {
        if (!(dsvo instanceof MBusDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");

        switch (getAddressing()) {
            case PRIMARY:
                if ((address & 0xFF) > (MBusUtils.LAST_REGULAR_PRIMARY_ADDRESS & 0xFF)) {
                    response.addContextualMessage("address", "validate.invalidValue");
                }
                break;
            case SECONDARY:
                if ((address == MBusUtils.BROADCAST_NO_ANSWER_PRIMARY_ADDRESS)
                        || (address == MBusUtils.BROADCAST_WITH_ANSWER_PRIMARY_ADDRESS)) {
                        response.addContextualMessage("address", "validate.invalidValue");
                }
                break;
        }

        if (subUnit < 0) {
            response.addContextualMessage("subUnit", "validate.required");
        }

        if (tariff < 0) {
            response.addContextualMessage("tariff", "validate.required");
        }

        if (storageNumber < 0) {
            response.addContextualMessage("storageNumber", "validate.required");
        }
        
        try{
           VifTypes t = VifTypes.fromLabel(vifType);
           switch(t) {
               case PRIMARY:
                   try{
                       VifPrimary.assemble(vifLabel, unitOfMeasurement, siPrefix, exponent);
                   }catch(Exception e) {
                       response.addContextualMessage("vifLabel", "validate.invalidValue");
                   }
                   break;
               case FB_EXTENTION:
                   try{
                       VifFB.assemble(vifLabel, unitOfMeasurement, siPrefix, exponent);
                   }catch(Exception e) {
                       response.addContextualMessage("vifLabel", "validate.invalidValue");
                   }
                   break;
               case FD_EXTENTION:
                   try{
                       VifFD.assemble(vifLabel, unitOfMeasurement, siPrefix, exponent);
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

        if (vifeLabels.length > 0) {
            if (vifeLabels.length != vifeTypes.length) {
                response.addContextualMessage("vifeLabels", "mbus.validate.vifeLengthsInvalid");
                response.addContextualMessage("vifeTypes", "mbus.validate.vifeLengthsInvalid");
            }
            for (int i = 0; i < vifeLabels.length; i++) {
                try {
                    DataBlock.getVife(vifeTypes[i], vifeLabels[i]);
                } catch (IllegalArgumentException ex) {
                    response.addContextualMessage("vifeTypes[" + i + "]", "validate.invalidValue");
                    response.addContextualMessage("vifeLabels[" + i + "]", "validate.invalidValue");
                }
            }
        }
        if ((responseFrame == null) || (responseFrame.length() == 0)) {
            response.addContextualMessage("responseFrame", "validate.required");
        }
        if (((version & 0xFF) < 0) || ((version & 0xFF) > 0xFF)) {
            response.addContextualMessage("version", "validate.required");
        }
        if (identNumber < 0) {
            response.addContextualMessage("identNumber", "validate.required");
        }
        if ((manufacturer == null) || (manufacturer.length() != 3)) {
            response.addContextualMessage("manufacturer", "validate.required");
        }
    }
    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int SERIAL_VERSION = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(SERIAL_VERSION);
        SerializationHelper.writeSafeUTF(out, addressing.name());

        out.writeInt(dbIndex);
        SerializationHelper.writeSafeUTF(out, effectiveSiPrefix.name());

        out.writeByte(address);
        out.writeByte(version);
        out.writeInt(identNumber);
        SerializationHelper.writeSafeUTF(out, manufacturer);
        SerializationHelper.writeSafeUTF(out, medium.name());

        SerializationHelper.writeSafeUTF(out, responseFrame);

        SerializationHelper.writeSafeUTF(out, difCode.name());
        SerializationHelper.writeSafeUTF(out, functionField.name());
        out.writeInt(subUnit);
        out.writeInt(tariff);
        out.writeLong(storageNumber);
        SerializationHelper.writeSafeUTF(out, vifType);
        SerializationHelper.writeSafeUTF(out, vifLabel);
        SerializationHelper.writeSafeUTF(out, unitOfMeasurement != null ? unitOfMeasurement.name() : null);
        SerializationHelper.writeSafeUTF(out, siPrefix.name());
        out.writeObject(exponent);
        out.writeInt(vifeLabels.length);
        for (int i = 0; i < vifeLabels.length; i++) {
            SerializationHelper.writeSafeUTF(out, vifeTypes[i]);
            SerializationHelper.writeSafeUTF(out, vifeLabels[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        switch (ver) {
            case 1: readVersion1(in);
            break;
            case 2: readVersion2(in);
            break;
            default: throw new RuntimeException("Version: " + ver + " is not supported!");
        }
    }
    
    private void readVersion1(ObjectInputStream in) throws IOException, ClassNotFoundException {
            addressing = MBusAddressing.fromLabel(SerializationHelper.readSafeUTF(in));

            dbIndex = -1;

            address = in.readByte();
            version = in.readByte();
            identNumber = in.readInt();
            manufacturer = SerializationHelper.readSafeUTF(in);
            medium = MBusMedium.fromLabel(SerializationHelper.readSafeUTF(in));

            responseFrame = SerializationHelper.readSafeUTF(in);

            difCode = DataFieldCode.fromLabel(SerializationHelper.readSafeUTF(in));
            functionField = FunctionField.fromLabel(SerializationHelper.readSafeUTF(in));
            subUnit = in.readInt();
            tariff = in.readInt();
            storageNumber = in.readLong();
            vifType = SerializationHelper.readSafeUTF(in);
            vifLabel = SerializationHelper.readSafeUTF(in);
            unitOfMeasurement = UnitOfMeasurement.fromLabel(SerializationHelper.readSafeUTF(in));
            siPrefix = SiPrefix.fromLabel(SerializationHelper.readSafeUTF(in));
            effectiveSiPrefix = siPrefix;
            exponent = (Integer) in.readObject();
            final int vifeLength = in.readInt();
            if (vifeLength == 0) {
                vifeLabels = EMPTY_STRING_ARRAY;
                vifeTypes = EMPTY_STRING_ARRAY;
            } else {
                vifeLabels = new String[vifeLength];
                vifeTypes = new String[vifeLength];
                for (int i = 0; i < vifeLength; i++) {
                    vifeTypes[i] = SerializationHelper.readSafeUTF(in);
                    vifeLabels[i] = SerializationHelper.readSafeUTF(in);
                }
            }
    }
    
    private void readVersion2(ObjectInputStream in) throws IOException, ClassNotFoundException{
            addressing = MBusAddressing.fromLabel(SerializationHelper.readSafeUTF(in));

            dbIndex = in.readInt();
            effectiveSiPrefix = SiPrefix.valueOf(SerializationHelper.readSafeUTF(in));

            address = in.readByte();
            version = in.readByte();
            identNumber = in.readInt();
            manufacturer = SerializationHelper.readSafeUTF(in);
            medium = MBusMedium.valueOf(SerializationHelper.readSafeUTF(in));

            responseFrame = SerializationHelper.readSafeUTF(in);

            difCode = DataFieldCode.valueOf(SerializationHelper.readSafeUTF(in));
            functionField = FunctionField.fromLabel(SerializationHelper.readSafeUTF(in));
            subUnit = in.readInt();
            tariff = in.readInt();
            storageNumber = in.readLong();
            vifType = SerializationHelper.readSafeUTF(in);
            vifLabel = SerializationHelper.readSafeUTF(in);
            final String unitOfMeasurementStr = SerializationHelper.readSafeUTF(in);
            if (unitOfMeasurementStr != null) {
                unitOfMeasurement = UnitOfMeasurement.valueOf(unitOfMeasurementStr);
            } else {
                unitOfMeasurement = null;
            }
            siPrefix = SiPrefix.valueOf(SerializationHelper.readSafeUTF(in));
            exponent = (Integer) in.readObject();
            final int vifeLength = in.readInt();
            if (vifeLength == 0) {
                vifeLabels = EMPTY_STRING_ARRAY;
                vifeTypes = EMPTY_STRING_ARRAY;
            } else {
                vifeLabels = new String[vifeLength];
                vifeTypes = new String[vifeLength];
                for (int i = 0; i < vifeLength; i++) {
                    vifeTypes[i] = SerializationHelper.readSafeUTF(in);
                    vifeLabels[i] = SerializationHelper.readSafeUTF(in);
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
     * @return the subUnit
     */
    public int getSubUnit() {
        return subUnit;
    }

    /**
     * @param subUnit
     *            the subUnit to set
     */
    public void setSubUnit(int subUnit) {
        this.subUnit = subUnit;
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
    	if(difCode != null)
    		return difCode.getLabel();
    	else 
    		return null;
    }

    /**
     * @param difCode
     *            the difCode to set
     */
    public void setDifCode(String difCode) {
        this.difCode = DataFieldCode.fromLabel(difCode);
    }

    /**
     * @return the functionField
     */
    public String getFunctionField() {
    	if(functionField != null)
    		return functionField.getLabel();
    	else
    		return null;
    }

    /**
     * @param functionField
     *            the functionField to set
     */
    public void setFunctionField(String functionField) {
        this.functionField = FunctionField.fromLabel(functionField);
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
    	if(unitOfMeasurement != null) {
            return unitOfMeasurement.getLabel();
        } else {
            return null;
        }
    }

    /**
     * @param unitOfMeasurement
     *            the unitOfMeasurement to set
     */
    public void setUnitOfMeasurement(String unitOfMeasurement) {
        if (unitOfMeasurement != null) {
            this.unitOfMeasurement = UnitOfMeasurement.fromLabel(unitOfMeasurement);
        } else {
            this.unitOfMeasurement = null;
        }
    }

    
    public DataFieldCode difCode() {
        return difCode;
    }
    
    public FunctionField functionField() {
        return functionField;
    }
    
    public SiPrefix siPrefix() {
        return siPrefix;
    }
    
    public UnitOfMeasurement unitOfMeasurement() {
        return unitOfMeasurement;
    }
    
    public SiPrefix effectiveSiPrefix() {
        return effectiveSiPrefix;
    }
    
    /**
     * @return the siPrefix
     */
    public String getSiPrefix() {
    	if(siPrefix == null)
    		return null;
    	return siPrefix.getLabel();
    }

    /**
     * @param siPrefix
     *            the siPrefix to set
     */
    public void setSiPrefix(String siPrefix) {
        this.siPrefix = SiPrefix.fromLabel(siPrefix);
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
     * @param vifeLabels
     *            the vifeLabels to set
     */
    public void setVifeLabels(String[] vifeLabels) {
        this.vifeLabels = vifeLabels;
    }

    /**
     * @return the medium
     */
    public MBusMedium getMedium() {
        return medium;
    }

    /**
     * @param medium
     *            the medium to set
     */
    public void setMedium(MBusMedium medium) {
        this.medium = medium;
    }

    /**
     * @return the addressing
     */
    public MBusAddressing getAddressing() {
        return addressing;
    }

    /**
     * @param addressing
     *            the addressing to set
     */
    public void setAddressing(MBusAddressing addressing) {
        this.addressing = addressing;
    }

    /**
     * Helper for JSP
     * 
     * @return
     */
    public boolean isPrimaryAddressing() {
        return MBusAddressing.PRIMARY.equals(addressing);
    }

    /**
     * Helper for JSP
     * 
     * @return
     */
    public boolean isSecondaryAddressing() {
        return MBusAddressing.SECONDARY.equals(addressing);
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

    public String getDeviceName() {
        return String.format("%s %s 0x%02X %08d @0x%02X", getManufacturer(), getMedium(), getVersion(), getIdentNumber(), getAddress());
    }
    
    public String getParams() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>dataType = </b> \"").append(getDifCode()).append("\"<br>");
        sb.append("<b>description =</b> \"").append(vifLabel).append("\"</br>");
        sb.append("<b>unitOfMeasurement =</b> \"").append(getUnitOfMeasurement()).append("\"<br>");
        sb.append("<b>exponent =</b> \"").append(exponent).append("\"<br>");
        sb.append("<b>siPrefix =</b> \"").append(getSiPrefix()).append("\"<br>");
        sb.append("<b>tariff =</b> \"").append(tariff).append("\"<br>");
        sb.append("<b>storageNumber =</b> \"").append(storageNumber).append("\"<br>");
        sb.append("<b>functionField =</b> \"").append(getFunctionField()).append("\"<br>");
        return sb.toString();
    }

        @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MBusPointLocatorVO{");
        sb.append(String.format("man=%s, medium=%s, version=0x%02X, id=%08d, address=@0x%02X, ", getManufacturer(), getMedium(), getVersion(), getIdentNumber(), getAddress()));
        sb.append("dataType=").append(getDifCode()).append(", ");
        sb.append("description=").append(vifLabel).append(", ");
        sb.append("unitOfMeasurement=").append(getUnitOfMeasurement()).append(", ");
        sb.append("exponent=").append(exponent).append(", ");
        sb.append("siPrefix=").append(getSiPrefix()).append(", ");
        sb.append("tariff=").append(tariff).append(" ,");
        sb.append("storageNumber=").append(storageNumber).append(", ");
        sb.append("functionField=").append(functionField);
        sb.append("}");
        return sb.toString();
    }

    /**
     * @return the dbIndex
     */
    public int getDbIndex() {
        return dbIndex;
    }

    /**
     * @param dbIndex the dbIndex to set
     */
    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /**
     * @return the effectiveSiPrefix
     */
    public String getEffectiveSiPrefix() {
    	if(effectiveSiPrefix != null) {
            return effectiveSiPrefix.getLabel();
        } else {
            return null;
            }
    }

    /**
     * @param effectiveSiPrefix the effectiveSiPrefix to set
     */
    public void setEffectiveSiPrefix(String effectiveSiPrefix) {
        this.effectiveSiPrefix = SiPrefix.fromLabel(effectiveSiPrefix);
    }
    
	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException,
			JsonException {
		writer.writeEntry("difCode", difCode.getLabel());
		writer.writeEntry("functionField", functionField.getLabel());
		writer.writeEntry("unitOfMeasurement", unitOfMeasurement);
	    writer.writeEntry("siPrefix", siPrefix);
	    writer.writeEntry("effectiveSiPrefix", effectiveSiPrefix);
		writer.writeEntry("medium", medium.getLabel());
		writer.writeEntry("addressing", addressing.getLabel());
	}
    
	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject)
			throws JsonException {
		String text = jsonObject.getString("difCode");
		if(text != null)
			difCode = DataFieldCode.fromLabel(text);
		
		text = jsonObject.getString("functionField");
		if(text != null)
			functionField = FunctionField.fromLabel(text);
		
		text = jsonObject.getString("unitOfMeasurement");
		if(text != null)
			unitOfMeasurement = UnitOfMeasurement.fromLabel(text);
		
		text = jsonObject.getString("siPrefix");
		if(text != null)
			siPrefix = SiPrefix.fromLabel(text);
		
		text = jsonObject.getString("effectiveSiPrefix");
		if(text != null)
			effectiveSiPrefix = SiPrefix.fromLabel(text);
		
		text = jsonObject.getString("addressing");
		if(text != null)
			addressing = MBusAddressing.fromLabel(text);
		
		text = jsonObject.getString("medium");
		if(text != null)
			medium = MBusMedium.fromLabel(text);
	}
}
