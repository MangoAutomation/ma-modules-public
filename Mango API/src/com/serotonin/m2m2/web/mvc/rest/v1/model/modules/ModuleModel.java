/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.Module;

/**
 * 
 * @author Terry Packer
 */
public class ModuleModel {
	
	private String name;
	private String version;
	private String buildNumber;
	private String licenseType;
    private String description;
    private String longDescription;
    private String vendor;
    private String vendorUrl;
    private String dependencies;
    private String releaseNotes;
    
	public ModuleModel(){ }
	
	public ModuleModel(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public ModuleModel(Module module){
		this.name = module.getName();
		this.version = module.getVersion();
		this.licenseType = module.getLicenseType();
		TranslatableMessage m = module.getDescription();
		if(m != null)
			this.description = m.translate(Common.getTranslations());
		this.vendor = module.getVendor();
		this.vendorUrl = module.getVendorUrl();
		this.dependencies = module.getDependencies();
	}
	
	/**
	 * Create a model out of the JSON Store Response
	 * @param v
	 */
	public ModuleModel(JsonValue v) {
		JsonValue sub = v.getJsonValue("name");
		if(sub != null)
			this.name = sub.toString();
		sub = v.getJsonValue("version");
		if(sub != null)
			this.version = sub.toString();
		sub = v.getJsonValue("shortDescription");
		if(sub!= null)
			this.description = sub.toString();
		sub = v.getJsonValue("longDescription");
		if(sub != null)
			this.longDescription = sub.toString();
		sub = v.getJsonValue("vendorName");
		if(sub != null)
			this.vendor = sub.toString();
		sub = v.getJsonValue("vendorUrl");
		if(sub != null)
			this.vendorUrl = sub.toString();
		sub = v.getJsonValue("releaseNotes");
		if(sub != null)
			this.releaseNotes = sub.toString();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVendorUrl() {
		return vendorUrl;
	}

	public void setVendorUrl(String vendorUrl) {
		this.vendorUrl = vendorUrl;
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String getReleaseNotes() {
		return releaseNotes;
	}

	public void setReleaseNotes(String releaseNotes) {
		this.releaseNotes = releaseNotes;
	}
	
}
