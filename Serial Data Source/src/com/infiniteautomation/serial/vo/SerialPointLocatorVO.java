package com.infiniteautomation.serial.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.infiniteautomation.serial.rt.SerialPointLocatorRT;
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

public class SerialPointLocatorVO extends AbstractPointLocatorVO<SerialPointLocatorVO> implements JsonSerializable{
	
	@Override
	public int getDataTypeId() {
		return dataTypeId;
	}

	@Override
	public TranslatableMessage getConfigurationDescription() {
		//TODO add the properties to this
		return new TranslatableMessage("serial.point.configuration",pointIdentifier);
	}

	@Override
	public boolean isSettable() {
		return true;
	}

	@Override
	public SerialPointLocatorRT createRuntime() {
		return new SerialPointLocatorRT(this);
	}
	
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#validate(com.serotonin.m2m2.i18n.ProcessResult, com.serotonin.m2m2.vo.DataPointVO, com.serotonin.m2m2.vo.dataSource.DataSourceVO)
     */
    @Override
    public void validate(ProcessResult response, DataPointVO dpvo, DataSourceVO<?> dsvo) {
        if (!(dsvo instanceof SerialDataSourceVO))
            response.addContextualMessage("dataSourceId", "dpEdit.validate.invalidDataSourceType");
		if (pointIdentifier == null)
            response.addContextualMessage("pointIdentifier", "validate.invalidValue");	

		if (SerialDataSourceVO.isBlank(valueRegex))
            response.addContextualMessage("valueRegex", "validate.required");	
		try {
			Pattern.compile(valueRegex).matcher("").find(); // Validate the regex
		} catch (PatternSyntaxException e) {
			response.addContextualMessage("valueRegex", "serial.validate.badRegex", e.getMessage());
		}
		
		if(valueIndex < 0)
			response.addContextualMessage("valueIndex","validate.invalidValue");
		
		if (!DataTypes.CODES.isValidId(dataTypeId))
            response.addContextualMessage("dataTypeId", "validate.invalidValue");

	}

	@JsonProperty
	private String pointIdentifier; //Address or unique ID in message for this point
	@JsonProperty
	private String valueRegex;
	@JsonProperty
	private int valueIndex;
	private int dataTypeId = DataTypes.ALPHANUMERIC;
	
	public String getPointIdentifier() {
		return pointIdentifier;
	}

	public void setPointIdentifier(String pointIdentifier) {
		this.pointIdentifier = pointIdentifier;
	}
	
	public String getValueRegex() {
		return valueRegex;
	}

	public void setValueRegex(String valueRegex) {
		this.valueRegex = valueRegex;
	}

	public int getValueIndex() {
		return valueIndex;
	}

	public void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}

	public void setDataTypeId(int dataTypeId) {
		this.dataTypeId = dataTypeId;
	}
	
    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, pointIdentifier);
        SerializationHelper.writeSafeUTF(out, valueRegex);
        out.writeInt(valueIndex);
        out.writeInt(dataTypeId);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
        	pointIdentifier= SerializationHelper.readSafeUTF(in);
        	valueRegex= SerializationHelper.readSafeUTF(in);
        	valueIndex = in.readInt();
        	dataTypeId = in.readInt();
        }
    }

	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
		Integer value = readDataType(jsonObject, DataTypes.IMAGE);
        if (value != null)
            dataTypeId = value;
	}

	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
		writeDataType(writer);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#asModel()
	 */
	@Override
	public SerialPointLocatorModel asModel() {
		return new SerialPointLocatorModel(this);
	}

	
	
}
