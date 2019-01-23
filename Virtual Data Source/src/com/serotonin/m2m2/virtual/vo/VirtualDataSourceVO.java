/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
import com.serotonin.m2m2.vo.dataSource.PollingDataSourceVO;

public class VirtualDataSourceVO extends PollingDataSourceVO<VirtualDataSourceVO> {
    

	private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(VirtualDataSourceRT.POLL_ABORTED_EVENT, POLL_ABORTED);
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

    @JsonProperty
    private boolean polling = true;
    private long delay;

    public boolean isPolling() {
    	return polling;
    }
    public void setPolling(boolean polling) {
    	this.polling = polling;
    }

	public long getDelay(){
		return delay;
	}
	
	public void setDelay(long delay){
		this.delay = delay;
	}
    
    @Override
    public void validate(ProcessResult response) {
        super.validate(response);
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 4;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
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
        }else if(ver == 4) {
            polling = in.readBoolean();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
		//Hidden value to allow testing aborted polls by setting delay to longer than poll period
		if(jsonObject.containsKey("delay"))
			delay = jsonObject.getLong("delay");
    }
}
