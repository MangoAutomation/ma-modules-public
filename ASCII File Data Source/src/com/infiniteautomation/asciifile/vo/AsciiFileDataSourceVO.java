package com.infiniteautomation.asciifile.vo;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.asciifile.AsciiFileSystemSettingsDefinition;
import com.infiniteautomation.asciifile.rt.AsciiFileDataSourceRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
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
        EVENT_CODES.addElement(AsciiFileDataSourceRT.POLL_ABORTED_EVENT, "POLL_ABORTED");
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
	public AsciiFilePointLocatorVO createPointLocator() {
		return new AsciiFilePointLocatorVO();
	}

	@Override
	public AsciiFileDataSourceRT createDataSourceRT() {
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
		eventTypes.add(createPollAbortedEventType(AsciiFileDataSourceRT.POLL_ABORTED_EVENT));
	}
	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getPollAbortedExceptionEventId()
	 */
	@Override
	public int getPollAbortedExceptionEventId() {
		return AsciiFileDataSourceRT.POLL_ABORTED_EVENT;
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
//        if (updatePeriods < 0)
//        	response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        if(!StringUtils.isEmpty(this.filePath)) {
        	File file = new File(this.filePath);
        	try {
        		this.filePath = file.getCanonicalPath();
        	} catch(IOException e) {
        		response.addContextualMessage("filePath", "dsEdit.file.ioexceptionCanonical", filePath);
        		return;
        	}
	        String restrictedPaths = SystemSettingsDao.instance.getValue(AsciiFileSystemSettingsDefinition.RESTRICTED_PATH);
	        if(!StringUtils.isEmpty(restrictedPaths))
		        for(String rPath : restrictedPaths.split(";")) {
		        	if(this.filePath.startsWith(rPath))
		        		response.addContextualMessage("filePath", "dsEdit.file.pathRestrictedBy", filePath);
		        }
        }
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
	public AsciiFileDataSourceModel asModel() {
		return new AsciiFileDataSourceModel(this);
	}
    
}
