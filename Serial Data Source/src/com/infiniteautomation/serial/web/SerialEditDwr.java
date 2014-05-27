package com.infiniteautomation.serial.web;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

import freemarker.template.utility.StringUtil;

public class SerialEditDwr extends DataSourceEditDwr{

	   @DwrPermission(user = true)
	    public ProcessResult saveSerialDataSource(BasicDataSourceVO basic, String commPortId, int baudRate, int flowControlIn,
	            int flowControlOut, int dataBits, int stopBits, int parity, int readTimeout, String messageTerminator,
	            String messageRegex, int pointIdentifierIndex) {
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
	        ds.setMessageTerminator(StringEscapeUtils.unescapeJava(messageTerminator));
	        ds.setMessageRegex(messageRegex);
	        ds.setPointIdentifierIndex(pointIdentifierIndex);
	        
	        return tryDataSourceSave(ds);
	    }	
	
	    @DwrPermission(user = true)
	    public ProcessResult savePointLocator(int id, String xid, String name,SerialPointLocatorVO locator) {
	        return validatePoint(id, xid, name, locator, null);
	    }
}
