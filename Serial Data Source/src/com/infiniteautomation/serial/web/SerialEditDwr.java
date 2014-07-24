package com.infiniteautomation.serial.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.io.serial.SerialPortException;
import com.serotonin.io.serial.SerialPortProxy;
import com.serotonin.io.serial.SerialUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

import freemarker.template.utility.StringUtil;

public class SerialEditDwr extends DataSourceEditDwr{

	   @DwrPermission(user = true)
	    public ProcessResult saveSerialDataSource(BasicDataSourceVO basic, String commPortId, int baudRate, int flowControlIn,
	            int flowControlOut, int dataBits, int stopBits, int parity, int readTimeout, boolean useTerminator,
	            String messageTerminator, String messageRegex, int pointIdentifierIndex) {
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
	        
	        return tryDataSourceSave(ds);
	    }	
	
	    @DwrPermission(user = true)
	    public ProcessResult savePointLocator(int id, String xid, String name,SerialPointLocatorVO locator) {
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
	    	String msg = StringEscapeUtils.unescapeJava(raw);
	    	SerialDataSourceVO ds = (SerialDataSourceVO) Common.getUser().getEditDataSource();
	    	if(ds.getId() == -1) {
	    		pr.addContextualMessage("testString", "serial.test.needsSave");
	    		return pr;
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
	    			String identifier = m.group(ds.getPointIdentifierIndex());
	    			for(DataPointVO pnt : points) {
	    				SerialPointLocatorVO plVo = (SerialPointLocatorVO) pnt.getPointLocator();
	    				if(identifier.equals(plVo.getPointIdentifier())) {
	    					Pattern v = Pattern.compile(plVo.getValueRegex());
	    					Matcher vm = v.matcher(msg);
	    					if(vm.find())
	    						pr.addContextualMessage("testString", "serial.test.consumed", pnt.getName(), vm.group(0));
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
