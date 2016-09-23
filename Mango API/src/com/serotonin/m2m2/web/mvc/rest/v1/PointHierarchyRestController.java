/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.serotonin.m2m2.module.definitions.SuperadminPermissionDefinition;
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
	    	PointHiearchyFolderStream stream = new PointHiearchyFolderStream(ph.getRoot(), user, getSubFolders);
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
    public ResponseEntity<ObjectStream<PointHierarchyModel>> getFolder(
            @PathVariable String folderName,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {
		
    	RestProcessResult<ObjectStream<PointHierarchyModel>> result = new RestProcessResult<ObjectStream<PointHierarchyModel>>(HttpStatus.OK);
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
		    	PointHiearchyFolderStream stream = new PointHiearchyFolderStream(desiredFolder, user, getSubFolders);
		    	return result.createResponseEntity(stream);
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
    public ResponseEntity<ObjectStream<PointHierarchyModel>> getFolder(
            @PathVariable List<String> folderPath,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {
        
        RestProcessResult<ObjectStream<PointHierarchyModel>> result = new RestProcessResult<ObjectStream<PointHierarchyModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            PointHierarchy ph = DataPointDao.instance.getPointHierarchy(true);
            PointHierarchyPathStream stream = new PointHierarchyPathStream(ph.getRoot(), user, getSubFolders, folderPath);
	    	return result.createResponseEntity(stream);
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
    public ResponseEntity<ObjectStream<PointHierarchyModel>> getFolder(
            @PathVariable Integer folderId,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            HttpServletRequest request) {
		
    	RestProcessResult<ObjectStream<PointHierarchyModel>> result = new RestProcessResult<ObjectStream<PointHierarchyModel>>(HttpStatus.OK);
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
				PointHiearchyFolderStream stream = new PointHiearchyFolderStream(desiredFolder, user, getSubFolders);
		    	return result.createResponseEntity(stream);
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
	 * Stream out the folder (and possibly sub-folders) while filtering points based on permissions
	 * by finding a folder based on its path
	 * @author Terry Packer
	 *
	 */
	class PointHierarchyPathStream extends PointHiearchyFolderStream{

		protected List<String> path;
		
		/**
		 * @param folder
		 * @param user
		 * @param getSubFolders
		 */
		public PointHierarchyPathStream(PointFolder folder, User user, boolean getSubFolders, List<String> path) {
			super(folder, user, getSubFolders);
			this.path = path;
		}
		
		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(JsonGenerator jgen) throws IOException {
			writeFolderRecursively(this.folder, this.path, 0, jgen);
		}
		
		protected void writeFolderRecursively(PointFolder currentFolder, List<String> path, int pathIndex, JsonGenerator jgen) throws IOException{
			// reached end of path, include points
	        if (pathIndex >= path.size()) {
	        	writeFoldersRecursively(currentFolder, true, jgen);
	        }
	        String segment = path.get(pathIndex);

	        // getting sub-folders, don't return this folder's points
	        //folder.setPoints(Collections.<DataPointSummary>emptyList());

	        List<String> folderNamesToKeep = Arrays.asList(segment.split("\\s*\\|\\s*"));
	        boolean keepAll = folderNamesToKeep.contains("*");
	        boolean explicitFolderName = !keepAll && folderNamesToKeep.size() == 1;
	        
	        List<PointFolder> subFolders = currentFolder.getSubfolders();
	        Iterator<PointFolder> folderIt = subFolders.iterator();
	        // step over the sub-folders and check if they match the path
	        while (folderIt.hasNext()) {
	            PointFolder subFolder = folderIt.next();
	            if (keepAll || folderNamesToKeep.contains(subFolder.getName())) {
	            	//Write subFolder
	            	writeFoldersRecursively(subFolder, false, jgen);
	                writeFolderRecursively(subFolder, path, pathIndex + 1, jgen);
	            } else {
	                // folder name doesn't match, remove it
	                folderIt.remove();
	            }
	        }
	        
	        if (currentFolder.getPoints().isEmpty() && subFolders.isEmpty()) {
	            return;
	        }
	        
	        return;
		}
		
		
	}
	
	/**
	 * Stream out the folder (and possibly sub-folders) while filtering points based on permissions
	 * @author Terry Packer
	 *
	 */
	class PointHiearchyFolderStream implements ObjectStream<PointHierarchyModel>{

		protected PointFolder folder;
		protected Set<String> userPermissions;
		protected boolean getSubFolders;
		protected Map<Integer, DataSourceSummary> dsXidMap;
		
		/**
		 * @param folder
		 * @param user
		 * @param getSubFolders
		 */
		public PointHiearchyFolderStream(PointFolder folder, User user, boolean getSubFolders) {
			this.folder = folder;
			this.userPermissions = Permissions.explodePermissionGroups(user.getPermissions());
			this.getSubFolders = getSubFolders;
			
			this.dsXidMap = new HashMap<Integer, DataSourceSummary>();
			for(DataSourceVO<?> ds : DataSourceDao.instance.getAll()){
				dsXidMap.put(ds.getId(), new DataSourceSummary(ds.getId(), ds.getXid(), Permissions.explodePermissionGroups(ds.getEditPermission())));
			}
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(JsonGenerator jgen) throws IOException {
	    	writeFoldersRecursively(this.folder, this.getSubFolders, jgen);
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
		void writeFoldersRecursively(PointFolder folder, boolean getSubFolders, JsonGenerator jgen) throws IOException{
			
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
			    DataSourceSummary ds = this.dsXidMap.get(pt.getDataSourceId());
			    if (hasDataPointReadPermission(userPermissions, pt, ds)) {
			        jgen.writeObject(new DataPointSummaryModel(pt, ds.getXid()));
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
		            writeFoldersRecursively(f, getSubFolders, jgen);
		            jgen.writeEndObject();
		        }
		        jgen.writeEndArray();
			}
		}
		
		boolean hasDataPointReadPermission(Set<String> userPermissions, DataPointSummary dp, DataSourceSummary ds){
			//Is the user superadmin
			if(userPermissions.contains(SuperadminPermissionDefinition.GROUP_NAME))
				return true;
			
			//Check point read permissions
			else if(!Collections.disjoint(userPermissions, dp.getReadPermissionsSet()))
				return true;
			
			//Check set permissions
			else if(!Collections.disjoint(userPermissions, dp.getSetPermissionsSet()))
				return true;
			
			//Check data source edit permissions
			else if(!Collections.disjoint(userPermissions, ds.getEditPermissions()))
				return true;
			else 
				return false;
			
		}
		
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
	
	class DataSourceSummary{
		
		private int id;
		private String xid;
		private Set<String> editPermissions;
		
		public DataSourceSummary(int id, String xid, Set<String> editPermissions){
			this.id = id;
			this.xid = xid;
			this.editPermissions = editPermissions;			
		}

		public int getId() {
			return id;
		}

		public String getXid() {
			return xid;
		}

		public Set<String> getEditPermissions() {
			return editPermissions;
		}
	}
	
}
