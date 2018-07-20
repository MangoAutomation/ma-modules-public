package com.infiniteautomation.serial.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

import com.infiniteautomation.mango.regex.MatchCallback;
import com.infiniteautomation.serial.rt.SerialDataSourceRT;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class SerialEditDwr extends DataSourceEditDwr{
	private final Log LOG = LogFactory.getLog(SerialEditDwr.class);

   @DwrPermission(user = true)
    public ProcessResult saveSerialDataSource(BasicDataSourceVO basic, String commPortId, int baudRate, int flowControlIn,
            int flowControlOut, int dataBits, int stopBits, int parity, int readTimeout, boolean useTerminator,
            String messageTerminator, String messageRegex, int pointIdentifierIndex,
            boolean hex, boolean logIO, int maxMessageSize, float ioLogFileSizeMBytes, int maxHistoricalIOLogs, int retries) {
        SerialDataSourceVO ds = (SerialDataSourceVO) Common.getHttpUser().getEditDataSource();

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
        ds.setRetries(retries);
        
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
    	SerialDataSourceVO ds = (SerialDataSourceVO) Common.getHttpUser().getEditDataSource();
    	return StringEscapeUtils.escapeJava(ds.getMessageTerminator());
    }
    

    @DwrPermission(user = true)
    public ProcessResult testString(String raw, int dsId, String messageRegex, String messageTerminator, 
    		int pointIdentifierIndex, boolean isHex, boolean useTerminator) {
    	final ProcessResult pr = new ProcessResult();
    	
    	//Message we will work with
    	String msg;

    	if(dsId == -1) {
    		pr.addContextualMessage("testString", "serial.test.needsSave");
    		return pr;
    	}
    	
    	
    	msg = StringEscapeUtils.unescapeJava(raw);
    	messageRegex = StringEscapeUtils.unescapeJava(messageRegex);
    	messageTerminator = StringEscapeUtils.unescapeJava(messageTerminator);
    	
    	//Are we a hex string
    	if(isHex){
	    	 if(!msg.matches("[0-9A-Fa-f]+")){
    			 pr.addContextualMessage("testString", "serial.validate.notHex");
    			 return pr;
    		 }
    	}
    	
		//Map to store the values vs the points they are for
    	final List<Map<String,String>> results = new ArrayList<Map<String,String>>();
		pr.addData("results", results);
    	
    	DataPointDao dpd = DataPointDao.instance;
    	List<DataPointVO> points = dpd.getDataPoints(dsId, null);
    	
    	if(useTerminator) { 
	    	
    		//Convert the message
	    	String[] messages = SerialDataSourceRT.splitMessages(msg, messageTerminator);

    		for(String message : messages){
    			if(SerialDataSourceRT.canProcessTerminatedMessage(message, messageTerminator)){
        			if(LOG.isDebugEnabled())
            			LOG.debug("Matching will use String: " + message);
        			//Check all the points
        			for(final DataPointVO vo : points){
        				final Map<String, String> result = new HashMap<String,String>();
        				MatchCallback callback = new MatchCallback(){

							@Override
							public void onMatch(String pointIdentifier, PointValueTime pvt) {
								result.put("name", vo.getName());
								result.put("value", pvt.toString());
								result.put("identifier", pointIdentifier);
								result.put("success", "true");
							}

							@Override
							public void pointPatternMismatch(String message, String messageRegex) {
								result.put("success", "false");
								result.put("name", vo.getName());
								result.put("error", new TranslatableMessage("serial.test.noPointRegexMatch").translate(Common.getTranslations()));
							}
							
							@Override
							public void messagePatternMismatch(String message, String messageRegex) {
								result.put("success", "false");
								result.put("name", vo.getName());
								result.put("error", new TranslatableMessage("serial.test.noMessageMatch").translate(Common.getTranslations()));
							}
							
							@Override
							public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
								result.put("success", "false");
								result.put("name", vo.getName());
								result.put("error", new TranslatableMessage("serial.test.noIdentifierFound").translate(Common.getTranslations()));
							}
							
							/* (non-Javadoc)
							 * @see com.infiniteautomation.mango.regex.MatchCallback#matchGeneralFailure(java.lang.Exception)
							 */
							@Override
							public void matchGeneralFailure(Exception e) {
								result.put("success", "false");
								result.put("name", vo.getName());
								result.put("error", new TranslatableMessage("common.default", e.getMessage()).translate(Common.getTranslations()));
							}
                		};
        				
        				try{
        					SerialDataSourceRT.matchPointValue(message, 
        						messageRegex, 
        						pointIdentifierIndex,
        						(SerialPointLocatorVO)vo.getPointLocator(),
        						isHex, LOG, callback);
        				}catch(Exception e){
        					callback.matchGeneralFailure(e);
        				}
        				
        				if(result.size() > 0){
            				result.put("message", message);
            				results.add(result);
        				}

        			}	
        		}else{
        			Map<String, String> result = new HashMap<String,String>();
        			result.put("success", "false");
        			result.put("message", message);
					result.put("error", new TranslatableMessage("serial.test.noTerminator").translate(Common.getTranslations()));
        			results.add(result);
        		}
    		}
    	}
    	else {
    		if(LOG.isDebugEnabled())
    			LOG.debug("Matching will use String: " + msg);
			//Check all the points
			for(final DataPointVO vo : points){
				final Map<String, String> result = new HashMap<String,String>();
				MatchCallback callback = new MatchCallback(){

					@Override
					public void onMatch(String pointIdentifier, PointValueTime pvt) {
						result.put("name", vo.getName());
						result.put("value", pvt.toString());
						result.put("identifier", pointIdentifier);
						result.put("success", "true");
					}

					@Override
					public void pointPatternMismatch(String message, String messageRegex) {
						result.put("success", "false");
						result.put("name", vo.getName());
						result.put("error", new TranslatableMessage("serial.test.noPointRegexMatch").translate(Common.getTranslations()));
					}
					
					@Override
					public void messagePatternMismatch(String message, String messageRegex) { 
						result.put("success", "false");
						result.put("name", vo.getName());
						result.put("error", new TranslatableMessage("serial.test.noMessageMatch").translate(Common.getTranslations()));
					}
					
					@Override
					public void pointNotIdentified(String message, String messageRegex, int pointIdentifierIndex) {
						result.put("success", "false");
						result.put("name", vo.getName());
						result.put("error", new TranslatableMessage("serial.test.noIdentifierFound").translate(Common.getTranslations()));
					}
					/* (non-Javadoc)
					 * @see com.infiniteautomation.mango.regex.MatchCallback#matchGeneralFailure(java.lang.Exception)
					 */
					@Override
					public void matchGeneralFailure(Exception e) {
						result.put("success", "false");
						result.put("name", vo.getName());
						result.put("error", new TranslatableMessage("common.default", e.getMessage()).translate(Common.getTranslations()));
					}
        		};
        		try{
        			SerialDataSourceRT.matchPointValue(msg, 
						messageRegex, 
						pointIdentifierIndex, 
						(SerialPointLocatorVO)vo.getPointLocator(), 
						isHex,
						LOG,
						callback);
        		}catch(Exception e){
        			callback.matchGeneralFailure(e);
        		}
				
				if(result.size() > 0){
    				result.put("message", msg);
    				results.add(result);
				}
			}	
    	}
    	
//    	if(results.size() == 0){
//			pr.addContextualMessage("testString", "serial.test.noPointValueMatch", identifier);
//    	}
    	
    	return pr;
    }
}
