package com.infiniteautomation.asciifile.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.infiniteautomation.asciifile.rt.AsciiFileDataSourceRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
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
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.util.SerializationHelper;

/**
 * @author Phillip Dunlap
 */

@JsonEntity
public class AsciiFileDataSourceVO extends DataSourceVO<AsciiFileDataSourceVO>{
	
    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(AsciiFileDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(AsciiFileDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(AsciiFileDataSourceRT.POINT_READ_PATTERN_MISMATCH_EVENT, "POINT_READ_PATTERN_MISMATCH_EVENT");
   }
    
    @JsonProperty
    private String filePath;
    @JsonProperty
    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonProperty
    private int updatePeriods = 5;
    
	@Override
	public TranslatableMessage getConnectionDescription() {
		return new TranslatableMessage("file.path",this.filePath);
	}

	@Override
	public PointLocatorVO createPointLocator() {
		return new AsciiFilePointLocatorVO();
	}

	@Override
	public DataSourceRT createDataSourceRT() {
		return new AsciiFileDataSourceRT(this);
	}

	@Override
	public ExportCodes getEventCodes() {
		return EVENT_CODES;
	}

	@Override
	protected void addEventTypes(List<EventTypeVO> eventTypes) {
		eventTypes.add(createEventType(AsciiFileDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource")));
		eventTypes.add(createEventType(AsciiFileDataSourceRT.POINT_READ_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointRead")));	
	}

	public String getFilePath() {
		return this.filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
        AuditEventType.addPropertyMessage(list, "dsEdit.file.path", filePath);
        AuditEventType.addPeriodMessage(list, "dsEdit.updatePeriod", updatePeriodType, updatePeriods);
    }

    @Override
    protected void addPropertyChangesImpl(List<TranslatableMessage> list, AsciiFileDataSourceVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.file.path", from.filePath, filePath);
        AuditEventType.maybeAddPeriodChangeMessage(list, "dsEdit.updatePeriod", from.updatePeriodType,
                from.updatePeriods, updatePeriodType, updatePeriods);
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
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            this.filePath = SerializationHelper.readSafeUTF(in);
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
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

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getModel()
	 */
	@Override
	public AbstractDataSourceModel<AsciiFileDataSourceVO> getModel() {
		return new AsciiFileDataSourceModel(this);
	}
    
}
