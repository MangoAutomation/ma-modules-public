package com.infiniteautomation.serial.rt;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;

public class SerialDataSourceRT extends PollingDataSource implements SerialPortEventListener{
	private final Log LOG = LogFactory.getLog(SerialDataSourceRT.class);
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    
	private SerialPort port; //Serial Communication Port
	
	
	public SerialDataSourceRT(SerialDataSourceVO vo) {
		super(vo);
		
	}


	/**
	 * Connect to a serial port
	 * @param portName
	 * @throws Exception 
	 */
	public boolean connect () throws Exception{
		SerialDataSourceVO vo = (SerialDataSourceVO) this.getVo();
		
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(vo.getCommPortId());
        if ( portIdentifier.isCurrentlyOwned() ){
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("serial.event.portInUse",vo.getCommPortId()));
			return false;
        }else{
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort ){
                this.port = (SerialPort) commPort;
                this.port.setSerialPortParams(vo.getBaudRate(),
                		vo.getDataBits(),
                		vo.getStopBits(),
                		vo.getParity());
                this.port.setFlowControlMode(vo.getFlowControlMode());
                this.port.enableReceiveTimeout(vo.getReadTimeout()); //Number of ms to wait before timeout...
                this.port.enableReceiveThreshold(1); //Number of bytes to read each time...
                this.port.addEventListener(this);
                this.port.notifyOnDataAvailable(true);
                return true;
            }else{
    			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("serial.event.invalidPort"));
    			return false;
            }
        }

    }
	
    @Override
    public void initialize() {
    	boolean connected = false;
    	try{
    		connected = this.connect();
    	}catch(NoSuchPortException e1){
    		SerialDataSourceVO vo = (SerialDataSourceVO)this.getVo();
    		LOG.debug("No Such Port: " + vo.getCommPortId());
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("serial.event.noSuchPort",vo.getCommPortId()));
    	}catch(Exception e){
    		LOG.debug("Error while initializing data source", e);
    		String msg = e.getMessage();
    		if(msg == null){
    			msg = "Unknown";
    		}
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("serial.event.connectFailed",msg));
    	}
    	
    	if(connected){
    		returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
    	}else{
    		SerialDataSourceVO vo = (SerialDataSourceVO) this.getVo();
			vo.setEnabled(false);
			Common.runtimeManager.saveDataSource(vo);
    	}
    	
    	
    }
    @Override
    public void terminate() {
        super.terminate();
        if(this.port != null)
        	
        	this.port.close();

    }
    
	@Override
	public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime,
			SetPointSource source) {

		//Are we connected?
		if(this.port == null)
			return;
		
		try {
			OutputStream os = this.port.getOutputStream();
			//Pin the terminator on the end
			String messageTerminator = ((SerialDataSourceVO)this.getVo()).getMessageTerminator();
	        messageTerminator = StringEscapeUtils.unescapeJava(messageTerminator);
	        //Do we need to or is it already on the end?
	        String fullMsg = valueTime.getStringValue();
	        if(!fullMsg.endsWith(messageTerminator)){
	        	fullMsg +=  messageTerminator;
	        }
			PointValueTime newValue = new PointValueTime(fullMsg,valueTime.getTime());

			String output = newValue.getStringValue();
			byte[] data = output.getBytes();
			for(byte b : data){
				os.write(b);
			}
			os.flush();
			//Finally Set the point value
			dataPoint.setPointValue(newValue, source);
			returnToNormal(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis());
		} catch (IOException e) {
			raiseEvent(POINT_WRITE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("serial.event.writeFailed",e.getMessage()));
		}
		
		
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		//We recieved some data, now parse it.
		byte[] buffer = new byte[1024]; //Max size TBD
		try{
			InputStream in = this.port.getInputStream();
            int len = 0;
            int data;
            String messageTerminator = ((SerialDataSourceVO)this.getVo()).getMessageTerminator();
	        messageTerminator = StringEscapeUtils.unescapeJava(messageTerminator);
	        char terminator = messageTerminator.charAt(0);
	        //TODO add Event to notify that no termination char was received
            while (( data = in.read()) > -1 ){
                buffer[len++] = (byte)data;
                if ( data == terminator) {
                    break;
                }
            }
            
            if(!this.dataPoints.isEmpty()){
            	//Update all points...
            	PointValueTime newValue = new PointValueTime(new String(buffer,0,len),new Date().getTime());
            	for(DataPointRT dp: this.dataPoints){
            		dp.updatePointValue(newValue);
            	}
            }
            returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
        }catch ( IOException e ){
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("serial.event.readFailed",e.getMessage()));
        }             
		
	}

	@Override
	protected void doPoll(long time) {
		//For now do nothing as we are event driven.
		
	}

	
	
	
	
}
