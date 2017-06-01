/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import com.fasterxml.jackson.annotation.JsonView;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.Module;

/**
 * 
 * @author Terry Packer
 */
public class ModuleModel {
    
    public interface AdminView {}
	
	String name;
	String version;
    String normalVersion;
	String licenseType;
    @JsonView(AdminView.class)
    String description;
    @JsonView(AdminView.class)
    String longDescription;
    @JsonView(AdminView.class)
    String vendor;
    @JsonView(AdminView.class)
    String vendorUrl;
    @JsonView(AdminView.class)
    String dependencies;
    @JsonView(AdminView.class)
    boolean markedForDeletion;
    @JsonView(AdminView.class)
    boolean unloaded;
    @JsonView(AdminView.class)
    boolean signed;
    
	public ModuleModel(){ }
	
	public ModuleModel(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public ModuleModel(Module module){
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
		this.signed = module.isSigned();
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

	public boolean markedForDeletion(){
		return this.markedForDeletion;
	}

	public void setUnloaded(boolean unloaded){
		this.unloaded = unloaded;
	}

	public boolean isUnloaded(){
		return this.unloaded;
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	
	public boolean isSigned() {
		return signed;
	}
}
