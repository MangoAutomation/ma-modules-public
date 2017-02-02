/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.envcan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;

/**
 * @author Matthew Lohbihler
 */
public class EnvCanDataSourceVO extends DataSourceVO<EnvCanDataSourceVO> {
    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
        ets.add(createEventType(EnvCanDataSourceRT.DATA_RETRIEVAL_FAILURE_EVENT, new TranslatableMessage(
                "event.ds.dataSource"), EventType.DuplicateHandling.IGNORE_SAME_MESSAGE, AlarmLevels.URGENT));
        ets.add(createEventType(EnvCanDataSourceRT.PARSE_EXCEPTION_EVENT, new TranslatableMessage("event.ds.dataParse")));
        ets.add(createPollAbortedEventType(EnvCanDataSourceRT.POLL_ABORTED_EVENT));
    }
	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getPollAbortedExceptionEventId()
	 */
	@Override
	public int getPollAbortedExceptionEventId() {
		return EnvCanDataSourceRT.POLL_ABORTED_EVENT;
	}
	
    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(EnvCanDataSourceRT.DATA_RETRIEVAL_FAILURE_EVENT, "DATA_RETRIEVAL_FAILURE_EVENT");
        EVENT_CODES.addElement(EnvCanDataSourceRT.PARSE_EXCEPTION_EVENT, "PARSE_EXCEPTION");
        EVENT_CODES.addElement(EnvCanDataSourceRT.POLL_ABORTED_EVENT, "POLL_ABORTED");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public TranslatableMessage getConnectionDescription() {
        return new TranslatableMessage("envcands.dsconn", stationId);
    }

    @Override
    public EnvCanDataSourceRT createDataSourceRT() {
        return new EnvCanDataSourceRT(this);
    }

    @Override
    public EnvCanPointLocatorVO createPointLocator() {
        return new EnvCanPointLocatorVO();
    }

    @JsonProperty
    private int stationId;

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);

        if (stationId < 1)
            response.addContextualMessage("stationId", "validate.greaterThanZero", stationId);
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(stationId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            stationId = in.readInt();
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#asModel()
	 */
	@Override
	public EnvCanDataSourceModel asModel() {
		return new EnvCanDataSourceModel(this);
	}
}
