package com.infiniteautomation.serial.rt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.io.serial.SerialPortException;
import com.serotonin.io.serial.SerialPortProxy;
import com.serotonin.io.serial.SerialPortProxyEvent;
import com.serotonin.io.serial.SerialPortProxyEventListener;
import com.serotonin.io.serial.SerialUtils;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import com.serotonin.util.queue.ByteQueue;

public class SerialDataSourceRT extends PollingDataSource implements SerialPortProxyEventListener{
	private final Log LOG = LogFactory.getLog(SerialDataSourceRT.class);
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    public static final int POINT_READ_PATTERN_MISMATCH_EVENT = 4;
    
	private SerialPortProxy port; //Serial Communication Port
	private ByteQueue buffer; //Max size TBD
	private byte[] terminator;
	private int index;
	
	
	public SerialDataSourceRT(SerialDataSourceVO vo) {
		super(vo);
		buffer = new ByteQueue(1024);
		SerialDataSourceVO properties = (SerialDataSourceVO)this.getVo();
		terminator = properties.getMessageTerminator().getBytes();
	}


	/**
	 * Connect to a serial port
	 * @param portName
	 * @throws Exception 
	 */
	public boolean connect () throws Exception{
		SerialDataSourceVO vo = (SerialDataSourceVO) this.getVo();
		
		SerialParameters params = new SerialParameters();
		params.setCommPortId(vo.getCommPortId());
        params.setPortOwnerName("Mango Serial Data Source");
        params.setBaudRate(vo.getBaudRate());
        params.setFlowControlIn(vo.getFlowControlIn());
        params.setFlowControlOut(vo.getFlowControlOut());
        params.setDataBits(vo.getDataBits());
        params.setStopBits(vo.getStopBits());
        params.setParity(vo.getParity());
        params.setRecieveTimeout(vo.getReadTimeout());
		
        if ( SerialUtils.portOwned(vo.getCommPortId()) ){
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portInUse",vo.getCommPortId()));
			return false;
        }else{
        	try{
                this.port = SerialUtils.openSerialPort(params);
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
    		connected = this.connect();
 // No longer able to detect this exact exception
//    	}catch(NoSuchPortException e1){
//    		SerialDataSourceVO vo = (SerialDataSourceVO)this.getVo();
//    		LOG.debug("No Such Port: " + vo.getCommPortId());
//			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.noSuchPort",vo.getCommPortId()));
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
        super.initialize();
    	
    }
    @Override
    public void terminate() {
        super.terminate();
        if(this.port != null)
			try {
				SerialUtils.close(this.port);
			} catch (SerialPortException e) {
	    		LOG.debug("Error while closing serial port", e);
				raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.portError",this.port.getParameters().getCommPortId(),e.getLocalizedMessage()));

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
			//Pin the terminator on the end
			String messageTerminator = ((SerialDataSourceVO)this.getVo()).getMessageTerminator();
	        messageTerminator = StringEscapeUtils.unescapeJava(messageTerminator);
	        
	        //Create Message from Message Start 
	        SerialPointLocatorRT pl = dataPoint.getPointLocator();
	        String identifier = pl.getVo().getPointIdentifier();
	        
	        //Do we need to or is it already on the end?
	        String fullMsg = identifier +  valueTime.getStringValue();
	        if(!fullMsg.endsWith(messageTerminator)){
	        	fullMsg +=  messageTerminator;
	        }
			//PointValueTime newValue = new PointValueTime(fullMsg,valueTime.getTime());

			//String output = newValue.getStringValue();
			byte[] data = fullMsg.getBytes();
			for(byte b : data){
				os.write(b);
			}
			os.flush();
			//Finally Set the point value (to the incoming one)
			//dataPoint.setPointValue(valueTime, source);
			returnToNormal(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis());
		} catch (IOException e) {
			raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.writeFailed",e.getMessage()));
		}
		
		
	}
	
	private boolean isTerminatorFound() {
		if(buffer.size() < terminator.length)
			return false;
		byte[] tw = buffer.peek(buffer.size()-terminator.length, terminator.length);
		return ArrayUtils.isEquals(tw, terminator);
	}

	@Override
	public void serialEvent(SerialPortProxyEvent evt) {
		//Should never happen
		int maxLoops = 1023;
		int count = 0;
		while(count < maxLoops) {
			count += 1;
			if(this.port == null){
				raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailedPortNotSetup"));
				return;
			}
			//We recieved some data, now parse it.
			try{
				InputStream in = this.port.getInputStream();
				SerialDataSourceVO vo = ((SerialDataSourceVO)this.getVo());
	            int data;
	            while (( data = in.read()) > -1 ){
	            	index += 1;
	                buffer.push(data);
	                if (isTerminatorFound()) {
	                    break;
	                }
	            }
	            if(!isTerminatorFound())
	            	return;
	            
	            String msg = buffer.popString(index, Charset.forName("ASCII"));
	            index = 0;
	            
	            if(!this.dataPoints.isEmpty()){
	                
	                //DS Information
	                String messageRegex = vo.getMessageRegex(); //"!([A-Z0-9]{3,3})([a-zA-Z])(.*);";
	                int pointIdentifierIndex = vo.getPointIdentifierIndex();
	                
	            	Pattern messagePattern = Pattern.compile(messageRegex);
	            	Matcher messageMatcher = messagePattern.matcher(msg);
	                if(messageMatcher.matches()){
	                	
	                    //Parse out the Identifier
	                	String pointIdentifier = messageMatcher.group(pointIdentifierIndex);
	                	
	                	//Update all points that have this Identifier
	                	for(DataPointRT dp: this.dataPoints){
	                		SerialPointLocatorRT pl = dp.getPointLocator();
	                		SerialPointLocatorVO plVo = pl.getVo();
	                		if(plVo.getPointIdentifier().equals(pointIdentifier)){
	                			Pattern pointValuePattern = Pattern.compile(plVo.getValueRegex());
	                			Matcher pointValueMatcher = pointValuePattern.matcher(msg); //Use the index from the above message
	                        	if(pointValueMatcher.matches()){
		                        	String value = pointValueMatcher.group(plVo.getValueIndex());                	
		                        	PointValueTime newValue;
		                        	
		                        	//Switch on the type
		                        	switch(plVo.getDataTypeId()){
		                        	case DataTypes.ALPHANUMERIC:
		                        		newValue = new PointValueTime(value,new Date().getTime());
		                        		break;
		                        	case DataTypes.NUMERIC:
		                        		newValue = new PointValueTime(Double.parseDouble(value),new Date().getTime());
		                        		break;
		                        	case DataTypes.MULTISTATE:
		                        		newValue = new PointValueTime(Integer.parseInt(value),new Date().getTime());
		                        		break;
		                        	case DataTypes.BINARY:
		                        		newValue = new PointValueTime(Boolean.parseBoolean(value),new Date().getTime());
		                        		break;
		                        	default:
		                        		throw new ShouldNeverHappenException("Uknown Data type for point");
		                        	}
		                    		dp.updatePointValue(newValue);
	                        	}//end if value matches
		                	}//end for this point id
	                	}
	                	
	                	
	                }else{
	                	raiseEvent(POINT_READ_PATTERN_MISMATCH_EVENT,System.currentTimeMillis(),true,new TranslatableMessage("event.serial.patternMismatch",vo.getMessageRegex(),msg));
	                }
	                
	            }
	            returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
	        }catch ( IOException e ){
				raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailed",e.getMessage()));
	        }
		}
	}

	@Override
	protected void doPoll(long time) {
		//For now do nothing as we are event driven.
		if(this.port == null){
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("event.serial.readFailedPortNotSetup"));
			return;
		}

	}

	
	
	
	
}
