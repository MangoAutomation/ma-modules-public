/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.rt.event.handlers.EventHandlerRT;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.handlers.AbstractEventHandlerModel;

/**
 * 
 * @author Terry Packer
 */
public class ReportEventHandlerVO extends AbstractEventHandlerVO<ReportEventHandlerVO>{

	private int activeReportId = Common.NEW_ID;
	private int inactiveReportId = Common.NEW_ID;
	
	public int getActiveReportId() {
		return activeReportId;
	}

	public void setActiveReportId(int activeReportId) {
		this.activeReportId = activeReportId;
	}

	public int getInactiveReportId() {
		return inactiveReportId;
	}

	public void setInactiveReportId(int inactiveReportId) {
		this.inactiveReportId = inactiveReportId;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.event.AbstractEventHandlerVO#createRuntime()
	 */
	@Override
	public EventHandlerRT<?> createRuntime() {
		return new ReportEventHandlerRT(this);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.event.AbstractEventHandlerVO#asModel()
	 */
	@Override
	public AbstractEventHandlerModel<?> asModel() {
		return new ReportEventHandlerModel(this);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.event.AbstractEventHandlerVO#jsonRead(com.serotonin.json.JsonReader, com.serotonin.json.type.JsonObject)
	 */
	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
		super.jsonRead(reader, jsonObject);
		String text = jsonObject.getString("activeReportXid");
        if (text != null){
        	ReportVO report = ReportDao.instance.getByXid(text);
        	if(report != null)
        		this.activeReportId = report.getId();
        }else
        	this.activeReportId = Common.NEW_ID;
        
		text = jsonObject.getString("inActiveReportXid");
        if (text != null){
        	ReportVO report = ReportDao.instance.getByXid(text);
        	if(report != null)
        		this.inactiveReportId = report.getId();
        }else
        	this.inactiveReportId = Common.NEW_ID;
        	
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.event.AbstractEventHandlerVO#jsonWrite(com.serotonin.json.ObjectWriter)
	 */
	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
		super.jsonWrite(writer);
		ReportVO report = ReportDao.instance.get(activeReportId);
		if(report != null)
			writer.writeEntry("activeReportXid", report.getXid());
		report = ReportDao.instance.get(inactiveReportId);
		if(report != null)
			writer.writeEntry("inActiveReportXid", report.getXid());		
	}
	
    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 8;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(activeReportId);
        out.writeInt(inactiveReportId);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
        	activeReportId = in.readInt();
        	inactiveReportId = in.readInt();
        }
    }
}
