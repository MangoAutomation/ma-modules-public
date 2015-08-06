/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi.web;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.mangoApi.MangoApiModuleDefinition;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Terry Packer
 *
 */
public class MangoApiSystemSettingsDwr extends ModuleDwr{

	private final Log LOG = LogFactory.getLog(MangoApiSystemSettingsDwr.class);
	
	@DwrPermission(admin = true)
	public ProcessResult loadHeaders() {
		ProcessResult result = new ProcessResult();
		List<StringStringPair> headersList = new ArrayList<StringStringPair>();
		MangoApiModuleDefinition.props.checkForReload(true);
		Properties headers = MangoApiModuleDefinition.props.getPropertiesCopy();
		Iterator<Object> it = headers.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			headersList.add(new StringStringPair(key, MangoApiModuleDefinition.props.getString(key)));
		}

		result.addData("headers", headersList);
		return result;
	}
	
	@DwrPermission(admin = true)
	public ProcessResult removeHeader(String keyToRemove) {
		keyToRemove = keyToRemove.trim();
		Properties oldHeaders = MangoApiModuleDefinition.props.getPropertiesCopy();
		Properties newHeaders = new Properties();
		
		Iterator<Object> it = oldHeaders.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			if(!key.equalsIgnoreCase(keyToRemove)){
				newHeaders.put(key, MangoApiModuleDefinition.props.getString(key));
			}
		}
		User user = Common.getUser();
		String comments = "Saved by: ";
		if(user != null)
			comments += user.getUsername();
		else
			comments += "unknown";		
		File file = new File(Common.MA_HOME + File.separator + "overrides" + File.separator + "classes" + File.separator + "mangoApiHeaders.properties");
		try{
			FileWriter writer = new FileWriter(file);
			newHeaders.store(writer, comments);
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
			ProcessResult result = new ProcessResult();
			result.addContextualMessage("saveMangoApiMessage", "common.default", e.getMessage());
			return result;
		}
		ProcessResult result = this.loadHeaders();
		return result;
	}
	
	@DwrPermission(admin = true)
	public ProcessResult addHeader(String newKey, String value) {
		//Clean them up
		newKey = newKey.trim();
		value = value.trim();
		Properties oldHeaders = MangoApiModuleDefinition.props.getPropertiesCopy();
		Properties newHeaders = new Properties();
		
		Iterator<Object> it = oldHeaders.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			newHeaders.put(key, MangoApiModuleDefinition.props.getString(key));

		}
		
		newHeaders.put(newKey, value);
		
		User user = Common.getUser();
		String comments = "Saved by: ";
		if(user != null)
			comments += user.getUsername();
		else
			comments += "unknown";
		File file = new File(Common.MA_HOME + File.separator + "overrides" + File.separator + "classes" + File.separator + "mangoApiHeaders.properties");
		try{
			FileWriter writer = new FileWriter(file);
			newHeaders.store(writer, comments);
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
			ProcessResult result = new ProcessResult();
			result.addContextualMessage("saveMangoApiMessage", "common.default", e.getMessage());
			return result;
		}
		ProcessResult result = this.loadHeaders();
		return result;
	}
	
}
