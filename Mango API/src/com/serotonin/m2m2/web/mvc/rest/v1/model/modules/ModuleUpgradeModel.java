/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.Module;

/**
 * 
 * @author Terry Packer
 */
public class ModuleUpgradeModel extends ModuleModel {

	String newVersion;
    String releaseNotes;
    Map<String, String> dependencyVersions;
    
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
        this.dependencyVersions = new HashMap<>();
        
        JsonValue sub = v.getJsonValue("name");
        if(sub != null)
            this.name = sub.toString();
        sub = v.getJsonValue("version");
        if(sub != null)
            this.newVersion = sub.toString();
        sub = v.getJsonValue("fullVersion");
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
        
        JsonValue dependencies = v.getJsonValue("dependencies");
        JsonValue fullDependencies = v.getJsonValue("fullDependencies");
        
        if (dependencies instanceof JsonArray && !(fullDependencies instanceof JsonObject)) {
            dependencies.toList().forEach((obj) -> {
                String moduleName = obj.toString();
                String versionRange = Common.getVersion().getMajorVersion() + "." + Common.getVersion().getMinorVersion();
                this.dependencyVersions.put(moduleName, versionRange);
            });
        }
        
        if (fullDependencies instanceof JsonObject) {
            fullDependencies.toMap().forEach((key, value) -> {
                this.dependencyVersions.put(key, value.toString());
            });
        }
        
        this.dependencies = this.dependencyVersions.entrySet().stream()
            .map((entry) -> {
                return entry.getKey() + ":" + entry.getValue();
            })
            .collect(Collectors.joining(","));
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

    public Map<String, String> getDependencyVersions() {
        return dependencyVersions;
    }

    public void setDependencyVersions(Map<String, String> dependencyVersions) {
        this.dependencyVersions = dependencyVersions;
    }

}
