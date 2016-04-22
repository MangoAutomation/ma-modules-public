package com.infiniteautomation.asciifile.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;

import com.infiniteautomation.asciifile.rt.AsciiFilePointLocatorRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;
import com.serotonin.util.SerializationHelper;

/**
 * @author Phillip Dunlap
 */

public class AsciiFilePointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable{

	private static final long serialVersionUID = 1L;

	@Override
	public TranslatableMessage getConfigurationDescription() {
		//TODO add the properties to this
		return new TranslatableMessage("file.point.configuration",pointIdentifier);
	}

	@Override
	public boolean isSettable() {
		return false;
	}

	@Override
	public PointLocatorRT createRuntime() {
		return new AsciiFilePointLocatorRT(this);
	}

	@Override
	public void validate(ProcessResult response) {

		if (AsciiFileDataSourceVO.isBlank(valueRegex))
            response.addContextualMessage("valueRegex", "validate.required");
		
		//Validate the regex
		if(!Pattern.compile("([^\\\\]|^)\\(.*[^\\\\]\\)").matcher(valueRegex).find())
			response.addContextualMessage("valueRegex", "file.validate.noCaptureGroup");
		
		if(pointIdentifierIndex < 0)
			response.addContextualMessage("pointIdentifierIndex","validate.invalidValue");
		
		if(valueIndex < 0)
			response.addContextualMessage("valueIndex","validate.invalidValue");
		
		if (!DataTypes.CODES.isValidId(dataType))
            response.addContextualMessage("dataTypeId", "validate.invalidValue");
		
		if(hasTimestamp) {
			if(timestampIndex < 0)
				response.addContextualMessage("timestampIndex","validate.invalidValue");
			if(timestampFormat == null || timestampFormat.equals(""))
				response.addContextualMessage("timestampFormat","validate.invalidValue");
		}

	}

	@JsonProperty
	private String pointIdentifier; //Address or unique ID in message for this point
	@JsonProperty
	private String valueRegex;
	@JsonProperty
	private int pointIdentifierIndex;
	@JsonProperty
	private int valueIndex;
	private int dataType;
	@JsonProperty
	private boolean hasTimestamp;
	@JsonProperty
	private int timestampIndex;
	@JsonProperty
	private String timestampFormat;
	
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
	
	public int getPointIdentifierIndex() {
		return pointIdentifierIndex;
	}
	
	public void setPointIdentifierIndex(int pointIdentifierIndex) {
		this.pointIdentifierIndex = pointIdentifierIndex;
	}

	public int getValueIndex() {
		return valueIndex;
	}

	public void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}
	
	@Override
	public int getDataTypeId() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public boolean getHasTimestamp() {
		return hasTimestamp;
	}

	public void setHasTimestamp(boolean hasTimestamp) {
		this.hasTimestamp = hasTimestamp;
	}

	public int getTimestampIndex() {
		return timestampIndex;
	}

	public void setTimestampIndex(int timestampIndex) {
		this.timestampIndex = timestampIndex;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}
	
    //
    //
    // Serialization
    //
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, pointIdentifier);
        SerializationHelper.writeSafeUTF(out, valueRegex);
		out.writeInt(pointIdentifierIndex);
        out.writeInt(valueIndex);
        out.writeInt(dataType);
        out.writeBoolean(hasTimestamp);
        out.writeInt(timestampIndex);
        SerializationHelper.writeSafeUTF(out, timestampFormat);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
        	pointIdentifier= SerializationHelper.readSafeUTF(in);
        	valueRegex= SerializationHelper.readSafeUTF(in);
			pointIdentifierIndex = in.readInt();
        	valueIndex = in.readInt();
        	dataType = in.readInt();
        	hasTimestamp = false;
        	timestampIndex = 0;
        	timestampFormat = "";
        }
        if (ver == 2) {
        	pointIdentifier= SerializationHelper.readSafeUTF(in);
        	valueRegex= SerializationHelper.readSafeUTF(in);
			pointIdentifierIndex = in.readInt();
        	valueIndex = in.readInt();
        	dataType = in.readInt();
        	hasTimestamp = in.readBoolean();
        	timestampIndex = in.readInt();
        	timestampFormat = SerializationHelper.readSafeUTF(in);
        }
    }

	@Override
	public void jsonRead(JsonReader reader, JsonObject jo) throws JsonException {
		if(jo.containsKey("dataType"))
			dataType = DataTypes.CODES.getId(jo.getString("dataType"));
		else
			throw new TranslatableJsonException("emport.error.missing", "dataType");
	}

	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
		writer.writeEntry("dataType", DataTypes.CODES.getCode(dataType));
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#asModel()
	 */
	@Override
	public PointLocatorModel<?> asModel() {
		return new AsciiFilePointLocatorModel(this);
	}

	
	
}
