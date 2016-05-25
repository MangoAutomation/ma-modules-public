/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.db.pair.StringStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Terry Packer
 *
 */
public class MangoApiSystemSettingsDwr extends ModuleDwr{

	@DwrPermission(admin = true)
	public ProcessResult loadHeaders() {
		ProcessResult result = new ProcessResult();
		List<StringStringPair> headersList = new ArrayList<StringStringPair>();
		
		String header = Common.envProps.getString("rest.cors.allowedOrigins", "");
		if(!StringUtils.isEmpty(header))
			headersList.add(new StringStringPair("Access-Control-Allow-Origin", header));
		
		header = Common.envProps.getString("rest.cors.allowedMethods", "");
		if(!StringUtils.isEmpty(header))
			headersList.add(new StringStringPair("Access-Control-Allow-Methods", header));
		
		header = Common.envProps.getString("rest.cors.allowedHeaders", "");
		if(!StringUtils.isEmpty(header))
			headersList.add(new StringStringPair("Access-Control-Allow-Headers", header));

		header = Common.envProps.getString("rest.cors.exposedHeaders", "");
		if(!StringUtils.isEmpty(header))
			headersList.add(new StringStringPair("Access-Control-Expose-Headers", header));
		
		headersList.add(new StringStringPair("Access-Control-Allow-Credentials", Boolean.toString(Common.envProps.getBoolean("rest.cors.allowCredentials", false))));
		
		header = Common.envProps.getString("rest.cors.maxAge", "");
		if(!StringUtils.isEmpty(header))
			headersList.add(new StringStringPair("Access-Control-Max-Age", header));

		result.addData("enabled", Common.envProps.getBoolean("rest.cors.enabled", false));
		result.addData("headers", headersList);
		return result;
	}
}
