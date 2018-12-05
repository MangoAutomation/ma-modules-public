/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.hierarchy.PointFolder;
import com.serotonin.m2m2.vo.hierarchy.PointHierarchy;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.message.RestProcessResult;
import com.serotonin.m2m2.web.mvc.rest.v1.model.DataPointSummaryModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PointHierarchyModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.DataPointFilter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.DataSourceSummary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Terry Packer
 *
 */
@Api(value="Point Hierarchy", description="Point Hierarchy")
@Controller
@RequestMapping("/hierarchy")
public class PointHierarchyRestController extends MangoRestController{

	
	//private static Log LOG = LogFactory.getLog(PointHierarchyRestController.class);
	
	/**
	 * Get the entire Point Hierarchy
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "Get full point hierarchy", notes = "Hierarchy based on user priviledges")
    @RequestMapping(method = RequestMethod.GET, value = "/full")
    public ResponseEntity<JsonStream<PointHierarchyModel>> getPointHierarchy(
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            @RequestParam(name="points", defaultValue="true") boolean getPoints,
            HttpServletRequest request) {

    	RestProcessResult<JsonStream<PointHierarchyModel>> result = new RestProcessResult<JsonStream<PointHierarchyModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		PointHierarchy ph = DataPointDao.getInstance().getPointHierarchy(true);
	    	PointHiearchyFolderStream stream = new PointHiearchyFolderStream(ph.getRoot(), user, getSubFolders, getPoints);
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
	@RequestMapping(method = RequestMethod.GET, value = "/by-name/{folderName}")
    public ResponseEntity<JsonStream<PointHierarchyModel>> getFolder(
            @PathVariable String folderName,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            @RequestParam(name="points", defaultValue="true") boolean getPoints,
            HttpServletRequest request) {
		
    	RestProcessResult<JsonStream<PointHierarchyModel>> result = new RestProcessResult<JsonStream<PointHierarchyModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
			PointHierarchy ph = DataPointDao.getInstance().getPointHierarchy(true);
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
		    	PointHiearchyFolderStream stream = new PointHiearchyFolderStream(desiredFolder, user, getSubFolders, getPoints);
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
    @RequestMapping(method = RequestMethod.GET, value = "/by-path/{folderPath}")
    public ResponseEntity<JsonStream<PointHierarchyModel>> getFolder(
            @PathVariable List<String> folderPath,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            @RequestParam(name="points", defaultValue="true") boolean getPoints,
            HttpServletRequest request) {
        
        RestProcessResult<JsonStream<PointHierarchyModel>> result = new RestProcessResult<JsonStream<PointHierarchyModel>>(HttpStatus.OK);
        User user = this.checkUser(request, result);
        if (result.isOk()) {
            PointHierarchy ph = DataPointDao.getInstance().getPointHierarchy(true);
            PointHierarchyPathStream stream = new PointHierarchyPathStream(ph.getRoot(), user, getSubFolders, getPoints, folderPath);
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
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{folderId}")
    public ResponseEntity<JsonStream<PointHierarchyModel>> getFolder(
            @PathVariable Integer folderId,
            @RequestParam(name="subfolders", defaultValue="true") boolean getSubFolders,
            @RequestParam(name="points", defaultValue="true") boolean getPoints,
            HttpServletRequest request) {
		
    	RestProcessResult<JsonStream<PointHierarchyModel>> result = new RestProcessResult<JsonStream<PointHierarchyModel>>(HttpStatus.OK);
    	User user = this.checkUser(request, result);
    	if(result.isOk()){
    		
			PointHierarchy ph = DataPointDao.getInstance().getPointHierarchy(true);
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
				PointHiearchyFolderStream stream = new PointHiearchyFolderStream(desiredFolder, user, getSubFolders, getPoints);
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
	@RequestMapping(method = RequestMethod.GET, value = "/path/{xid}")
    public ResponseEntity<List<String>> getPath(@PathVariable String xid, HttpServletRequest request) {

    	RestProcessResult<List<String>> result = new RestProcessResult<List<String>>(HttpStatus.OK);
    	
		PointHierarchy ph = DataPointDao.getInstance().getPointHierarchy(true);

		User user = this.checkUser(request, result);
		if(result.isOk()){
			DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
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
	static class PointHierarchyPathStream extends PointHiearchyFolderStream{

		protected List<String> path;
		
		/**
		 * @param folder
		 * @param user
		 * @param getSubFolders
		 */
		public PointHierarchyPathStream(PointFolder folder, User user, boolean getSubFolders, boolean getPoints, List<String> path) {
			super(folder, user, getSubFolders, getPoints);
			this.path = path;
		}
		
		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(JsonGenerator jgen) throws IOException {
		    this.jgen = jgen;
		    writeFoldersInPathRecursively(this.folder, 0, false);
		}

		protected void writeFoldersInPathRecursively(PointFolder currentFolder, int pathIndex, boolean parentWritten) throws IOException {
            boolean endOfPath = pathIndex >= path.size();
            if (endOfPath) {
                writeFoldersRecursively(currentFolder);
                return;
            }
            
		    String segment = path.get(pathIndex);
            List<String> folderNamesToKeep = Arrays.asList(segment.split("\\s*\\|\\s*"));
            boolean keepAll = folderNamesToKeep.contains("*");
            boolean explicitFolderName = !keepAll && folderNamesToKeep.size() == 1;
            List<PointFolder> subFolders = currentFolder.getSubfolders();
            boolean writeThisObject = parentWritten || !explicitFolderName;

            if (writeThisObject) {
                jgen.writeStartObject();
                
                //Write the folder name
                jgen.writeStringField("name", currentFolder.getName());
                //Write the folder id
                jgen.writeNumberField("id", currentFolder.getId());
    
                //Write the subfolders
                jgen.writeArrayFieldStart("subfolders");
            }
            
            Iterator<PointFolder> subFoldersIt = subFolders.iterator();
            while (subFoldersIt.hasNext()) {
                PointFolder subFolder = subFoldersIt.next();
                
                if (keepAll || folderNamesToKeep.contains(subFolder.getName())) {
                    writeFoldersInPathRecursively(subFolder, pathIndex + 1, writeThisObject);
                }
            }
            
            if (writeThisObject) {
                jgen.writeEndArray();
                jgen.writeEndObject();
            }
		}
	}
	
	/**
	 * Stream out the folder (and possibly sub-folders) while filtering points based on permissions
	 * @author Terry Packer
	 *
	 */
	static class PointHiearchyFolderStream extends DataPointFilter implements JsonStream<PointHierarchyModel>{

		protected PointFolder folder;
		protected boolean getSubFolders;
		protected boolean getPoints;
        protected JsonGenerator jgen;
		
		/**
		 * @param folder
		 * @param user
		 * @param getSubFolders
		 */
		public PointHiearchyFolderStream(PointFolder folder, User user, boolean getSubFolders, boolean getPoints) {
			super(user);
			this.folder = folder;
			this.getSubFolders = getSubFolders;
			this.getPoints = getPoints;
		}

		/* (non-Javadoc)
		 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.ObjectStream#streamData(com.fasterxml.jackson.core.JsonGenerator)
		 */
		@Override
		public void streamData(JsonGenerator jgen) throws IOException {
		    this.jgen = jgen;
	    	writeFoldersRecursively(this.folder);
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
		void writeFoldersRecursively(PointFolder folder) throws IOException{
            jgen.writeStartObject();
            
			//Write the folder name
			jgen.writeStringField("name", folder.getName());
			//Write the folder id
			jgen.writeNumberField("id", folder.getId());

			//Write out the points and count them
			int pointCount = 0;
			if(this.getPoints)
				jgen.writeArrayFieldStart("points");
				
			List<DataPointSummary> points = folder.getPoints();
			Iterator<DataPointSummary> ptIt = points.iterator();
			while (ptIt.hasNext()) {
			    DataPointSummary pt = ptIt.next();
			    DataSourceSummary ds = this.dsIdMap.get(pt.getDataSourceId());
			    if (hasDataPointReadPermission(pt, ds)) {
			    	pointCount++;
			    	if(this.getPoints)
			    		jgen.writeObject(new DataPointSummaryModel(pt, ds.getXid()));
			    }
			}
			if(this.getPoints)
				jgen.writeEndArray();
			
			jgen.writeNumberField("pointCount", pointCount);
			
			if(getSubFolders){
				//Write the subfolders
				jgen.writeArrayFieldStart("subfolders");
				List<PointFolder> folders = folder.getSubfolders();
				Iterator<PointFolder> folderIt = folders.iterator();
		        while (folderIt.hasNext()) {
		            PointFolder f = folderIt.next();
		            writeFoldersRecursively(f);
		        }
		        jgen.writeEndArray();
			}
			
            jgen.writeEndObject();
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
}
