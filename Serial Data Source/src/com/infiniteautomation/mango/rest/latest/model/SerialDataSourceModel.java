/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.io.serial.DataBits;
import com.infiniteautomation.mango.io.serial.FlowControl;
import com.infiniteautomation.mango.io.serial.Parity;
import com.infiniteautomation.mango.io.serial.StopBits;
import com.infiniteautomation.mango.rest.latest.model.datasource.AbstractDataSourceModel;
import com.infiniteautomation.serial.SerialDataSourceDefinition;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;

import io.swagger.annotations.ApiModel;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value=SerialDataSourceDefinition.DATA_SOURCE_TYPE, parent=AbstractDataSourceModel.class)
public class SerialDataSourceModel extends AbstractDataSourceModel<SerialDataSourceVO>{
    
    
    private String commPortId;
    private int baudRate = 9600;
    private FlowControl flowControlIn;
    private FlowControl flowControlOut;
    private DataBits dataBits;
    private StopBits stopBits;
    private Parity parity;
    private int readTimeout;
    private boolean useTerminator;
    private String messageTerminator;
    private String messageRegex;
    private int pointIdentifierIndex;
    private boolean hex;
    private boolean logIO;
    private int maxMessageSize;
    private float ioLogFileSizeMBytes;
    private int maxHistoricalIOLogs;
    private int retries;
    
    
    public SerialDataSourceModel() {
        super();
    }
    
    public SerialDataSourceModel(SerialDataSourceVO data) {
        fromVO(data);
    }

    @Override
    public String getModelType() {
        return SerialDataSourceDefinition.DATA_SOURCE_TYPE;
    }
    
    @Override
    public SerialDataSourceVO toVO() {
        SerialDataSourceVO vo = super.toVO();
        vo.setCommPortId(commPortId);
        vo.setBaudRate(baudRate);
        vo.setFlowControlIn(flowControlIn);
        vo.setFlowControlOut(flowControlOut);
        vo.setDataBits(dataBits);
        vo.setStopBits(stopBits);
        vo.setParity(parity);
        vo.setReadTimeout(readTimeout);
        vo.setUseTerminator(useTerminator);
        vo.setMessageTerminator(messageTerminator);
        vo.setMessageRegex(messageRegex);
        vo.setPointIdentifierIndex(pointIdentifierIndex);
        vo.setHex(hex);
        vo.setLogIO(logIO);
        vo.setMaxMessageSize(maxMessageSize);
        vo.setIoLogFileSizeMBytes(ioLogFileSizeMBytes);
        vo.setMaxHistoricalIOLogs(maxHistoricalIOLogs);
        vo.setRetries(retries);
        return vo;
    }
    
    @Override
    public void fromVO(SerialDataSourceVO vo) {
        super.fromVO(vo);
        this.commPortId = vo.getCommPortId();
        this.baudRate = vo.getBaudRate();
        this.flowControlIn = vo.getFlowControlIn();
        this.flowControlOut = vo.getFlowControlOut();
        this.dataBits = vo.getDataBits();
        this.stopBits = vo.getStopBits();
        this.parity = vo.getParity();
        this.readTimeout = vo.getReadTimeout();
        this.useTerminator = vo.getUseTerminator();
        this.messageTerminator = vo.getMessageTerminator();
        this.messageRegex = vo.getMessageRegex();
        this.pointIdentifierIndex = vo.getPointIdentifierIndex();
        this.hex = vo.isHex();
        this.logIO = vo.isLogIO();
        this.maxMessageSize = vo.getMaxMessageSize();
        this.ioLogFileSizeMBytes = vo.getIoLogFileSizeMBytes();
        this.maxHistoricalIOLogs = vo.getMaxHistoricalIOLogs();
        this.retries = vo.getRetries();
    }

    /**
     * @return the commPortId
     */
    public String getCommPortId() {
        return commPortId;
    }

    /**
     * @param commPortId the commPortId to set
     */
    public void setCommPortId(String commPortId) {
        this.commPortId = commPortId;
    }

    /**
     * @return the baudRate
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * @param baudRate the baudRate to set
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * @return the flowControlIn
     */
    public FlowControl getFlowControlIn() {
        return flowControlIn;
    }

    /**
     * @param flowControlIn the flowControlIn to set
     */
    public void setFlowControlIn(FlowControl flowControlIn) {
        this.flowControlIn = flowControlIn;
    }

    /**
     * @return the flowControlOut
     */
    public FlowControl getFlowControlOut() {
        return flowControlOut;
    }

    /**
     * @param flowControlOut the flowControlOut to set
     */
    public void setFlowControlOut(FlowControl flowControlOut) {
        this.flowControlOut = flowControlOut;
    }

    /**
     * @return the dataBits
     */
    public DataBits getDataBits() {
        return dataBits;
    }

    /**
     * @param dataBits the dataBits to set
     */
    public void setDataBits(DataBits dataBits) {
        this.dataBits = dataBits;
    }

    /**
     * @return the stopBits
     */
    public StopBits getStopBits() {
        return stopBits;
    }

    /**
     * @param stopBits the stopBits to set
     */
    public void setStopBits(StopBits stopBits) {
        this.stopBits = stopBits;
    }

    /**
     * @return the parity
     */
    public Parity getParity() {
        return parity;
    }

    /**
     * @param parity the parity to set
     */
    public void setParity(Parity parity) {
        this.parity = parity;
    }

    /**
     * @return the readTimeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * @return the useTerminator
     */
    public boolean isUseTerminator() {
        return useTerminator;
    }

    /**
     * @param useTerminator the useTerminator to set
     */
    public void setUseTerminator(boolean useTerminator) {
        this.useTerminator = useTerminator;
    }

    /**
     * @return the messageTerminator
     */
    public String getMessageTerminator() {
        return messageTerminator;
    }

    /**
     * @param messageTerminator the messageTerminator to set
     */
    public void setMessageTerminator(String messageTerminator) {
        this.messageTerminator = messageTerminator;
    }

    /**
     * @return the messageRegex
     */
    public String getMessageRegex() {
        return messageRegex;
    }

    /**
     * @param messageRegex the messageRegex to set
     */
    public void setMessageRegex(String messageRegex) {
        this.messageRegex = messageRegex;
    }

    /**
     * @return the pointIdentifierIndex
     */
    public int getPointIdentifierIndex() {
        return pointIdentifierIndex;
    }

    /**
     * @param pointIdentifierIndex the pointIdentifierIndex to set
     */
    public void setPointIdentifierIndex(int pointIdentifierIndex) {
        this.pointIdentifierIndex = pointIdentifierIndex;
    }

    /**
     * @return the hex
     */
    public boolean isHex() {
        return hex;
    }

    /**
     * @param hex the hex to set
     */
    public void setHex(boolean hex) {
        this.hex = hex;
    }

    /**
     * @return the logIO
     */
    public boolean isLogIO() {
        return logIO;
    }

    /**
     * @param logIO the logIO to set
     */
    public void setLogIO(boolean logIO) {
        this.logIO = logIO;
    }

    /**
     * @return the maxMessageSize
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     */
    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * @return the ioLogFileSizeMBytes
     */
    public float getIoLogFileSizeMBytes() {
        return ioLogFileSizeMBytes;
    }

    /**
     * @param ioLogFileSizeMBytes the ioLogFileSizeMBytes to set
     */
    public void setIoLogFileSizeMBytes(float ioLogFileSizeMBytes) {
        this.ioLogFileSizeMBytes = ioLogFileSizeMBytes;
    }

    /**
     * @return the maxHistoricalIOLogs
     */
    public int getMaxHistoricalIOLogs() {
        return maxHistoricalIOLogs;
    }

    /**
     * @param maxHistoricalIOLogs the maxHistoricalIOLogs to set
     */
    public void setMaxHistoricalIOLogs(int maxHistoricalIOLogs) {
        this.maxHistoricalIOLogs = maxHistoricalIOLogs;
    }

    /**
     * @return the retries1
     */
    public int getRetries() {
        return retries;
    }

    /**
     * @param retries1 the retries1 to set
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }
    
    
}
