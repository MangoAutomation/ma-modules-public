package com.infiniteautomation.asciifile.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
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
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
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
		
		if (!DataTypes.CODES.isValidId(dataTypeId))
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
	@JsonProperty
	private int dataTypeId;
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
	
	public int getDataTypeId() {
		return dataTypeId;
	}

	public void setDataTypeId(int dataTypeId) {
		this.dataTypeId = dataTypeId;
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
	
	@Override
	public void addProperties(List<TranslatableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.file.pointIdentifier", pointIdentifier);
        AuditEventType.addPropertyMessage(list, "dsEdit.file.valueRegex", valueRegex);
		AuditEventType.addPropertyMessage(list, "dsEdit.file.pointIdentifierIndex", pointIdentifierIndex);
        AuditEventType.addPropertyMessage(list, "dsEdit.file.valueIndex", valueIndex);
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", dataTypeId);
        AuditEventType.addPropertyMessage(list, "dsEdit.file.hasTimestamp", hasTimestamp);
        AuditEventType.addPropertyMessage(list, "dsEdit.file.timestampIndex", timestampIndex);
        AuditEventType.addPropertyMessage(list, "dsEdit.file.timestampFormat", timestampFormat);

	}

	@Override
	public void addPropertyChanges(List<TranslatableMessage> list, Object o) {
		AsciiFilePointLocatorVO from = (AsciiFilePointLocatorVO)o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.pointIdentifier", from.pointIdentifier, pointIdentifier);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.valueRegex", from.valueRegex, valueRegex);
		AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.pointIdentifierIndex", from.pointIdentifierIndex, pointIdentifierIndex);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.valueIndex", from.valueIndex, valueIndex);
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.dataTypeId, dataTypeId);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.hasTimestamp", from.hasTimestamp, hasTimestamp);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.timestampIndex", from.timestampIndex, timestampIndex);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.timestampFormat", from.timestampFormat, timestampFormat);

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
        out.writeInt(dataTypeId);
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
        	dataTypeId = in.readInt();
        	hasTimestamp = false;
        	timestampIndex = 0;
        	timestampFormat = "";
        }
        if (ver == 2) {
        	pointIdentifier= SerializationHelper.readSafeUTF(in);
        	valueRegex= SerializationHelper.readSafeUTF(in);
			pointIdentifierIndex = in.readInt();
        	valueIndex = in.readInt();
        	dataTypeId = in.readInt();
        	hasTimestamp = in.readBoolean();
        	timestampIndex = in.readInt();
        	timestampFormat = SerializationHelper.readSafeUTF(in);
        }
    }

	@Override
	public void jsonRead(JsonReader arg0, JsonObject arg1) throws JsonException {
	}

	@Override
	public void jsonWrite(ObjectWriter arg0) throws IOException, JsonException {
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.PointLocatorVO#asModel()
	 */
	@Override
	public PointLocatorModel<?> asModel() {
		return new AsciiFilePointLocatorModel(this);
	}

	
	
}
