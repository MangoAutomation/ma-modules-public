/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi;

import com.serotonin.m2m2.module.SystemSettingsDefinition;

/**
 * @author Terry Packer
 *
 */
public class MangoApiSystemSettingsDefinition extends SystemSettingsDefinition{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#getDescriptionKey()
	 */
	@Override
	public String getDescriptionKey() {
		return "rest.settings.title";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#getSectionJspPath()
	 */
	@Override
	public String getSectionJspPath() {
		return "web/settings.jsp";
	}

}
