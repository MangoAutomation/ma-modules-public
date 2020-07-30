/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.modules;

/**
 * 
 * @author Terry Packer
 */
public enum ModuleNotificationTypeEnum {
	MODULE_DOWNLOADED,
	MODULE_UPGRADE_AVAILABLE,
	NEW_MODULE_AVAILABLE,
	UPGRADE_STATE_CHANGE,
	UPGRADE_FINISHED,
	MODULE_DOWNLOAD_FAILED,
	UPGRADE_ERROR;
}
