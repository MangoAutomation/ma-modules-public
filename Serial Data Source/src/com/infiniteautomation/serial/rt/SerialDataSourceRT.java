package com.infiniteautomation.serial.rt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.infiniteautomation.mango.io.serial.SerialPortProxyEvent;
import com.infiniteautomation.mango.io.serial.SerialPortProxyEventListener;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.io.StreamUtils;
import com.serotonin.log.RollingIOLog;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.util.queue.ByteQueue;

public class SerialDataSourceRT extends PollingDataSource implements SerialPortProxyEventListener{
	private final Log LOG = LogFactory.getLog(SerialDataSourceRT.class);
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    public static final int POINT_READ_PATTERN_MISMATCH_EVENT = 4;
    
	private SerialPortProxy port; //Serial Communication Port
	private ByteQueue buffer; //Max size is Max Message Size
	private TimeoutTask timeoutTask; //Task to retrieve buffer contents after timeout
	private SerialDataSourceVO vo;
	private long lastRxTime; //Time we last received a message, used to determine message timeout (not wall clock time)
	private int timeout;
	
	private RollingIOLog ioLog;
	
	public SerialDataSourceRT(SerialDataSourceVO vo) {
		super(vo);
		this.vo = vo;
		buffer = new ByteQueue(vo.getMaxMessageSize());
		this.timeout = vo.getReadTimeout() * 1000000; //Timeout in MS from user (nanoseconds here)

	}


	/**
	 * Connect to a serial port
	 * @param portName
	 * @throws Exception 
	 */
	public boolean connect() throws Exception{
		
        if (Common.serialPortManager.portOwned(vo.getCommPortId())){
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portInUse",vo.getCommPortId()));
			return false;
        }else{
        	try{
                this.port = Common.serialPortManager.open(
                		"Mango Serial Data Source",
                		vo.getCommPortId(),
                		vo.getBaudRate(),
                		vo.getFlowControlIn(),
                		vo.getFlowControlOut(),
                		vo.getDataBits(),
                		vo.getStopBits(),
                		vo.getParity());
                this.port.addEventListener(this);
                return true;
              
            }catch(Exception e){
    			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portError",vo.getCommPortId(),e.getLocalizedMessage()));
    			return false;
            }
        }

    }
	
    @Override
    public void initialize() {
    	boolean connected = false;
    	try{
            if (this.vo.isLogIO()) {
                //PrintWriter log = new PrintWriter(new FileWriter(file, true));
            	int fileSize = (int)(vo.getIoLogFileSizeMBytes() * 1000000f);
            	int maxFiles = vo.getMaxHistoricalIOLogs();

                ioLog = new RollingIOLog(getIOLogFileName(vo.getId()), Common.getLogsDir(), fileSize, maxFiles);
                ioLog.log("Data source started");
            }
    		connected = this.connect();
    	}catch(Exception e){
    		LOG.debug("Error while initializing data source", e);
    		String msg = e.getMessage();
    		if(msg == null){
    			msg = "Unknown";
    		}
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.connectFailed",msg));
			
    	}
    	
    	if(connected){
    		returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
    	}
    	this.lastRxTime = System.nanoTime(); //Nanosecond accuracy
    	super.initialize();
    }
    @Override
    public void terminate() {
        super.terminate();
        if(this.port != null)
			try {
				Common.serialPortManager.close(this.port);
			} catch (SerialPortException e) {
	    		LOG.debug("Error while closing serial port", e);
				raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portError",this.port.getCommPortId(),e.getLocalizedMessage()));

			}

        if(this.vo.isLogIO()){
        	this.ioLog.log("Data source stopped");
        	this.ioLog.close();
    	}
    }
    
	@Override
	public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime,
			SetPointSource source) {

		//Are we connected?
		if(this.port == null){
			raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.writeFailedPortNotSetup"));
			return;
		}
		
		try {
			OutputStream os = this.port.getOutputStream();

			//Create Message from Message Start 
	        SerialPointLocatorRT pl = dataPoint.getPointLocator();
	        
	        byte[] data;

	        if(this.vo.isHex()){
	        	//Convert to Hex
	        	try{
	        		switch(dataPoint.getDataTypeId()){
	        		case DataTypes.ALPHANUMERIC:
	        			data = convertToHex(valueTime.getStringValue());
	        			break;
	        		case DataTypes.BINARY:
	        			if(valueTime.getBooleanValue())
	        				data = convertToHex("00");
	        			else
	        				data = convertToHex("01");
	        			break;
	        		case DataTypes.MULTISTATE:
	        			String intValue = Integer.toString(valueTime.getIntegerValue());
	        			if(intValue.length()%2 != 0)
	        				intValue = "0" + intValue;
	        			data = convertToHex(intValue);
	        			break;
	        		case DataTypes.NUMERIC:
	        			String numValue = Integer.toString(valueTime.getIntegerValue());
	        			if(numValue.length()%2 != 0)
	        				numValue = "0" + numValue;
	        			data = convertToHex(numValue);
	        			break;
	        		default:
	        			throw new ShouldNeverHappenException("Unsupported data type" + dataPoint.getDataTypeId());
	        		}
	        		
	        	}catch(Exception e){
	        		LOG.error(e.getMessage(),e);
	    			raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.notHex"));
	    			return;
	        	}
	        }else{
	        	//Pin the terminator on the end
				String messageTerminator = ((SerialDataSourceVO)this.getVo()).getMessageTerminator();
	        	
		        //Do we need to or is it already on the end?
		        String identifier = pl.getVo().getPointIdentifier();
		        
		        String fullMsg = identifier +  valueTime.getStringValue();
		        if(!fullMsg.endsWith(messageTerminator)){
		        	fullMsg +=  messageTerminator;
		        }
		        
		      //String output = newValue.getStringValue();
		      data = fullMsg.getBytes();
	        }
	        if(this.vo.isLogIO())
	        	this.ioLog.log(false, data);
	        
			for(byte b : data){
				os.write(b);
			}
			os.flush();
			
			returnToNormal(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.writeFailed",e.getMessage()));
			//Try and reset the connection
			try{
				if(this.port != null){
					Common.serialPortManager.close(this.port);
					this.connect();
				}
			}catch(Exception e2){
				LOG.error("Error re-connecting to serial port.", e2);
			}
		}
		
		
	}
	
	@Override
	public void serialEvent(SerialPortProxyEvent evt){
		//Keep a lock on the buffer while we do this
		synchronized(this.buffer){
			//If our port is dead, say so
			if(this.port == null){
				raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailedPortNotSetup"));
				return;
			}
			
			//The first message that we might process
			String msg = null;
			
            
            //For message timeout we will capture the delay between reads
			long rxTime = System.nanoTime();
            long rxDelay = rxTime - this.lastRxTime;
            this.lastRxTime = rxTime;
			
            //If we have experienced a delay long enough to be a message, pop it 
            // before we read in the next message
            if((this.timeout > 0)&&(rxDelay >= this.timeout)){
            	//We have a timeout, pop everything into the message and assume its a message
    			if(this.vo.isHex()){
    				msg = convertFromHex(buffer.popAll());
        		}else{
        			msg = new String(buffer.popAll(), Common.UTF8_CS);
        		}
            	
            }
            
            
			//Read the data in from the port
			try{
				InputStream in = this.port.getInputStream();
				SerialDataSourceVO vo = ((SerialDataSourceVO)this.getVo());
	            int data;
	            //Read in all the data we can from the InputStream
	            // this may not be the full message, or may read multiple messages
	            while (( data = in.read()) > -1 ){
	            	if(buffer.size() >= this.vo.getMaxMessageSize()){
	            		buffer.popAll();
	    				raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed", "Max message size reached!"));
	    				return; //Give up
	            	}
	            	buffer.push(data);
	            }
	            
	            //Log our buffer contents 
            	byte[] logMsg = buffer.peekAll();
            	if(this.vo.isLogIO())
    	        	this.ioLog.log(true, logMsg);
    			if(this.vo.isHex()){
    				if(LOG.isDebugEnabled())
    					LOG.debug("Buffer after read: " + StreamUtils.dumpHex(logMsg, 0, logMsg.length));
        		}else{
        			if(LOG.isDebugEnabled())
        				LOG.debug("Buffer after read: " + new String(logMsg, Common.UTF8_CS));
        		}
	            
	            //We either use a terminator OR we use a Timeout
	            if(vo.getUseTerminator()) {
	            	
	                String messageRegex = vo.getMessageRegex(); //"!([A-Z0-9]{3,3})([a-zA-Z])(.*);";
	                //DS Information
	                int pointIdentifierIndex = vo.getPointIdentifierIndex();
	            	
	            	//First check if the previous message timed out
	            	if(msg != null){
	            		String[] messages = msg.split("(?<=" + this.vo.getMessageTerminator() + ")");
	            		for(String message : messages) {
		            		if(message.contains(this.vo.getMessageTerminator())){
	                			if(LOG.isDebugEnabled())
	                    			LOG.debug("Matching will use String: " + message);
	                			matchPointValues(message, messageRegex, pointIdentifierIndex);
			            		returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
	                		}
	            		}
	            	}
	            	
	            	
	            	//Using the terminator
	            	//Create a String so we can use Regex and matching
	    	        byte[] b = buffer.peekAll();
	            	
        			if(this.vo.isHex()){
        				msg = convertFromHex(b);
            		}else{
            			msg = new String(b, Common.UTF8_CS);
            		}
            		
            		//Now we have a string that contains the entire contents of the buffer,
            		// split on terminator, keep it on the end of the message and process any full messages
            		// and pop them from the buffer
            		String[] messages = msg.split("(?<=" + this.vo.getMessageTerminator() + ")");
            		for(String message : messages){
            			//Does our message contain the terminator?
            			//It should be impossible to have a non-terminated message
            			// that is before a message with a terminator in the buffer
            			// so it is assumed here that popping from the buffer will 
            			// not cause any issues. As the only data left in the buffer will 
            			// potentially be one incomplete message.
                   		if(message.contains(this.vo.getMessageTerminator())){
                   			//Pop off this message
                   			this.buffer.pop(message.length());
                			if(LOG.isDebugEnabled())
                    			LOG.debug("Matching will use String: " + message);
                			matchPointValues(message, messageRegex, pointIdentifierIndex);
		            		returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
                		}
            		}
	            	return;
	            }else{
	            	
	            	if(this.timeoutTask != null){
	            		this.timeoutTask.cancel();
	            	}
	            	
	            	//Setup a Timeout Task to fire after the message timeout
	            	if(this.buffer.size() > 0)
	            		this.timeoutTask = new TimeoutTask(this.vo.getReadTimeout(), new SerialTimeoutClient(this));
	            	
	            	//Do we have a timeout generated message?
	            	if(!StringUtils.isEmpty(msg)){
		            	//Just do a match on the Entire Message because we are not using Terminator
		            	//String messageRegex = ".*"; //Match everything
		            	//int pointIdentifierIndex = 0; //Whole message
		            	String messageRegex = vo.getMessageRegex(); //"!([A-Z0-9]{3,3})([a-zA-Z])(.*);";
		                //DS Information
		                int pointIdentifierIndex = vo.getPointIdentifierIndex();
		            	matchPointValues(msg, messageRegex, pointIdentifierIndex);
		            	returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
	            	}
	            }
			}catch(Exception e){
				LOG.error(e.getMessage(),e);
	        	this.buffer.popAll(); //Ensure we clear out the buffer...
				raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed",e.getMessage()));
				
			}
		}//End synch
	}
	
	


	/**
	 * Match point values from a message. 
	 * 1. overall message is checked for a match
	 * 2. the 
	 * then individual points try to extract a value from the point identifier string
	 * 
	 * 
	 * @param msg - Message to use for match
	 * @param messageRegex - Pattern to match with
	 * @param pointIdentifierIndex - Index to use from messageRegex Pattern
	 */
	private void matchPointValues(String msg, String messageRegex, int pointIdentifierIndex) {

		boolean matcherFailed = false;
		
		if(!this.dataPoints.isEmpty()){
        	Pattern messagePattern = Pattern.compile(messageRegex);
        	Matcher messageMatcher = messagePattern.matcher(msg);
            if(messageMatcher.matches()){
            	if(LOG.isDebugEnabled())
            		LOG.debug("Message matched regex: " + messageRegex);
                //Parse out the Identifier
            	String pointIdentifier = messageMatcher.group(pointIdentifierIndex);
            	if(LOG.isDebugEnabled())
            		LOG.debug("Point Identified: " + pointIdentifier);
            	
            	//Update all points that have this Identifier
            	for(DataPointRT dp: this.dataPoints){
            		SerialPointLocatorRT pl = dp.getPointLocator();
            		SerialPointLocatorVO plVo = pl.getVo();
            		if(plVo.getPointIdentifier().equals(pointIdentifier)){
            			Pattern pointValuePattern = Pattern.compile(plVo.getValueRegex());
            			Matcher pointValueMatcher = pointValuePattern.matcher(msg); //Use the index from the above message
                    	if(pointValueMatcher.find()){
                        	String value = pointValueMatcher.group(plVo.getValueIndex());
                        	if(LOG.isDebugEnabled()){
                        		LOG.debug("Point Value matched regex: " + plVo.getValueRegex() + " and extracted value " + value);
                        	}
                        	
                        	//Parse out the value
                        	DataValue dataValue = null;
                        	if(this.vo.isHex()){
                    			try{
	                    			byte[] data = convertToHex(value);
	                    			
	                    			switch(plVo.getDataTypeId()){
	                    				case DataTypes.ALPHANUMERIC:
	                    					dataValue = new AlphanumericValue(new String(data, Common.UTF8_CS));
	                    				break;
	                    				case DataTypes.BINARY:
	                    					if(data.length > 0){
	                    						dataValue = new BinaryValue((data[0]==1)?true:false);
	                    					}
	                    				break;
	                    				case DataTypes.MULTISTATE:
	                    					ByteBuffer buffer = ByteBuffer.wrap(data);
	                    					if(data.length == 2)
	                    						dataValue = new MultistateValue(buffer.getShort());
	                    					else
	                    						dataValue = new MultistateValue(buffer.getInt());
	                    				break;
	                    				case DataTypes.NUMERIC:
	                    					ByteBuffer nBuffer = ByteBuffer.wrap(data);
	                    					if(data.length == 4)
	                    						dataValue = new NumericValue(nBuffer.getFloat());
	                    					else
	                    						dataValue = new NumericValue(nBuffer.getDouble());
	                    				break;
	                    				default:
	                    					throw new ShouldNeverHappenException("Un-supported data type: " + plVo.getDataTypeId());
	                    			}
                    			}catch(Exception e){
                    				LOG.error(e.getMessage(),e);
                    			}
                    		}else{
                    			dataValue = DataValue.stringToValue(value, plVo.getDataTypeId());
                    		}
                        	
                        	if(dataValue != null){
	                        	PointValueTime newValue = new PointValueTime(dataValue,Common.timer.currentTimeMillis());
	                    		dp.updatePointValue(newValue);
                        		if(LOG.isDebugEnabled())
                        			LOG.debug("Saving value: " + newValue.toString());
                        	}else{
                            	raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(),true,new TranslatableMessage("event.serial.invalidValue",dp.getVO().getXid()));
                            	matcherFailed = true;
                        	}
                    	
                    	}//end if value matches
                	}//end for this point id
            	}
            }else{
            	raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(),true,new TranslatableMessage("event.serial.patternMismatch",vo.getMessageRegex(),msg));
            	matcherFailed = true;
            }
            
        }
		
		//If no failures...
		if(!matcherFailed)
			returnToNormal(POINT_READ_PATTERN_MISMATCH_EVENT, System.currentTimeMillis());
	}


//	/**
//	 * This method might be dumpable if we choose to go with the timeout option
	// A loop within a recursive function is cause for worry, this will be slow
//	 * @param msg
//	 * @param depth
//	 */
//	private void searchRegex(String msg, int depth) {
//		if(depth > 255)
//			return;
//		for(DataPointRT rt : dataPoints) {
//    		Pattern p = ((SerialPointLocatorRT)rt.getPointLocator()).getPattern();
//    		Matcher m = p.matcher(msg);
//    		if(m.find()) { //Could use length consumed to allow many points to receive values from the same strings
//    			SerialPointLocatorRT pl = rt.getPointLocator();
//        		SerialPointLocatorVO plVo = pl.getVo();
//    			String value = m.group(plVo.getValueIndex());                	
//            	PointValueTime newValue = new PointValueTime(DataValue.stringToValue(value, plVo.getDataTypeId()),
//            			Common.timer.currentTimeMillis());
//        		rt.updatePointValue(newValue);
//    			buffer.pop(m.group(0).length());
//    			index -= m.group(0).length();
//    			searchRegex(new String(buffer.peekAll()), depth+1);
//    			return;
//    		}
//    	}
//	}

	@Override
	protected void doPoll(long time) {
		//For now do nothing as we are event driven.
		if(this.port == null){
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailedPortNotSetup"));
			return;
		}
	}

	
	/**
	 * Convert a string value to HEX
	 * @param stringValue
	 * @return
	 */
	public static byte[] convertToHex(String stringValue) throws Exception{
		int len = stringValue.length();
		byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(stringValue.charAt(i), 16) << 4)
	                             + Character.digit(stringValue.charAt(i+1), 16));
	    }
	    
	    return data;
	}
	
	/**
	 * Convert a string value to HEX
	 * @param stringValue
	 * @return
	 */
	public  static String convertFromHex(byte[] hexValue) {
		return StreamUtils.dumpHex(hexValue, 0, hexValue.length);
	}
	
    public static String getIOLogFileName(int dataSourceId) {
        return "serialIO-" +  + dataSourceId + ".log" ;
    }
    
    
    class SerialTimeoutClient implements TimeoutClient{

    	private SerialDataSourceRT rt;
    	
    	public SerialTimeoutClient(SerialDataSourceRT rt){
    		this.rt = rt;
    	}
    	
		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.util.timeout.TimeoutClient#scheduleTimeout(long)
		 */
		@Override
		public void scheduleTimeout(long fireTime) {
			rt.serialEvent(new SerialPortProxyEvent(fireTime));
		}
    	
    }
    
    void forcePointReload() {
    	updateChangedPoints(System.currentTimeMillis());
    }
	
}
