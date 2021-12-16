package com.infiniteautomation.serial.vo;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import com.infiniteautomation.mango.io.serial.DataBits;
import com.infiniteautomation.mango.io.serial.FlowControl;
import com.infiniteautomation.mango.io.serial.Parity;
import com.infiniteautomation.mango.io.serial.StopBits;
import com.infiniteautomation.serial.rt.SerialDataSourceRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonNumber;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonString;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
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
    private FlowControl flowControlIn = FlowControl.NONE;
    private FlowControl flowControlOut = FlowControl.NONE;
    private DataBits dataBits = DataBits.DATA_BITS_8;
    private StopBits stopBits = StopBits.STOP_BITS_1;
    private Parity parity = Parity.NONE;
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
    @JsonProperty
    private int retries = 1;
    
	@Override
	public TranslatableMessage getConnectionDescription() {
		return new TranslatableMessage("serial.connection",this.commPortId);
	}

	@Override
	public SerialPointLocatorVO createPointLocator() {
		return new SerialPointLocatorVO();
	}

	@Override
	public SerialDataSourceRT createDataSourceRT() {
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
	
	public int getRetries() {
	    return retries;
	}
	
	public void setRetries(int retries) {
	    this.retries = retries;
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
        if (flowControlIn == null)
            response.addContextualMessage("flowControlIn", "validate.required");
        if (flowControlOut == null)
            response.addContextualMessage("flowControlOut", "validate.required");
        if (dataBits == null)
            response.addContextualMessage("dataBits", "validate.required");
        if (stopBits == null)
            response.addContextualMessage("stopBits", "validate.required");
        if (parity == null)
            response.addContextualMessage("parity", "validate.required");
        
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
        
        if(retries < 0)
            response.addContextualMessage("retries", "validate.cannotBeNegative");

     }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 6;
    //Track previous data types for field serialization
    private static final ObjectStreamField[] serialPersistentFields
        = { 
                new ObjectStreamField("flowControlIn", int.class),
                new ObjectStreamField("flowControlOut", int.class),
                new ObjectStreamField("dataBits", int.class),
                new ObjectStreamField("stopBits", int.class),
                new ObjectStreamField("parity", int.class)
          };
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, commPortId);
        out.writeInt(baudRate);
        out.writeInt(flowControlIn.value());
        out.writeInt(flowControlOut.value());
        out.writeInt(dataBits.value());
        out.writeInt(stopBits.value());
        out.writeInt(parity.value());
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
        out.writeInt(retries);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            //Convert legacy JSSC ints
            switch(in.readInt()) {
                case 1:
                    flowControlIn = FlowControl.RTSCTS;
                    break;
                case 4:
                    flowControlIn = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlIn = FlowControl.NONE;
                    break;
            }
            switch(in.readInt()){
                case 2:
                    flowControlOut = FlowControl.RTSCTS;
                    break;
                case 8:
                    flowControlOut = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlOut = FlowControl.NONE;
                    break;
            }
            dataBits = DataBits.fromValue(in.readInt());
            stopBits = StopBits.fromValue(in.readInt());
            parity = Parity.fromValue(in.readInt());
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
            retries = 1;
        }else if (ver == 2) {
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            //Convert legacy JSSC ints
            switch(in.readInt()) {
                case 1:
                    flowControlIn = FlowControl.RTSCTS;
                    break;
                case 4:
                    flowControlIn = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlIn = FlowControl.NONE;
                    break;
            }
            switch(in.readInt()){
                case 2:
                    flowControlOut = FlowControl.RTSCTS;
                    break;
                case 8:
                    flowControlOut = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlOut = FlowControl.NONE;
                    break;
            }
            dataBits = DataBits.fromValue(in.readInt());
            stopBits = StopBits.fromValue(in.readInt());
            parity = Parity.fromValue(in.readInt());
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
            retries = 1;
        }else if (ver == 3) {
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            //Convert legacy JSSC ints
            switch(in.readInt()) {
                case 1:
                    flowControlIn = FlowControl.RTSCTS;
                    break;
                case 4:
                    flowControlIn = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlIn = FlowControl.NONE;
                    break;
            }
            switch(in.readInt()){
                case 2:
                    flowControlOut = FlowControl.RTSCTS;
                    break;
                case 8:
                    flowControlOut = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlOut = FlowControl.NONE;
                    break;
            }
            dataBits = DataBits.fromValue(in.readInt());
            stopBits = StopBits.fromValue(in.readInt());
            parity = Parity.fromValue(in.readInt());
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
            retries = 1;
        }else if(ver == 4){
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            //Convert legacy JSSC ints
            switch(in.readInt()) {
                case 1:
                    flowControlIn = FlowControl.RTSCTS;
                    break;
                case 4:
                    flowControlIn = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlIn = FlowControl.NONE;
                    break;
            }
            switch(in.readInt()){
                case 2:
                    flowControlOut = FlowControl.RTSCTS;
                    break;
                case 8:
                    flowControlOut = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlOut = FlowControl.NONE;
                    break;
            }
            dataBits = DataBits.fromValue(in.readInt());
            stopBits = StopBits.fromValue(in.readInt());
            parity = Parity.fromValue(in.readInt());
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
            retries = 1;
        }else if(ver == 5){
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            //Convert legacy JSSC ints
            switch(in.readInt()) {
                case 1:
                    flowControlIn = FlowControl.RTSCTS;
                    break;
                case 4:
                    flowControlIn = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlIn = FlowControl.NONE;
                    break;
            }
            switch(in.readInt()){
                case 2:
                    flowControlOut = FlowControl.RTSCTS;
                    break;
                case 8:
                    flowControlOut = FlowControl.XONXOFF;
                    break;
                default:
                    flowControlOut = FlowControl.NONE;
                    break;
            }
            dataBits = DataBits.fromValue(in.readInt());
            stopBits = StopBits.fromValue(in.readInt());
            parity = Parity.fromValue(in.readInt());
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
            retries = in.readInt();
        }else if(ver == 6){
            commPortId = SerializationHelper.readSafeUTF(in);
            baudRate = in.readInt();
            flowControlIn = FlowControl.fromValue(in.readInt());
            flowControlOut = FlowControl.fromValue(in.readInt());
            dataBits = DataBits.fromValue(in.readInt());
            stopBits = StopBits.fromValue(in.readInt());
            parity = Parity.fromValue(in.readInt());
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
            retries = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("flowControlIn", flowControlIn);
        writer.writeEntry("flowControlOut", flowControlOut);
        writer.writeEntry("dataBits", dataBits);
        writer.writeEntry("stopBits", stopBits);
        writer.writeEntry("parity", parity);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        
        JsonValue value = jsonObject.get("flowControlIn");
        if(value != null) {
            if(value instanceof JsonString) {
                try{
                    flowControlIn = FlowControl.fromName(value.toString());
                }catch(IllegalArgumentException e) {
                    throw new TranslatableJsonException("emport.error.invalid", "flowControlIn", value, FlowControl.values());
                }
            }else if(value instanceof JsonNumber) {
                //Legacy integer value
                switch (((JsonNumber) value).intValue()) {
                    case 1:
                        flowControlIn = FlowControl.RTSCTS;
                        break;
                    case 4:
                        flowControlIn = FlowControl.XONXOFF;
                        break;
                    default:
                        flowControlIn = FlowControl.NONE;
                        break;
                }
            }
        }
        
        value = jsonObject.get("flowControlOut");
        if(value != null) {
            if(value instanceof JsonString) {
                try{
                    flowControlOut = FlowControl.fromName(value.toString());
                }catch(IllegalArgumentException e) {
                    throw new TranslatableJsonException("emport.error.invalid", "flowControlOut", value, FlowControl.values());
                }
            }else if(value instanceof JsonNumber) {
                //Legacy integer value
                switch (((JsonNumber) value).intValue()) {
                    case 2:
                        flowControlOut = FlowControl.RTSCTS;
                        break;
                    case 8:
                        flowControlOut = FlowControl.XONXOFF;
                        break;
                    default:
                        flowControlOut = FlowControl.NONE;
                        break;
                }
            }
        }
        
        value = jsonObject.get("dataBits");
        if(value != null) {
            try {
                if(value instanceof JsonString) {
                    dataBits = DataBits.fromName(value.toString());
                }else if(value instanceof JsonNumber) {
                    dataBits = DataBits.fromValue(((JsonNumber)value).intValue());
                }
            }catch(IllegalArgumentException e) {
                throw new TranslatableJsonException("emport.error.invalid", "dataBits", value, DataBits.values());
            }
        }
        
        value = jsonObject.get("stopBits");
        if(value != null) {
            try {
                if(value instanceof JsonString) {
                    stopBits = StopBits.fromName(value.toString());
                }else if(value instanceof JsonNumber) {
                    stopBits = StopBits.fromValue(((JsonNumber)value).intValue());
                }
            }catch(IllegalArgumentException e) {
                throw new TranslatableJsonException("emport.error.invalid", "stopBits", value, StopBits.values());
            }
        }
        
        if(value != null) {
            try {
                value = jsonObject.get("parity");
                if(value instanceof JsonString) {
                    parity = Parity.fromName(value.toString());
                }else if(value instanceof JsonNumber) {
                    parity = Parity.fromValue(((JsonNumber)value).intValue());
                }
            }catch(IllegalArgumentException e) {
                throw new TranslatableJsonException("emport.error.invalid", "parity", value, Parity.values());
            }
        }
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
}
