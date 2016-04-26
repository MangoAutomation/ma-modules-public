/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.util.SerializationHelper;

public class MBusDataSourceVO extends DataSourceVO<MBusDataSourceVO> {
    private static final ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(MBusDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(MBusDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(MBusDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, "POINT_WRITE_EXCEPTION");
        EVENT_CODES.addElement(MBusDataSourceRT.POLL_ABORTED_EVENT, "POLL_ABORTED");
        
    }

    @JsonProperty
    private String commPortId;
    @JsonProperty
    private int updatePeriodType = Common.TimePeriods.DAYS;
    @JsonProperty
    private int updatePeriods = 1;
    @JsonProperty
    private MBusConnectionType connectionType = MBusConnectionType.SERIAL_DIRECT;

    @JsonProperty
    private int baudRate = 2400;
    @JsonProperty
    private int flowControlIn = 1;  //RTSCTS
    @JsonProperty
    private int flowControlOut = 2; //RTSCTS
    @JsonProperty
    private int dataBits = 8;
    @JsonProperty
    private int stopBits = 1;
    @JsonProperty
    private int parity = 2; //Event Parity
    
    // TODO implement
    @JsonProperty
    private String phonenumber = "";
    @JsonProperty
    private int responseTimeoutOffset = 1000;

    @Override
    protected void addEventTypes(List<EventTypeVO> eventTypes) {
        eventTypes.add(createEventType(MBusDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource")));
        eventTypes.add(createEventType(MBusDataSourceRT.POINT_READ_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointRead")));
        eventTypes.add(createEventType(MBusDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointWrite")));
        eventTypes.add(createPollAbortedEventType(MBusDataSourceRT.POLL_ABORTED_EVENT));
    }

	/*
	 * (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getPollAbortedExceptionEventId()
	 */
	@Override
	public int getPollAbortedExceptionEventId() {
		return MBusDataSourceRT.POLL_ABORTED_EVENT;
	}
	
    @Override
    public TranslatableMessage getConnectionDescription() {
        return new TranslatableMessage("common.default", commPortId);
    }

    @Override
    public PointLocatorVO createPointLocator() {
        return new MBusPointLocatorVO();
    }

    @Override
    public DataSourceRT createDataSourceRT() {
        return new MBusDataSourceRT(this);
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    public String getCommPortId() {
        return commPortId;
    }

    public void setCommPortId(String commPortId) {
        this.commPortId = commPortId;
    }

    public int getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    public int getResponseTimeoutOffset() {
		return responseTimeoutOffset;
	}

	public void setResponseTimeoutOffset(int responseTimeoutOffset) {
		this.responseTimeoutOffset = responseTimeoutOffset;
	}

	@Override
    public void validate(ProcessResult response) {
        super.validate(response);

        if (StringUtils.isBlank(commPortId)) {
            response.addContextualMessage("commPortId", "validate.required");
        }
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType, Common.TimePeriods.MILLISECONDS)) {
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        }
        if (updatePeriods <= 0) {
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        }
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 3;

    // Serialization for saveDataSource
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeUTF(connectionType.name());
        switch (connectionType) {
        case SERIAL_DIRECT:
            SerializationHelper.writeSafeUTF(out, commPortId);
            break;
        default:
        case SERIAL_AT_MODEM:
            // TODO Modem stuff goes here
            break;
        }

        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeInt(baudRate);
        out.writeInt(flowControlIn);
        out.writeInt(flowControlOut);
        out.writeInt(dataBits);
        out.writeInt(stopBits);
        out.writeInt(parity);
        out.writeInt(responseTimeoutOffset);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if((ver ==2)||(ver == 1)){
            connectionType = MBusConnectionType.valueOf(in.readUTF());
            switch (connectionType) {
            case SERIAL_DIRECT:
                commPortId = SerializationHelper.readSafeUTF(in);
                break;
            default:
            case SERIAL_AT_MODEM:
                // TODO modem stuff goes here
                break;
            }
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            baudRate = in.readInt();
            flowControlIn = in.readInt();
            flowControlOut = in.readInt();
            dataBits = in.readInt();
            stopBits = in.readInt();
            parity = in.readInt();
            responseTimeoutOffset = 1000;
        }else if(ver == 3){
            connectionType = MBusConnectionType.valueOf(in.readUTF());
            switch (connectionType) {
            case SERIAL_DIRECT:
                commPortId = SerializationHelper.readSafeUTF(in);
                break;
            default:
            case SERIAL_AT_MODEM:
                // TODO modem stuff goes here
                break;
            }
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            baudRate = in.readInt();
            flowControlIn = in.readInt();
            flowControlOut = in.readInt();
            dataBits = in.readInt();
            stopBits = in.readInt();
            parity = in.readInt();
            responseTimeoutOffset = in.readInt();
        }

    }

    public void setConnectionType(MBusConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * @return the connectionType
     */
    public MBusConnectionType getConnectionType() {
        return connectionType;
    }

    /**
     * Helper for JSP
     * 
     * @return
     */
    public boolean isSerialDirect() {
        return MBusConnectionType.SERIAL_DIRECT.equals(connectionType);
    }

    /**
     * Helper for JSP
     * 
     * @return
     */
    public boolean isSerialAtModem() {
        return MBusConnectionType.SERIAL_AT_MODEM.equals(connectionType);
    }

    /**
     * @return the flowControlIn
     */
    public int getFlowControlIn() {
        return flowControlIn;
    }

    /**
     * @param flowControlIn
     *            the flowControlIn to set
     */
    public void setFlowControlIn(int flowControlIn) {
        this.flowControlIn = flowControlIn;
    }

    /**
     * @return the baudRate
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * @param baudRate
     *            the baudRate to set
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * @return the flowControlOut
     */
    public int getFlowControlOut() {
        return flowControlOut;
    }

    /**
     * @param flowControlOut
     *            the flowControlOut to set
     */
    public void setFlowControlOut(int flowControlOut) {
        this.flowControlOut = flowControlOut;
    }

    /**
     * @return the dataBits
     */
    public int getDataBits() {
        return dataBits;
    }

    /**
     * @param dataBits
     *            the dataBits to set
     */
    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    /**
     * @return the stopBits
     */
    public int getStopBits() {
        return stopBits;
    }

    /**
     * @param stopBits
     *            the stopBits to set
     */
    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    /**
     * @return the parity
     */
    public int getParity() {
        return parity;
    }

    /**
     * @param parity
     *            the parity to set
     */
    public void setParity(int parity) {
        this.parity = parity;
    }

    /**
     * @return the phonenumber
     */
    public String getPhonenumber() {
        return phonenumber;
    }

    /**
     * @param phonenumber
     *            the phonenumber to set
     */
    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#getModel()
	 */
	@Override
	public AbstractDataSourceModel<MBusDataSourceVO> asModel() {
		return new MBusDataSourceModel(this);
	}
}