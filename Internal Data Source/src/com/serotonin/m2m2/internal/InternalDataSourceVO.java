/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
public class InternalDataSourceVO extends DataSourceVO<InternalDataSourceVO> {
    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
    	ets.add(createPollAbortedEventType(InternalDataSourceRT.POLL_ABORTED_EVENT));
    }
	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getPollAbortedExceptionEventId()
	 */
	@Override
	public int getPollAbortedExceptionEventId() {
		return InternalDataSourceRT.POLL_ABORTED_EVENT;
	}
	
    private static ExportCodes EVENT_CODES = new ExportCodes();
    static{
    	EVENT_CODES.addElement(InternalDataSourceRT.POLL_ABORTED_EVENT, "POLL_ABORTED");
    }
    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public TranslatableMessage getConnectionDescription() {
        return Common.getPeriodDescription(updatePeriodType, updatePeriods);
    }

    @Override
    public InternalDataSourceRT createDataSourceRT() {
        return new InternalDataSourceRT(this);
    }

    @Override
    public InternalPointLocatorVO createPointLocator() {
        return new InternalPointLocatorVO();
    }

    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonProperty
    private int updatePeriods = 5;
    @JsonProperty
    private String createPointsPattern;

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    public int getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }
    
    public String getCreatePointsPattern() {
        return createPointsPattern;
    }
    
    public void setCreatePointsPattern(String createPointsPattern) {
        this.createPointsPattern = createPointsPattern;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        if (updatePeriods <= 0)
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        if (!StringUtils.isEmpty(createPointsPattern)) {
            try {
                Pattern.compile(createPointsPattern);
            } catch(PatternSyntaxException e) {
                response.addContextualMessage("createPointsPattern", "validate.invalidRegex");
            }
        }
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        SerializationHelper.writeSafeUTF(out, createPointsPattern);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            createPointsPattern = null;
        } 
        else if (ver == 2) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            createPointsPattern = SerializationHelper.readSafeUTF(in);
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writeUpdatePeriodType(writer, updatePeriodType);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        Integer value = readUpdatePeriodType(jsonObject);
        if (value != null)
            updatePeriodType = value;
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#asModel()
	 */
	@Override
	public AbstractDataSourceModel<InternalDataSourceVO> asModel() {
		return new InternalDataSourceModel(this);
	}
}
