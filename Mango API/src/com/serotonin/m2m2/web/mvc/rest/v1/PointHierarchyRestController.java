/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonGenerator;
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
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointSummaryModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream;
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
    public ResponseEntity<ObjectStream<PointHierarchyModel>> getPointHierarchy(
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {

    	RestProcessResult<ObjectStream<PointHierarchyModel>> result = new RestProcessResult<ObjectStream<PointHierarchyModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);
	    	PointHiearchyPointlessStream stream = new PointHiearchyPointlessStream(ph.getRoot(), user, getSubFolders);
	    	return result.createResponseEntity(stream);
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
    public ResponseEntity<PointHierarchyModel> getFolder(
            @PathVariable String folderName,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {
		
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
			    desiredFolder = copyFolder(desiredFolder);
	            
	            if (!getSubFolders) {
	                desiredFolder.setSubfolders(Collections.<PointFolder>emptyList());
	            }
	            
	            //Clean out based on permissions
	            prune(desiredFolder, user, false);
				return result.createResponseEntity(new PointHierarchyModel(desiredFolder, getDataSourceXidMap())); 
			}

    	}
    	
    	return result.createResponseEntity();
    }
	
	 /**
     * Get the folder via a path
     * @param folderPath
     * @param request
     * @return
     */
    @ApiOperation(value = "Get point hierarchy folder by path", notes = "Points returned based on user priviledges")
    @RequestMapping(method = RequestMethod.GET, value = "/by-path/{folderPath}", produces={"application/json"})
    public ResponseEntity<PointHierarchyModel> getFolder(
            @PathVariable List<String> folderPath,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {
        
        RestProcessResult<PointHierarchyModel> result = new RestProcessResult<PointHierarchyModel>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            
            PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);
            
            PointFolder folder = copyFolder(ph.getRoot());
            folder = removeSubFoldersByPath(folder, folderPath, 0);
            
            if (folder == null) {
                result.addRestMessage(getDoesNotExistMessage());
                return result.createResponseEntity();
            } else {
                if (!getSubFolders) {
                    folder.setSubfolders(Collections.<PointFolder>emptyList());
                }
                
                //Clean out based on permissions
                prune(folder, user, false);
                return result.createResponseEntity(new PointHierarchyModel(folder, getDataSourceXidMap())); 
            }
        }
        
        return result.createResponseEntity();
    }
    
    private PointFolder removeSubFoldersByPath(PointFolder folder, List<String> path, int pathIndex) {
        // reached end of path, include points
        if (pathIndex >= path.size()) {
            return folder;
        }
        String segment = path.get(pathIndex);

        // getting sub-folders, don't return this folder's points
        folder.setPoints(Collections.<DataPointSummary>emptyList());

        List<String> folderNamesToKeep = Arrays.asList(segment.split("\\s*\\|\\s*"));
        boolean keepAll = folderNamesToKeep.contains("*");
        boolean explicitFolderName = !keepAll && folderNamesToKeep.size() == 1;
        
        List<PointFolder> subFolders = folder.getSubfolders();
        Iterator<PointFolder> folderIt = subFolders.iterator();
        // step over the sub-folders and check if they match the path
        while (folderIt.hasNext()) {
            PointFolder subFolder = folderIt.next();
            if (keepAll || folderNamesToKeep.contains(subFolder.getName())) {
                subFolder = removeSubFoldersByPath(subFolder, path, pathIndex + 1);
                if (subFolder == null) {
                    // folder name matches but its sub-folders didn't match the rest of the path, remove it
                    folderIt.remove();
                } else if (explicitFolderName) {
                    return subFolder;
                }
            } else {
                // folder name doesn't match, remove it
                folderIt.remove();
            }
        }
        
        if (folder.getPoints().isEmpty() && subFolders.isEmpty()) {
            return null;
        }
        
        return folder;
    }

	/**
	 * Get the folder via an Id
	 * @param folderName
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Get point hierarchy folder by ID", notes = "Points returned based on user priviledges")
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{folderId}", produces={"application/json"})
    public ResponseEntity<PointHierarchyModel> getFolder(
            @PathVariable Integer folderId,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {
		
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
			    desiredFolder = copyFolder(desiredFolder);
                
                if (!getSubFolders) {
                    desiredFolder.setSubfolders(Collections.<PointFolder>emptyList());
                }
                
				//Clean out based on permissions
				prune(desiredFolder, user, false);
				return result.createResponseEntity(new PointHierarchyModel(desiredFolder, getDataSourceXidMap())); 
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
	
	class PointHiearchyPointlessStream implements ObjectStream<PointHierarchyModel>{

		private PointFolder folder;
		private User user;
		private boolean getSubFolders;
		private Map<Integer, String> dsXidMap;
		
		/**
		 * @param folder
		 * @param user
		 * @param getSubFolders
		 */
		public PointHiearchyPointlessStream(PointFolder folder, User user, boolean getSubFolders) {
			this.folder = folder;
			this.user = user;
			this.getSubFolders = getSubFolders;
			
			this.dsXidMap = new HashMap<Integer, String>();
			for(DataSourceVO<?> ds : DataSourceDao.instance.getAll()){
				dsXidMap.put(ds.getId(), ds.getXid());
			}
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(JsonGenerator jgen) throws IOException {
	    	writeFoldersRecursively(folder, jgen);
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
		 */
		@Override
		public void streamData(CSVPojoWriter<PointHierarchyModel> jgen) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
		/**
		 * Write out the Folders
		 * @param folder
		 * @param user
		 * @throws IOException 
		 */
		void writeFoldersRecursively(PointFolder folder, JsonGenerator jgen) throws IOException{
			
			//Write the folder name
			jgen.writeStringField("name", folder.getName());
			//Write the folder id
			jgen.writeNumberField("id", folder.getId());

			//Write out the points
			jgen.writeArrayFieldStart("points");
			
			List<DataPointSummary> points = folder.getPoints();
			Iterator<DataPointSummary> ptIt = points.iterator();
			while (ptIt.hasNext()) {
			    DataPointSummary pt = ptIt.next();
			    if (Permissions.hasDataPointReadPermission(user, pt)) {
			        jgen.writeObject(new DataPointSummaryModel(pt, this.dsXidMap.get(pt.getDataSourceId())));
			    }
			}
			jgen.writeEndArray();
			
			if(getSubFolders){
				//Write the subfolders
				jgen.writeArrayFieldStart("subfolders");
				List<PointFolder> folders = folder.getSubfolders();
				Iterator<PointFolder> folderIt = folders.iterator();
		        while (folderIt.hasNext()) {
		            PointFolder f = folderIt.next();
		            jgen.writeStartObject();
		            writeFoldersRecursively(f, jgen);
		            jgen.writeEndObject();
		        }
		        jgen.writeEndArray();
			}
		}
		
	}
	
	
	/**
     * Remove any data points that are not readable by user and remove empty folders that result
     * 
     * Caution not to edit the folders in place as they are the real cached point hierarchy
     * 
     * @param ph
     * @param user
     * @param doCopy copy the folder before pruning it
     * @return PointFolder as a copy
     */
	private PointFolder prune(PointFolder root, User user, boolean doCopy) {
		//Make a copy from the highest level down
		PointFolder copy = doCopy ? copyFolder(root) : root;
		
		//Always prune the base folder's points but don't remove it.
		List<DataPointSummary> points = copy.getPoints();
		Iterator<DataPointSummary> ptIt = points.iterator();
		while (ptIt.hasNext()) {
		    DataPointSummary pt = ptIt.next();
		    if (!Permissions.hasDataPointReadPermission(user, pt)) {
		        ptIt.remove();
		    }
		}

		List<PointFolder> folders = copy.getSubfolders();
		Iterator<PointFolder> folderIt = folders.iterator();
        while (folderIt.hasNext()) {
            PointFolder folder = folderIt.next();
            prune(folder, user, false);
            if (folder.getPoints().isEmpty() && folder.getSubfolders().isEmpty()) {
                folderIt.remove();
            }
        }
		
		return copy;
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
		List<PointFolder> subFolders = folder.getSubfolders();
		List<PointFolder> folderCopies = new ArrayList<PointFolder>(subFolders.size());
		for(PointFolder f : subFolders)
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
