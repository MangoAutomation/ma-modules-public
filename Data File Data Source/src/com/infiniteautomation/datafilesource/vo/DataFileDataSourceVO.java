package com.infiniteautomation.datafilesource.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.infiniteautomation.datafilesource.rt.DataFileDataSourceRT;
import com.infiniteautomation.datafilesource.vo.DataFileDataSourceVO;
import com.infiniteautomation.datafilesource.vo.DataFilePointLocatorVO;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;

public class DataFileDataSourceVO extends DataSourceVO<DataFileDataSourceVO> {
	private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(DataFileDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(DataFileDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(DataFileDataSourceRT.POINT_READ_PATTERN_MISMATCH_EVENT, "POINT_READ_PATTERN_MISMATCH_EVENT");
   }
    
    @JsonProperty
    private String filePath;
    @JsonProperty
    private boolean deleteAfterImport = false;
    @JsonProperty
    private boolean createPoints = false;
    @JsonProperty
    private String addedPrefix = "imported_";
    @JsonProperty
    private int fileType;
    @JsonProperty
    private String template;
    @JsonProperty
    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonProperty
    private int updatePeriods = 5;
    
	@Override
	public TranslatableMessage getConnectionDescription() {
		return new TranslatableMessage("dsEdit.datafile.path",this.filePath);
	}

	@Override
	public DataSourceRT createDataSourceRT() {
		return new DataFileDataSourceRT(this);
	}

	@Override
	public ExportCodes getEventCodes() {
		return EVENT_CODES;
	}

	@Override
	protected void addEventTypes(List<EventTypeVO> eventTypes) {
		eventTypes.add(createEventType(DataFileDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource")));
		eventTypes.add(createEventType(DataFileDataSourceRT.POINT_READ_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.poSntRead")));	
	}

	public String getFilePath() {
		return this.filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public boolean isDeleteAfterImport() {
		return this.deleteAfterImport; 
	}
	
	public void setDeleteAfterImport(boolean deleteAfterImport) {
		this.deleteAfterImport = deleteAfterImport;
	}
	
	public boolean getCreatePoints() {
		return this.createPoints;
	}
	
	public void setCreatePoints(boolean createPoints) {
		this.createPoints = createPoints;
	}
	
	public String getAddedPrefix() {
		return this.addedPrefix;
	}
	
	public void setAddedPrefix(String addedPrefix) {
		this.addedPrefix = addedPrefix;
	}
	
	public int getUpdatePeriods() {
		return this.updatePeriods;
	}
	
	public void setUpdatePeriods(int updatePeriods) {
		this.updatePeriods = updatePeriods;
	}
	
	public int getUpdatePeriodType() {
		return this.updatePeriodType;
	}
	
	public void setUpdatePeriodType(int updatePeriodType) {
		this.updatePeriodType = updatePeriodType;
	}
	
	public int getFileType() {
		return this.fileType;
	}
	
	public String getTypeName() {
    	switch(this.fileType) {
    	case DataFileDataSourceRT.XML_TYPE:
    		return "XML";
    	case DataFileDataSourceRT.CSV_TYPE:
    		return "CSV";
    	case DataFileDataSourceRT.EXCEL_TYPE:
    		return "Excel";
    	default:
    		return "unmapped";
    	}
    }
	
	public void setFileType(int fileType) {
		this.fileType = fileType;
	}
	
	public String getTemplate() {
		return this.template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	@Override
    public void validate(ProcessResult response) {
        super.validate(response);
		//TODO: ensure the path syntax is reasonable
        if (isBlank(this.filePath))
            response.addContextualMessage("filePath", "validate.required");
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
//        if (updatePeriods <= 0)
//            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        
     }

    @Override
    protected void addPropertiesImpl(List<TranslatableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.path", filePath);
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.deleteAfterImport", deleteAfterImport);
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.createPoints", createPoints);
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.importedPrefix", addedPrefix);
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.fileType", fileType);
        AuditEventType.addPropertyMessage(list, "dsEdit.datafile.template", template);
        AuditEventType.addPeriodMessage(list, "dsEdit.updatePeriod", updatePeriodType, updatePeriods);
    }

    @Override
    protected void addPropertyChangesImpl(List<TranslatableMessage> list, DataFileDataSourceVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.path", from.filePath, filePath);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.deleteAfterImport", from.deleteAfterImport, deleteAfterImport);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.createPoints", from.createPoints, createPoints);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.importedPrefix", from.addedPrefix, addedPrefix);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.fileType", from.fileType, fileType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.datafile.template", from.template, template);
        AuditEventType.maybeAddPeriodChangeMessage(list, "dsEdit.updatePeriod", from.updatePeriodType,
                from.updatePeriods, updatePeriodType, updatePeriods);
    }
    
    //Very ugly work around to make this into a datasource.
    //TODO: Move this functionality onto a system setting module
    @Override
	public PointLocatorVO createPointLocator() {
		return new DataFilePointLocatorVO();
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
        SerializationHelper.writeSafeUTF(out, this.filePath);
        out.writeBoolean(this.deleteAfterImport);
        out.writeBoolean(this.createPoints);
        SerializationHelper.writeSafeUTF(out, this.addedPrefix);
        out.writeInt(this.fileType);
        SerializationHelper.writeSafeUTF(out, this.template);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            this.filePath = SerializationHelper.readSafeUTF(in);
            this.deleteAfterImport = in.readBoolean();
            this.createPoints = in.readBoolean();
            this.addedPrefix = SerializationHelper.readSafeUTF(in);
            this.fileType = in.readInt();
            this.template = SerializationHelper.readSafeUTF(in);
            this.updatePeriodType = in.readInt();
            this.updatePeriods = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
    }
	
	public static boolean isBlank(CharSequence cs) {
		int strLen;
		if ((cs == null) || ((strLen = cs.length()) == 0))
			return true;

		for (int i = 0; i < strLen; ++i) {
			if (!(Character.isWhitespace(cs.charAt(i)))) {
				return false;
			}
		}
		return true;
	}
}
