package com.infiniteautomation.datafilesource.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.infiniteautomation.datafilesource.rt.DataFilePointLocatorRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;

public class DataFilePointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {

	@Override
	public TranslatableMessage getConfigurationDescription() {
		return new TranslatableMessage("datafile.point.configuration", fromIdentifier, toXid);
	}

	@Override
	public boolean isSettable() {
		//Always returns false. These points are purely to map points from a data file to another point in the system.
		return false;
	}

	@Override
	public DataFilePointLocatorRT createRuntime() {
		return new DataFilePointLocatorRT(this);
	}

	@Override
	public void validate(ProcessResult response) {
		// TODO Implement checking that important fields are supplied.
		
	}
	
	@JsonProperty
	private boolean isMappingPoint;
	@JsonProperty
	private String toXid;
	@JsonProperty
	private String fromIdentifier;
	@JsonProperty
	private int dataTypeId;

	@Override
	public int getDataTypeId() {
		return dataTypeId;
	}
	
	public void setDataTypeId(int dataTypeId) {
		this.dataTypeId = dataTypeId;
	}
	
	public String getToXid() {
		return this.toXid;
	}
	
	public void setToXid(String toXid) {
		this.toXid = toXid;
	}
	
	public String getFromIdentifier() {
		return this.fromIdentifier;
	}
	
	public void setFromIdentifier(String fromIdentifier) {
		this.fromIdentifier = fromIdentifier;
	}
	
	public boolean getIsMappingPoint() {
		return this.isMappingPoint;
	}
	
	public void setIsMappingPoint(boolean isMappingPoint) {
		this.isMappingPoint = isMappingPoint;
	}

	@Override
	public void addProperties(List<TranslatableMessage> list) {
		AuditEventType.addPropertyMessage(list, "dsEdit.datafile.mappingPoint", isMappingPoint);
		AuditEventType.addPropertyMessage(list, "dsEdit.datafile.toXid", toXid);
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.fromIdentifer", fromIdentifier);
        AuditEventType.addDataTypeMessage(list, "dsEdit.pointDataType", dataTypeId);
	}

	@Override
	public void addPropertyChanges(List<TranslatableMessage> list, Object o) {
		DataFilePointLocatorVO from = (DataFilePointLocatorVO) o;
		AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.mappingPoint", from.isMappingPoint, isMappingPoint);
		AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.toXid", from.toXid, toXid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.fromIdentifier", from.fromIdentifier, fromIdentifier);
        AuditEventType.maybeAddDataTypeChangeMessage(list, "dsEdit.pointDataType", from.dataTypeId, dataTypeId);
	}
	
    //
    //
    // Serialization
    //
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeBoolean(isMappingPoint);
        SerializationHelper.writeSafeUTF(out, toXid);
        SerializationHelper.writeSafeUTF(out, fromIdentifier);
        out.writeInt(dataTypeId);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
        	isMappingPoint = in.readBoolean();
        	toXid = SerializationHelper.readSafeUTF(in);
        	fromIdentifier = SerializationHelper.readSafeUTF(in);
        	dataTypeId = in.readInt();
        }
    }

	@Override
	public void jsonRead(JsonReader arg0, JsonObject arg1) throws JsonException {
		//TODO Support json serialization
	}

	@Override
	public void jsonWrite(ObjectWriter arg0) throws IOException, JsonException {
		//TODO Support json serialization
	}
}
