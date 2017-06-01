/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.module.Module;

/**
 * 
 * @author Terry Packer
 */
public class ModuleUpgradeModel extends ModuleModel {

	String newVersion;
    String releaseNotes;
    
	public ModuleUpgradeModel() {
	}

	public ModuleUpgradeModel(Module module, JsonValue v) {
	    super(module);
        loadFromStoreJson(v);
	}
	
	public ModuleUpgradeModel(JsonValue v) {
	    loadFromStoreJson(v);
	}

	public void loadFromStoreJson(JsonValue v) {
        JsonValue sub = v.getJsonValue("name");
        if(sub != null)
            this.name = sub.toString();
        sub = v.getJsonValue("version");
        if(sub != null)
            this.newVersion = sub.toString();
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
	
	public String getReleaseNotes() {
		return releaseNotes;
	}

	public void setReleaseNotes(String releaseNotes) {
		this.releaseNotes = releaseNotes;
	}
	
    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }
}
