package com.infiniteautomation.serial.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class SerialEditDwr extends DataSourceEditDwr{

	   @DwrPermission(user = true)
	    public ProcessResult saveSerialDataSource(BasicDataSourceVO basic, String commPortId, int baudRate, int flowControlIn,
	            int flowControlOut, int dataBits, int stopBits, int parity, int readTimeout, boolean useTerminator,
	            String messageTerminator, String messageRegex, int pointIdentifierIndex,
	            boolean hex, boolean logIO, int maxMessageSize, float ioLogFileSizeMBytes, int maxHistoricalIOLogs) {
	        SerialDataSourceVO ds = (SerialDataSourceVO) Common.getUser().getEditDataSource();

	        setBasicProps(ds, basic);
	        ds.setCommPortId(commPortId);
	        ds.setBaudRate(baudRate);
	        ds.setFlowControlIn(flowControlIn);
	        ds.setFlowControlOut(flowControlOut);
	        ds.setDataBits(dataBits);
	        ds.setStopBits(stopBits);
	        ds.setParity(parity);
	        ds.setReadTimeout(readTimeout);
	        ds.setUseTerminator(useTerminator);
	        ds.setMessageTerminator(StringEscapeUtils.unescapeJava(messageTerminator));
	        ds.setMessageRegex(messageRegex);
	        ds.setPointIdentifierIndex(pointIdentifierIndex);
	        ds.setHex(hex);
	        ds.setLogIO(logIO);
	        ds.setMaxMessageSize(maxMessageSize);
	        ds.setIoLogFileSizeMBytes(ioLogFileSizeMBytes);
	        ds.setMaxHistoricalIOLogs(maxHistoricalIOLogs);
	        
	        return tryDataSourceSave(ds);
	    }	
	
	    @DwrPermission(user = true)
	    public ProcessResult savePointLocator(int id, String xid, String name,SerialPointLocatorVO locator) {
	    	if(locator.getPointIdentifier() == null)
	    		locator.setPointIdentifier(new String()); //Sometimes we want to match an empty string
	        return validatePoint(id, xid, name, locator, null);
	    }
	    
	    @DwrPermission(user = true)
	    public String getSafeTerminator() {
	    	SerialDataSourceVO ds = (SerialDataSourceVO) Common.getUser().getEditDataSource();
	    	return StringEscapeUtils.escapeJava(ds.getMessageTerminator());
	    }
	    
	    @DwrPermission(user = true)
	    public ProcessResult testString(String raw) {
	    	ProcessResult pr = new ProcessResult();
	    	
	    	//Message we will work with
	    	String msg;

	    	SerialDataSourceVO ds = (SerialDataSourceVO) Common.getUser().getEditDataSource();
	    	if(ds.getId() == -1) {
	    		pr.addContextualMessage("testString", "serial.test.needsSave");
	    		return pr;
	    	}
	    	
	    	//Convert the message
	    	msg = StringEscapeUtils.unescapeJava(raw);
	    	
	    	//Are we a hex string
	    	if(ds.isHex()){
		    	 if(!msg.matches("[0-9A-Fa-f]+")){
	    			 pr.addContextualMessage("testString", "serial.validate.notHex");
	    		 }
	    	}
	    	
	    	
	    	DataPointDao dpd = new DataPointDao();
	    	List<DataPointVO> points = dpd.getDataPoints(ds.getId(), null);
	    	if(ds.getUseTerminator()) { 
	    		if(msg.indexOf(ds.getMessageTerminator()) != -1) {
	    			msg = msg.substring(0, msg.indexOf(ds.getMessageTerminator())+1);
	    			Pattern p = Pattern.compile(ds.getMessageRegex());
	    			Matcher m = p.matcher(msg);
	    			if(!m.matches()) {
	    				pr.addContextualMessage("testString", "serial.test.noMessageMatch");
	    				return pr;
	    			}
	    			//TODO save all the groups that were matched for the user
	    			
	    			//Save the identifier group
	    			String identifier = m.group(ds.getPointIdentifierIndex());
	    			pr.addData("pointIdentifier", identifier);
	    			
	    			for(DataPointVO pnt : points) {
	    				SerialPointLocatorVO plVo = (SerialPointLocatorVO) pnt.getPointLocator();
	    				if(identifier.equals(plVo.getPointIdentifier())) {
	    					Pattern v = Pattern.compile(plVo.getValueRegex());
	    					Matcher vm = v.matcher(msg);
	    					if(vm.find()){
	    						//TODO change this to save the point and the value that would be set
	    						pr.addContextualMessage("testString", "serial.test.consumed", pnt.getName(), vm.group(0));
	    					}
	    				}
	    					
	    			}
	    		}
	    		else {
	    			pr.addContextualMessage("testString", "serial.test.noTerminator");
	    			return pr;
	    		}
	    	}
	    	else {
			    for(DataPointVO pnt : points) {
			    	SerialPointLocatorVO plVo = (SerialPointLocatorVO) pnt.getPointLocator();
			    	Pattern p = Pattern.compile(plVo.getValueRegex());
			    	Matcher m = p.matcher(msg); 
			    	if(m.find())
			    		pr.addContextualMessage("testString", "serial.test.consumed", pnt.getName(), m.group(0));	    		
			    }
	    	}
	    	return pr;
	    }
}
