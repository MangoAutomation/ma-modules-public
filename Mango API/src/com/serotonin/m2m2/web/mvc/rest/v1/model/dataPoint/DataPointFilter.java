/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.module.definitions.permissions.SuperadminPermissionDefinition;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.DataSourceSummary;

/**
 * Class to aid in permissions checking for streams of data points
 * 
 * @author Terry Packer
 *
 */
public class DataPointFilter {

	protected Set<String> userPermissions;
	protected Map<Integer, DataSourceSummary> dsIdMap;
	
	public DataPointFilter(User user){
		this.userPermissions = user.getPermissionsSet();
		
		this.dsIdMap = new HashMap<Integer, DataSourceSummary>();
		for(DataSourceVO<?> ds : DataSourceDao.instance.getAll()){
			dsIdMap.put(ds.getId(), new DataSourceSummary(ds.getId(), ds.getXid(), Permissions.explodePermissionGroups(ds.getEditPermission())));
		}
	}

	public static boolean hasDataPointReadPermission(Set<String> userPermissions, Set<String> dataPointReadPermissions, Set<String> dataPointSetPermissions, Set<String> dataSourceEditPermissions){
		//Is the user superadmin
		if(userPermissions.contains(SuperadminPermissionDefinition.GROUP_NAME))
			return true;
		
		//Check point read permissions
		else if(!Collections.disjoint(userPermissions, dataPointReadPermissions))
			return true;
		
		//Check set permissions
		else if(!Collections.disjoint(userPermissions, dataPointSetPermissions))
			return true;
		
		//Check data source edit permissions
		else if(!Collections.disjoint(userPermissions, dataSourceEditPermissions))
			return true;
		else 
			return false;	
	}
	
	public boolean hasDataPointReadPermission(DataPointSummary dp){
		return hasDataPointReadPermission(userPermissions, dp.getReadPermissionsSet(), dp.getSetPermissionsSet(), this.dsIdMap.get(dp.getDataSourceId()).getEditPermissions());
	}

	public boolean hasDataPointReadPermission(DataPointSummary dp, DataSourceSummary ds){
		return hasDataPointReadPermission(userPermissions, dp.getReadPermissionsSet(), dp.getSetPermissionsSet(), ds.getEditPermissions());
	}
	
	public boolean hasDataPointReadPermission(DataPointVO vo){
		return hasDataPointReadPermission(userPermissions, Permissions.explodePermissionGroups(vo.getReadPermission()), 
				Permissions.explodePermissionGroups(vo.getSetPermission()), this.dsIdMap.get(vo.getDataSourceId()).getEditPermissions());
	}
}
