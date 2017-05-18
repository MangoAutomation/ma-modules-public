/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import com.fasterxml.jackson.annotation.JsonView;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.Module;

/**
 * 
 * @author Terry Packer
 */
public class ModuleModel {
    
    public interface AdminView {}
	
	private String name;
	private String version;
    private String normalVersion;
	private String licenseType;
	
    @JsonView(AdminView.class)
    private String description;
    @JsonView(AdminView.class)
    private String longDescription;
    @JsonView(AdminView.class)
    private String vendor;
    @JsonView(AdminView.class)
    private String vendorUrl;
    @JsonView(AdminView.class)
    private String dependencies;
    @JsonView(AdminView.class)
    private String releaseNotes;
    @JsonView(AdminView.class)
    private boolean markedForDeletion;
    @JsonView(AdminView.class)
    private boolean unloaded;
    
	public ModuleModel(){ }
	
	public ModuleModel(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public ModuleModel(Module module) {
		this.name = module.getName();
        this.version = module.getVersion().toString();
        this.normalVersion = module.getVersion().getNormalVersion();
		this.licenseType = module.getLicenseType();
		TranslatableMessage m = module.getDescription();
		if(m != null)
			this.description = m.translate(Common.getTranslations());
		this.vendor = module.getVendor();
		this.vendorUrl = module.getVendorUrl();
		this.dependencies = module.getDependencies();
		this.markedForDeletion = module.isMarkedForDeletion();
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
	
   public String getNormalVersion() {
        return normalVersion;
    }

    public void setNormalVersion(String normalVersion) {
        this.normalVersion = normalVersion;
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

	public boolean markedForDeletion(){
		return this.markedForDeletion;
	}

	public void setUnloaded(boolean unloaded){
		this.unloaded = unloaded;
	}

	public boolean isUnloaded(){
		return this.unloaded;
	}
}
