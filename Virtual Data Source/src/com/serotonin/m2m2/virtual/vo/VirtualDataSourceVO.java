/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.virtual.rt.VirtualDataSourceRT;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

public class VirtualDataSourceVO extends DataSourceVO<VirtualDataSourceVO> {
    

	private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(VirtualDataSourceRT.POLL_ABORTED_EVENT, "POLL_ABORTED");
    }
    
    
	@Override
    protected void addEventTypes(List<EventTypeVO> ets) {
    	 ets.add(createPollAbortedEventType(VirtualDataSourceRT.POLL_ABORTED_EVENT));
    }
    
	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getPollAbortedExceptionEventId()
	 */
	@Override
	public int getPollAbortedExceptionEventId() {
		return VirtualDataSourceRT.POLL_ABORTED_EVENT;
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
    public DataSourceRT<VirtualDataSourceVO> createDataSourceRT() {
        return new VirtualDataSourceRT(this);
    }

    @Override
    public VirtualPointLocatorVO createPointLocator() {
        return new VirtualPointLocatorVO();
    }

    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonProperty
    private int updatePeriods = 5;
    @JsonProperty
    private boolean polling = true;

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
    public boolean isPolling() {
    	return polling;
    }
    public void setPolling(boolean polling) {
    	this.polling = polling;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        if (updatePeriods <= 0)
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeBoolean(polling);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            updatePeriodType = Common.TimePeriods.SECONDS;
            updatePeriods = in.readInt();
            polling = true;
        }
        else if (ver == 2) {
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            polling = true;
        }
        else if (ver == 3) {
        	updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            polling = in.readBoolean();
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
	public AbstractDataSourceModel<VirtualDataSourceVO> asModel() {
		return new VirtualDataSourceModel(this);
	}
}
