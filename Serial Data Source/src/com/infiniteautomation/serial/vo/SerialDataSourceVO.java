package com.infiniteautomation.serial.vo;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.infiniteautomation.serial.rt.SerialDataSourceRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.DataSourceRT;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;
import com.serotonin.util.SerializationHelper;

@JsonEntity
public class SerialDataSourceVO extends DataSourceVO<SerialDataSourceVO>{
	
    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(SerialDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(SerialDataSourceRT.POINT_READ_EXCEPTION_EVENT, "POINT_READ_EXCEPTION");
        EVENT_CODES.addElement(SerialDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, "POINT_WRITE_EXCEPTION");
        EVENT_CODES.addElement(SerialDataSourceRT.POINT_READ_PATTERN_MISMATCH_EVENT, "POINT_READ_PATTERN_MISMATCH_EVENT");
   }
    
    @JsonProperty
    private String commPortId;
    @JsonProperty
    private int baudRate = 9600;
    @JsonProperty
    private int flowControlIn = 0;
    @JsonProperty
    private int flowControlOut = 0;
    @JsonProperty
    private int dataBits = 8;
    @JsonProperty
    private int stopBits = 1;
    @JsonProperty
    private int parity = 0;
    @JsonProperty
    private int readTimeout = 1000; //Timeout in ms
    @JsonProperty
    private boolean useTerminator = true;
    @JsonProperty
    private String messageTerminator;
    @JsonProperty
    private String messageRegex;
    @JsonProperty
    private int pointIdentifierIndex;
    @JsonProperty
    private boolean hex = false; //Is the setup in Hex Strings?
    @JsonProperty
    private boolean logIO = false;
    @JsonProperty
    private int maxMessageSize = 1024;
    @JsonProperty
    private float ioLogFileSizeMBytes = 1.0f; //1MB
    @JsonProperty
    private int maxHistoricalIOLogs = 1;
    
	@Override
	public TranslatableMessage getConnectionDescription() {
		return new TranslatableMessage("serial.connection",this.commPortId);
	}

	@Override
	public PointLocatorVO createPointLocator() {
		return new SerialPointLocatorVO();
	}

	@Override
	public DataSourceRT createDataSourceRT() {
		return new SerialDataSourceRT(this);
	}

	@Override
	public ExportCodes getEventCodes() {
		return EVENT_CODES;
	}

	@Override
	protected void addEventTypes(List<EventTypeVO> eventTypes) {
		eventTypes.add(createEventType(SerialDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.dataSource")));
		eventTypes.add(createEventType(SerialDataSourceRT.POINT_READ_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointRead")));
		eventTypes.add(createEventType(SerialDataSourceRT.POINT_WRITE_EXCEPTION_EVENT, new TranslatableMessage(
                "event.ds.pointWrite")));
		eventTypes.add(createEventType(SerialDataSourceRT.POINT_READ_PATTERN_MISMATCH_EVENT, new TranslatableMessage(
                "event.serial.patternMismatchException")));
		
	}
	public int getFlowControlMode() {
		return (this.getFlowControlIn() | this.getFlowControlOut());
	}

	public String getCommPortId() {
		return commPortId;
	}

	public void setCommPortId(String commPortId) {
		this.commPortId = commPortId;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getFlowControlIn() {
		return flowControlIn;
	}

	public void setFlowControlIn(int flowControlIn) {
		this.flowControlIn = flowControlIn;
	}

	public int getFlowControlOut() {
		return flowControlOut;
	}

	public void setFlowControlOut(int flowControlOut) {
		this.flowControlOut = flowControlOut;
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}
	
    public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
	public boolean getUseTerminator() {
		return useTerminator;
	}
	
	public void setUseTerminator(boolean useTerminator) {
		this.useTerminator = useTerminator;
	}

	public String getMessageTerminator() {
		return messageTerminator;
	}

	public void setMessageTerminator(String messageTerminator) {
		this.messageTerminator = messageTerminator;
	}

	public String getMessageRegex() {
		return messageRegex;
	}

	public void setMessageRegex(String messageRegex) {
		this.messageRegex = messageRegex;
	}

	public int getPointIdentifierIndex() {
		return pointIdentifierIndex;
	}

	public void setPointIdentifierIndex(int pointIdentifierIndex) {
		this.pointIdentifierIndex = pointIdentifierIndex;
	}
	
	public boolean isHex() {
		return hex;
	}

	public void setHex(boolean hex) {
		this.hex = hex;
	}

	public boolean isLogIO() {
		return logIO;
	}

	public void setLogIO(boolean logIO) {
		this.logIO = logIO;
	}
	
	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}
    public float getIoLogFileSizeMBytes() {
		return ioLogFileSizeMBytes;
	}

	public void setIoLogFileSizeMBytes(float ioLogFileSizeMBytes) {
		this.ioLogFileSizeMBytes = ioLogFileSizeMBytes;
	}

	public int getMaxHistoricalIOLogs() {
		return maxHistoricalIOLogs;
	}

	public void setMaxHistoricalIOLogs(int maxHistoricalIOLogs) {
		this.maxHistoricalIOLogs = maxHistoricalIOLogs;
	}

	
    public String getIoLogPath() {
    	return new File(Common.getLogsDir(), SerialDataSourceRT.getIOLogFileName(getId())).getPath();
    }
	
	
	@Override
    public void validate(ProcessResult response) {
        super.validate(response);
        if (isBlank(commPortId))
            response.addContextualMessage("commPortId", "validate.required");
        if (baudRate <= 0)
            response.addContextualMessage("baudRate", "validate.invalidValue");
        if (!(flowControlIn == 0 || flowControlIn == 1 || flowControlIn == 4))
            response.addContextualMessage("flowControlIn", "validate.invalidValue");
        if (!(flowControlOut == 0 || flowControlOut == 2 || flowControlOut == 8))
            response.addContextualMessage("flowControlOut", "validate.invalidValue");
        if (dataBits < 5 || dataBits > 8)
            response.addContextualMessage("dataBits", "validate.invalidValue");
        if (stopBits < 1 || stopBits > 3)
            response.addContextualMessage("stopBits", "validate.invalidValue");
        if (parity < 0 || parity > 4)
            response.addContextualMessage("parityBits", "validate.invalidValue");
        
        if(useTerminator) {
        	if(messageTerminator.length() <= 0)
        		response.addContextualMessage("messageTerminator", "validate.required");
        	 if (isBlank(messageRegex))
                 response.addContextualMessage("messageRegex", "validate.required");
        	 if(pointIdentifierIndex < 0)
             	response.addContextualMessage("pointIdentifierIndex", "validate.invalidValue");
        	 
        	 if(hex){
        		 if(!messageTerminator.matches("[0-9A-Fa-f]+")){
        			 response.addContextualMessage("messageTerminator", "serial.validate.notHex");
        		 }
        	 }
        	 
        }
        
        if(readTimeout <= 0)
        	response.addContextualMessage("readTimeout","validate.greaterThanZero");        
        
        if(maxMessageSize <= 0){
        	response.addContextualMessage("maxMessageSize","validate.greaterThanZero"); 
        }
        
        if (ioLogFileSizeMBytes <= 0)
            response.addContextualMessage("ioLogFileSizeMBytes", "validate.greaterThanZero");
        if (maxHistoricalIOLogs <= 0)
            response.addContextualMessage("maxHistoricalIOLogs", "validate.greaterThanZero");        

     }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 4;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, commPortId);
        out.writeInt(baudRate);
        out.writeInt(flowControlIn);
        out.writeInt(flowControlOut);
        out.writeInt(dataBits);
        out.writeInt(stopBits);
        out.writeInt(parity);
        SerializationHelper.writeSafeUTF(out, messageTerminator);
        out.writeInt(readTimeout);
        SerializationHelper.writeSafeUTF(out, messageRegex);
        out.writeInt(pointIdentifierIndex);
        out.writeBoolean(useTerminator);
        out.writeBoolean(hex);
        out.writeBoolean(logIO);
        out.writeInt(maxMessageSize);
        out.writeFloat(ioLogFileSizeMBytes);
        out.writeInt(maxHistoricalIOLogs);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            flowControlIn = in.readInt();
            flowControlOut = in.readInt();
            dataBits = in.readInt();
            stopBits = in.readInt();
            parity = in.readInt();
            messageTerminator = StringEscapeUtils.unescapeJava(SerializationHelper.readSafeUTF(in));
            readTimeout = in.readInt();
            messageRegex = SerializationHelper.readSafeUTF(in);
            pointIdentifierIndex = in.readInt();
            useTerminator = true;
            hex = false;
            logIO = false;
            maxMessageSize = 1024;
            ioLogFileSizeMBytes = 1;
            maxHistoricalIOLogs = 1;
        }
        if (ver == 2) {
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            flowControlIn = in.readInt();
            flowControlOut = in.readInt();
            dataBits = in.readInt();
            stopBits = in.readInt();
            parity = in.readInt();
            messageTerminator = SerializationHelper.readSafeUTF(in);
            readTimeout = in.readInt();
            messageRegex = SerializationHelper.readSafeUTF(in);
            pointIdentifierIndex = in.readInt();
            useTerminator = true;
            hex = false;
            logIO = false;
            maxMessageSize = 1024;
            ioLogFileSizeMBytes = 1;
            maxHistoricalIOLogs = 1;
        }
        if (ver == 3) {
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            flowControlIn = in.readInt();
            flowControlOut = in.readInt();
            dataBits = in.readInt();
            stopBits = in.readInt();
            parity = in.readInt();
            messageTerminator = SerializationHelper.readSafeUTF(in);
            readTimeout = in.readInt();
            messageRegex = SerializationHelper.readSafeUTF(in);
            pointIdentifierIndex = in.readInt();
            useTerminator = in.readBoolean();
            hex = false;
            logIO = false;
            maxMessageSize = 1024;
            ioLogFileSizeMBytes = 1;
            maxHistoricalIOLogs = 1;
        }
        if(ver == 4){
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            flowControlIn = in.readInt();
            flowControlOut = in.readInt();
            dataBits = in.readInt();
            stopBits = in.readInt();
            parity = in.readInt();
            messageTerminator = SerializationHelper.readSafeUTF(in);
            readTimeout = in.readInt();
            messageRegex = SerializationHelper.readSafeUTF(in);
            pointIdentifierIndex = in.readInt();
            useTerminator = in.readBoolean();
            hex = in.readBoolean();
            logIO = in.readBoolean();
            maxMessageSize = in.readInt();
            ioLogFileSizeMBytes = in.readFloat();
            maxHistoricalIOLogs = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
    }
	
	public static boolean isBlank(CharSequence cs) {
		int strLen;
		if ((cs == null) || ((strLen = cs.length()) == 0))
			return true;

		for (int i = 0; i < strLen; ++i) {
			if (!(Character.isWhitespace(cs.charAt(i)))) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.dataSource.DataSourceVO#asModel()
	 */
	@Override
	public AbstractDataSourceModel<SerialDataSourceVO> asModel() {
		return new SerialDataSourceModel(this);
	}
    
}
