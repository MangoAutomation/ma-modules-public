/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infiniteautomation.mango.io.serial.DataBits;
import com.infiniteautomation.mango.io.serial.FlowControl;
import com.infiniteautomation.mango.io.serial.StopBits;
import com.infiniteautomation.serial.SerialDataSourceDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
@JsonPropertyOrder({"commPort", "baudRate"})
public class SerialDataSourceModel extends AbstractDataSourceModel<SerialDataSourceVO>{
	
	public SerialDataSourceModel(){
		super();
	}
	
	/**
	 * @param data
	 */
	public SerialDataSourceModel(SerialDataSourceVO data) {
		super(data);
	}

	@JsonGetter("commPortId")
	public String getCommPortId() {
	    return this.data.getCommPortId();
	}

	@JsonSetter("commPortId")
	public void setCommPortId(String commPortId) {
	    this.data.setCommPortId(commPortId);
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
	public FlowControl getFlowControlIn() {
	    return this.data.getFlowControlIn();
	}

	@JsonSetter("flowControlIn")
	public void setFlowControlIn(FlowControl flow) {
	    this.data.setFlowControlIn(flow);
	}

	@JsonGetter("flowControlOut")
	public int getFlowControlOut() {
	    return this.data.getFlowControlOut().value();
	}

	@JsonSetter("flowControlOut")
	public void setFlowControlOut(FlowControl flowControlOut) {
	    this.data.setFlowControlOut(flowControlOut);
	}

	@JsonGetter("dataBits")
	public DataBits getDataBits() {
	    return this.data.getDataBits();
	}

	@JsonSetter("dataBits")
	public void setDataBits(DataBits dataBits) {
	    this.data.setDataBits(dataBits);
	}

	@JsonGetter("stopBits")
	public StopBits getStopBits() {
	    return this.data.getStopBits();
	}

	@JsonSetter("stopBits")
	public void setStopBits(StopBits stopBits) {
	    this.data.setStopBits(stopBits);
	}

	@JsonGetter("readTimeout")
	public int getReadTimeout() {
	    return this.data.getReadTimeout();
	}

	@JsonSetter("readTimeout")
	public void setReadTimeout(int readTimeout) {
	    this.data.setReadTimeout(readTimeout);
	}

	@JsonGetter("useTerminator")
	public boolean isUseTerminator() {
	    return this.data.getUseTerminator();
	}

	@JsonSetter("useTerminator")
	public void setUseTerminator(boolean useTerminator) {
	    this.data.setUseTerminator(useTerminator);
	}

	@JsonGetter("messageTerminator")
	public String getMessageTerminator() {
	    return this.data.getMessageTerminator();
	}

	@JsonSetter("messageTerminator")
	public void setMessageTerminator(String messageTerminator) {
	    this.data.setMessageTerminator(messageTerminator);
	}

	@JsonGetter("messageRegex")
	public String getMessageRegex() {
	    return this.data.getMessageRegex();
	}

	@JsonSetter("messageRegex")
	public void setMessageRegex(String messageRegex) {
	    this.data.setMessageRegex(messageRegex);
	}

	@JsonGetter("pointIdentifierIndex")
	public int getPointIdentifierIndex() {
	    return this.data.getPointIdentifierIndex();
	}

	@JsonSetter("pointIdentifierIndex")
	public void setPointIdentifierIndex(int pointIdentifierIndex) {
	    this.data.setPointIdentifierIndex(pointIdentifierIndex);
	}

	@JsonGetter("hex")
	public boolean isHex() {
	    return this.data.isHex();
	}

	@JsonSetter("hex")
	public void setHex(boolean hex) {
	    this.data.setHex(hex);
	}

	@JsonGetter("logIO")
	public boolean isLogIO() {
	    return this.data.isLogIO();
	}

	@JsonSetter("logIO")
	public void setLogIO(boolean logIO) {
	    this.data.setLogIO(logIO);
	}

	@JsonGetter("maxMessageSize")
	public int getMaxMessageSize() {
	    return this.data.getMaxMessageSize();
	}

	@JsonSetter("maxMessageSize")
	public void setMaxMessageSize(int maxMessageSize) {
	    this.data.setMaxMessageSize(maxMessageSize);
	}

	@JsonGetter("ioLogFileSizeMBytes")
	public float getIoLogFileSizeMBytes() {
	    return this.data.getIoLogFileSizeMBytes();
	}

	@JsonSetter("ioLogFileSizeMBytes")
	public void setIoLogFileSizeMBytes(float ioLogFileSizeMBytes) {
	    this.data.setIoLogFileSizeMBytes(ioLogFileSizeMBytes);
	}

	@JsonGetter("maxHistoricalIOLogs")
	public int getMaxHistoricalIOLogs() {
	    return this.data.getMaxHistoricalIOLogs();
	}

	@JsonSetter("maxHistoricalIOLogs")
	public void setMaxHistoricalIOLogs(int maxHistoricalIOLogs) {
	    this.data.setMaxHistoricalIOLogs(maxHistoricalIOLogs);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return SerialDataSourceDefinition.DATA_SOURCE_TYPE;
	}

	
}
