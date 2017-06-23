/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import java.util.Map;

/**
 * 
 * @author Terry Packer
 */
public class UpdateLicensePayloadModel{
	private String guid;
	private String description;
	private String distributor;
	private Map<String, String> modules;
    private String storeUrl;
	
	public UpdateLicensePayloadModel(){ }

	/**
	 * @param guid
	 * @param description
	 * @param distributor
	 * @param modules
	 */
	public UpdateLicensePayloadModel(String guid, String description, String distributor,
			Map<String, String> modules, String storeUrl) {
		super();
		this.guid = guid;
		this.description = description;
		this.distributor = distributor;
		this.modules = modules;
		this.storeUrl = storeUrl;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDistributor() {
		return distributor;
	}

	public void setDistributor(String distributor) {
		this.distributor = distributor;
	}

	public Map<String, String> getModules() {
		return modules;
	}

	public void setModules(Map<String, String> modules) {
		this.modules = modules;
	}

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }
}