/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.hierarchy.PointFolder;
import com.serotonin.m2m2.vo.hierarchy.PointHierarchy;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PointHierarchyModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Point Hierarchy", description="Point Hierarchy")
@Controller
@RequestMapping("/v1/hierarchy")
public class PointHierarchyRestController extends MangoRestController{

	
	//private static Log LOG = LogFactory.getLog(PointHierarchyRestController.class);
	
	/**
	 * Get the entire Point Hierarchy
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Get full point hierarchy", notes = "Hierarchy based on user priviledges")
    @RequestMapping(method = RequestMethod.GET, value = "/full", produces={"application/json"})
    public ResponseEntity<PointHierarchyModel> getPointHierarchy(HttpServletRequest request) {

    	RestProcessResult<PointHierarchyModel> result = new RestProcessResult<PointHierarchyModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
	    	PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);
	    	//Clean out based on permissions
	    	PointFolder root = prune(ph.getRoot(), user);
	    	PointHierarchyModel model = new PointHierarchyModel(root, getDataSourceXidMap());
	    	return result.createResponseEntity(model);
    	}
    	
    	return result.createResponseEntity();
    
    }
    

	/**
	 * Get the folder via a name
	 * @param folderName
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Get point hierarchy folder by name", notes = "Points returned based on user priviledges")
	@RequestMapping(method = RequestMethod.GET, value = "/by-name/{folderName}", produces={"application/json"})
    public ResponseEntity<PointHierarchyModel> getFolder(@PathVariable String folderName, HttpServletRequest request) {
		
    	RestProcessResult<PointHierarchyModel> result = new RestProcessResult<PointHierarchyModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
			PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);
			PointFolder folder = ph.getRoot();
			PointFolder desiredFolder = null;
			if(folder.getName().equals(folderName))
		    	desiredFolder = folder; 
			else
				desiredFolder = recursiveFolderSearch(folder, folderName);
			
			
			if (desiredFolder == null){
				result.addRestMessage(getDoesNotExistMessage());
	            return result.createResponseEntity();
			}else{
		    	//Clean out based on permissions
				PointFolder root = prune(desiredFolder, user);
				return result.createResponseEntity(new PointHierarchyModel(root, getDataSourceXidMap())); 
			}

    	}
    	
    	return result.createResponseEntity();
    }

	/**
	 * Get the folder via an Id
	 * @param folderName
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Get point hierarchy folder by ID", notes = "Points returned based on user priviledges")
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{folderId}", produces={"application/json"})
    public ResponseEntity<PointHierarchyModel> getFolder(@PathVariable Integer folderId, HttpServletRequest request) {
		
    	RestProcessResult<PointHierarchyModel> result = new RestProcessResult<PointHierarchyModel>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
			PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);
			PointFolder folder = ph.getRoot();
			PointFolder desiredFolder = null;
			if(folder.getId() == folderId)
				desiredFolder = folder;
			else
				desiredFolder = recursiveFolderSearch(folder, folderId);
			
			if (desiredFolder == null){
				result.addRestMessage(getDoesNotExistMessage());
	            return result.createResponseEntity();
			}else{
				//Clean out based on permissions
				PointFolder root = prune(desiredFolder, user);
				return result.createResponseEntity(new PointHierarchyModel(root, getDataSourceXidMap())); 
			}

    	}
    	
    	return result.createResponseEntity();
    }
	
	/**
	 * Get a path to a folder
	 * @param xid
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Get path to a point using point's XID", notes = "Points returned based on user priviledges")
	@RequestMapping(method = RequestMethod.GET, value = "/path/{xid}", produces={"application/json"})
    public ResponseEntity<List<String>> getPath(@PathVariable String xid, HttpServletRequest request) {

    	RestProcessResult<List<String>> result = new RestProcessResult<List<String>>(HttpStatus.OK);
    	
		PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);

		User user = this.checkUser(request, result);
		if(result.isOk()){
			DataPointVO vo = DataPointDao.instance.getByXid(xid);
			if(vo == null){
				result.addRestMessage(getDoesNotExistMessage());
				return result.createResponseEntity();
			}
			
			//Check permissions
			try{
				if(!Permissions.hasDataPointReadPermission(user, vo)){
					result.addRestMessage(getUnauthorizedMessage());
					return result.createResponseEntity();
				}else{
					return result.createResponseEntity(ph.getPath(vo.getId()));
				}
			}catch(PermissionException e){
				result.addRestMessage(getUnauthorizedMessage());
				return result.createResponseEntity();
			}
		}else{
			return result.createResponseEntity();
		}
    }
	
	
	/**
	 * Remove any data points that are not readable by user and remove empty folders that result
	 * 
	 * Caution not to edit the folders in place as they are the real cached point hierarchy
	 * 
	 * @param ph
	 * @param user
	 * @return PointFolder as a copy
	 */
	private PointFolder prune(PointFolder root, User user) {
		
		//Make a copy from the highest level down
		PointFolder copy = copyFolder(root);
		
		//Always prune the base folder's points but don't remove it.
		List<DataPointSummary> points = copy.getPoints();
		List<DataPointSummary> pointsToKeep = new ArrayList<DataPointSummary>();
		for(DataPointSummary summary : points){
			if(Permissions.hasDataPointReadPermission(user, summary))
				pointsToKeep.add(summary);
		}
		copy.setPoints(pointsToKeep);
		
		List<PointFolder> folders = copy.getSubfolders();
		List<PointFolder> foldersToKeep = new ArrayList<PointFolder>();

		for(PointFolder folder : folders)
			if((pruneFolder(folder, user) > 0) || (folder.getSubfolders().size() > 0))
				foldersToKeep.add(folder);
		
		copy.setSubfolders(foldersToKeep);
		
		return copy;
	}
	
	/**
	 * Recursively check all sub-folders and remove points based on Data Point Read Permission
	 * @param folder
	 * @param user
	 * @return
	 */
	private int pruneFolder(PointFolder folder, User user){
		List<DataPointSummary> points = folder.getPoints();
		List<DataPointSummary> pointsToKeep = new ArrayList<DataPointSummary>();
		for(DataPointSummary summary : points){
			if(Permissions.hasDataPointReadPermission(user, summary))
				pointsToKeep.add(summary);
		}
		
		folder.setPoints(pointsToKeep);
		
		List<PointFolder> foldersToKeep = new ArrayList<PointFolder>();
		for(PointFolder f : folder.getSubfolders())
			if((pruneFolder(f, user) > 0) || (f.getSubfolders().size() > 0))
				foldersToKeep.add(f);

		folder.setSubfolders(foldersToKeep);
		
		return pointsToKeep.size();
	}
	
	/**
	 * Make a copy of the folder and copies of its subfolders
	 * @param folder
	 * @return
	 */
	private PointFolder copyFolder(PointFolder folder){
		PointFolder copy = new PointFolder(folder.getId(), folder.getName());
		copy.setPoints(new ArrayList<DataPointSummary>(folder.getPoints()));
		
		//Copy the subfolders
		List<PointFolder> folderCopies = new ArrayList<PointFolder>();
		for(PointFolder f : folder.getSubfolders())
			folderCopies.add(copyFolder(f));
		
		copy.setSubfolders(folderCopies);
		return copy;
	}
	
	/**
	 * @param ph
	 * @param folderName
	 * @return
	 */
	private PointFolder recursiveFolderSearch(PointFolder root,
			String folderName) {
		if(root.getName().equals(folderName))
			return root;
		
		for(PointFolder folder : root.getSubfolders()){
			PointFolder found = recursiveFolderSearch(folder, folderName);
			if( found != null)
				return found;
		}
		
		return null;
	}

	/**
	 * @param ph
	 * @param folderName
	 * @return
	 */
	private PointFolder recursiveFolderSearch(PointFolder root,
			Integer folderId) {
		if(root.getId() == folderId)
			return root;
		
		for(PointFolder folder : root.getSubfolders()){
			PointFolder found = recursiveFolderSearch(folder, folderId);
			if( found != null)
				return found;
		}
		
		return null;
	}
	
	private Map<Integer, String> getDataSourceXidMap(){
		Map<Integer, String> dsXidMap = new HashMap<Integer, String>();
		
		for(DataSourceVO<?> ds : DataSourceDao.instance.getAll()){
			dsXidMap.put(ds.getId(), ds.getXid());
		}
		
		return dsXidMap;
	}
	
}
