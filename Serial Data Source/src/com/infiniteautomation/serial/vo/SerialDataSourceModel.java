/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
@JsonPropertyOrder({"commPort", "baudRate"})
public class SerialDataSourceModel extends AbstractDataSourceModel<SerialDataSourceVO>{
	
	public SerialDataSourceModel(){
		super(new SerialDataSourceVO());
	}
	
	/**
	 * @param data
	 */
	public SerialDataSourceModel(SerialDataSourceVO data) {
		super(data);
	}

	@JsonGetter("commPort")
	public String getCommPortId(){
		return ((SerialDataSourceVO) this.data).getCommPortId();
	}
	@JsonSetter("commPort")
	public void setCommPortId(String id){
		((SerialDataSourceVO) this.data).setCommPortId(id);
	}
	
	@JsonGetter("baudRate")
	public int getBaudRate(){
		return ((SerialDataSourceVO) this.data).getBaudRate();
	}
	@JsonSetter("baudRate")
	public void setBaudRate(int rate){
		((SerialDataSourceVO) this.data).setBaudRate(rate);
	}

//    @JsonProperty
//    private int flowControlIn = 0;
//    @JsonProperty
//    private int flowControlOut = 0;
//    @JsonProperty
//    private int dataBits = 8;
//    @JsonProperty
//    private int stopBits = 1;
//    @JsonProperty
//    private int parity = 0;
//    @JsonProperty
//    private int readTimeout = 1000; //Timeout in ms
//    @JsonProperty
//    private boolean useTerminator = true;
//    @JsonProperty
//    private String messageTerminator;
//    @JsonProperty
//    private String messageRegex;
//    @JsonProperty
//    private int pointIdentifierIndex;
//    @JsonProperty
//    private boolean hex = false; //Is the setup in Hex Strings?
//    @JsonProperty
//    private boolean logIO = false;
//    @JsonProperty
//    private int maxMessageSize = 1024;
//    @JsonProperty
//    private float ioLogFileSizeMBytes = 1.0f; //1MB
//    @JsonProperty
//    private int maxHistoricalIOLogs = 1;
	
	
	
}
