package com.infiniteautomation.asciifile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.SystemSettingsDefinition;

public class AsciiFileSystemSettingsDefinition extends SystemSettingsDefinition {
	public static final String RESTRICTED_PATH = "asciiFile.restrictedPath";

	@Override
	public String getDescriptionKey() {
		return "dsEdit.file.systemSettingsDescription";
	}

	@Override
	public Map<String, Object> getDefaultValues() {
		Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(RESTRICTED_PATH, Common.MA_HOME);
		return defaults;
	}

	@Override
	public Integer convertToValueFromCode(String key, String code) {
		//not implemented
		return null;
	}

	@Override
	public String convertToCodeFromValue(String key, Integer value) {
		//not implemented
		return null;
	}

	@Override
	public void validateSettings(Map<String, Object> settings, ProcessResult response) {
		if(settings.containsKey(RESTRICTED_PATH)) {
			Map<String, String> simple = new HashMap<String, String>();
			simple.put(RESTRICTED_PATH, (String)settings.get(RESTRICTED_PATH));
			validate(simple, response);
			if(!response.getHasMessages())
				settings.put(RESTRICTED_PATH, simple.get(RESTRICTED_PATH));
		}
	}
	
	public static void validate(Map<String, String> settings, ProcessResult response) {
		String restrictedPaths = settings.get(RESTRICTED_PATH);
		if(restrictedPaths != null && !StringUtils.isEmpty(restrictedPaths)) {
			String absolutePaths = "";
			for(String path : restrictedPaths.split(";")) {
				try {
					File f = new File(path);
					if(f.exists()) {
						absolutePaths += f.getCanonicalPath().toString() + ";";
					} else {
						response.addContextualMessage(RESTRICTED_PATH, "dsEdit.file.pathDoesntExist", path);
						return;
					}
				} catch(IOException e) {
					response.addContextualMessage(RESTRICTED_PATH, "dsEdit.file.ioexceptionCanonical", path);
					return;
				}
			}
			settings.put(RESTRICTED_PATH, absolutePaths.substring(0, absolutePaths.length()-1));
		}
	}

}
