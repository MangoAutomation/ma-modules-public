/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class MBusDataSourceModel extends AbstractDataSourceModel<MBusDataSourceVO>{

	private MBusDataSourceVO data;
	/**
	 * @param data
	 */
	public MBusDataSourceModel(MBusDataSourceVO data) {
		super(data);
		this.data = data;
	}

	public MBusDataSourceModel() {
		super(new MBusDataSourceVO());
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return MBusDataSourceDefinition.DATA_SOURCE_TYPE;
	}
	
	@JsonGetter(value="pollPeriod")
	public TimePeriod getPollPeriod(){
	    return new TimePeriod(this.data.getUpdatePeriods(), 
	            TimePeriodType.convertTo(this.data.getUpdatePeriodType()));
	}

	@JsonSetter(value="pollPeriod")
	public void setPollPeriod(TimePeriod pollPeriod){
	    this.data.setUpdatePeriods(pollPeriod.getPeriods());
	    this.data.setUpdatePeriodType(TimePeriodType.convertFrom(pollPeriod.getType()));
	}

	@JsonGetter("commPortId")
	public String getCommPortId() {
	    return this.data.getCommPortId();
	}

	@JsonSetter("commPortId")
	public void setCommPortId(String commPortId) {
	    this.data.setCommPortId(commPortId);
	}

	@JsonGetter("connectionType")
	public String getConnectionType() {
		return this.data.getConnectionType().name();
	}

	@JsonSetter("connectionType")
	public void setConnectionType(String connectionType) {
	   this.data.setConnectionType(MBusConnectionType.valueOf(connectionType));
	}

	@JsonGetter("baudRate")
	public int getBaudRate() {
	    return this.data.getBaudRate();
	}

	@JsonSetter("baudRate")
	public void setBaudRate(int baudRate) {
	    this.data.setBaudRate(baudRate);
	}

	@JsonGetter("flowControlIn")
	public int getFlowControlIn() {
	    return this.data.getFlowControlIn();
	}

	@JsonSetter("flowControlIn")
	public void setFlowControlIn(int flowControlIn) {
	    this.data.setFlowControlIn(flowControlIn);
	}

	@JsonGetter("flowControlOut")
	public int getFlowControlOut() {
	    return this.data.getFlowControlOut();
	}

	@JsonSetter("flowControlOut")
	public void setFlowControlOut(int flowControlOut) {
	    this.data.setFlowControlOut(flowControlOut);
	}

	@JsonGetter("dataBits")
	public int getDataBits() {
	    return this.data.getDataBits();
	}

	@JsonSetter("dataBits")
	public void setDataBits(int dataBits) {
	    this.data.setDataBits(dataBits);
	}

	@JsonGetter("stopBits")
	public int getStopBits() {
	    return this.data.getStopBits();
	}

	@JsonSetter("stopBits")
	public void setStopBits(int stopBits) {
	    this.data.setStopBits(stopBits);
	}

	@JsonGetter("parity")
	public int getParity() {
	    return this.data.getParity();
	}

	@JsonSetter("parity")
	public void setParity(int parity) {
	    this.data.setParity(parity);
	}

	@JsonGetter("phoneNumber")
	public String getPhoneNumber() {
	    return this.data.getPhonenumber();
	}

	@JsonSetter("phoneNumber")
	public void setPhoneNumber(String phonenumber) {
	    this.data.setPhonenumber(phonenumber);
	}

	@JsonGetter("responseTimeoutOffset")
	public int getResponseTimeoutOffset() {
	    return this.data.getResponseTimeoutOffset();
	}

	@JsonSetter("responseTimeoutOffset")
	public void setResponseTimeoutOffset(int responseTimeoutOffset) {
	    this.data.setResponseTimeoutOffset(responseTimeoutOffset);
	}



}
