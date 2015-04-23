package com.serotonin.m2m2.internal;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.module.PermissionDefinition;

public class StatusPermissionDef extends PermissionDefinition {
	
	public static final String PERMISSION = "internal.status";
	
    @Override
    public String getPermissionKey() {
        return "internal.status";
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }
    
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PermissionDefinition#getDefaultGroups()
	 */
	@Override
	public List<String> getDefaultGroups() {
		List<String> groups = new ArrayList<String>();
		groups.add("user");
		return groups;
	}
}
